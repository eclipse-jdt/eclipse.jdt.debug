package org.eclipse.jdt.internal.debug.eval.ast.engine;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.eval.EvaluationResult;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionsEvaluationMessages;

/**
 * An evaluation thread is a reusable object which
 * can perform a series of evaluations in a single thread.
 */
class EvaluationThread {
	private boolean fEvaluating = false;
	private Thread fEvaluationThread;
	private boolean fStopped = false;
	private Object fLock = new Object();
	private ASTEvaluationEngine fEngine;
	private Interpreter fInterpreter;
	/**
	 * Evaluation variables
	 */
	private ICompiledExpression fExpression;
	private IRuntimeContext fContext;
	private IJavaThread fThread;
	private IEvaluationListener fListener;
	private int fEvaluationDetail;
	private boolean fHitBreakpoints;
	private CoreException fException;

	/**
	 * Creates a new evaluation thread for the given
	 * evaluation engine. The engine will be notified
	 * whenever this evaluation thread completes an
	 * evaluation.
	 */
	public EvaluationThread(ASTEvaluationEngine engine) {
		fEngine = engine;
	}

	/**
	 * Returns whether this evaluation thread is currently
	 * performing an evaluation.
	 */
	public boolean isEvaluating() {
		return fEvaluating;
	}

	/**
	 * Stops any ongoing evaluations and allows
	 * the underlying thread to terminate. This method
	 * prevents the evaluation thread from reusing the
	 * underlying thread.
	 */
	public void stop() {
		fStopped = true;
		if (fInterpreter != null) {
			fInterpreter.stop();
		}
		synchronized (fLock) {
			fLock.notify();
		}
	}

	/**
	 * Perform an evaluation. If an underlying thread already exists, the
	 * evalution thread will be carried out in that thread. Otherwise,
	 * a new thread will be created for the evaluation.
	 */
	public void evaluate(ICompiledExpression expression, IRuntimeContext context, IJavaThread thread, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints) {
		fExpression = expression;
		fContext = context;
		fThread = thread;
		fListener = listener;
		fEvaluationDetail = evaluationDetail;
		fHitBreakpoints = hitBreakpoints;
		fException= null;
		if (fEvaluationThread == null) {
			// Create a new thread
			fEvaluationThread = new Thread(new Runnable() {
				public void run() {
					while (!fStopped) {
						synchronized (fLock) {
							doEvaluation();
							try {
								// Sleep until the next evaluation
								if (!fStopped) {
									fLock.wait();
								}
							} catch (InterruptedException exception) {
							}
						}
					}
				}
			}, "Evaluation thread"); //$NON-NLS-1$
			fEvaluationThread.start();
		} else {
			// Use the existing thread
			synchronized (fLock) {
				fLock.notifyAll();
			}
		}
	}

	/**
	 * Do the actual work of an evaluation. This method
	 * is intended to be called from within the underlying
	 * evaluation thread.
	 */
	private synchronized void doEvaluation() {
		fEvaluating = true;
		EvaluationResult result = new EvaluationResult(fEngine, fExpression.getSnippet(), fThread);
		if (fExpression.hasErrors()) {
			Message[] errors = fExpression.getErrors();
			for (int i = 0, numErrors = errors.length; i < numErrors; i++) {
				result.addError(errors[i]);
			}
			evaluationFinished(result);
			return;
		}
		fInterpreter = new Interpreter((InstructionSequence) fExpression, fContext);

		IEvaluationRunnable er = new IEvaluationRunnable() {
			public void run(IJavaThread jt, IProgressMonitor pm) {
				try {
					fInterpreter.execute();
				} catch (CoreException exception) {
					fException = exception;
				} catch (Throwable exception) {
					JDIDebugPlugin.log(exception);
					fException = new CoreException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.ERROR, InstructionsEvaluationMessages.getString("InstructionSequence.Runtime_exception_occurred_during_evaluation._See_log_for_details_1"), exception)); //$NON-NLS-1$
				}
			}
		};
		CoreException exception = null;
		try {
			fThread.runEvaluation(er, null, fEvaluationDetail, fHitBreakpoints);
		} catch (DebugException e) {
			exception = e;
		}
		IJavaValue value = fInterpreter.getResult();

		if (exception == null) {
			exception = fException;
		}

		if (value != null) {
			result.setValue(value);
		} else {
			result.addError(new Message(EvaluationEngineMessages.getString("EvaluationThreadAn_unknown_error_occurred_during_evaluation_1"), 0)); //$NON-NLS-1$
		}
		if (exception != null) {
			if (exception instanceof DebugException) {
				result.setException((DebugException)exception);
			} else {
				result.setException(new DebugException(exception.getStatus()));
			}
		}
		evaluationFinished(result);
	}
	
	/**
	 * Notifies the listener and the evaluation engine that
	 * this thread has finished an evaluation.
	 */
	private void evaluationFinished(IEvaluationResult result) {
		fEvaluating = false;
		fEngine.evaluationThreadFinished(this);
		fListener.evaluationComplete(result);
		fExpression= null;
		fContext= null;
		fThread= null;
		fListener= null;
		fException= null;
	}
}