package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaEvaluationListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import java.util.*;

/** 
 * Proxy to a thread reference on the target.
 */
public class JDIThread extends JDIDebugElement implements IJavaThread, ITimeoutListener {

	// Resource String keys
	private static final String PREFIX= "jdi_thread.";
	private static final String ERROR= PREFIX + "error.";
	
	private static final String ERROR_GET_NAME= ERROR + "get_name";
	private static final String ERROR_GET_PRIORITY= ERROR + "get_priority";
	private static final String ERROR_GET_THREAD_GROUP= ERROR + "get_thread_group";
	private static final String ERROR_GET_THREAD_GROUP_NAME= ERROR + "get_thread_group_name";
	private static final String ERROR_DROPPING_FRAME= ERROR + "dropping_frame";
	private static final String ERROR_CREATING_STEP_REQUEST= ERROR + "creating_step_request";
	private static final String ERROR_INVOKING_METHOD= ERROR + "invoking_method";
	private static final String ERROR_RESUME= ERROR + "resume";
	private static final String ERROR_STEP= ERROR + "step";
	private static final String ERROR_SUSPEND= ERROR + "suspend";
	private static final String ERROR_TERMINATE= ERROR + "terminate";
	private static final String CANT_TERMINATE= ERROR + "cant_terminate";
	protected static final String IN_EVALUATION= ERROR + "in_evaluation";
	protected static final String NO_BUILT_STATE= ERROR + "no_built_state";
	protected static final String INVALID_EVALUATION_LOCATION= ERROR + "invalid_evaluation_location";
	
	protected static final String MAIN_THREAD_GROUP = "main";

	/**
	 * Underlying thread.
	 */
	protected ThreadReference fThread;

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
	protected IMarker fCurrentBreakpoint;

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
			internalError(e);
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
			internalError(e);
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
	public IMarker getBreakpoint() {
		if (fCurrentBreakpoint != null && !fCurrentBreakpoint.exists()) {
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
				targetRequestFailed(ERROR_GET_THREAD_GROUP_NAME, e);
			}
			if (tgn != null && tgn.equals(MAIN_THREAD_GROUP)) {
				fIsSystemThread= false;
				break;
			}
		}
	}

	protected void enableStepRequest(int type) throws DebugException {
		EventRequestManager erm= getEventRequestManager();
		try {
			if (fStepRequest != null)
				erm.deleteEventRequest(fStepRequest);
			fStepRequest= erm.createStepRequest(fThread, StepRequest.STEP_LINE, type);
			fStepRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			fStepRequest.addCountFilter(1);
			fStepRequest.enable();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_CREATING_STEP_REQUEST, e);
		}
	}
	
	/**
	 * @see IDebugElement
	 */
	public boolean hasChildren() {
		return isSuspended();
	}

	/**
	 * @see IDebugElement
	 */
	protected List getChildren0() throws DebugException {
		if (isSuspended()) {
			if (isTerminated()) {
				fChildren = Collections.EMPTY_LIST;
				return fChildren;
			}
			if (fRefreshChildren) {
				if (fChildren == null || fChildren.isEmpty()) {
					fChildren = createAllStackFrames();
				} else {
					// compute new or removed stack frames
					List frames= getUnderlyingFrames();
					int offset= 0, length= frames.size();
					if (length > fChildren.size()) {
						// compute new children
						offset= length - fChildren.size();
						for (int i= offset - 1; i >= 0; i--) {
							JDIStackFrame newStackFrame= new JDIStackFrame(this, (StackFrame) frames.get(i));
							// addChild appends - we need a stack, so insert manually
							fChildren.add(0, newStackFrame);
						}
						length= fChildren.size() - offset;
					} else
						if (length < fChildren.size()) {
							// compute removed children
							int removed= fChildren.size() - length;
							for (int i= 0; i < removed; i++) {
								fChildren.remove(0);
							}
						} else {
							if (frames.isEmpty()) {
								fChildren = Collections.EMPTY_LIST;
								return fChildren;
							} else {
								// same number of stack frames - if the TOS is different, remove/replace all stack frames
								Method oldMethod= ((JDIStackFrame) fChildren.get(0)).getUnderlyingMethod();
								if (oldMethod == null) {
									fChildren = createAllStackFrames();
									return fChildren;
								}
								StackFrame newTOS= (StackFrame) frames.get(0);
								Method newMethod= getUnderlyingMethod(newTOS);
								if (newMethod == null) {
									fChildren = createAllStackFrames();
									return fChildren;
								}
								if (!oldMethod.equals(newMethod)) {
									// remove & replace all stack frames
									fChildren= createAllStackFrames();
									// no stack frames to update
									offset= fChildren.size();
								}
							}
						}
					// update existing frames
					if (offset < fChildren.size()) {
						updateStackFrames(frames, offset, fChildren, length);
					}
				}
				fRefreshChildren = false;
			}
		} else
			return Collections.EMPTY_LIST;
		return fChildren;
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
			targetRequestFailed(ERROR_GET_CHILDREN, e);
		} catch (VMDisconnectedException e) {
			return Collections.EMPTY_LIST;
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_CHILDREN, e);
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
			targetRequestFailed(ERROR_GET_CHILDREN, e);
		}
		return method;
	}


	/**
	 * Invokes a method in this thread, and returns the result. Only one receiver may
	 * be specified - either a class or an object, the other must be <code>null</code>.
	 */
	protected Value invokeMethod(ClassType receiverClass, ObjectReference receiverObject, Method method, List args) throws DebugException {
		if (fInEvaluation) {
			requestFailed(IN_EVALUATION, null);
		}
		Value result= null;
		int timeout= getReqeustTimeout();
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
			requestFailed(IN_EVALUATION, null);
		}
		ObjectReference result= null;
		int timeout= getReqeustTimeout();
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
		targetRequestFailed(ERROR_INVOKING_METHOD, e);
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
	 * Sets the timeout interval for jdi requests in millieseconds
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
	protected int getReqeustTimeout() {
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
				targetRequestFailed(ERROR_GET_NAME, e);
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
			p= getUnderlyingThread().referenceType().fieldByName("priority");
			if (p == null) {
				requestFailed(ERROR_GET_PRIORITY, null);
			}
			Value v= getUnderlyingThread().getValue(p);
			if (v instanceof IntegerValue) {
				return ((IntegerValue)v).value();
			} else {
				requestFailed(ERROR_GET_PRIORITY, null);
			}
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_GET_PRIORITY, e);
		}
		return -1;
	}

	/**
	 * @see IThread
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		if (isSuspended()) {
			List c= getChildren0();
			if (c.isEmpty()) {
				return null;
			} else {
				return (IStackFrame) c.get(0);
			}
		} else {
			return null;
		}
	}

	protected void handleBreakpoint(BreakpointEvent event) {
		abortDropAndStep();
		IBreakpointManager bpManager= getBreakpointManager();
		fCurrentBreakpoint= (IMarker) event.request().getProperty(IDebugConstants.BREAKPOINT_MARKER);
		setRunning(false, DebugEvent.BREAKPOINT);
		((JDIDebugTarget) getDebugTarget()).expireHitCount(event);
	}

	protected void handleException(ExceptionEvent event) {
		abortDropAndStep();
		fCurrentBreakpoint= (IMarker) event.request().getProperty(IDebugConstants.BREAKPOINT_MARKER);
		setRunning(false, DebugEvent.BREAKPOINT);
	}

	/**
	 * Suspend the thread based on the method entry event
	 */
	protected void handleSuspendMethodEntry(IMarker breakpoint) {
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
				} else if (getChildren0().indexOf(fDestinationFrame) == -1) {
					fDestinationFrame = null;
				} else {
					if (hasPendingEvents()) {
						fDestinationFrame = null;
					} else {
						stepReturn0();
						fRunning = true;
						fStepCount--;
						return;
					}
				}
			} catch (DebugException e) {
				abortDropAndStep();
				internalError(e);
			}
		} else if (fDropping) {
			fFramesToDrop--;
			fDropping= fFramesToDrop > 0;
			if (fDropping) {
				try {
					dropTopFrame();
				} catch (DebugException e) {
					abortDropAndStep();
					internalError(e);
				}
			} else {
				try {
					reenterTopFrame();
				} catch (DebugException e) {
					abortDropAndStep();
					internalError(e);
				}
			}
		} else if (fReentering) {
			fReentering= false;
			try {
				stepInto0();
			} catch (DebugException e) {
				abortDropAndStep();
				internalError(e);
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
				targetRequestFailed(ERROR_GET_THREAD_GROUP_NAME, e);
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
			targetRequestFailed(ERROR_RESUME, e);
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
					fChildren = null;
				}
				if (!fStepping || fStepCount == 1) {
					fireResumeEvent(detail);
				}
			} else {
				if (detail == DebugEvent.STEP_END) {
					fStepCount--;
				}
				if (fStepCount == 0) {
					stopStepTimer();
					// update underlying stack frames
					try {
						getChildren0();
					} catch (DebugException e) {
						internalError(e);
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
		if (fChildren != null) {
			Iterator frames = fChildren.iterator();
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
			targetRequestFailed(ERROR_STEP, e);
		}
	}
	
	/**
	 * A step has timed out. Children are disposed.
	 */
	public void timeout() {
		fChildren = Collections.EMPTY_LIST;
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
					getEventRequestManager().deleteEventRequest(fStepRequest);
				} catch (VMDisconnectedException e) {
				} catch (RuntimeException e) {
					targetRequestFailed(ERROR_SUSPEND, e);
				}
			}
			fThread.suspend();
			abortDropAndStep();
			setRunning(false, DebugEvent.CLIENT_REQUEST);
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			setRunning(true);
			targetRequestFailed(ERROR_SUSPEND, e);
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
				targetRequestFailed(ERROR_TERMINATE, e);
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				targetRequestFailed(ERROR_TERMINATE, e);
			}

			// Resume the thread so that stop will work
			resume();
		} else {
			requestFailed(CANT_TERMINATE, null);
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

		fFramesToDrop= getChildren0().indexOf(frame);
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
			targetRequestFailed(ERROR_DROPPING_FRAME, e);
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
			EventRequestManager erm= getEventRequestManager();
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
			targetRequestFailed(ERROR_DROPPING_FRAME, e);
		}
	}

	protected void abortDropAndStep() {
		fStepCount = 0;
		abortDrop();
		abortStep();
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
		EventRequestManager erm= getEventRequestManager();
		try {
			if (fStepRequest != null)
				erm.deleteEventRequest(fStepRequest);
			fStepRequest = null;
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			internalError(e);
		}
	}

	/**
	 * @see IVariableLookup
	 */
	public IVariable findVariable(String varName) throws DebugException {
		if (isSuspended()) {
			Iterator stackframes= getChildren0().iterator();
			while (stackframes.hasNext()) {
				JDIStackFrame sf= (JDIStackFrame) stackframes.next();
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
			requestFailed(IN_EVALUATION, null);
		}
		if (!evaluationContext.getProject().hasBuildState()) {
			requestFailed(NO_BUILT_STATE, null);
		}
		if (!fEventSuspend) {
			requestFailed(INVALID_EVALUATION_LOCATION, null);
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
				targetRequestFailed(ERROR_GET_THREAD_GROUP, e);
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

