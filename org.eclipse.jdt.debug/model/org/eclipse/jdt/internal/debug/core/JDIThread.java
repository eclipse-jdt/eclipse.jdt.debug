package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaEvaluationListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

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
	 * Currently pending step handler, <code>null</code>
	 * when not performing a step.
	 */
	private StepHandler fStepHandler= null;
	
	/**
	 * Whether running.
	 */
	private boolean fRunning;
		
	/**
	 * Whether suspended by an event in the VM such as a
	 * breakpoint or step, or via an explicit user
	 * request to suspend.
	 */
	protected boolean fEventSuspend = false;

	/**
	 * Action timer. During a long running action,
	 * stack frames are collapsed.
	 */
	private Timer fTimer;

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
	 * Whether this thread is currently performing
	 * an evaluation (invoke method). Nested method
	 * invocations cannot be performed.
	 */
	protected boolean fInEvaluation = false;
	protected boolean fEvaluationAborted = false;
	
	/**
	 * Whether this thread has been interrupted during
	 * the last evaluation. That is, was a breakpoint
	 * hit, did the user suspend the thread, or did
	 * the evaluation timeout.
	 */
	private boolean fInterrupted = false;

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
		try {
			fRunning= !getUnderlyingThread().isSuspended();
		} catch (VMDisconnectedException e) {
			fTerminated = true;
			fRunning = false;
			if (getDebugTarget().isDisconnected() || getDebugTarget().isTerminated()) {
				return;
			}
			logError(e);
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
			} catch (UnsupportedOperationException e) {
				fIsSystemThread = false;
				break;
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_determining_if_system_thread"), new String[] {e.toString()}), e); //$NON-NLS-1$
				return;
			}
			if (tgn != null && tgn.equals(MAIN_THREAD_GROUP)) {
				fIsSystemThread= false;
				break;
			}
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
	 * Sets the stack frames for this thread.
	 * 
	 * @param frames a list of stack frames
	 */
	protected void setStackFrames(List frames) {
		fStackFrames = frames;
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
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_stack_frames_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
			return Collections.EMPTY_LIST;
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
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_method"), new String[] {e.toString()}), e); //$NON-NLS-1$
			return null;
		}
		return method;
	}

	/**
	 * Returns the number of frames on the stack from the
	 * underlying thread.
	 * 
	 * @return number of frames on the stack
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This thread is not suspended</li>
	 * </ul>
	 */
	protected int getUnderlyingFrameCount() throws DebugException {
		try {
			return getUnderlyingThread().frameCount();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format("{0} occurred retrieving frame count.", new String[] {e.toString()}), e);
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(MessageFormat.format("{0} occurred retrieving frame count.", new String[] {e.toString()}), e);
		}
		// execution will not reach here - try block will either
		// return or exception will be thrown
		return -1;
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
			preserveStackFrames();
			resetInterrupted();
			startCollapseTimer();
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
	 * Called when an evaluation may have been interrupted.
	 * If an evaluation gets suspended, or times out, we need
	 * to fire a suspend event when the evaluation compeletes,
	 * to update the UI.
	 */
	protected void interrupted() {
		if (isPerformingEvaluation()) {
			fInterrupted = true;
			stopCollapseTimer();
		}
	}
	
	/**
	 * Resets the interrupted state to <code>false</code>. 
	 */
	protected void resetInterrupted() {
		fInterrupted = false;
	}
	
	/**
	 * Returns whether this thread was interrupted during the
	 * last evaluation.
	 */
	protected boolean wasInterrupted() {
		return fInterrupted;
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
			preserveStackFrames();
			resetInterrupted();
			startCollapseTimer();
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
		stopCollapseTimer();
		boolean interrupted= wasInterrupted();
		setRunning(false);
		fInEvaluation = false;
		setRequestTimeout(restoreTimeout);
		// update preserved stack frames
		try {
			getStackFrames0();
		} catch (DebugException e) {
			logError(e);
		}
		if (interrupted) {
			fireSuspendEvent(-1);
		}
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
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
				fName = getUnknownMessage();
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
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_priority"), new String[] {e.toString()}), e); //$NON-NLS-1$
			return -1;
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
		abortStep();
		fCurrentBreakpoint= breakpoint;
		setRunning(false);
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}

	/**
	 * @see IStep
	 */
	public boolean isStepping() {
		return getPendingStepHandler() != null;
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
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_group_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
				return getUnknownMessage();
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
			disposeStackFrames();
			fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			fThread.resume();
		} catch (RuntimeException e) {
			setRunning(false);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_resuming"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}
		
	/**
	 * Sets whether this thread is currently executing.
	 * When set to <code>true</code>, this thread's current
	 * breakpoint is cleared. When set to <code>false</code>
	 * a note is made that this thread has been interrupted.
	 * 
	 * @param running whether this thread is executing
	 */
	protected void setRunning(boolean running) {
		fRunning = running;
		if (running) {
			fCurrentBreakpoint = null;
		} else {
			interrupted();
		}
	}

	/**
	 * Preserves stack frames to be used on the next suspend event.
	 * Iterates through all current stack frames, setting their
	 * state as invalid. This is done when this thread is resumed,
	 * but we want to re-use stack frames when it later suspends.
	 */
	protected void preserveStackFrames() {
		if (fStackFrames != null) {
			fRefreshChildren = true;
			Iterator frames = fStackFrames.iterator();
			while (frames.hasNext()) {
				((JDIStackFrame)frames.next()).invalidateVariables();
			}
		}
	}

	/**
	 * Disposes stack frames, to be completely re-computed on
	 * the next suspend event.
	 */
	protected void disposeStackFrames() {
		setStackFrames(Collections.EMPTY_LIST);
		fRefreshChildren = true;
	}
	
	/**
	 * @see IStep
	 */
	public void stepInto() throws DebugException {
		if (!canStepInto()) {
			return;
		}
		StepHandler handler = new StepIntoHandler();
		handler.step();
	}

	/**
	 * @see IStep
	 */
	public void stepOver() throws DebugException {
		if (!canStepOver()) {
			return;
		}
		StepHandler handler = new StepOverHandler();
		handler.step();
	}

	/**
	 * @see IStep
	 */
	public void stepReturn() throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		StepHandler handler = new StepReturnHandler();
		handler.step();
	}
	
	/**
	 * @see ISuspendResume
	 */
	public void suspend() throws DebugException {
		try {
			// Abort any pending step request
			abortStep();
			fThread.suspend();
			setRunning(false);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
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
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_terminating_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
				return;
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
		StepHandler handler = new DropToFrameHandler(frame);
		handler.step();
	}

	protected void stepToFrame(IStackFrame frame) throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		StepHandler handler = new StepToFrameHanlder(frame);
		handler.step();
	}
		
	/**
	 * Aborts the current step, if any.
	 */
	protected void abortStep() {
		StepHandler handler = getPendingStepHandler();
		if (handler != null) {
			handler.abort();
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
	 * When a suspend event is fired, a thread keeps track of the
	 * cause of the suspend. If the suspend is due to a an event
	 * request in the underlying VM, evaluations may be performed,
	 * otherwise evaluations are disallowed.
	 * 
	 * @see JDIDebugElement#fireSuspendEvent(int)
	 */
	protected void fireSuspendEvent(int detail) {
		fEventSuspend = (detail != DebugEvent.CLIENT_REQUEST);
		super.fireSuspendEvent(detail);
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
			} catch (UnsupportedOperationException e) {
				return null;
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_group"), new String[] {e.toString()}), e); //$NON-NLS-1$
				return null;
			}
		}
		return fThreadGroup;
	}
	
	/**
	 * Returns this thread's timer.
	 */
	private Timer getTimer() {
		if (fTimer == null) {
			fTimer = new Timer();
		}
		return fTimer;
	}
	
	/**
	 * An action has timed out. Dispose stack frames.
	 */
	public void timeout() {
		System.out.println("timeout");
		disposeStackFrames();
		interrupted();
		fireChangeEvent();
	}
	
	/**
	 * Starts a timer. In the event of a timeout,
	 * stack frames are collapsed.
	 */
	protected void startCollapseTimer() {
		getTimer().start(this, 3000);
	}
	
	protected void stopCollapseTimer() {
		getTimer().stop();
	}
		 	
	/**
	 * Returns whether this thread is currently performing
	 * an evaluation.
	 * 
	 * @return whether this thread is currently performing
	 * 	an evaluation
	 */
	protected boolean isPerformingEvaluation() {
		return fInEvaluation;
	}
	
	/**
	 * Sets the step handler currently handling a step
	 * request.
	 * 
	 * @param handler the current step handler, or <code>null</code>
	 * 	if none
	 */
	protected void setPendingStepHandler(StepHandler handler) {
		fStepHandler = handler;
	}
	
	/**
	 * Returns the step handler currently handling a step
	 * request, or <code>null</code> if none.
	 * 
	 * @return step handler, or <code>null</code> if none
	 */
	protected StepHandler getPendingStepHandler() {
		return fStepHandler;
	}
	
	
	/**
	 * Helper class to perform stepping an a thread.
	 */
	abstract class StepHandler implements IJDIEventListener {
		/**
		 * Request for stepping in the underlying VM
		 */
		private StepRequest fStepRequest;
		
		/**
		 * Initiates a step in the underlying VM by creating a step
		 * request of the appropriate kind (over, into, return),
		 * and resuming this thread. When a step is initiated it
		 * is registered with its thread as a pending step. A pending
		 * step could be cancelled if a breakpoint suspends execution
		 * during the step.
		 * <p>
		 * This thread's state is set to running and stepping, and
		 * stack frames are invalidated (but preserved to be re-used
		 * when the step completes). A resume event with a step detail
		 * is fired for this thread. A step timer is started. If the timer
		 * expires, stack frames are cleared and not re-used on the
		 * eventual suspend.
		 * </p>
		 * 
		 * @exception DebugException if this method fails.  Reasons include:
		 * <ul>
		 * <li>Failure communicating with the VM.  The DebugException's
		 * status code contains the underlying exception responsible for
		 * the failure.</li>
		 * </ul>
		 */
		protected void step() throws DebugException {
			setStepRequest(createStepRequest());
			setPendingStepHandler(this);
			addJDIEventListener(this, getStepRequest());
			setRunning(true);
			preserveStackFrames();
			fireResumeEvent(DebugEvent.STEP_START);
			startCollapseTimer();
			invokeThread();
		}
		
		/**
		 * Resumes the underlying thread to initiate the step.
		 * By default the thread is resumed. Step handlers that
		 * require other actions can override this method.
		 * 
		 * @exception DebugException if this method fails.  Reasons include:
		 * <ul>
		 * <li>Failure communicating with the VM.  The DebugException's
		 * status code contains the underlying exception responsible for
		 * the failure.</li>
		 * </ul>
		 */
		protected void invokeThread() throws DebugException {
			try {
				getUnderlyingThread().resume();
			} catch (RuntimeException e) {
				stepEnd();
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_stepping"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
		}
		
		/**
		 * Creates and returns a step request specific to this step
		 * handler. Subclasses must override <code>getStepKind()</code>
		 * to return the kind of step it implements.
		 * 
		 * @return step request
		 * @exception DebugException if this method fails.  Reasons include:
		 * <ul>
		 * <li>Failure communicating with the VM.  The DebugException's
		 * status code contains the underlying exception responsible for
		 * the failure.</li>
		 * </ul>
		 */
		protected StepRequest createStepRequest() throws DebugException {
			try {
				StepRequest request = getEventRequestManager().createStepRequest(getUnderlyingThread(), StepRequest.STEP_LINE, getStepKind());
				request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
				request.addCountFilter(1);
				attachFiltersToStepRequest(request);
				request.enable();
				return request;
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_creating_step_request"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}			
			// this line will never be executed, as the try block
			// will either return, or the catch block with throw 
			// an exception
			return null;
		}
		
		/**
		 * Returns the kind of step this handler implements.
		 * 
		 * @return one of <code>StepRequest.STEP_INTO</code>,
		 * 	<code>StepRequest.STEP_OVER</code>, <code>StepRequest.STEP_OUT</code>
		 */
		protected abstract int getStepKind();
		
		/**
		 * Sets the step request created by this handler in
		 * the underlying VM. Set to <code>null<code> when
		 * this handler deletes its request.
		 * 
		 * @param request step request
		 */
		protected void setStepRequest(StepRequest request) {
			fStepRequest = request;
		}
		
		/**
		 * Returns the step request created by this handler in
		 * the underlying VM.
		 * 
		 * @return step request
		 */
		protected StepRequest getStepRequest() {
			return fStepRequest;
		}
		
		/**
		 * Deletes this handler's step request from the underlying VM
		 * and removes this handler as an event listener.
		 */
		protected void deleteStepRequest() {
			removeJDIEventListener(this, getStepRequest());
			try {
				getEventRequestManager().deleteEventRequest(getStepRequest());
				setStepRequest(null);
			} catch (RuntimeException e) {
				logError(e);
			}
		}
		
		/**
		 * If step filters are currently switched on, set all active filters on the step request.
		 */
		protected void attachFiltersToStepRequest(StepRequest request) {
			if (applyStepFilters() && JDIDebugModel.useStepFilters()) {
				List activeFilters = JDIDebugModel.getActiveStepFilters();
				Iterator iterator = activeFilters.iterator();
				while (iterator.hasNext()) {
					String filter = (String)iterator.next();
					request.addClassExclusionFilter(filter);
				}
			}
		}
		
		/**
		 * Returns whether this step handler should use step
		 * filters when creating its step request. By default,
		 * step filters are used. Subclasses must override 
		 * if/when required.
		 * 
		 * @return whether this step handler should use step
		 * filters when creating its step request
		 */
		protected boolean applyStepFilters() {
			return true;
		}
		
		/**
		 * Notification that the step has completed.
		 * 
		 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget)
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			stepEnd();
			return false;
		}
		
		/**
		 * Cleans up when a step completes.<ul>
		 * <li>Stops the step timer</code>
		 * <li>Thread state is set to suspended.</li>
		 * <li>Stepping state is set to false</li>
		 * <li>Stack frames and variables are incrementally updated</li>
		 * <li>The step request is deleted and removed as
		 * 		and event listener</li>
		 * <li>A suspend event is fired</li>
		 * </ul>
		 */
		protected void stepEnd() {
			stopCollapseTimer();
			setRunning(false);
			deleteStepRequest();
			setPendingStepHandler(null);
			fireSuspendEvent(DebugEvent.STEP_END);
		}
		
		/**
		 * Aborts this step request if active. The step timer is
		 * stopped, and the step event request is deleted
		 * from the underlying VM.
		 */
		protected void abort() {
			if (getStepRequest() != null) {
				stopCollapseTimer();
				deleteStepRequest();
				setPendingStepHandler(null);
			}
		}
		
	}
	
	/**
	 * Handler for step over requests.
	 */
	class StepOverHandler extends StepHandler {
		/**
		 * @see StepHandler#getStepKind()
		 */
		protected int getStepKind() {
			return StepRequest.STEP_OVER;
		}	
	}
	
	/**
	 * Handler for step into requests.
	 */
	class StepIntoHandler extends StepHandler {
		/**
		 * @see StepHandler#getStepKind()
		 */
		protected int getStepKind() {
			return StepRequest.STEP_INTO;
		}	
	}
	
	/**
	 * Handler for step return requests.
	 */
	class StepReturnHandler extends StepHandler {
		/**
		 * @see StepHandler#getStepKind()
		 */
		protected int getStepKind() {
			return StepRequest.STEP_OUT;
		}	
	}
	
	/**
	 * Handler for stepping to a specific stack frame
	 * (stepping in the non-top stack frame). Step returns
	 * are performed until a specified stack frame is reached
	 * or the thread is suspended (explicitly, or by a
	 * breakpoint).
	 */
	class StepToFrameHanlder extends StepReturnHandler {
		
		/**
		 * The number of frames that should be left on the stack
		 */
		private int fRemainingFrames;
		
		/**
		 * Constructs a step handler to step until the specified
		 * stack frame is reached.
		 * 
		 * @param frame the stack frame to step to
		 * @exception DebugException if this method fails.  Reasons include:
		 * <ul>
		 * <li>Failure communicating with the VM.  The DebugException's
		 * status code contains the underlying exception responsible for
		 * the failure.</li>
		 * </ul>
		 */
		protected StepToFrameHanlder(IStackFrame frame) throws DebugException {
			List frames = getStackFrames0();
			setRemainingFrames(frames.size() - getStackFrames0().indexOf(frame));
		}
		
		/**
		 * Sets the number of frames that should be
		 * remaining on the stack when done.
		 * 
		 * @param num number of remaining frames
		 */
		protected void setRemainingFrames(int num) {
			fRemainingFrames = num;
		}
		
		/**
		 * Returns number of frames that should be
		 * remaining on the stack when done
		 * 
		 * @return number of frames that should be left
		 */
		protected int getRemainingFrames() {
			return fRemainingFrames;
		}
		
		/**
		 * Notification the step request has completed.
		 * If in the desired frame, complete the step
		 * request nomally. If not in the desired frame,
		 * another step request is created and this thread
		 * is resumed.
		 * 
		 * @see IJDIDebugEventListener#handleEvent(Event, JDIDebugTarget)
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			try {
				int numFrames = getUnderlyingFrameCount();
				// tos should not be null
				if (numFrames <= getRemainingFrames()) {
					stepEnd();
					return false;
				} else {
					// reset running state and keep going
					setRunning(true);
					deleteStepRequest();
					createSecondaryStepRequest();
					return true;
				}
			} catch (DebugException e) {
				logError(e);
				stepEnd();
				return false;
			}
		}

		/**
		 * Creates another step request in the underlying of the
		 * appropriate kind (over, into, return). This thread will
		 * be resumed by the event dispatcher as this event handler
		 * will vote to resume suspended threads. When a step is
		 * initiated it is registered with its thread as a pending
		 * step. A pending step could be cancelled if a breakpoint
		 * suspends execution during the step.
		 * 
		 * @exception DebugException if this method fails.  Reasons include:
		 * <ul>
		 * <li>Failure communicating with the VM.  The DebugException's
		 * status code contains the underlying exception responsible for
		 * the failure.</li>
		 * </ul>
		 */
		protected void createSecondaryStepRequest() throws DebugException {
			setStepRequest(createStepRequest());
			setPendingStepHandler(this);
			addJDIEventListener(this, getStepRequest());
		}	
		
		/**
		 * Returns <code>false</code>. To step to a particular frame,
		 * step filters are not used.
		 * 
		 * @see StepHandler#applyStepFilters()
		 */
		protected boolean applyStepFilters() {
			return false;
		}
				
	}
	
	/**
	 * Handles dropping to a specified frame.
	 */
	class DropToFrameHandler extends StepReturnHandler {
		
		/**
		 * The number of frames to drop off the
		 * stack.
		 */
		private int fFramesToDrop;
		
		/**
		 * Constructs a handler to drop to the the specified
		 * stack frame.
		 * 
		 * @param frame the stack frame to drop to
		 * @exception DebugException if this method fails.  Reasons include:
		 * <ul>
		 * <li>Failure communicating with the VM.  The DebugException's
		 * status code contains the underlying exception responsible for
		 * the failure.</li>
		 * </ul>
		 */		
		protected DropToFrameHandler(IStackFrame frame) throws DebugException {
			List frames = getStackFrames0();
			setFramesToDrop(frames.indexOf(frame));
		}
		
		/**
		 * Sets the number of frames to pop off the stack.
		 * 
		 * @param num number of frames to pop
		 */
		protected void setFramesToDrop(int num) {
			fFramesToDrop = num;
		}
		
		/**
		 * Returns the number of frames to pop off the stack.
		 * 
		 * @return remaining number of frames to pop
		 */
		protected int getFramesToDrop() {
			return fFramesToDrop;
		}		
		
		/**
		 * To drop a frame or re-enter, the underlying thread is instructed
		 * to do a return. When the frame count is less than zero, the
		 * step being performed is a "step return", so a regular invocation
		 * is performed. 
		 * 
		 * @see StepHandler#invokeThread()
		 */
		protected void invokeThread() throws DebugException {
			if (getFramesToDrop() < 0) {
				super.invokeThread();
			} else {
				try {
					org.eclipse.jdi.hcr.ThreadReference hcrThread= (org.eclipse.jdi.hcr.ThreadReference) getUnderlyingThread();
					hcrThread.doReturn(null, true);
				} catch (RuntimeException e) {
					stepEnd();
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("{0} occurred while popping stack frame."), new String[] {e.toString()}), e);
				}
			}
		}
		
		/**
		 * Notification that the pop has completed. If there are
		 * more frames to pop, keep going, otherwise re-enter the 
		 * top frame. Returns false, as this handler will resume this
		 * thread with a special invocation (<code>doReturn</code>).
		 * 
		 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget)
		 * @see #invokeThread()
		 */		
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			// pop is complete, update number of frames to drop
			setFramesToDrop(getFramesToDrop() - 1);
			try {
				if (getFramesToDrop() >= -1) {
					deleteStepRequest();
					doSecondaryStep();
				} else {
					stepEnd();
				}
			} catch (DebugException e) {
				stepEnd();
				logError(e);
			}
			return false;
		}
		
		/**
		 * Pops a secondary frame off the stack, does a re-enter,
		 * or a step-into.
		 * 
		 * @exception DebugException if this method fails.  Reasons include:
		 * <ul>
		 * <li>Failure communicating with the VM.  The DebugException's
		 * status code contains the underlying exception responsible for
		 * the failure.</li>
		 * </ul>
		 */
		protected void doSecondaryStep() throws DebugException {
			setStepRequest(createStepRequest());
			setPendingStepHandler(this);
			addJDIEventListener(this, getStepRequest());
			invokeThread();
		}		

		/**
		 * Creates and returns a step request. If there
		 * are no more frames to drop, a re-enter request
		 * is made. If the re-enter is complete, a step-into
		 * request is created.
		 * 
		 * @return step request
		 * @exception DebugException if this method fails.  Reasons include:
		 * <ul>
		 * <li>Failure communicating with the VM.  The DebugException's
		 * status code contains the underlying exception responsible for
		 * the failure.</li>
		 * </ul>
		 */
		protected StepRequest createStepRequest() throws DebugException {
			int num = getFramesToDrop();
			if (num > 0) {
				return super.createStepRequest();
			} else if (num == 0) {
				try {
					EventRequestManager erm= getEventRequestManager();
					StepRequest request = ((org.eclipse.jdi.hcr.EventRequestManager) erm).createReenterStepRequest(getUnderlyingThread());
					request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					request.addCountFilter(1);
					request.enable();
					return request;
				} catch (RuntimeException e) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_creating_step_request"), new String[] {e.toString()}), e); //$NON-NLS-1$
				}			
			} else if (num == -1) {
				try {
					StepRequest request = getEventRequestManager().createStepRequest(getUnderlyingThread(), StepRequest.STEP_LINE, StepRequest.STEP_INTO);
					request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					request.addCountFilter(1);
					request.enable();
					return request;
				} catch (RuntimeException e) {
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_creating_step_request"), new String[] {e.toString()}), e); //$NON-NLS-1$
				}					
			}
			// this line will never be executed, as the try block
			// will either return, or the catch block with throw 
			// an exception
			return null;
		}
	}
}