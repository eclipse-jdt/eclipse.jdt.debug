/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.VMDisconnectedException;

/**
 * Breakpoint listener to handle breakpoint conditions.
 * 
 * @since 3.5
 */
public class ConditionalBreakpointHandler implements IJavaBreakpointListener {

	/**
	 * Whether the condition had compile or runtime errors
	 */
	private boolean fHasErrors = false;

	/**
	 * Listens for evaluation completion for condition evaluation. If an
	 * evaluation evaluates <code>true</code> or has an error, this breakpoint
	 * will suspend the thread in which the breakpoint was hit. If the
	 * evaluation returns <code>false</code>, the thread is resumed.
	 */
	class EvaluationListener implements IEvaluationListener {

		/**
		 * Lock for synchronizing evaluation
		 */
		private Object fLock = new Object();

		/**
		 * The breakpoint that was hit
		 */
		private JavaLineBreakpoint fBreakpoint;

		/**
		 * Result of the vote
		 */
		private int fVote;

		EvaluationListener(JavaLineBreakpoint breakpoint) {
			fBreakpoint = breakpoint;
		}

		public void evaluationComplete(IEvaluationResult result) {
			fVote = determineVote(result);
			synchronized (fLock) {
				fLock.notifyAll();
			}
		}

		/**
		 * Processes the result to determine whether to suspend or resume.
		 * 
		 * @param result
		 *            evaluation result
		 * @return vote
		 */
		private int determineVote(IEvaluationResult result) {
			if (result.isTerminated()) {
				// indicates the user terminated the evaluation
				return SUSPEND;
			}
			JDIThread thread = (JDIThread) result.getThread();
			if (result.hasErrors()) {
				DebugException exception = result.getException();
				Throwable wrappedException = exception.getStatus()
						.getException();
				if (wrappedException instanceof VMDisconnectedException) {
					// VM terminated/disconnected during evaluation
					return DONT_SUSPEND;
				} 
				fireConditionHasRuntimeErrors(fBreakpoint, exception);
				return SUSPEND;
			} 
			try {
				IValue value = result.getValue();
				if (fBreakpoint.isConditionSuspendOnTrue()) {
					if (value instanceof IJavaPrimitiveValue) {
						// Suspend when the condition evaluates true
						IJavaPrimitiveValue javaValue = (IJavaPrimitiveValue) value;
						if (javaValue.getJavaType().getName()
								.equals("boolean")) { //$NON-NLS-1$
							if (javaValue.getBooleanValue()) {
								return SUSPEND;
							} 
							return DONT_SUSPEND;
						}
					}
					IStatus status = new Status(
							IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							MessageFormat.format(JDIDebugBreakpointMessages.ConditionalBreakpointHandler_1, value.getReferenceTypeName()));
					// result was not boolean
					fireConditionHasRuntimeErrors(fBreakpoint, new DebugException(status));
					return SUSPEND;
				} 
				IDebugTarget debugTarget = thread.getDebugTarget();
				IValue lastValue = fBreakpoint
						.setCurrentConditionValue(debugTarget, value);
				if (!value.equals(lastValue)) {
					return SUSPEND;
				} 
				return DONT_SUSPEND;
			} catch (DebugException e) {
				// Suspend when an error occurs
				JDIDebugPlugin.log(e);
				return SUSPEND;
			}
		}

		/**
		 * Result of the conditional expression evaluation - to resume or not
		 * resume, that is the question.
		 * 
		 * @return vote result
		 */
		int getVote() {
			return fVote;
		}

		/**
		 * Returns the lock object to synchronize this evaluation.
		 * 
		 * @return lock object
		 */
		Object getLock() {
			return fLock;
		}
	}

	public void addingBreakpoint(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
	}

	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint,
			Message[] errors) {
	}

	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint,
			DebugException exception) {
	}

	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		if (breakpoint instanceof IJavaLineBreakpoint) {
			JavaLineBreakpoint lineBreakpoint = (JavaLineBreakpoint) breakpoint;
			try {
				final String condition = lineBreakpoint.getCondition();
				if (condition == null) {
					return SUSPEND;
				}
				EvaluationListener listener = new EvaluationListener(
						lineBreakpoint);
				IJavaStackFrame frame = (IJavaStackFrame) thread
						.getTopStackFrame();
				IJavaProject project = lineBreakpoint.getJavaProject(frame);
				if (project == null) {
					fireConditionHasErrors(
							lineBreakpoint,
							new Message[] { new Message(
									JDIDebugBreakpointMessages.JavaLineBreakpoint_Unable_to_compile_conditional_breakpoint___missing_Java_project_context__1,
									-1) });
					return SUSPEND;
				}
				IJavaDebugTarget target = (IJavaDebugTarget) thread
						.getDebugTarget();
				IAstEvaluationEngine engine = getEvaluationEngine(target,
						project);
				if (engine == null) {
					// If no engine is available, suspend
					return SUSPEND;
				}
				ICompiledExpression expression = lineBreakpoint
						.getExpression(thread);
				if (expression == null) {
					expression = engine.getCompiledExpression(condition, frame);
					lineBreakpoint.setExpression(thread, expression);
				}
				if (expression.hasErrors()) {
					fireConditionHasErrors(lineBreakpoint,
							getMessages(expression));
					return SUSPEND;
				}
				Object lock = listener.getLock();
				synchronized (lock) {
					engine.evaluateExpression(expression, frame, listener,
							DebugEvent.EVALUATION_IMPLICIT, false);
					// TODO: timeout?
					try {
						lock.wait();
					} catch (InterruptedException e) {
						fireConditionHasRuntimeErrors(
								lineBreakpoint,
								new DebugException(
										new Status(
												IStatus.ERROR,
												JDIDebugPlugin
														.getUniqueIdentifier(),
												JDIDebugBreakpointMessages.ConditionalBreakpointHandler_0,
												e)));
						return SUSPEND;
					}
				}
				return listener.getVote();
			} catch (CoreException e) {
				DebugException de = null;
				if (e instanceof DebugException) {
					de = (DebugException) e;
				} else {
					de = new DebugException(e.getStatus());
				}
				fireConditionHasRuntimeErrors(lineBreakpoint, de);
			}
		}
		return SUSPEND;
	}

	public void breakpointInstalled(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
	}

	public void breakpointRemoved(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
	}

	public int installingBreakpoint(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint, IJavaType type) {
		return 0;
	}

	/**
	 * Returns an evaluation engine for evaluating this breakpoint's condition
	 * in the given target and project context.
	 * @param vm the VM to get an evaluation engine for
	 * @param project the project context
	 * @return a new {@link IAstEvaluationEngine}
	 */
	private IAstEvaluationEngine getEvaluationEngine(IJavaDebugTarget vm, IJavaProject project) {
		return ((JDIDebugTarget) vm).getEvaluationEngine(project);
	}

	private void fireConditionHasRuntimeErrors(IJavaLineBreakpoint breakpoint, DebugException exception) {
		fHasErrors = true;
		JDIDebugPlugin.getDefault().fireBreakpointHasRuntimeException(breakpoint, exception);
	}

	/**
	 * Notifies listeners that a conditional breakpoint expression has been
	 * compiled that contains errors
	 * @param breakpoint the breakpoint that has errors in its condition
	 * @param messages the error messages
	 */
	private void fireConditionHasErrors(IJavaLineBreakpoint breakpoint, Message[] messages) {
		fHasErrors = true;
		JDIDebugPlugin.getDefault().fireBreakpointHasCompilationErrors(breakpoint, messages);
	}

	/**
	 * Convert an array of <code>String</code> to an array of
	 * <code>Message</code>.
	 * @param expression the expression to get messages from
	 * @return the array of {@link Message}s from the expression
	 */
	private Message[] getMessages(ICompiledExpression expression) {
		String[] errorMessages = expression.getErrorMessages();
		Message[] messages = new Message[errorMessages.length];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = new Message(errorMessages[i], -1);
		}
		return messages;
	}

	/**
	 * Returns whether errors were encountered when evaluating the condition
	 * (compilation or runtime).
	 * 
	 * @return whether errors were encountered when evaluating the condition
	 */
	public boolean hasErrors() {
		return fHasErrors;
	}
}
