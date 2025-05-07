/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

/**
 * Handles stepping into a selected method, for a specific thread.
 */
public class StepIntoSelectionHandler implements IDebugEventFilter {

	/**
	 * The method to step into
	 */
	private final IMethod fMethod;

	/**
	 * Resolved signature of the method to step into
	 */
	private String fResolvedSignature;

	/**
	 * The thread in which to step
	 */
	private final IJavaThread fThread;

	/**
	 * The initial stack frame
	 */
	private String fOriginalName;
	private String fOriginalSignature;
	private String fOriginalTypeName;
	private int fOriginalStackDepth;

	/**
	 * Whether this is the first step into.
	 */
	private boolean fFirstStep = true;

	/**
	 * The state of step filters before the step.
	 */
	private boolean fStepFilterEnabledState;

	/**
	 * Expected event kind
	 */
	private int fExpectedKind = -1;

	/**
	 * Expected event detail
	 */
	private int fExpectedDetail = -1;

	/**
	 * Constructs a step handler to step into the given method in the given thread
	 * starting from the given stack frame.
	 */
	public StepIntoSelectionHandler(IJavaThread thread, IJavaStackFrame frame, IMethod method) {
		fMethod = method;
		fThread = thread;
		try {
			fOriginalName = frame.getName();
			fOriginalSignature = frame.getSignature();
			fOriginalTypeName = frame.getDeclaringTypeName();
			if (method.isBinary()) {
				fResolvedSignature = method.getSignature();
			} else {
				fResolvedSignature = ToggleBreakpointAdapter.resolveMethodSignature(method);
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * Returns the target thread for the step.
	 *
	 * @return the target thread for the step
	 */
	protected IJavaThread getThread() {
		return fThread;
	}

	protected IJavaDebugTarget getDebugTarget() {
		return (IJavaDebugTarget)getThread().getDebugTarget();
	}

	/**
	 * Returns the method to step into
	 *
	 * @return the method to step into
	 */
	protected IMethod getMethod() {
		return fMethod;
	}

	/**
	 * Returns the resolved signature of the method to step into
	 *
	 * @return the resolved signature of the method to step into
	 */
	protected String getSignature() {
		return fResolvedSignature;
	}

	/**
	 * @see org.eclipse.debug.core.IDebugEventFilter#filterDebugEvents(DebugEvent[])
	 */
	@Override
	public DebugEvent[] filterDebugEvents(DebugEvent[] events) {
		// we only expect one event from our thread - find the event
		DebugEvent event = null;
		int index = -1;
		int threadEvents = 0;
		for (int i = 0; i < events.length; i++) {
			DebugEvent e = events[i];
			if (isExpectedEvent(e)) {
				event = e;
				index = i;
				threadEvents++;
			} else if (e.getSource() == getThread()) {
				threadEvents++;
			}
		}

		if (event == null) {
			// nothing to process in this event set
			return events;
		}

		// create filtered event set
		DebugEvent[] filtered = new DebugEvent[events.length - 1];
		if (filtered.length > 0) {
			int j = 0;
			for (int i = 0; i < events.length; i++) {
				if (i != index) {
					filtered[j] = events[i];
					j++;
				}
			}
		}

		// if more than one event in our thread, abort (filtering our event)
		if (threadEvents > 1) {
			cleanup();
			return filtered;
		}

		// we have the one expected event - process it
		switch (event.getKind()) {
			case DebugEvent.RESUME:
				// next, we expect a step end
				setExpectedEvent(DebugEvent.SUSPEND, DebugEvent.STEP_END);
				if (fFirstStep) {
					fFirstStep = false;
					return events; // include the first resume event
				}
				// secondary step - filter the event
				return filtered;
			case DebugEvent.SUSPEND:
				// compare location to desired location
				try {
					final IJavaStackFrame frame = (IJavaStackFrame)getThread().getTopStackFrame();
					int stackDepth = frame.getThread().getStackFrames().length;
					String name = null;
					if (frame.isConstructor()) {
						name = frame.getDeclaringTypeName();
						index = name.lastIndexOf('.');
						if (index >= 0) {
							name = name.substring(index + 1);
						}
					} else {
						name = frame.getName();
					}
					if (name.equals(getMethod().getElementName()) && frame.getSignature().equals(getSignature())) {
						// hit
						cleanup();
						return events;
					}
					// step again
					Runnable r = null;
					if (stackDepth > fOriginalStackDepth) {
                        if (frame.isSynthetic()) {
                            // step thru synthetic methods
                            r = new Runnable() {
                                @Override
								public void run() {
                                    try {
                                        setExpectedEvent(DebugEvent.RESUME, DebugEvent.STEP_INTO);
                                        frame.stepInto();
                                    } catch (DebugException e) {
                                        JDIDebugUIPlugin.log(e);
                                        cleanup();
                                        DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(getDebugTarget(), DebugEvent.CHANGE)});
                                    }
                                }
                            };
                        } else {
    						r = new Runnable() {
    							@Override
								public void run() {
    								try {
    									setExpectedEvent(DebugEvent.RESUME, DebugEvent.STEP_RETURN);
    									frame.stepReturn();
    								} catch (DebugException e) {
    									JDIDebugUIPlugin.log(e);
    									cleanup();
    									DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(getDebugTarget(), DebugEvent.CHANGE)});
    								}
    							}
    						};
                        }
					} else if (stackDepth == fOriginalStackDepth){
						// we should be back in the original stack frame - if not, abort
						if (!(frame.getSignature().equals(fOriginalSignature) && frame.getName().equals(fOriginalName) && frame.getDeclaringTypeName().equals(fOriginalTypeName))) {
							missed();
							return events;
						}
						r = new Runnable() {
							@Override
							public void run() {
								try {
									setExpectedEvent(DebugEvent.RESUME, DebugEvent.STEP_INTO);
									frame.stepInto();
								} catch (DebugException e) {
									JDIDebugUIPlugin.log(e);
									cleanup();
									DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(getDebugTarget(), DebugEvent.CHANGE)});
								}
							}
						};
					} else {
						// we returned from the original frame - never hit the desired method
						missed();
						return events;
					}
					DebugPlugin.getDefault().asyncExec(r);
					// filter the events
						return filtered;
				} catch (CoreException e) {
					// abort
					JDIDebugUIPlugin.log(e);
					cleanup();
					return events;
				}
		}
		// execution should not reach here
		return events;

	}

	/**
	 * Called when stepping returned from the original frame without entering the desired method.
	 */
	protected void missed() {
		cleanup();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				String methodName = null;
				try {
					methodName = Signature.toString(getMethod().getSignature(), getMethod().getElementName(), getMethod().getParameterNames(), false, false);
				} catch (JavaModelException e) {
					methodName = getMethod().getElementName();
				}
				new MessageDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(),  ActionMessages.StepIntoSelectionHandler_1, null, NLS.bind(ActionMessages.StepIntoSelectionHandler_Execution_did_not_enter____0____before_the_current_method_returned__1, methodName), MessageDialog.INFORMATION, new String[] {ActionMessages.StepIntoSelectionHandler_2}, 0).open();
			}
		};
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(r);
	}

	/**
	 * Performs the step.
	 */
	public void step() {
		// add event filter and turn off step filters
		DebugPlugin.getDefault().addDebugEventFilter(this);
		fStepFilterEnabledState = getDebugTarget().isStepFiltersEnabled();
		getDebugTarget().setStepFiltersEnabled(false);
		try {
			fOriginalStackDepth = getThread().getStackFrames().length;
			setExpectedEvent(DebugEvent.RESUME, DebugEvent.STEP_INTO);
			getThread().stepInto();
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
			cleanup();
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(getDebugTarget(), DebugEvent.CHANGE)});
		}
	}

	/**
	 * Cleans up when the step is complete/aborted.
	 */
	protected void cleanup() {
		DebugPlugin.getDefault().removeDebugEventFilter(this);
		// restore step filter state
		getDebugTarget().setStepFiltersEnabled(fStepFilterEnabledState);
	}

	/**
	 * Sets the expected debug event kind and detail we are waiting for next.
	 *
	 * @param kind event kind
	 * @param detail event detail
	 */
	private void setExpectedEvent(int kind, int detail) {
		fExpectedKind = kind;
		fExpectedDetail = detail;
	}

	/**
	 * Returns whether the given event is what we expected.
	 *
	 * @param event fire event
	 * @return whether the event is what we expected
	 */
	protected boolean isExpectedEvent(DebugEvent event) {
		return event.getSource().equals(getThread()) &&
			event.getKind() == fExpectedKind &&
			event.getDetail() == fExpectedDetail;
	}
}
