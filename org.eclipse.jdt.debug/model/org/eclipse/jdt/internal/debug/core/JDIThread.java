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
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ISuspendResume;
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
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
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
 * Model thread implementation for an underlying
 * thread on a VM.
 */
public class JDIThread extends JDIDebugElement implements IJavaThread, ITimeoutListener {
	
	/**
	 * Constant for the name of the main thread group.
	 */
	private static final String MAIN_THREAD_GROUP = "main"; //$NON-NLS-1$
	
	/**
	 * Number of milliseconds used as a timeout before
	 * 'collapsing' stack frames when performing a step
	 * or method invocation.
	 */
	private static final int COLLAPSE_TIMEOUT = 3000;

	/**
	 * Underlying thread.
	 */
	private ThreadReference fThread;
	
	/**
	 * Collection of stack frames
	 */
	private List fStackFrames;

	/**
	 * Underlying thread group, cached on first access.
	 */
	private ThreadGroupReference fThreadGroup;
	
	/**
	 * Name of underlying thread group, cached on first access.
	 */
	private String fThreadGroupName;
	
	/**
	 * Whether children need to be refreshed. Set to
	 * <code>true</code> when stack frames are re-used
	 * on the next suspend.
	 */
	private boolean fRefreshChildren = true;
	
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
	 * <code>true</code> when suspended by an event in the
	 * VM such as a breakpoint or step, and <code>false</code>
	 * when suspended by an explicit user request (i.e. when
	 * a call is made to <code>#suspend()</code>).
	 */
	private boolean fEventSuspend = false;

	/**
	 * Action timer. During a long running action,
	 * stack frames are collapsed.
	 */
	private Timer fTimer;

	/**
	 * Whether terminated.
	 */
	private boolean fTerminated;
	/**
	 * Whether this thread is a system thread.
	 */
	private boolean fIsSystemThread;

	/**
	 * The breakpoint that caused the last suspend, or
	 * <code>null</code> if none.
	 */
	private IJavaBreakpoint fCurrentBreakpoint;

	/**
	 * The cached named of the underlying thread.
	 */
	private String fName= null;
	
	/**
	 * Whether this thread is currently performing
	 * an evaluation (invoke method). Nested method
	 * invocations cannot be performed.
	 */
	private boolean fInEvaluation = false;
	
	/**
	 * When performing an evaluation, a client
	 * may decide to abort the evaluation, in which
	 * case this flag gets set to <code>true</code>.
	 * When an evaluation is aborted, before it completes,
	 * the evaluation thread is eventually (automatically)
	 * resumed when the evaluation completes. When 
	 * an evaluation is not aborted, the evaluation thread
	 * remains suspended when on completion of the
	 * evaluation.
	 */
	private boolean fEvaluationAborted = false;
	
	/**
	 * Whether this thread has been interrupted during
	 * the last evaluation. That is, was a breakpoint
	 * hit, did the user suspend the thread, or did
	 * the evaluation timeout.
	 */
	private boolean fInterrupted = false;
	
	/**
	 * The kind of step that was originally requested.  Zero or
	 * more 'secondary steps' may be performed programmatically after
	 * the original user-requested step, and this field tracks the
	 * type (step into, over, return) of the original step.
	 */
	private int fOriginalStepKind;

	/**
	 * The JDI Location from which an original user-requested step began.
	 */
	private Location fOriginalStepLocation;

	/**
	 * The total stack depth at the time an original (user-requested) step
	 * is initiated.  This is used along with the original step Location
	 * to determine if a step into comes back to the starting location and
	 * needs to be 'nudged' forward.  Checking the stack depth eliminates 
	 * undesired 'nudging' in recursive methods.
	 */
	private int fOriginalStepStackDepth;

	/**
	 * Creates a new thread on the underlying thread reference
	 * in the given debug target.
	 * 
	 * @param target the debug target in which this thread is contained
	 * @param thread the underlying thread on the VM
	 */
	public JDIThread(JDIDebugTarget target, ThreadReference thread) {
		super(target);
		setUnderlyingThread(thread);
		initialize();
	}

	/**
	 * Thread initialization:<ul>
	 * <li>Determines if this thread is a system thread</li>
	 * <li>Sets terminated state to <code>false</code></li>
	 * <li>Determines suspended state from underlying thread</li> 
	 * <li>Sets this threads stack frames to an empty collection</li>
	 * </ul>
	 */
	protected void initialize() {
		fStackFrames= Collections.EMPTY_LIST;
		// system thread
		try {
			determineIfSystemThread();
		} catch (DebugException e) {
			Throwable underlyingException= e.getStatus().getException();
			if (underlyingException instanceof VMDisconnectedException) {
				// Threads may be created by the VM at shutdown
				// as finalizers. The VM may be disconnected by
				// the time we hear about the thread creation.
				disconnected();
				return;
			}			
			logError(e);
		}

		// state
		setTerminated(false);
		setRunning(false);
		try {
			setRunning(!getUnderlyingThread().isSuspended());
		} catch (VMDisconnectedException e) {
			disconnected();
			return;
		} catch (RuntimeException e) {
			logError(e);
		}
	}
	
	/**
	 * @see IDebugElement#getElementType()
	 */
	public int getElementType() {
		return THREAD;
	}

	/**
	 * @see IThread#getBreakpoint()
	 */
	public IBreakpoint getBreakpoint() {
		if (fCurrentBreakpoint != null && !fCurrentBreakpoint.getMarker().exists()) {
			fCurrentBreakpoint= null;
		}
		return fCurrentBreakpoint;
	}

	/**
	 * @see ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return isSuspended();
	}

	/**
	 * @see ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !isSuspended();
	}

	/**
	 * @see ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		ObjectReference threadDeath= ((JDIDebugTarget) getDebugTarget()).getThreadDeathInstance();
		return threadDeath != null && !isSystemThread() && !isTerminated();
	}

	/**
	 * @see IStep@canStepInto()
	 */
	public boolean canStepInto() {
		return isSuspended() && !isStepping();
	}

	/**
	 * @see IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return isSuspended() && !isStepping();
	}

	/**
	 * @see IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return isSuspended() && !isStepping();
	}

	/**
	 * Determines and sets whether this thread represents a system thread.
	 * 
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
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
				// execution will not reach this line, as
				// #targetRequestFailed will throw an exception				
				return;
			}
			if (tgn != null && tgn.equals(MAIN_THREAD_GROUP)) {
				fIsSystemThread= false;
				break;
			}
		}
	}

	/**
	 * NOTE: this method returns a copy of this thread's stack frames.
	 * 
	 * @see IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException {
		List list = computeStackFrames();
		return (IStackFrame[])list.toArray(new IStackFrame[list.size()]);
	}
	
	/**
	 * Returns this thread's current stack frames as a list, computing
	 * them if required. Returns an empty collection if this thread is
	 * not currently suspended, or this thread is terminated. This
	 * method should be used internally to get the current stack frames,
	 * instead of calling <code>#getStackFrames()</code>, which makes a
	 * copy of the current list.
	 * <p>
	 * Before a thread is resumed a call must be made to one of:<ul>
	 * <li><code>preserveStackFrames()</code></li>
	 * <li><code>disposeStackFrames()</code></li>
	 * </ul>
	 * If stack frames are disposed before a thread is resumed, stack frames
	 * are completely re-computed on the next call to this method. If stack
	 * frames are to be preserved, this method will attempt to re-use any stack
	 * frame objects which represent the same stack frame as on the previous
	 * suspend. Stack frames are cached until a subsequent call to preserve
	 * or dispose stack frames.
	 * </p>
	 * 
	 * @return list of <code>IJavaStackFrame</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */	
	protected synchronized List computeStackFrames() throws DebugException {
		if (isSuspended()) {
			if (isTerminated()) {
				fStackFrames = Collections.EMPTY_LIST;
			} else if (fRefreshChildren) {
				if (fStackFrames.isEmpty()) {
					fStackFrames = createAllStackFrames();
					if (fStackFrames.isEmpty()) {	
						//leave fRefreshChildren == true
						//bug 6393
						return fStackFrames;
					}
				} 
				List frames= getUnderlyingFrames();
				if (JDIDebugModel.useStepFilters() && !JDIDebugModel.getActiveStepFilters().isEmpty()) {
					// If step filters are active, stepping can invalidate cached stack frame
					// data. If any of the cached stack frame are out of synch with the underlying
					// frames, discard the cached frames.
					int numModelFrames= fStackFrames.size();
					int numUnderlyingFrames= frames.size();
					
					int modelFramesIndex = 0;
					int underlyingFramesIndex = 0;
					int numFramesToCompare = 0;
					if (numModelFrames > numUnderlyingFrames) {
						modelFramesIndex = numModelFrames - numUnderlyingFrames;
						numFramesToCompare = numUnderlyingFrames;
					} else {
						underlyingFramesIndex = numUnderlyingFrames - numModelFrames;
						numFramesToCompare = numModelFrames;
					}
					
					StackFrame underlyingFrame= null;
					JDIStackFrame modelFrame= null;
					for ( ; modelFramesIndex < numModelFrames; modelFramesIndex++, underlyingFramesIndex++) {
						underlyingFrame= (StackFrame) frames.get(underlyingFramesIndex);
						modelFrame= (JDIStackFrame) fStackFrames.get(modelFramesIndex);
						if (!underlyingFrame.equals(modelFrame.getLastUnderlyingStackFrame())) {
							// Replace the out of synch frame
							fStackFrames.set(modelFramesIndex, new JDIStackFrame(this, underlyingFrame));
						}
					}
				}
				// compute new or removed stack frames
				int offset= 0, length= frames.size();
				if (length > fStackFrames.size()) {
					// compute new frames
					offset= length - fStackFrames.size();
					for (int i= offset - 1; i >= 0; i--) {
						JDIStackFrame newStackFrame= new JDIStackFrame(this, (StackFrame) frames.get(i));
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
						} else {
							// same number of stack frames - if the TOS is different, remove/replace all stack frames
							Method oldMethod= ((JDIStackFrame) fStackFrames.get(0)).getUnderlyingMethod();
							StackFrame newTOS= (StackFrame) frames.get(0);
							Method newMethod= getUnderlyingMethod(newTOS);
							if (!oldMethod.equals(newMethod)) {
								// replace top stack frame
								fStackFrames.set(0, new JDIStackFrame(this, newTOS));
								// no stack frames to update
								offset= fStackFrames.size();
							}
						}
					}
				// update preserved frames
				if (offset < fStackFrames.size()) {
					updateStackFrames(frames, offset, fStackFrames, length);
				}
			}
			fRefreshChildren = false;
		} else {
			return Collections.EMPTY_LIST;
		}
		return fStackFrames;
	}
	

	/**
	 * Helper method for <code>#computeStackFrames()</code> to create all
	 * underlying stack frames.
	 * 
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected List createAllStackFrames() throws DebugException {
		List frames= getUnderlyingFrames();
		List list= new ArrayList(frames.size());
		Iterator iter= frames.iterator();
		while (iter.hasNext()) {
			JDIStackFrame newStackFrame= new JDIStackFrame(this, (StackFrame) iter.next());
			list.add(newStackFrame);
		}
		return list;
	}

	/**
	 * Retrieves and returns all underlying stack frames
	 * 
	 * @return list of <code>StackFrame</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected List getUnderlyingFrames() throws DebugException {
		try {
			return getUnderlyingThread().frames();
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_stack_frames"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_stack_frames_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;
		}
	}

	/**
	 * Returns the underlying method for the given stack frame
	 * 
	 * @param frame an underlying JDI stack frame
	 * @return underlying method
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected Method getUnderlyingMethod(StackFrame frame) throws DebugException {
		try {
			return frame.location().method();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_method"), new String[] {e.toString()}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will thrown an exception			
			return null;			
		}
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
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_frame_count"), new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_frame_count"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		// execution will not reach here - try block will either
		// return or exception will be thrown
		return -1;
	}

	/**
	 * Invokes a method on the target, in this thread, and returns the result. Only
	 * one receiver may be specified - either a class or an object, the other must
	 * be <code>null</code>. This thread is left suspended after the invocation
	 * is complete, unless a call is made to <code>abortEvaluation<code> while
	 * performing a method invocation. In that case, this thread is automatically
	 * resumed when/if this invocation (eventually) completes.
	 * <p>
	 * Method invocations cannot be nested. That is, this method must
	 * return before another call to this method can be made. This
	 * method does not return until the invocation is complete.
	 * Breakpoints can suspend a method invocation, and it is possible
	 * that an invocation will not complete due to an infinite loop
	 * or deadlock.
	 * </p>
	 * <p>
	 * Stack frames are preserved during method invocations, unless
	 * a timeout occurs. Although this thread's state is updated to
	 * running while performing an evaluation, no debug events are
	 * fired unless this invocation is interrupted by a breakpoint,
	 * or the invocation times out.
	 * </p>
	 * <p>
	 * When performing an invocation, the communication timeout with
	 * the target VM is set to infinite, as the invocation may not 
	 * complete in a timely fashion, if at all. The timeout value
	 * is reset to its original value when the invocation completes.
	 * </p>
	 * 
	 * @param receiverClass the class in the target representing the receiver
	 * 	of a static message send, or <code>null</code>
	 * @param receiverObject the object in the target to be the receiver of
	 * 	the message send, or <code>null</code>
	 * @param method the underlying method to be invoked
	 * @param args the arguments to invoke the method with (an empty list
	 *  if none) 
	 * @return the result of the method, as an underlying value
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected Value invokeMethod(ClassType receiverClass, ObjectReference receiverObject, Method method, List args) throws DebugException {
		if (receiverClass != null && receiverObject != null) {
			throw new IllegalArgumentException(JDIDebugModelMessages.getString("JDIThread.can_only_specify_one_receiver_for_a_method_invocation")); //$NON-NLS-1$
		}
		if (isPerformingEvaluation()) {
			requestFailed(JDIDebugModelMessages.getString("JDIThread.Cannot_perform_nested_evaluations"), null); //$NON-NLS-1$
		}
		Value result= null;
		int timeout= getRequestTimeout();
		try {
			// set the request timeout to be infinite
			setRequestTimeout(Integer.MAX_VALUE);
			resetAbortEvaluation();
			setRunning(true);
			setPerformingEvaluation(true);
			preserveStackFrames();
			resetInterrupted();
			startCollapseTimer();
			if (receiverClass == null) {
				result= receiverObject.invokeMethod(getUnderlyingThread(), method, args, ClassType.INVOKE_SINGLE_THREADED);
			} else {
				result= receiverClass.invokeMethod(getUnderlyingThread(), method, args, ClassType.INVOKE_SINGLE_THREADED);
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
		if (wasEvaluationAborted()) {
			resume();
		}
		return result;
	}
	
	/**
	 * Called when this thread suspends. If this thread is performing
	 * a method invocation, a not is made that the invocation was
	 * interrupted. When an invocation is interrupted, a suspend
	 * event must be fired when/if the invocation eventually completes.
	 * 
	 * @see #invokeMethod(ClassType, ObjectReference, Method, List)
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
	 * Causes this thread to be automatically resumed when
	 * it returns from its current invocation, if any.
	 * <p>
	 * For example, this is called by <code>JDIValue</code>
	 * when a 'to string' evaluation times out. If the invocation
	 * ever completes, the thread will not suspend when/if the
	 * 'to string' evaluation completes.
	 * </p>
	 * 
	 * @see #invokeMethod(ClassType, ObjectReference, Method, List)
	 */
	protected void abortEvaluation() {
		fEvaluationAborted = true;
	}
	
	/**
	 * Resets the abort flag to <code>false</code> before
	 * an evaluation.
	 */
	protected void resetAbortEvaluation() {
		fEvaluationAborted = false;
	}
	
	/**
	 * Returns whether a client asked to abort an evaluation
	 * while performing a method invocation.
	 * 
	 * @see #invokeMethod(ClassType, ObjectReference, Method, List)
	 */
	protected boolean wasEvaluationAborted() {
		return fEvaluationAborted;
	}
	
	/**
	 * Invokes a constructor in this thread, creating a new instance of the given
	 * class, and returns the result as an object reference.
	 * This thread is left suspended after the invocation
	 * is complete.
	 * <p>
	 * Method invocations cannot be nested. That is, this method must
	 * return before another call to this method can be made. This
	 * method does not return until the invocation is complete.
	 * Breakpoints can suspend a method invocation, and it is possible
	 * that an invocation will not complete due to an infinite loop
	 * or deadlock.
	 * </p>
	 * <p>
	 * Stack frames are preserved during method invocations, unless
	 * a timeout occurs. Although this thread's state is updated to
	 * running while performing an evaluation, no debug events are
	 * fired unless this invocation is interrupted by a breakpoint,
	 * or the invocation times out.
	 * </p>
	 * <p>
	 * When performing an invocation, the communication timeout with
	 * the target VM is set to infinite, as the invocation may not 
	 * complete in a timely fashion, if at all. The timeout value
	 * is reset to its original value when the invocation completes.
	 * </p>
	 * 
	 * @param receiverClass the class in the target representing the receiver
	 * 	of the 'new' message send
	 * @param constructor the underlying constructor to be invoked
	 * @param args the arguments to invoke the constructor with (an empty list
	 *  if none) 
	 * @return a new object reference
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
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
			setPerformingEvaluation(true);
			preserveStackFrames();
			resetInterrupted();
			startCollapseTimer();
			result= receiverClass.newInstance(getUnderlyingThread(), constructor, args, ClassType.INVOKE_SINGLE_THREADED);
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
	 * Called when an invocation fails. Performs cleanup
	 * and throws an exception.
	 * 
	 * @param e the exception that caused the failure
	 * @param restoreTimeout the communication timeout value,
	 * 	in milliseconds, that should be reset
	 * @see #invokeComplete(int)
	 * @exception DebugException.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected void invokeFailed(Throwable e, int restoreTimeout) throws DebugException {
		invokeComplete(restoreTimeout);
		targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_invoking_method"), new String[] {e.toString()}), e); //$NON-NLS-1$
	}
	
	/**
	 * Called when a method invocation has returned, successfully
	 * or not. This method performs cleanup:<ul>
	 * <li>Stops the frame collapse timer</li>
	 * <li>Resets the state of this thread to suspended</li>
	 * <li>Restores the communication timeout value</li>
	 * <li>Computes the new set of stack frames for this thread</code>
	 * <li>Fires a suspend event, iff the invocation was interrupted</code>
	 * </ul>
	 * 
	 * @param restoreTimeout the communication timeout value,
	 * 	in milliseconds, that should be reset
	 * @see #invokeMethod(ClassType, ObjectReference, Method, List)
 	 * @see #newInstance(ClassType, Method, List)
	 */
	protected void invokeComplete(int restoreTimeout) {
		stopCollapseTimer();
		boolean interrupted= wasInterrupted();
		setRunning(false);
		setPerformingEvaluation(false);
		setRequestTimeout(restoreTimeout);
		// update preserved stack frames
		try {
			computeStackFrames();
		} catch (DebugException e) {
			logError(e);
		}
		if (interrupted) {
			fireSuspendEvent(-1);
		} else {
			// fire a change event for the top stack frame
			// to update any variables that may have changed
			// value
			try {
				((JDIStackFrame)getTopStackFrame()).fireChangeEvent();
			} catch (DebugException e) {
				logError(e);
			}
		}
	}
	
	/**
	 * Sets the timeout interval for jdi requests in milliseconds
	 * 
	 * @param timeout the communication timeout, in milliseconds
	 */
	protected void setRequestTimeout(int timeout) {
		VirtualMachine vm = getVM();
		if (vm instanceof org.eclipse.jdi.VirtualMachine) {
			((org.eclipse.jdi.VirtualMachine) vm).setRequestTimeout(timeout);
		}
	}
	
	/**
	 * Returns the timeout interval for JDI requests in milliseconds,
	 * or -1 if not supported
	 * 
	 * @return timeout value, in milliseconds, or -1 if not supported
	 */
	protected int getRequestTimeout() {
		VirtualMachine vm = getVM();
		if (vm instanceof org.eclipse.jdi.VirtualMachine) {
			return ((org.eclipse.jdi.VirtualMachine) vm).getRequestTimeout();
		}
		return -1;
	}
	
	/**
	 * @see IThread#getName()
	 */
	public String getName() throws DebugException {
		if (fName == null) {
			try {
				fName = getUnderlyingThread().name();
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not fall through, as
				// #targetRequestFailed will thrown an exception
			}
		}
		return fName;
	}

	/**
	 * @see IThread#getPriority
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
		}
		// execution will not fall through to this line, as
		// #targetRequestFailed or #requestFailed will throw
		// an exception		
		return -1;
	}

	/**
	 * @see IThread#getTopStackFrame()
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		List c= computeStackFrames();
		if (c.isEmpty()) {
			return null;
		} else {
			return (IStackFrame) c.get(0);
		}
	}

	/**
	 * A breakpoint has suspended execution of this thread.
	 * Aborts any step currently in process and fires a
	 * suspend event.
	 * 
	 * @param breakpoint the breakpoint that caused the suspend
	 */
	protected void handleSuspendForBreakpoint(JavaBreakpoint breakpoint) {
		abortStep();
		fCurrentBreakpoint= breakpoint;
		setRunning(false);
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}

	/**
	 * @see IStep#isStepping()
	 */
	public boolean isStepping() {
		return getPendingStepHandler() != null;
	}

	/**
	 * @see ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return !fRunning && !fTerminated;
	}

	/**
	 * @see IJavaThread#isSystemThread()
	 */
	public boolean isSystemThread() {
		return fIsSystemThread;
	}

	/**
	 * @see IJavaThread#getThreadGroupName()
	 */
	public String getThreadGroupName() throws DebugException {
		if (fThreadGroupName == null) {
			ThreadGroupReference tgr= getUnderlyingThreadGroup();
			try {
				fThreadGroupName = tgr.name();
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_group_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as
				// #targetRequestFailed will thrown an exception
				return null;
			}
		}
		return fThreadGroupName;
	}

	/**
	 * @see ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}
	
	public boolean isOutOfSynch() throws DebugException {
		if (isSuspended()) {
			List frames= computeStackFrames();
			Iterator iter= frames.iterator();
			while (iter.hasNext()) {
				if (((JDIStackFrame) iter.next()).isOutOfSynch()) {
					return true;
				}
			}
			return false;
		} else {
			// If the thread is not suspended, there's no way to 
			// say for certain that it is running out of synch code
			return false;
		}
	}
	
	public boolean mayBeOutOfSynch() throws DebugException {
		if (!isSuspended()) {
			return ((JDIDebugTarget)getDebugTarget()).hasHCRFailed();
		}
		return false;
	}	
	
	/**
	 * Sets whether this thread is terminated
	 * 
	 * @param terminated whether this thread is terminated
	 */
	protected void setTerminated(boolean terminated) {
		fTerminated= terminated;
	}

	/**
	 * @see ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		if (!isSuspended()) {
			return;
		}
		try {
			setRunning(true);
			disposeStackFrames();
			fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			getUnderlyingThread().resume();
		} catch (VMDisconnectedException e) {
			disconnected();
		} catch (RuntimeException e) {
			setRunning(false);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_resuming"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}
		
	/**
	 * Sets whether this thread is currently executing.
	 * When set to <code>true</code>, this thread's current
	 * breakpoint is cleared. When set to <code>false</code>,
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
	 * state as invalid. This method should be called before this thread
	 * is resumed, when stack frames are to be re-used when it later
	 * suspends.
	 * 
	 * @see computeStackFrames()
	 */
	protected void preserveStackFrames() {
		fRefreshChildren = true;
		Iterator frames = fStackFrames.iterator();
		while (frames.hasNext()) {
			((JDIStackFrame)frames.next()).setUnderlyingStackFrame(null);
		}
	}

	/**
	 * Disposes stack frames, to be completely re-computed on
	 * the next suspend event. This method should be called before
	 * this thread is resumed when stack frames are not to be re-used
	 * on the next suspend.
	 * 
	 * @see computeStackFrames()
	 */
	protected void disposeStackFrames() {
		fStackFrames= Collections.EMPTY_LIST;
		fRefreshChildren = true;
	}
	
	/**
	 * @see IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		if (!canStepInto()) {
			return;
		}
		StepHandler handler = new StepIntoHandler();
		handler.step();
	}

	/**
	 * @see IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		if (!canStepOver()) {
			return;
		}
		StepHandler handler = new StepOverHandler();
		handler.step();
	}

	/**
	 * @see IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		if (!canStepReturn()) {
			return;
		}
		StepHandler handler = new StepReturnHandler();
		handler.step();
	}
	
	protected void setOriginalStepKind(int stepKind) {
		fOriginalStepKind = stepKind;
	}
	
	protected int getOriginalStepKind() {
		return fOriginalStepKind;
	}
	
	protected void setOriginalStepLocation(Location location) {
		fOriginalStepLocation = location;
	}
	
	protected Location getOriginalStepLocation() {
		return fOriginalStepLocation;
	}
	
	protected void setOriginalStepStackDepth(int depth) {
		fOriginalStepStackDepth = depth;
	}
	
	protected int getOriginalStepStackDepth() {
		return fOriginalStepStackDepth;
	}
	
	/**
	 * In cases where a user-requested step into encounters nothing but filtered code
	 * (static initializers, synthetic methods, etc.), the default JDI behavior is to
	 * put the instruction pointer back where it was before the step into.  This requires
	 * a second step to move forward.  Since this is confusing to the user, we do an 
	 * extra step into in such situations.  This method determines when such an extra 
	 * step into is necessary.  It compares the current Location to the original
	 * Location when the user step into was initiated.  It also makes sure the stack depth
	 * now is the same as when the step was initiated.
	 */
	protected boolean shouldDoExtraStepInto(Location location) throws DebugException {
		if (getOriginalStepKind() != StepRequest.STEP_INTO) {
			return false;
		}
		if (getOriginalStepStackDepth() != getUnderlyingFrames().size()) {
			return false;
		}
		Location origLocation = getOriginalStepLocation();
		if (origLocation == null) {
			return false;
		}	
		// We cannot simply check if the two Locations are equal using the equals()
		// method, since this checks the code index within the method.  Even if the
		// code indices are different, the line numbers may be the same, in which case
		// we need to do the extra step into.
		Method origMethod = origLocation.method();
		Method currMethod = location.method();
		if (!origMethod.equals(currMethod)) {
			return false;
		}	
		if (origLocation.lineNumber() != location.lineNumber()) {
			return false;
		}			
		return true;
	}
	
	/**
	 * @see ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		try {
			// Abort any pending step request
			abortStep();
			getUnderlyingThread().suspend();
			setRunning(false);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
		} catch (RuntimeException e) {
			setRunning(true);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_suspending"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}

	/**
	 * @see ITerminate#terminate()
	 */
	public void terminate() throws DebugException {

		ObjectReference threadDeath= ((JDIDebugTarget) getDebugTarget()).getThreadDeathInstance();
		if (threadDeath != null) {
			try {
				getUnderlyingThread().stop(threadDeath);
			} catch (InvalidTypeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_terminating"), new String[] {e.toString()}), e); //$NON-NLS-1$
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_terminating_2"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as
				// #targetRequestFailed will thrown an exception							
				return;
			}
			// Resume the thread so that stop will work
			resume();
		} else {
			requestFailed(JDIDebugModelMessages.getString("JDIThread.unable_to_terminate"), null); //$NON-NLS-1$
		}

	}

	/**
	 * Replaces the underlying stack frame objects in the preserved frames
	 * list with the current underlying stack frames.
	 * 
	 * @param newFrames list of current underlying <code>StackFrame</code>s.
	 * 	Frames from this list are assigned to the underlying frames in
	 *  the <code>oldFrames</code> list.
	 * @param offset the offset in the lists at which to start replacing
	 *  the old underlying frames
	 * @param oldFrames list of preserved frames, of type <code>JDIStackFrame</code>
	 * @param length the number of frames to replace
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
	 * 
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected void dropToFrame(IStackFrame frame) throws DebugException {
		VirtualMachine vm= getVM();
		if (vm.canPopFrames()) {
			// JDK 1.4 support
			List frames = computeStackFrames();
			Object lastFrame = frames.get(frames.size() - 1);
			boolean last = frame.equals(lastFrame);
			try {
				// Pop the drop frame and all frames above it
				StackFrame jdiFrame = ((JDIStackFrame) frame).getUnderlyingStackFrame();
				preserveStackFrames();
				fThread.popFrames(jdiFrame);
				computeStackFrames();
				if (last) {
					fireSuspendEvent(DebugEvent.STEP_END);
				} else {
					stepInto();	
				}
			} catch (IncompatibleThreadStateException exception) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_dropping_to_frame"), new String[] {exception.toString()}),exception); //$NON-NLS-1$
			} catch (RuntimeException exception) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_dropping_to_frame"), new String[] {exception.toString()}),exception); //$NON-NLS-1$
			}
		} else {
			// J9 support
			StepHandler handler = new DropToFrameHandler(frame);
			handler.step();
		}
	}
	
	/**
	 * Steps until the specified stack frame is the top frame. Provides
	 * ability to step over/return in the non-top stack frame.
	 * 
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
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
	 * @see IJavaThread#findVariable(String)
	 */
	public IVariable findVariable(String varName) throws DebugException {
		if (isSuspended()) {
			Iterator stackFrames= computeStackFrames().iterator();
			while (stackFrames.hasNext()) {
				JDIStackFrame sf= (JDIStackFrame)stackFrames.next();
				IVariable var= sf.findVariable(varName);
				if (var != null) {
					return var;
				}
			}
		}
		return null;
	}

	/**
	 * @see IJavaEvaluation#evaluate(String, IJavaEvaluationListener, IJavaProject)
	 */
	public void evaluate(String snippet, IJavaEvaluationListener listener, IJavaProject project) throws DebugException {
		IEvaluationContext underlyingContext = ((JDIDebugTarget)getDebugTarget()).getEvaluationContext(project);
		evaluate(snippet, listener, underlyingContext);
	}
	
	/**
	 * @see IJavaEvaluation#evaluate(String, IJavaEvaluationListener, IEvaluationContext)
	 */
	public void evaluate(String snippet, IJavaEvaluationListener listener, IEvaluationContext evaluationContext) throws DebugException {
		verifyEvaluation(evaluationContext);
		ThreadEvaluationContext context = new ThreadEvaluationContext(this, evaluationContext);
		context.evaluate(snippet, listener);
	}
	
	/**
	 * Verifies this thread is in a valid state to perform an evaluation,
	 * and throws an exception if not.
	 * 
	 * @exception DebugException if not in a valid state for an evaluation. 
	 *  Reasons include:
	 * <ul>
	 * <li>This thread is already performing an evaluation</li>
	 * <li>The evaluation project context does not have a built state</li>
	 * <li>This thread was not suspended by a breakpoint or step request.
	 * 	When a thread has been suspended by an explicit call to <code>suspend</code>,
	 *  it is not in a state that allows evaluations to be performed.</li>
	 * </ul>
	 */
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
	 * @see IJavaEvaluate#canPerformEvaluation()
	 */
	public boolean canPerformEvaluation() {
		return isSuspended() && !fInEvaluation && fEventSuspend;
	}
	
	/**
	 * When a thread is disposed, its timer thread is disposed.
	 */
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
	 * and fire a terminate event.
	 */
	protected void terminated() {
		setTerminated(true);
		setRunning(false);
		dispose();		
		fireTerminateEvent();
	}
	
	/** 
	 * Returns this thread on the underlying VM which this
	 * model thread is a proxy to.
	 * 
	 * @return underlying thread
	 */
	protected ThreadReference getUnderlyingThread() {
		return fThread;
	}
	
	/**
	 * Sets the underlying thread that this model object
	 * is a proxy to.
	 * 
	 * @param thread underlying thread on target VM
	 */
	protected void setUnderlyingThread(ThreadReference thread) {
		fThread = thread;
	}
	
	/** 
	 * Returns this thread's underlying thread group.
	 * 
	 * @return thread group
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>Retrieving the underlying thread group is not supported
	 * on the underlying VM</li>
	 * </ul>
	 */
	protected ThreadGroupReference getUnderlyingThreadGroup() throws DebugException {
		if (fThreadGroup == null) {
			try {
				fThreadGroup = getUnderlyingThread().threadGroup();
			} catch (UnsupportedOperationException e) {
				requestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_group"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as
				// #requestFailed will throw an exception				
				return null;
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIThread.exception_retrieving_thread_group"), new String[] {e.toString()}), e); //$NON-NLS-1$
				// execution will not reach this line, as
				// #targetRequestFailed will throw an exception				
				return null;
			}
		}
		return fThreadGroup;
	}
	
	/**
	 * Returns this thread's timer.
	 * 
	 * @return timer
	 */
	private Timer getTimer() {
		if (fTimer == null) {
			fTimer = new Timer();
		}
		return fTimer;
	}
	
	/**
	 * An action has timed out. Stack frames are
	 * disposed and a change event is fired.
	 */
	public void timeout() {
		disposeStackFrames();
		interrupted();
		fireChangeEvent();
	}
	
	/**
	 * Starts a timer. In the event of a timeout,
	 * stack frames are collapsed.
	 */
	protected void startCollapseTimer() {
		getTimer().start(this, COLLAPSE_TIMEOUT);
	}
	
	/**
	 * Stops the current timer to cancel a pending
	 * stack frame collapse.
	 */
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
	 * Sets whether this thread is currently performing
	 * an evaluation.
	 * 
	 * @param evaluating whether this thread is currently
	 *  performing an evaluation
	 */
	protected void setPerformingEvaluation(boolean evaluating) {
		fInEvaluation= evaluating;
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
			setOriginalStepKind(getStepKind());
			setOriginalStepLocation(((JDIStackFrame)getTopStackFrame()).getUnderlyingStackFrame().location());
			setOriginalStepStackDepth(getUnderlyingFrames().size());
			setStepRequest(createStepRequest());
			setPendingStepHandler(this);
			addJDIEventListener(this, getStepRequest());
			setRunning(true);
			preserveStackFrames();
			fireResumeEvent(DebugEvent.STEP_START);
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
			// will either return, or the catch block will throw 
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
		 * Notification the step request has completed.
		 * If the current location matches one of the user-specified
		 * step filter criteria (e.g., synthetic methods, static initializers),
		 * then continue stepping.
		 * 
		 * @see IJDIDebugEventListener#handleEvent(Event, JDIDebugTarget)
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			try {
				StepEvent stepEvent = (StepEvent) event;
				Location location = stepEvent.location();
				Method method = location.method();				

				// if the ending step location is filtered, or if we're back where
				// we started on a step into, do another step of the same kind
				if (locationIsFiltered(method) || shouldDoExtraStepInto(location)) {
					setRunning(true);
					deleteStepRequest();
					createSecondaryStepRequest();			
					return true;		
				// otherwise, we're done stepping
				} else {
					stepEnd();
					return false;
				}
			} catch (DebugException e) {
				logError(e);
				stepEnd();
				return false;
			}
		}
		
		/**
		 * Return true if the StepEvent's Location is a Method that the 
		 * user has indicated (via the step filter preferences) should be 
		 * filtered.  Return false otherwise.
		 */
		protected boolean locationIsFiltered(Method method) {
			boolean filterStatics = JDIDebugModel.filterStatics();
			boolean filterSynthetics = JDIDebugModel.filterSynthetics();
			boolean filterConstructors = JDIDebugModel.filterConstructors();
			if (!(filterStatics || filterSynthetics  || filterConstructors)) {
				return false;
			}			
			
			if ((filterStatics && method.isStaticInitializer())	||
				(filterSynthetics && method.isSynthetic()) ||
				(filterConstructors && method.isConstructor()) ) {
				return true;	
			}
			
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
			setRunning(false);
			deleteStepRequest();
			setPendingStepHandler(null);
			fireSuspendEvent(DebugEvent.STEP_END);
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
		 * Aborts this step request if active. The step timer is
		 * stopped, and the step event request is deleted
		 * from the underlying VM.
		 */
		protected void abort() {
			if (getStepRequest() != null) {
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
			List frames = computeStackFrames();
			setRemainingFrames(frames.size() - frames.indexOf(frame));
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
		 * request normally. If not in the desired frame,
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
		 * Constructs a handler to drop to the specified
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
			List frames = computeStackFrames();
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
					targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString(JDIDebugModelMessages.getString("JDIThread.exception_while_popping_stack_frame")), new String[] {e.toString()}), e); //$NON-NLS-1$
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