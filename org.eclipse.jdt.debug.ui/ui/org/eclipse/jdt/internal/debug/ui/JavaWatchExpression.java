package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * A watch expression is an expression which is re-evaluated after every thread
 * suspend event on the the top stack frame of the thread.
 * The expression can be re-evaluated in the context of another stack frame by
 * using <code>evaluateExpression(IJavaStackFrame)</code>.
 * Change debug events, with this as the source, are fired every time the
 * status/result of the expression changed.
 * 
 * @see org.eclipse.debug.core.model.IExpression
 */
public class JavaWatchExpression extends PlatformObject implements IExpression, IDebugEventSetListener {
	
	/**
	 * Runnable used to evaluate the snippet.
	 */
	private final class EvaluationRunnable implements Runnable {
		
		private final IJavaStackFrame javaStackFrame;
		
		private EvaluationRunnable(IJavaStackFrame javaStackFrame) {
			super();
			this.javaStackFrame= javaStackFrame;
		}
		
		public void run() {
			IAstEvaluationEngine evaluationEngine= JDIDebugUIPlugin.getDefault().getEvaluationEngine(getProject(javaStackFrame), fDebugTarget);
			// the evaluation listener
			IEvaluationListener listener= new IEvaluationListener() {
				public void evaluationComplete(IEvaluationResult result) {
					if (result.hasErrors()) {
						setHasError(true);
					} else {
						fResultValue= result.getValue();
					}
					setPending(false);
					refresh();
					synchronized (this) {
						notifyAll();
					}
				}
			};
			synchronized (listener) {
				try {
					evaluationEngine.evaluate(fExpressionText, javaStackFrame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
				} catch (DebugException e) {
					JDIDebugPlugin.log(e);
				}
				try {
					listener.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * The expression is enable for implicit (re-)evaluation.
	 */
	public final static int STATUS_ENABLE= 0x001;

	/**
	 * The result value of the expression is pending. An (re-)evaluation has
	 * been requested, the result of the evaluation has not been returned.
	 */
	public final static int STATUS_PENDING= 0x002;

	/**
	 * The last evaluation generated an error.
	 */
	public final static int STATUS_HAS_ERROR= 0x004;
	
	/**
	 * The current result value of the expression is obsolete. The expression
	 * has not been re-evaluated for the last thread suspend event. The result
	 * is not up-to-date.
	 */
	public final static int STATUS_OBSOLETE= 0x008;

	/**
	 * The snippet which is evaluated.
	 */
	private String fExpressionText;

	/**
	 * The debug target on which the expression as been evaluated the last time.
	 */
	private IJavaDebugTarget fDebugTarget;

	/**
	 * The result of the last performed evaluation.
	 */
	private IJavaValue fResultValue;
	
	/**
	 * The status of this watch expression.
	 */
	private int fStatus;

	/**
	 * Constructor for JavaWatchExpression.
	 * 
	 * @param expressionText the snippet to evaluate.
	 */
	public JavaWatchExpression(String expressionText) {
		fExpressionText= expressionText;
		fStatus= STATUS_ENABLE;
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * @see org.eclipse.debug.core.model.IExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fExpressionText;
	}

	/**
	 * @see org.eclipse.debug.core.model.IExpression#getValue()
	 */
	public IValue getValue() {
		return fResultValue;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/**
	 * @see org.eclipse.debug.core.model.IExpression#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	/**
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent)
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		// if more than one suspended thread ?
		for (int i= 0, length= events.length; i < length; i ++) {
			DebugEvent event= events[i];
			Object source= event.getSource();
			switch (event.getKind()) {
				case DebugEvent.SUSPEND:
					// if it is a suspend thread event (not the result of an previous implicite evaluation),
					// perform an implicit evaluation.
					if (event.getDetail() != DebugEvent.EVALUATION_IMPLICIT) {
						if (source instanceof IJavaThread) {
							IJavaStackFrame stackFrame= null;
							try {
								stackFrame= (IJavaStackFrame) ((IJavaThread) source).getTopStackFrame();
							} catch (DebugException e) {
							}
							final IJavaStackFrame finalStackFrame= stackFrame;
							Runnable runnable= new Runnable() {
								public void run() {
									DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
										public void run() {
											evaluateExpression(finalStackFrame, true);
										}
									});
								}
							};
							DebugPlugin.getDefault().asyncExec(runnable);
						}
					}
					break;
				case DebugEvent.TERMINATE:
					// if the last debug target on which the expression as been evaluated terminates,
					// discard the result.
					if (source.equals(fDebugTarget)) {
						fResultValue= null;
						setHasError(false);
						setObsolete(false);
						refresh();
					}
					break;
			}
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return JDIDebugModel.getPluginIdentifier();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fDebugTarget.getLaunch();
	}

	/**
	 * Ask to evaluate the expression in the context of the given stack frame.
	 * Equivalent to <code>evaluateExpression(javaStackFrame, false)</code>.
	 * 
	 * @param javaStackFrame the stack frame in the context of which performed
	 * the evaluation.
	 * 
	 * @see JavaWatchExpression#evaluateExpression(IJavaStackFrame, boolean)
	 */
	public void evaluateExpression(IJavaStackFrame javaStackFrame) {
		evaluateExpression(javaStackFrame, false);
	}

	/**
	 * Ask to evaluate the expression in the context of the given stack frame.
	 * 
	 * The evaluation is performed asynchronously. A change debug event, with
	 * this as the source, is fired when the evaluation is completed.
	 * 
	 * @param javaStackFrame the stack frame in the context of which performed
	 * the evaluation.
	 * @param implicit indicate if the evaluation is implicite or not. If the
	 * expression is disabled, implicite evaluation wont be performed.
	 */
	public void evaluateExpression(final IJavaStackFrame javaStackFrame, boolean implicit) {
		if (javaStackFrame == null) {
			refresh();
			return;
		}
		if (implicit && !isEnabled()) {
			if (fResultValue != null || hasError()) {
				setObsolete(true);
				refresh();
			}
			return;
		}
		fResultValue= null;
		setHasError(false);
		setObsolete(false);
		if (getProject(javaStackFrame) == null) {
			setHasError(true);
			refresh();
			return;
		}
		setPending(true);
		refresh();
		fDebugTarget= (IJavaDebugTarget)javaStackFrame.getDebugTarget();
		DebugPlugin.getDefault().asyncExec(new EvaluationRunnable(javaStackFrame));
	}

	/**
	 * Return the project associated with the given stack frame.
	 */
	private IJavaProject getProject(IJavaStackFrame javaStackFrame) {
			ILaunch launch = javaStackFrame.getLaunch();
			if (launch == null) {
				return null;
			}
			ISourceLocator locator= launch.getSourceLocator();
			if (locator == null) {
				return null;
			}

			Object sourceElement = locator.getSourceElement(javaStackFrame);
			if (sourceElement instanceof IJavaElement) {
				return ((IJavaElement) sourceElement).getJavaProject();
			}
			return null;
	}

	/**
	 * Indicate if the expression is enable for implicit (re-)evaluation.
	 * @see JavaWatchExpression#STATUS_ENABLE
	 */
	public boolean isEnabled() {
		return (fStatus & STATUS_ENABLE) != 0;
	}
	
	/**
	 * Indicate if the result value of the expression is pending.
	 * @see JavaWatchExpression#STATUS_PENDING
	 */
	public boolean isPending() {
		return (fStatus & STATUS_PENDING) != 0;
	}

	/**
	 * Indicate if the result value of the expression is obsolete.
	 * @see JavaWatchExpression#STATUS_OBSOLETE
	 */
	public boolean isObsolete() {
		return (fStatus & STATUS_OBSOLETE) != 0;
	}

	/**
	 * Indicate if the last evaluation of the expression generated errors.
	 * @see JavaWatchExpression#STATUS_HAS_ERROR
	 */
	public boolean hasError() {
		return (fStatus & STATUS_HAS_ERROR) != 0;
	}
	
	/**
	 * Return the status flags.
	 */
	public int getStatus() {
		return fStatus;
	}

	/**
	 * Set the enable flag.
	 * @see JavaWatchExpression#STATUS_ENABLE
	 */
	public void setEnabled(boolean isEnabled) {
		setStatus(STATUS_ENABLE, isEnabled);
	}
	
	/**
	 * Set the pending flag.
	 * @see JavaWatchExpression#STATUS_PENDING
	 */
	protected void setPending(boolean isPending) {
		setStatus(STATUS_PENDING, isPending);
	}
	
	/**
	 * Set the has_error flag.
	 * @see JavaWatchExpression#STATUS_HAS_ERROR
	 */
	protected void setHasError(boolean hasError) {
		setStatus(STATUS_HAS_ERROR, hasError);
	}

	/**
	 * Set the obsolete flag.
	 * @see JavaWatchExpression#STATUS_OBSOLETE
	 */
	protected void setObsolete(boolean isObsolete) {
		setStatus(STATUS_OBSOLETE, isObsolete);
	}

	/**
	 * Update the value of the given flag in the status integer.
	 */
	protected void setStatus(int flag, boolean value) {
		if (value) {
			fStatus |= flag;
		} else {
			fStatus &= ~flag;
		}
	}

	/**
	 * Set the snippet associated with this expression.
	 * @param expressionText
	 */	
	public void setExpressionText(String expressionText) {
		fExpressionText= expressionText;
	}
	
	/**
	 * Set a change debug event, with this as the source, to notified that the
	 * status/content of the expression has changed.
	 */
	public void refresh() {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {new DebugEvent(this, DebugEvent.CHANGE)});
	}
	
	/**
	 * Method for debug purpose only.
	 */	
	public String toString() {
		StringBuffer result= new StringBuffer("JavaWatchExpression \""); //$NON-NLS-1$
		result.append(fExpressionText).append('"');
		if (isEnabled()) {
			result.append(" enabled"); //$NON-NLS-1$
		}
		if (hasError()) {
			result.append(" has_error"); //$NON-NLS-1$
		}
		if (isObsolete()) {
			result.append(" obsolete"); //$NON-NLS-1$
		}
		if (isPending()) {
			result.append(" pending"); //$NON-NLS-1$
		}
		return result.toString();
	}

}
