package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.*;

import com.sun.jdi.*;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.*;

/** 
 * Proxy to a thread reference on the target.
 */
public class JDIThread extends JDIDebugElement implements IJavaThread, ITimeoutListener {
	
	protected static final String MAIN_THREAD_GROUP = "main"; //$NON-NLS-1$

	/**
	 * Underlying thread.
	 */
	protected ThreadReference fThread;
	
	/**
	 * Collection of stack frames
	 */
	protected List fStackFrames;

	/**
	 * Underlying thread group.
	 */
	protected ThreadGroupReference fThreadGroup;
	
	/**
	 * Whether children need to be refreshed. Set to
	 * to true after a step has started.
	 */
	protected boolean fRefreshChildren = true;

	/**
	 * Step request used to step in this thread (only one is allowed per
	 * thread).
	 */
	protected StepRequest fStepRequest= null;
	/**
	 * Whether running.
	 */
	protected boolean fRunning;
	/**
	 * Whether stepping.
	 */
	protected boolean fStepping;
	protected int fStepCount= 0;
		
	/**
	 * Whether suspended by an event in the VM such as a
	 * breakpoint or step, or via an explicit user
	 * request to suspend.
	 */
	protected boolean fEventSuspend = false;
	
	/**
	 * The destination stack frame when stepping
	 * in the non-top stack frame, or <code>null</code>
	 * when stepping in the top stack frame.
	 */
	protected IStackFrame fDestinationFrame;

	/**
	 * Step timer. During a long running step, children are disposed.
	 */
	protected Timer fTimer;

	/**
	 * Whether terminated.
	 */
	protected boolean fTerminated;
	/**
	 * Whether this thread is a system thread.
	 */
	protected boolean fIsSystemThread;

	/**
	 * The breakpoint that caused the last suspend, or
	 * <code>null</code> if none.
	 */
	protected IJavaBreakpoint fCurrentBreakpoint;

	/**
	 * The cached named of the underlying thread.
	 */
	protected String fName= null;

	/**
	 * Whether this thread is doing a "drop to frame".
	 */
	protected boolean fDropping= false;

	/**
	 * Whether this thread is doing a "reenter top frame"
	 */
	protected boolean fReentering= false;

	/**
	 * A count of the number of frames remaining to drop
	 */
	protected int fFramesToDrop= 0;
	
	/**
	 * Whether this thread is currently performing
	 * an evaluation (invoke method). Nested method
	 * invocations cannot be performed.
	 */
	protected boolean fInEvaluation = false;
	protected boolean fEvaluationAborted = false;

	/**
	 * Creates a new thread on the underlying thread reference.
	 */
	public JDIThread(JDIDebugTarget target, ThreadReference thread) {
		super(target);
		fThread= thread;
		initialize();
	}

	/**
	 * Initializes this thread on creation
	 */
	protected void initialize() {
		// system thread
		try {
			determineIfSystemThread();
		} catch (DebugException e) {
			logError(e);
		}

		// state
		fTerminated= false;
		fStepping= false;
		try {
			fRunning= !getUnderlyingThread().isSuspended();
		} catch (VMDisconnectedException e) {
			fTerminated = true;
			fRunning = false;
		} catch (RuntimeException e) {
			logError(e);
			fRunning= false;
		}

	}
	
	/**
	 * @see IDebugElement
	 */
	public int getElementType() {
		return THREAD;
	}

	/**
	 * @see IDebugElement
	 */
	public IThread getThread() {
		return this;
	}

	/**
	 * @see IJavaThread
	 */
	public IJavaBreakpoint getBreakpoint() {
		if (fCurrentBreakpoint != null && !fCurrentBreakpoint.getMarker().exists()) {
			fCurrentBreakpoint= null;
		}
		return fCurrentBreakpoint;
	}

	/**
	 * @see ISuspendResume
	 */
	public boolean canResume() {
		return isSuspended();
	}

	/**
	 * @see ISuspendResume
	 */
	public boolean canSuspend() {
		return !isSuspended();
	}

	/**
	 * @see ITerminate
	 */
	public boolean canTerminate() {
		ObjectReference threadDeath= ((JDIDebugTarget) getDebugTarget()).getThreadDeathInstance();
		return threadDeath != null && !isSystemThread() && !isTerminated();
	}

	/**
	 * @see IStep
	 */
	public boolean canStepInto() {
		return isSuspended() && !isStepping();
	}

	/**
	 * @see IStep
	 */
	public boolean canStepOver() {
		return isSuspended() && !isStepping();
	}

	/**
	 * @see IStep
	 */
	public boolean canStepReturn() {
		return isSuspended() && !isStepping();
	}

	/**
	 * Determines and sets whether this thread represents a system thread.
	 */
	protected void determineIfSystemThread() throws DebugException {
		fIsSystemThread= false;
		ThreadGroupReference tgr= getUnderlyingThreadGroup();
		fIsSystemThread = tgr != null;
		while (tgr != null) {
			String tgn= null;
			try {
				tgn= tgr.name();
				tgr= tgr.parent();
			} catch (VMDisconnectedException e) {
				break;
			} catch (UnsupportedOperationException e) {
				fIsSystemThread = false;
				break;
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_determining_if_system_thread"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
			if (tgn != null && tgn.equals(MAIN_THREAD_GROUP)) {
				fIsSystemThread= false;
				break;
			}
		}
	}

	protected void enableStepRequest(int type) throws DebugException {
		EventRequestManager erm= getVM().eventRequestManager();
		try {
			if (fStepRequest != null)
				erm.deleteEventRequest(fStepRequest);
			fStepRequest= erm.createStepRequest(fThread, StepRequest.STEP_LINE, type);
			fStepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			fStepRequest.addCountFilter(1);
			attachFiltersToStepRequest(fStepRequest);
			fStepRequest.enable();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_creating_step_request"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * If step filters are currently switched on, set all active filters on the step request.
	 */
	protected void attachFiltersToStepRequest(StepRequest stepRequest) {
		if (!JDIDebugModel.useStepFilters()) {
			return;
		}
		List activeFilters = JDIDebugModel.getActiveStepFilters();
		Iterator iterator = activeFilters.iterator();
		while (iterator.hasNext()) {
			String filter = (String)iterator.next();
			fStepRequest.addClassExclusionFilter(filter);
		}
	}

	/**
	 * @see IThread
	 */
	public IStackFrame[] getStackFrames() throws DebugException {
		List list = getStackFrames0();
		return (IStackFrame[])list.toArray(new IStackFrame[list.size()]);
	}
	
	/**
	 *
	 */
	protected synchronized List getStackFrames0() throws DebugException {
		if (isSuspended()) {
			if (isTerminated()) {
				fStackFrames = Collections.EMPTY_LIST;
				return fStackFrames;
			}
			if (fRefreshChildren) {
				if (fStackFrames == null || fStackFrames.isEmpty()) {
					fStackFrames = createAllStackFrames();
				} else {
					// compute new or removed stack frames
					List frames= getUnderlyingFrames();
					int offset= 0, length= frames.size();
					if (length > fStackFrames.size()) {
						// compute new children
						offset= length - fStackFrames.size();
						for (int i= offset - 1; i >= 0; i--) {
							JDIStackFrame newStackFrame= new JDIStackFrame(this, (StackFrame) frames.get(i));
							// addChild appends - we need a stack, so insert manually
							fStackFrames.add(0, newStackFrame);
						}
						length= fStackFrames.size() - offset;
					} else
						if (length < fStackFrames.size()) {
							// compute removed children
							int removed= fStackFrames.size() - length;
							for (int i= 0; i < removed; i++) {
								fStackFrames.remove(0);
							}
						} else {
							if (frames.isEmpty()) {
								fStackFrames = Collections.EMPTY_LIST;
								return fStackFrames;
							} else {
								// same number of stack frames - if the TOS is different, remove/replace all stack frames
								Method oldMethod= ((JDIStackFrame) fStackFrames.get(0)).getUnderlyingMethod();
								if (oldMethod == null) {
									fStackFrames = createAllStackFrames();
									return fStackFrames;
								}
								StackFrame newTOS= (StackFrame) frames.get(0);
								Method newMethod= getUnderlyingMethod(newTOS);
								if (newMethod == null) {
									fStackFrames = createAllStackFrames();
									return fStackFrames;
								}
								if (!oldMethod.equals(newMethod)) {
									// remove & replace all stack frames
									fStackFrames= createAllStackFrames();
									// no stack frames to update
									offset= fStackFrames.size();
								}
							}
						}
					// update existing frames
					if (offset < fStackFrames.size()) {
						updateStackFrames(frames, offset, fStackFrames, length);
					}
				}
				fRefreshChildren = false;
			}
		} else
			return Collections.EMPTY_LIST;
		return fStackFrames;
	}

	/**
	 * Helper method for #getChildren0 to create new children for all frames
	 */
	protected List createAllStackFrames() throws DebugException {
		List frames= getUnderlyingFrames();
		if (frames == null) {
			return new ArrayList(0);
		}
		List list= new ArrayList(frames.size());
		Iterator iter= frames.iterator();
		while (iter.hasNext()) {
			JDIStackFrame newStackFrame= new JDIStackFrame(this, (StackFrame) iter.next());
			list.add(newStackFrame);
		}
		return list;
	}

	/**
	 * Helper method for #getChildren to retrieve stack frames for this thread
	 *
	 * @see com.sun.jdi.ThreadReference
	 */
	protected List getUnderlyingFrames() throws DebugException {
		List frames= null;
		try {
			frames= fThread.frames();
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_stack_frames"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (VMDisconnectedException e) {
			return Collections.EMPTY_LIST;
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_stack_frames_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return frames;
	}

	/**
	 * Helper method for #getChildren to retrieve the method for a stack frame
	 */
	protected Method getUnderlyingMethod(StackFrame frame) throws DebugException {
		Method method= null;
		try {
			method= frame.location().method();
		} catch (VMDisconnectedException e) {
			return null;
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_method"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return method;
	}


	/**
	 * Invokes a method in this thread, and returns the result. Only one receiver may
	 * be specified - either a class or an object, the other must be <code>null</code>.
	 */
	protected Value invokeMethod(ClassType receiverClass, ObjectReference receiverObject, Method method, List args) throws DebugException {
		if (fInEvaluation) {
			requestFailed(JDIDebugModelMessages.getString("JDIThread.Cannot_perform_nested_evaluations"), null); //$NON-NLS-1$
		}
		Value result= null;
		int timeout= getRequestTimeout();
		try {
			// set the request timeout to be infinite
			setRequestTimeout(Integer.MAX_VALUE);
			setRunning(true);
			fInEvaluation = true;
			if (receiverClass == null) {
				result= receiverObject.invokeMethod(fThread, method, args, ClassType.INVOKE_SINGLE_THREADED);
			} else {
				result= receiverClass.invokeMethod(fThread, method, args, ClassType.INVOKE_SINGLE_THREADED);
			}
		} catch (InvalidTypeException e) {
			invokeFailed(e, timeout);
		} catch (ClassNotLoadedException e) {
			invokeFailed(e, timeout);
		} catch (IncompatibleThreadStateException e) {
			invokeFailed(e, timeout);
		} catch (InvocationException e) {
			invokeFailed(e, timeout);
		} catch (RuntimeException e) {
			invokeFailed(e, timeout);
		}

		invokeComplete(timeout);
		if (fEvaluationAborted) {
			fEvaluationAborted = false;
			resume();
		}
		return result;
	}
	
	/**
	 * Called by JDIValue when an evaluation of
	 * #toString times out. Causes this thread to
	 * be automatically resumed when it returns from
	 * its evaluation - see <code>invokeMethod</code>.
	 */
	protected void abortEvaluation() {
		fEvaluationAborted = true;
	}
	
	/**
	 * Invokes a method in this thread, creating a new instance of the given
	 * class using the specified constructor, and returns the result.
	 */
	protected ObjectReference newInstance(ClassType receiverClass, Method constructor, List args) throws DebugException {
		if (fInEvaluation) {
			requestFailed(JDIDebugModelMessages.getString("JDIThread.Cannot_perform_nested_evaluations_2"), null); //$NON-NLS-1$
		}
		ObjectReference result= null;
		int timeout= getRequestTimeout();
		try {
			// set the request timeout to be infinite
			setRequestTimeout(Integer.MAX_VALUE);
			setRunning(true);
			fInEvaluation = true;
			result= receiverClass.newInstance(fThread, constructor, args, ClassType.INVOKE_SINGLE_THREADED);
		} catch (InvalidTypeException e) {
			invokeFailed(e, timeout);
		} catch (ClassNotLoadedException e) {
			invokeFailed(e, timeout);
		} catch (IncompatibleThreadStateException e) {
			invokeFailed(e, timeout);
		} catch (InvocationException e) {
			invokeFailed(e, timeout);
		} catch (RuntimeException e) {
			invokeFailed(e, timeout);
		}

		invokeComplete(timeout);
		return result;
	}
	
	/**
	 * An invocation failed. Restore the JDI timeout value and
	 * handle the exception.
	 */
	protected void invokeFailed(Throwable e, int restoreTimeout) throws DebugException {
		invokeComplete(restoreTimeout);
		targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_invoking_method"), new String[] {e.toString()}), e); //$NON-NLS-1$
	}
	
	/**
	 * Update state when invocation is complete. Restore
	 * the orginal timeout value for JDI requests.
	 */
	protected void invokeComplete(int restoreTimeout) {
		setRunning(false);
		fInEvaluation = false;
		setRequestTimeout(restoreTimeout);
	}
	
	/**
	 * Sets the timeout interval for jdi requests in milliseconds
	 */
	protected void setRequestTimeout(int timeout) {
		VirtualMachine vm = getVM();
		if (vm instanceof org.eclipse.jdi.VirtualMachine) {
			((org.eclipse.jdi.VirtualMachine) vm).setRequestTimeout(timeout);
		}
	}
	
	/**
	 * Returns the timeout interval for jdi requests in millieseconds,
	 * or -1 if not supported
	 */
	protected int getRequestTimeout() {
		VirtualMachine vm = getVM();
		if (vm instanceof org.eclipse.jdi.VirtualMachine) {
			return ((org.eclipse.jdi.VirtualMachine) vm).getRequestTimeout();
		}
		return -1;
	}
	
	/**
	 * @see IDebugElement
	 */
	public String getName() throws DebugException {
		if (fName == null) {
			try {
				fName = getUnderlyingThread().name();
			} catch (VMDisconnectedException e) {
				fName = getUnknownMessage();
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		return fName;
	}

	/**
	 * @see IThread
	 */
	public int getPriority() throws DebugException {
		// to get the priority, we must get the value from the "priority" field
		Field p= null;
		try {
			p= getUnderlyingThread().referenceType().fieldByName("priority"); //$NON-NLS-1$
			if (p == null) {
				requestFailed(JDIDebugModelMessages.getString("JDIThread.no_priority_field"), null); //$NON-NLS-1$
			}
			Value v= getUnderlyingThread().getValue(p);
			if (v instanceof IntegerValue) {
				return ((IntegerValue)v).value();
			} else {
				requestFailed(JDIDebugModelMessages.getString("JDIThread.priority_not_an_integer"), null); //$NON-NLS-1$
			}
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_priority"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		return -1;
	}

	/**
	 * @see IThread
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		if (isSuspended()) {
			List c= getStackFrames0();
			if (c.isEmpty()) {
				return null;
			} else {
				return (IStackFrame) c.get(0);
			}
		} else {
			return null;
		}
	}

	/**
	 * Suspend this thread for the given breakpoint. If notify equals <code>true</code>,
	 * send notification of the change. Otherwise, do not.
	 */
	protected void handleSuspendForBreakpoint(JavaBreakpoint breakpoint) {
		abortDropAndStep();
		fCurrentBreakpoint= breakpoint;
		setRunning(false, DebugEvent.BREAKPOINT);
	}

	protected void handleStep(StepEvent event) {
		fRunning = false;
		if (fDestinationFrame != null) {
			try {
				if (getTopStackFrame().equals(fDestinationFrame)) {
					fDestinationFrame = null;
				} else if (getStackFrames0().indexOf(fDestinationFrame) == -1) {
					fDestinationFrame = null;
				} else {
					if (hasPendingEvents()) {
						fDestinationFrame = null;
					} else {
						stepReturn0();
						fRunning = true;
						decrementStepCount();
						return;
					}
				}
			} catch (DebugException e) {
				abortDropAndStep();
				logError(e);
			}
		} else if (fDropping) {
			fFramesToDrop--;
			fDropping= fFramesToDrop > 0;
			if (fDropping) {
				try {
					dropTopFrame();
				} catch (DebugException e) {
					abortDropAndStep();
					logError(e);
				}
			} else {
				try {
					reenterTopFrame();
				} catch (DebugException e) {
					abortDropAndStep();
					logError(e);
				}
			}
		} else if (fReentering) {
			fReentering= false;
			try {
				stepInto0();
			} catch (DebugException e) {
				abortDropAndStep();
				logError(e);
			}
		} 
		fRunning = true;
		setRunning(false, DebugEvent.STEP_END);
	}

	/**
	 * @see IStep
	 */
	public boolean isStepping() {
		return fStepping;
	}

	/**
	 * @see IStep
	 */
	public boolean isSuspended() {
		return !fRunning && !fTerminated;
	}

	/**
	 * @see IJavaThread
	 */
	public boolean isSystemThread() {
		return fIsSystemThread;
	}

	/**
	 * @see IJavaThread
	 */
	public String getThreadGroupName() throws DebugException {
		ThreadGroupReference tgr= getUnderlyingThreadGroup();
		if (tgr != null) {
			try {
				return fThreadGroup.name();
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_group_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		return getUnknownMessage();
	}

	/**
	 * @see ITerminate
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/**
	 * @see ISuspendResume
	 */
	public void resume() throws DebugException {
		if (!isSuspended()) {
			return;
		}
		try {
			setRunning(true);
			fThread.resume();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			setRunning(false);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_resuming"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}

	/**
	 * Sets the running state for this thread. Invalidates
	 * children on a step start, or clears them on a resume.
	 * Fires resume/suspend events. Starts/stops step timer.
	 */
	void setRunning(boolean running, int detail) {
		if (fRunning != running) {
			fRunning= running;
			if (fRunning) {
				fCurrentBreakpoint= null;
				fRefreshChildren = true;
				if (detail == DebugEvent.STEP_START) {
					fStepCount++;
					fStepping = true;
					if (fStepCount == 1) {
						invalidateStackFrames();
						startStepTimer();
					}
				} else {
					fStackFrames = null;
				}
				if (!fStepping || fStepCount == 1) {
					fireResumeEvent(detail);
				}
			} else {
				if (detail == DebugEvent.STEP_END) {
					decrementStepCount();
				}
				if (fStepCount == 0) {
					stopStepTimer();
					// update underlying stack frames
					try {
						getStackFrames0();
					} catch (DebugException e) {
						logError(e);
					}
					fStepping= false;
					fireSuspendEvent(detail);
				}
				fEventSuspend = detail != DebugEvent.CLIENT_REQUEST;
			}
		}
	}

	void setRunning(boolean running) {
		setRunning(running, -1);
	}

	protected void invalidateStackFrames() {
		if (fStackFrames != null) {
			Iterator frames = fStackFrames.iterator();
			while (frames.hasNext()) {
				((JDIStackFrame)frames.next()).invalidateVariables();
			}
		}
	}
	
	protected void step(int type) throws DebugException {
		try {
			setRunning(true, DebugEvent.STEP_START);
			enableStepRequest(type);			
			fThread.resume();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			setRunning(false, DebugEvent.STEP_END);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_stepping"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * A step has timed out. Children are disposed.
	 */
	public void timeout() {
		fStackFrames = Collections.EMPTY_LIST;
		fireChangeEvent();
	}

	/**
	 * @see IStep
	 */
	public void stepInto() throws DebugException {
		if (!canStepInto()) {
			return;
		}
		stepInto0();
	}
	
	private void stepInto0() throws DebugException {
		step(StepRequest.STEP_INTO);
	}

	/**
	 * @see IStep
	 */
	public void stepOver() throws DebugException {
		if (!canStepOver()) {
			return;
		}
		step(StepRequest.STEP_OVER);
	}

	/**
	 * @see IStep
	 */
	public void stepReturn() throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		stepReturn0();
	}
	
	private void stepReturn0() throws DebugException {
		step(StepRequest.STEP_OUT);
	}

	/**
	 * @see ISuspendResume
	 */
	public void suspend() throws DebugException {
		try {
			// remove any pending step request
			if (fStepRequest != null) {
				try {
					getVM().eventRequestManager().deleteEventRequest(fStepRequest);
				} catch (VMDisconnectedException e) {
				} catch (RuntimeException e) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_deleting_step_request"), new String[] {e.toString()}), e); //$NON-NLS-1$
				}
			}
			fThread.suspend();
			abortDropAndStep();
			setRunning(false, DebugEvent.CLIENT_REQUEST);
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			setRunning(true);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_suspending"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}

	/**
	 * @see ITerminate
	 */
	public void terminate() throws DebugException {

		ObjectReference threadDeath= ((JDIDebugTarget) getDebugTarget()).getThreadDeathInstance();
		if (threadDeath != null) {
			try {
				fThread.stop(threadDeath);
			} catch (InvalidTypeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_terminating"), new String[] {e.toString()}), e); //$NON-NLS-1$
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_terminating_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}

			// Resume the thread so that stop will work
			resume();
		} else {
			requestFailed(JDIDebugModelMessages.getString("JDIThread.unable_to_terminate"), null); //$NON-NLS-1$
		}

	}

	/**
	 * Replaces the StackFrame objects in the old frames list with the objects
	 * from the new frames list. StackFrames are invalid after a resume or step
	 * and must be replaced with the new objects.
	 */
	protected void updateStackFrames(List newFrames, int offset, List oldFrames, int length) throws DebugException {
		for (int i= 0; i < length; i++) {
			JDIStackFrame frame= (JDIStackFrame) oldFrames.get(offset);
			frame.setUnderlyingStackFrame((StackFrame) newFrames.get(offset));
			offset++;
		}
	}

	/**
	 * Drops to the given stack frame
	 */
	protected void dropToFrame(IStackFrame frame) throws DebugException {

		fFramesToDrop= getStackFrames0().indexOf(frame);
		fDropping= fFramesToDrop > 0;
		if (fDropping) {
			dropTopFrame();
		} else {
			reenterTopFrame();
		}

	}

	/**
	 * Drops the top frame, sending a step start event
	 */
	protected void dropTopFrame() throws DebugException {
		try {
			enableStepRequest(StepRequest.STEP_OUT);
			setRunning(true, DebugEvent.STEP_START);
			// Resume with a do return
			org.eclipse.jdi.hcr.ThreadReference hcrThread= (org.eclipse.jdi.hcr.ThreadReference) fThread;
			hcrThread.doReturn(null, true);
		} catch (VMDisconnectedException e) {
			setRunning(false);
		} catch (RuntimeException e) {
			setRunning(false);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_popping"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}
	
	protected void stepToFrame(IStackFrame frame) throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		fDestinationFrame = frame;
		stepReturn();
	}
	
	/**
	 * Reenters the top frame
	 *
	 * @exception DebugException on failure
	 */
	private void reenterTopFrame() throws DebugException {
		try {
			fReentering= true;
			EventRequestManager erm= getVM().eventRequestManager();
			if (fStepRequest != null) {
				erm.deleteEventRequest(fStepRequest);
			}
			fStepRequest= ((org.eclipse.jdi.hcr.EventRequestManager) erm).createReenterStepRequest(fThread);
			fStepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			fStepRequest.addCountFilter(1);
			fStepRequest.enable();
			// Resume with a do return
			org.eclipse.jdi.hcr.ThreadReference hcrThread= (org.eclipse.jdi.hcr.ThreadReference) fThread;
			hcrThread.doReturn(null, true);
			setRunning(true, DebugEvent.STEP_START);
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			fReentering= false;
			setRunning(false);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_re-entering"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}

	protected void abortDropAndStep() {
		fStepCount = 0;
		abortDrop();
		abortStep();
	}
	
	/**
	 * Decrements the step count, ensuring it does not
	 * go negative.
	 */
	protected void decrementStepCount() {
		if (fStepCount > 0) {
			fStepCount--;
		}
	}
	
	/**
	 * Aborts the drop
	 */
	protected void abortDrop() {
		fDropping= false;
		fFramesToDrop= 0;
	}
	
	/**
	 * Aborts the current step
	 */
	protected void abortStep() {
		fDestinationFrame = null;
		EventRequestManager erm= getVM().eventRequestManager();
		try {
			if (fStepRequest != null) {
				erm.deleteEventRequest(fStepRequest);
			}
			fStepRequest = null;
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			internalError(e);
		}
	}

	/**
	 * @see IJavaThread
	 */
	public IVariable findVariable(String varName) throws DebugException {
		if (isSuspended()) {
			IStackFrame[] stackframes= getStackFrames();
			for (int i = 0; i < stackframes.length; i++) {
				JDIStackFrame sf= (JDIStackFrame)stackframes[i];
				IVariable var= sf.findVariable(varName);
				if (var != null) {
					return var;
				}
			}
		}
		return null;
	}

	/**
	 * Evaluates the snippet using this thread (no stack frame context)
	 *
	 * @see IJavaThread
	 */
	public void evaluate(String snippet, IJavaEvaluationListener listener, IJavaProject project) throws DebugException {
		IEvaluationContext underlyingContext = ((JDIDebugTarget)getDebugTarget()).getEvaluationContext(project);
		evaluate(snippet, listener, underlyingContext);
	}
	
	/**
	 *
	 * @see IJavaThread
	 */
	public void evaluate(String snippet, IJavaEvaluationListener listener, IEvaluationContext evaluationContext) throws DebugException {
		verifyEvaluation(evaluationContext);
		ThreadEvaluationContext context = new ThreadEvaluationContext(this, evaluationContext);
		context.evaluate(snippet, listener);
	}
	
	protected void verifyEvaluation(IEvaluationContext evaluationContext) throws DebugException {
		if (fInEvaluation) {
			requestFailed(JDIDebugModelMessages.getString("JDIThread.Cannot_perform_nested_evaluations_3"), null); //$NON-NLS-1$
		}
		if (!evaluationContext.getProject().hasBuildState()) {
			requestFailed(JDIDebugModelMessages.getString("JDIThread.Project_must_be_built"), null); //$NON-NLS-1$
		}
		if (!fEventSuspend) {
			requestFailed(JDIDebugModelMessages.getString("JDIThread.Unable_to_perform_evaluation_at_current_location"), null); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see IJavaEvaluate
	 */
	public boolean canPerformEvaluation() {
		return isSuspended() && !fInEvaluation && fEventSuspend;
	}
	
	protected void dispose() {
		if (fTimer != null) {
			fTimer.dispose();
		}
	}
	
	/**
	 * Notification this thread has terminated - update state
	 */
	protected void terminated() {
		fTerminated= true;
		fRunning= false;
		dispose();		
		fireTerminateEvent();
	}
	
	/** 
	 * Returns this thread's underlying thread reference
	 */
	protected ThreadReference getUnderlyingThread() {
		return fThread;
	}
	
	/** 
	 * Returns this thread's underlying thread group
	 */
	protected ThreadGroupReference getUnderlyingThreadGroup() throws DebugException {
		if (fThreadGroup == null) {
			try {
				fThreadGroup = getUnderlyingThread().threadGroup();
			} catch (VMDisconnectedException e) {
				return null;
			} catch (UnsupportedOperationException e) {
				return null;
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_group"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		return fThreadGroup;
	}
	
	/**
	 * Starts the step timer.
	 */
	protected void startStepTimer() {
		if (fTimer == null) {
			fTimer = new Timer();
		}
		fTimer.start(this, 3000);
	}
		 
	/**
	 * Stops the step timer.
	 */
	protected void stopStepTimer() {
		if (fTimer != null) {
			fTimer.stop();
		}
	}
}