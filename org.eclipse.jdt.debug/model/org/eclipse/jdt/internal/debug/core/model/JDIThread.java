/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     BEA - Daniel R Somerfield - Bug 89643
 *     Jesper Steen Moller - Enhancement 254677 - filter getters/setters
 *     Jesper Steen Møller <jesper@selskabet.org> - Bug 430839
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.IStepFilter;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint.SuspendOnRecurrenceStrategy;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.ConditionalBreakpointHandler;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.model.MethodResult.ResultType;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;

/**
 * Model thread implementation for an underlying thread on a VM.
 */
public class JDIThread extends JDIDebugElement implements IJavaThread {

	/**
	 * Constant for the name of the default Java stratum
	 */
	private static final String JAVA_STRATUM_CONSTANT = "Java"; //$NON-NLS-1$

	/**
	 * Constant for the name of the main thread group.
	 */
	private static final String MAIN_THREAD_GROUP = "main"; //$NON-NLS-1$

	/**
	 * @since 3.5
	 */
	public static final int RESUME_QUIET = 500;

	/**
	 * @since 3.5
	 */
	public static final int SUSPEND_QUIET = 501;

	/**
	 * Status code indicating that a request to suspend this thread has timed
	 * out
	 */
	public static final int SUSPEND_TIMEOUT = 161;
	/**
	 * Underlying thread.
	 */
	private ThreadReference fThread;
	/**
	 * Cache of previous name, used in case thread is garbage collected.
	 */
	private String fPreviousName;
	/**
	 * Collection of stack frames
	 */
	private volatile List<IJavaStackFrame> fStackFrames;
	/**
	 * Underlying thread group, cached on first access.
	 */
	private ThreadGroupReference fThreadGroup;
	/**
	 * Name of underlying thread group, cached on first access.
	 */
	private String fThreadGroupName;
	/**
	 * Whether children need to be refreshed. Set to <code>true</code> when
	 * stack frames are re-used on the next suspend.
	 */
	private boolean fRefreshChildren = true;
	/**
	 * Currently pending step handler, <code>null</code> when not performing a
	 * step.
	 */
	private volatile StepHandler fStepHandler;
	/**
	 * Whether running.
	 */
	private volatile boolean fRunning;
	/**
	 * Whether terminated.
	 */
	private volatile boolean fTerminated;

	/**
	 * Whether this thread is a system thread.
	 */
	private boolean fIsSystemThread;

	/**
	 * Whether this thread is a daemon thread
	 *
	 * @since 3.3
	 */
	private boolean fIsDaemon;

	/**
	 * Lock used to guard access to internal data that need to be updated in atomic manner
	 */
	private final Object breakpointAcessLock = new Object();

	/**
	 * The collection of breakpoints that caused the last suspend, or an empty collection if the thread is not suspended or was not suspended by any
	 * breakpoint(s).
	 */
	private final List<IBreakpoint> fCurrentBreakpoints = new Vector<>(2);
	/**
	 * Non-null when this thread is executing an evaluation runnable. An
	 * evaluation may involve a series of method invocations.
	 */
	private volatile IEvaluationRunnable fEvaluationRunnable;

	/**
	 * Whether this thread was manually suspended during an evaluation.
	 */
	private volatile boolean fEvaluationInterrupted;

	/**
	 * <code>true</code> when there has been a request to suspend this thread
	 * via {@link #suspend()}. Remains <code>true</code> until there is a
	 * request to resume this thread via {@link #resume()}.
	 */
	private volatile boolean fClientSuspendRequest;

	/**
	 * Whether this thread is currently invoking a method. Nested method
	 * invocations cannot be performed.
	 */
	private volatile boolean fIsInvokingMethod;

	/**
	 * Lock used to wait for method invocations to complete.
	 */
	private final Object fInvocationLock = new Object();

	/**
	 * Lock used to wait for evaluations to complete.
	 */
	private final Object fEvaluationLock = new Object();

	/**
	 * Whether or not this thread is currently honoring breakpoints. This flag
	 * allows breakpoints to be disabled during evaluations.
	 */
	private volatile boolean fHonorBreakpoints = true;

	/**
	 * Whether a suspend vote is currently in progress. While voting this thread
	 * does not allow other breakpoints to be hit.
	 *
	 * @since 3.5
	 */
	private volatile boolean fSuspendVoteInProgress;

	/**
	 * The kind of step that was originally requested. Zero or more 'secondary
	 * steps' may be performed programmatically after the original
	 * user-requested step, and this field tracks the type (step into, over,
	 * return) of the original step.
	 */
	private volatile int fOriginalStepKind;

	/**
	 * The JDI Location from which an original user-requested step began.
	 */
	private volatile Location fOriginalStepLocation;

	/**
	 * The total stack depth at the time an original (user-requested) step is
	 * initiated. This is used along with the original step Location to
	 * determine if a step into comes back to the starting location and needs to
	 * be 'nudged' forward. Checking the stack depth eliminates undesired
	 * 'nudging' in recursive methods.
	 */
	private volatile int fOriginalStepStackDepth;

	/**
	 * Whether or not this thread is currently suspending (user-requested).
	 */
	private volatile boolean fIsSuspending;

	private final ThreadJob fAsyncJob;

	private volatile boolean fRunningAsyncJob;

	/**
	 * The current MethodExitRequest if a step-return or step-over is in progress.
	 */
	private volatile MethodExitRequest fCurrentMethodExitRequest;

	private volatile Thread fCurrentMethodExitRequestDisabler;

	/**
	 * The current ExceptionRequest if a step-return or step-over is in progress.
	 */
	private volatile ExceptionRequest fCurrentExceptionRequest;

	/**
	 * The current MethodEntryRequest if a step-over is in progress.
	 */
	private volatile MethodEntryRequest fCurrentMethodEntryRequest;

	/**
	 * Method for which a result value is expected
	 */
	private volatile Method fStepResultMethod;

	/**
	 * The location if a step-over is in progress.
	 */
	private volatile Location fStepOverLocation;

	/**
	 * The depth if a step-over is in progress.
	 */
	private volatile int fStepOverFrameCount;

	/**
	 * Candidate for depth of stack that will be returned values belong to. Is copied to fStepReturnTargetDepth only when step-return is actually
	 * observed
	 */
	private volatile int fStepReturnTargetFrameCount;

	private volatile MethodResult fStepResultCandidate;

	private final AtomicBoolean fStepResultTimeoutTriggered = new AtomicBoolean();

	/**
	 * Result of the last step step-over or step-return operation or method exit breakpoint of exception break point
	 */
	private volatile MethodResult fMethodResult;

	/**
	 * If previous suspend was on an exception breakpoint, this variable holds that Java exception instance, else {@code null}.
	 */
	private volatile IJavaObject fPreviousException;

	/**
	 * Creates a new thread on the underlying thread reference in the given
	 * debug target.
	 *
	 * @param target
	 *            the debug target in which this thread is contained
	 * @param thread
	 *            the underlying thread on the VM
	 * @exception ObjectCollectedException
	 *                if the underlying thread has been garbage collected and
	 *                cannot be properly initialized
	 */
	public JDIThread(JDIDebugTarget target, ThreadReference thread)
			throws ObjectCollectedException {
		super(target);
		setUnderlyingThread(thread);
		fAsyncJob = new ThreadJob();
		initialize();
	}

	/**
	 * Thread initialization:
	 * <ul>
	 * <li>Determines if this thread is a system thread</li>
	 * <li>Sets terminated state to <code>false</code></li>
	 * <li>Determines suspended state from underlying thread</li>
	 * <li>Sets this threads stack frames to an empty collection</li>
	 * </ul>
	 *
	 * @exception ObjectCollectedException
	 *                if the thread has been garbage collected and cannot be
	 *                initialized
	 */
	protected void initialize() throws ObjectCollectedException {
		fStackFrames = new Vector<>();
		// system thread
		try {
			determineIfSystemThread();
		} catch (DebugException e) {
			Throwable underlyingException = e.getStatus().getException();
			if (underlyingException instanceof VMDisconnectedException) {
				// Threads may be created by the VM at shutdown
				// as finalizers. The VM may be disconnected by
				// the time we hear about the thread creation.
				disconnected();
				return;
			}
			if (underlyingException instanceof ObjectCollectedException) {
				throw (ObjectCollectedException) underlyingException;
			}
			logError(e);
		}

		try {
			determineIfDaemonThread();
		} catch (DebugException e) {
			Throwable underlyingException = e.getStatus().getException();
			if (underlyingException instanceof VMDisconnectedException) {
				// Threads may be created by the VM at shutdown
				// as finalizers. The VM may be disconnected by
				// the time we hear about the thread creation.
				disconnected();
				return;
			}
			logError(e);
		}

		try {
			ThreadGroupReference group = getUnderlyingThreadGroup();
			// might already be terminated
			if (group != null) {
				getJavaDebugTarget().addThreadGroup(group);
			}
		} catch (DebugException e1) {
		}

		// state
		setTerminated(false);
		setRunning(false);
		try {
			// see bug 30816
			if (fThread.status() == ThreadReference.THREAD_STATUS_UNKNOWN) {
				setRunning(true);
				return;
			}
		} catch (VMDisconnectedException e) {
			disconnected();
			return;
		} catch (ObjectCollectedException e) {
			throw e;
		} catch (RuntimeException e) {
			logError(e);
		}

		try {
			// This may be a transient suspend state (for example, a thread is
			// handling a
			// class prepare event quietly). The class prepare event handler
			// will notify
			// this thread when it resumes
			setRunning(!fThread.isSuspended());
		} catch (VMDisconnectedException e) {
			disconnected();
			return;
		} catch (ObjectCollectedException e) {
			throw e;
		} catch (RuntimeException e) {
			logError(e);
		}
	}

	/**
	 * Adds the given breakpoint to the list of breakpoints this thread is
	 * suspended at
	 *
	 * @param bp
	 *            the breakpoint to add to the listing
	 */
	protected void addCurrentBreakpoint(IBreakpoint bp) {
		synchronized (breakpointAcessLock) {
			fCurrentBreakpoints.add(bp);
		}
	}

	/**
	 * Removes the given breakpoint from the list of breakpoints this thread is
	 * suspended at (called when a breakpoint is deleted, in case we are
	 * suspended at that breakpoint)
	 *
	 * @param bp
	 *            the breakpoint to remove from the listing
	 */
	protected void removeCurrentBreakpoint(IBreakpoint bp) {
		synchronized (breakpointAcessLock) {
			fCurrentBreakpoints.remove(bp);
		}
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		synchronized (breakpointAcessLock) {
			return fCurrentBreakpoints.toArray(new IBreakpoint[fCurrentBreakpoints.size()]);
		}
	}

	@Override
	public boolean canResume() {
		return isSuspended()
				&& (!isPerformingEvaluation() || isInvokingMethod())
				&& !isSuspendVoteInProgress()
				|| getDebugTarget().isSuspended();
	}

	@Override
	public boolean canSuspend() {
		return !isSuspended()
				|| (isPerformingEvaluation() && !isInvokingMethod())
				|| isSuspendVoteInProgress();
	}

	@Override
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	@Override
	public boolean canStepInto() {
		return canStep();
	}

	@Override
	public boolean canStepOver() {
		return canStep();
	}

	@Override
	public boolean canStepReturn() {
		return canStep();
	}

	/**
	 * Returns whether this thread is in a valid state to step.
	 *
	 * @return whether this thread is in a valid state to step
	 */
	protected boolean canStep() {
		try {
			return isSuspended()
					&& (!isPerformingEvaluation() || isInvokingMethod())
					&& !isSuspendVoteInProgress() && !isStepping()
					&& getTopStackFrame() != null
					&& !getJavaDebugTarget().isPerformingHotCodeReplace();
		} catch (DebugException e) {
			return false;
		}
	}

	/**
	 * Determines and sets whether this thread represents a system thread.
	 *
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 */
	protected void determineIfSystemThread() throws DebugException {
		fIsSystemThread = false;
		ThreadGroupReference tgr = getUnderlyingThreadGroup();
		fIsSystemThread = tgr != null;
		while (tgr != null) {
			String tgn = null;
			try {
				tgn = tgr.name();
				tgr = tgr.parent();
			} catch (UnsupportedOperationException e) {
				fIsSystemThread = false;
				break;
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIThread_exception_determining_if_system_thread,
								e.toString()), e);
				// execution will not reach this line, as
				// #targetRequestFailed will throw an exception
				return;
			}
			if (tgn != null && tgn.equals(MAIN_THREAD_GROUP)) {
				fIsSystemThread = false;
				break;
			}
		}
	}

	/**
	 * Determines whether this is a daemon thread.
	 *
	 * @throws DebugException
	 *             on failure
	 */
	protected void determineIfDaemonThread() throws DebugException {
		fIsDaemon = false;
		try {
			ReferenceType referenceType = getUnderlyingThread().referenceType();
			Field field = referenceType.fieldByName("daemon"); //$NON-NLS-1$
			if (field == null) {
				field = referenceType.fieldByName("isDaemon"); //$NON-NLS-1$
			}
			if (field != null) {
				if (field.signature().equals(Signature.SIG_BOOLEAN)) {
					Value value = getUnderlyingThread().getValue(field);
					if (value instanceof BooleanValue) {
						fIsDaemon = ((BooleanValue) value).booleanValue();
					}
				}
			}
		} catch (ObjectCollectedException oce) {/*
												 * do nothing thread does not
												 * exist
												 */
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_47, e);
		}
	}

	/**
	 * NOTE: this method returns a copy of this thread's stack frames.
	 *
	 * @see IThread#getStackFrames()
	 */
	@Override
	public synchronized IStackFrame[] getStackFrames() throws DebugException {
		List<IJavaStackFrame> list = computeStackFrames();
		return list.toArray(new IStackFrame[list.size()]);
	}

	/**
	 * @see #computeStackFrames()
	 *
	 * @param refreshChildren
	 *            whether or not this method should request new stack frames
	 *            from the VM
	 * @return the list of stackframes
	 * @throws DebugException
	 *             if an exception occurs retrieving frames
	 */
	protected synchronized List<IJavaStackFrame> computeStackFrames(boolean refreshChildren)
			throws DebugException {
		if (isSuspended()) {
			if (isTerminated()) {
				fStackFrames.clear();
			} else if (refreshChildren) {
				List<StackFrame> frames = getUnderlyingFrames();
				int oldSize = fStackFrames.size();
				if (oldSize > 0) {
					((JDIStackFrame) fStackFrames.get(0)).setIsTop(false);
				}
				int newSize = frames.size();
				int discard = oldSize - newSize; // number of old frames to
													// discard, if any
				for (int i = 0; i < discard; i++) {
					JDIStackFrame invalid = (JDIStackFrame) fStackFrames
							.remove(0);
					invalid.bind(null, -1);
				}
				int newFrames = newSize - oldSize; // number of frames to
													// create, if any
				int depth = oldSize;
				for (int i = newFrames - 1; i >= 0; i--) {
					fStackFrames.add(0, new JDIStackFrame(this,
							frames.get(i), depth));
					depth++;
				}
				int numToRebind = Math.min(newSize, oldSize); // number of
																// frames to
																// attempt to
																// re-bind
				int offset = newSize - 1;
				for (depth = 0; depth < numToRebind; depth++) {
					JDIStackFrame oldFrame = (JDIStackFrame) fStackFrames
							.get(offset);
					StackFrame frame = frames.get(offset);
					JDIStackFrame newFrame = oldFrame.bind(frame, depth);
					if (newFrame != oldFrame) {
						fStackFrames.set(offset, newFrame);
					}
					offset--;
				}
				if (newSize > 0) {
					((JDIStackFrame) fStackFrames.get(0)).setIsTop(true);
				}
			}
			fRefreshChildren = false;
		} else {
			return Collections.EMPTY_LIST;
		}
		return fStackFrames;
	}

	/**
	 * Returns this thread's current stack frames as a list, computing them if
	 * required. Returns an empty collection if this thread is not currently
	 * suspended, or this thread is terminated. This method should be used
	 * internally to get the current stack frames, instead of calling
	 * <code>#getStackFrames()</code>, which makes a copy of the current list.
	 * <p>
	 * Before a thread is resumed a call must be made to one of:
	 * <ul>
	 * <li><code>preserveStackFrames()</code></li>
	 * <li><code>disposeStackFrames()</code></li>
	 * </ul>
	 * If stack frames are disposed before a thread is resumed, stack frames are
	 * completely re-computed on the next call to this method. If stack frames
	 * are to be preserved, this method will attempt to re-use any stack frame
	 * objects which represent the same stack frame as on the previous suspend.
	 * Stack frames are cached until a subsequent call to preserve or dispose
	 * stack frames.
	 * </p>
	 *
	 * @return list of <code>IJavaStackFrame</code>
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 */
	public synchronized List<IJavaStackFrame> computeStackFrames() throws DebugException {
		return computeStackFrames(fRefreshChildren);
	}

	/**
	 * This method differs from computeStackFrames() in that it always requests
	 * new stack frames from the VM. As this is an expensive operation, this
	 * method should only be used by clients who know for certain that the stack
	 * frames on the VM have changed.
	 *
	 * @see JDIThread#computeStackFrames()
	 * @return the listing of stackframes or an empty list, never
	 *         <code>null</code>
	 * @throws DebugException
	 *             if an exception occurs retrieving the stackframes
	 */
	public List<IJavaStackFrame> computeNewStackFrames() throws DebugException {
		return computeStackFrames(true);
	}

	private List<StackFrame> getUnderlyingFrames() throws DebugException {
		if (!isSuspended()) {
			// Checking isSuspended here eliminates a race condition in resume
			// between the time stack frames are preserved and the time the
			// underlying thread is actually resumed.
			requestFailed(
					JDIDebugModelMessages.JDIThread_Unable_to_retrieve_stack_frame___thread_not_suspended__1,
					null, IJavaThread.ERR_THREAD_NOT_SUSPENDED);
		}
		try {
			return fThread.frames();
		} catch (IncompatibleThreadStateException e) {
			requestFailed(
					JDIDebugModelMessages.JDIThread_Unable_to_retrieve_stack_frame___thread_not_suspended__1,
					e, IJavaThread.ERR_THREAD_NOT_SUSPENDED);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIThread_exception_retrieving_stack_frames_2,
							e.toString()), e);
		} catch (InternalError e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIThread_exception_retrieving_stack_frames_2,
							e.toString()), e);
		}
		// execution will not reach this line, as
		// #targetRequestFailed will thrown an exception
		return null;
	}

	/**
	 * Returns the number of frames on the stack from the underlying thread.
	 *
	 * @return number of frames on the stack
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                <li>This thread is not suspended</li>
	 *                </ul>
	 */
	protected int getUnderlyingFrameCount() throws DebugException {
		try {
			return fThread.frameCount();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIThread_exception_retrieving_frame_count,
							e.toString()), e);
		} catch (IncompatibleThreadStateException e) {
			requestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIThread_exception_retrieving_frame_count,
							e.toString()), e,
					IJavaThread.ERR_THREAD_NOT_SUSPENDED);
		}
		// execution will not reach here - try block will either
		// return or exception will be thrown
		return -1;
	}

	@Override
	public void runEvaluation(IEvaluationRunnable evaluation,
			IProgressMonitor monitor, int evaluationDetail,
			boolean hitBreakpoints) throws DebugException {
		if (isPerformingEvaluation()) {
			requestFailed(
					JDIDebugModelMessages.JDIThread_Cannot_perform_nested_evaluations,
					null, IJavaThread.ERR_NESTED_METHOD_INVOCATION); //
		}

		if (!canRunEvaluation()) {
			requestFailed(
					JDIDebugModelMessages.JDIThread_Evaluation_failed___thread_not_suspended,
					null, IJavaThread.ERR_THREAD_NOT_SUSPENDED);
		}

		synchronized (fEvaluationLock) {
			fEvaluationRunnable = evaluation;
			fHonorBreakpoints = hitBreakpoints;
		}
		boolean quiet = isSuspendVoteInProgress();
		if (quiet) {
			// evaluations are quiet when a suspend vote is in progress
			// (conditional breakpoints, etc.).
			fireEvent(new DebugEvent(this, DebugEvent.MODEL_SPECIFIC,
					RESUME_QUIET));
		} else {
			fireResumeEvent(evaluationDetail);
		}
		// save and restore current breakpoint information - bug 30837
		IBreakpoint[] breakpoints = getBreakpoints();
		ISchedulingRule rule = null;
		if (evaluationDetail == DebugEvent.EVALUATION_IMPLICIT) {
			rule = getThreadRule();
		}
		try {
			if (rule != null) {
				Job.getJobManager().beginRule(rule, monitor);
			}
			if (monitor == null || !monitor.isCanceled()) {
				evaluation.run(this, monitor);
			}
		} catch (DebugException e) {
			throw e;
		} finally {
			if (rule != null) {
				Job.getJobManager().endRule(rule);
			}
			synchronized (fEvaluationLock) {
				fEvaluationRunnable = null;
				fHonorBreakpoints = true;
				fEvaluationLock.notifyAll();
			}
			if (getBreakpoints().length == 0 && breakpoints.length > 0) {
				for (IBreakpoint breakpoint : breakpoints) {
					addCurrentBreakpoint(breakpoint);
				}
			}
			if (quiet) {
				fireEvent(new DebugEvent(this, DebugEvent.MODEL_SPECIFIC,
						SUSPEND_QUIET));
			} else {
				fireSuspendEvent(evaluationDetail);
			}
			if (fEvaluationInterrupted
					&& fAsyncJob.isEmpty() && fRunningAsyncJob == false) {
				// @see bug 31585:
				// When an evaluation was interrupted & resumed, the launch view
				// does
				// not update properly. It cannot know when it is safe to
				// display frames
				// since it does not know about queued evaluations. Thus, when
				// the queue
				// is empty, we fire a change event to force the view to update.
				fEvaluationInterrupted = false;
				fireChangeEvent(DebugEvent.CONTENT);
			}
		}
	}

	/**
	 * Returns whether this thread is in a valid state to run an evaluation.
	 *
	 * @return whether this thread is in a valid state to run an evaluation
	 */
	protected boolean canRunEvaluation() {
		// NOTE similar to #canStep, except an evaluation can be run when in the
		// middle of
		// a step (conditional breakpoint, breakpoint listener, etc.)
		try {
			return isSuspended()
					&& !(isPerformingEvaluation() || isInvokingMethod())
					&& getTopStackFrame() != null
					&& !getJavaDebugTarget().isPerformingHotCodeReplace();
		} catch (DebugException e) {
			return false;
		}
	}

	@Override
	public void queueRunnable(Runnable evaluation) {
		fAsyncJob.addRunnable(evaluation);
	}

	@Override
	public void terminateEvaluation() throws DebugException {
		synchronized (fEvaluationLock) {
			if (canTerminateEvaluation()) {
				fEvaluationInterrupted = true;
				((ITerminate) fEvaluationRunnable).terminate();
			}
		}
	}

	@Override
	public boolean canTerminateEvaluation() {
		synchronized (fEvaluationLock) {
			return fEvaluationRunnable instanceof ITerminate;
		}
	}

	/**
	 * Invokes a method on the target, in this thread, and returns the result.
	 * Only one receiver may be specified - either a class or an object, the
	 * other must be <code>null</code>. This thread is left suspended after the
	 * invocation is complete, unless a call is made to
	 * <code>abortEvaluation<code> while
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
	 * @param receiverClass
	 *            the class in the target representing the receiver of a static
	 *            message send, or <code>null</code>
	 * @param receiverObject
	 *            the object in the target to be the receiver of the message
	 *            send, or <code>null</code>
	 * @param method
	 *            the underlying method to be invoked
	 * @param args
	 *            the arguments to invoke the method with (an empty list if
	 *            none)
	 * @param invokeNonvirtual
	 *            if the super-class method should be invoked
	 * @return the result of the method, as an underlying value
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                <li>This thread is not suspended (status code
	 *                <code>IJavaThread.ERR_THREAD_NOT_SUSPENDED</code>)</li>
	 *                <li>This thread is already invoking a method (status code
	 *                <code>IJavaThread.ERR_NESTED_METHOD_INVOCATION</code>)</li>
	 *                <li>This thread is not suspended by a JDI request (status
	 *                code
	 *                <code>IJavaThread.ERR_INCOMPATIBLE_THREAD_STATE</code>)</li>
	 *                </ul>
	 */
	protected Value invokeMethod(ClassType receiverClass,
			ObjectReference receiverObject, Method method, List<? extends Value> args,
			boolean invokeNonvirtual) throws DebugException {
		if (receiverClass != null && receiverObject != null) {
			throw new IllegalArgumentException(
					JDIDebugModelMessages.JDIThread_can_only_specify_one_receiver_for_a_method_invocation);
		}
		Value result = null;
		int timeout = getRequestTimeout();
		try {
			// this is synchronized such that any other operation that
			// might be resuming this thread has a chance to complete before
			// we determine if it is safe to continue with a method invocation.
			// See bugs 6518, 14069
			synchronized (this) {
				if (!isSuspended()) {
					requestFailed(
							JDIDebugModelMessages.JDIThread_Evaluation_failed___thread_not_suspended,
							null, IJavaThread.ERR_THREAD_NOT_SUSPENDED);
				}
				if (isInvokingMethod()) {
					requestFailed(
							JDIDebugModelMessages.JDIThread_Cannot_perform_nested_evaluations,
							null, IJavaThread.ERR_NESTED_METHOD_INVOCATION);
				}
				// set the request timeout to be infinite
				setRequestTimeout(Integer.MAX_VALUE);
				setRunning(true);
				setInvokingMethod(true);
			}
			preserveStackFrames();
			int flags = ClassType.INVOKE_SINGLE_THREADED;
			if (invokeNonvirtual) {
				// Superclass method invocation must be performed non-virtual.
				flags |= ObjectReference.INVOKE_NONVIRTUAL;
			}
			if (receiverClass == null) {
				result = receiverObject.invokeMethod(fThread, method, args,
						flags);
			} else {
				result = receiverClass.invokeMethod(fThread, method, args,
						flags);
			}
		} catch (InvalidTypeException e) {
			invokeFailed(e, timeout);
		} catch (ClassNotLoadedException e) {
			invokeFailed(e, timeout);
		} catch (IncompatibleThreadStateException e) {
			invokeFailed(
					JDIDebugModelMessages.JDIThread_Thread_must_be_suspended_by_step_or_breakpoint_to_perform_method_invocation_1,
					IJavaThread.ERR_INCOMPATIBLE_THREAD_STATE, e, timeout);
		} catch (InvocationException e) {
			invokeFailed(e, timeout);
		} catch (RuntimeException e) {
			invokeFailed(e, timeout);
		}

		invokeComplete(timeout);
		return result;
	}

	/**
	 * Invokes a method on the target, in this thread, and returns the result.
	 * This thread is left suspended after the invocation is complete, unless
	 * a call is made to <code>abortEvaluation<code> while  performing a method
	 * invocation. In that case, this thread is automatically resumed when/if
	 * this invocation (eventually) completes.
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
	 * @param receiverInterface
	 *            the class in the target representing the receiver of a static
	 *            message send, or <code>null</code>
	 * @param method
	 *            the underlying method to be invoked
	 * @param args
	 *            the arguments to invoke the method with (an empty list if
	 *            none)
	 * @return the result of the method, as an underlying value
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                <li>This thread is not suspended (status code
	 *                <code>IJavaThread.ERR_THREAD_NOT_SUSPENDED</code>)</li>
	 *                <li>This thread is already invoking a method (status code
	 *                <code>IJavaThread.ERR_NESTED_METHOD_INVOCATION</code>)</li>
	 *                <li>This thread is not suspended by a JDI request (status
	 *                code
	 *                <code>IJavaThread.ERR_INCOMPATIBLE_THREAD_STATE</code>)</li>
	 *                </ul>
	 */
	protected Value invokeMethod(InterfaceType receiverInterface,
			Method method, List<? extends Value> args) throws DebugException {
		Value result = null;
		int timeout = getRequestTimeout();
		try {
			// this is synchronized such that any other operation that
			// might be resuming this thread has a chance to complete before
			// we determine if it is safe to continue with a method invocation.
			// See bugs 6518, 14069
			synchronized (this) {
				if (!isSuspended()) {
					requestFailed(
							JDIDebugModelMessages.JDIThread_Evaluation_failed___thread_not_suspended,
							null, IJavaThread.ERR_THREAD_NOT_SUSPENDED);
				}
				if (isInvokingMethod()) {
					requestFailed(
							JDIDebugModelMessages.JDIThread_Cannot_perform_nested_evaluations,
							null, IJavaThread.ERR_NESTED_METHOD_INVOCATION);
				}
				// set the request timeout to be infinite
				setRequestTimeout(Integer.MAX_VALUE);
				setRunning(true);
				setInvokingMethod(true);
			}
			preserveStackFrames();
			int flags = ClassType.INVOKE_SINGLE_THREADED;
			result = receiverInterface.invokeMethod(fThread, method, args,
					flags);
		} catch (InvalidTypeException e) {
			invokeFailed(e, timeout);
		} catch (ClassNotLoadedException e) {
			invokeFailed(e, timeout);
		} catch (IncompatibleThreadStateException e) {
			invokeFailed(
					JDIDebugModelMessages.JDIThread_Thread_must_be_suspended_by_step_or_breakpoint_to_perform_method_invocation_1,
					IJavaThread.ERR_INCOMPATIBLE_THREAD_STATE, e, timeout);
		} catch (InvocationException e) {
			invokeFailed(e, timeout);
		} catch (RuntimeException e) {
			invokeFailed(e, timeout);
		}

		invokeComplete(timeout);
		return result;
	}

	/**
	 * Invokes a constructor in this thread, creating a new instance of the
	 * given class, and returns the result as an object reference. This thread
	 * is left suspended after the invocation is complete.
	 * <p>
	 * Method invocations cannot be nested. That is, this method must return
	 * before another call to this method can be made. This method does not
	 * return until the invocation is complete. Breakpoints can suspend a method
	 * invocation, and it is possible that an invocation will not complete due
	 * to an infinite loop or deadlock.
	 * </p>
	 * <p>
	 * Stack frames are preserved during method invocations, unless a timeout
	 * occurs. Although this thread's state is updated to running while
	 * performing an evaluation, no debug events are fired unless this
	 * invocation is interrupted by a breakpoint, or the invocation times out.
	 * </p>
	 * <p>
	 * When performing an invocation, the communication timeout with the target
	 * VM is set to infinite, as the invocation may not complete in a timely
	 * fashion, if at all. The timeout value is reset to its original value when
	 * the invocation completes.
	 * </p>
	 *
	 * @param receiverClass
	 *            the class in the target representing the receiver of the 'new'
	 *            message send
	 * @param constructor
	 *            the underlying constructor to be invoked
	 * @param args
	 *            the arguments to invoke the constructor with (an empty list if
	 *            none)
	 * @return a new object reference
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 */
	protected ObjectReference newInstance(ClassType receiverClass,
			Method constructor, List<? extends Value> args) throws DebugException {
		if (isInvokingMethod()) {
			requestFailed(
					JDIDebugModelMessages.JDIThread_Cannot_perform_nested_evaluations_2,
					null);
		}
		ObjectReference result = null;
		int timeout = getRequestTimeout();
		try {
			// set the request timeout to be infinite
			setRequestTimeout(Integer.MAX_VALUE);
			setRunning(true);
			setInvokingMethod(true);
			preserveStackFrames();
			result = receiverClass.newInstance(fThread, constructor, args,
					ClassType.INVOKE_SINGLE_THREADED);
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
	 * Called when an invocation fails. Performs cleanup and throws an
	 * exception.
	 *
	 * @param e
	 *            the exception that caused the failure
	 * @param restoreTimeout
	 *            the communication timeout value, in milliseconds, that should
	 *            be reset
	 * @see #invokeComplete(int)
	 * @exception DebugException
	 *                Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 */
	protected void invokeFailed(Throwable e, int restoreTimeout)
			throws DebugException {
		invokeFailed(MessageFormat.format(
				JDIDebugModelMessages.JDIThread_exception_invoking_method,
				e.toString()),
				DebugException.TARGET_REQUEST_FAILED, e, restoreTimeout);
	}

	/**
	 * Called when an invocation fails. Performs cleanup and throws an
	 * exception.
	 *
	 * @param message
	 *            error message
	 * @param code
	 *            status code
	 * @param e
	 *            the exception that caused the failure
	 * @param restoreTimeout
	 *            the communication timeout value, in milliseconds, that should
	 *            be reset
	 * @see #invokeComplete(int)
	 * @exception DebugException
	 *                Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 */
	protected void invokeFailed(String message, int code, Throwable e,
			int restoreTimeout) throws DebugException {
		invokeComplete(restoreTimeout);
		requestFailed(message, e, code);
	}

	/**
	 * Called when a method invocation has returned, successfully or not. This
	 * method performs cleanup:
	 * <ul>
	 * <li>Resets the state of this thread to suspended</li>
	 * <li>Restores the communication timeout value</li>
	 * <li>Computes the new set of stack frames for this thread</code>
	 * </ul>
	 *
	 * @param restoreTimeout
	 *            the communication timeout value, in milliseconds, that should
	 *            be reset
	 * @see #invokeMethod(ClassType, ObjectReference, Method, List, boolean)
	 * @see #newInstance(ClassType, Method, List)
	 */
	protected synchronized void invokeComplete(int restoreTimeout) {
		setInvokingMethod(false);
		setRunning(false);
		setRequestTimeout(restoreTimeout);
		// update preserved stack frames
		try {
			computeStackFrames();
		} catch (DebugException e) {
			logError(e);
		}
	}

	@Override
	public String getName() throws DebugException {
		try {
			fPreviousName = fThread.name();
		} catch (RuntimeException e) {
			// Don't bother reporting the exception when retrieving the name
			// (bug 30785 & bug 33276)
			if (e instanceof ObjectCollectedException) {
				if (fPreviousName == null) {
					fPreviousName = JDIDebugModelMessages.JDIThread_garbage_collected_1;
				}
			} else if (e instanceof VMDisconnectedException) {
				if (fPreviousName == null) {
					fPreviousName = JDIDebugModelMessages.JDIThread_42;
				}
			} else {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIThread_exception_retrieving_thread_name,
								e.toString()), e);
			}
		}
		return fPreviousName;
	}

	/**
	 * Returns the priority from the underlying {@link ReferenceType}, failing
	 * that the backing {@link Value} for the underlying {@link ThreadReference}
	 * is consulted
	 *
	 * @return the priority from the backing {@link ReferenceType} or
	 *         {@link Value}
	 * @throws DebugException
	 *             if an exception occurs retrieving the priority
	 * @see IThread#getPriority
	 */
	@Override
	public int getPriority() throws DebugException {
		// to get the priority, we must get the value from the "priority" field
		Field p = null;
		try {
			p = fThread.referenceType().fieldByName("priority"); //$NON-NLS-1$
			if (p == null) {
				requestFailed(
						JDIDebugModelMessages.JDIThread_no_priority_field, null);
			}
			Value v = fThread.getValue(p);
			if (v instanceof IntegerValue) {
				return ((IntegerValue) v).value();
			}
			requestFailed(
					JDIDebugModelMessages.JDIThread_priority_not_an_integer,
					null);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIThread_exception_retrieving_thread_priority,
							e.toString()), e);
		}
		// execution will not fall through to this line, as
		// #targetRequestFailed or #requestFailed will throw
		// an exception
		return -1;
	}

	@Override
	public synchronized IStackFrame getTopStackFrame() throws DebugException {
		List<IJavaStackFrame> c = computeStackFrames();
		if (c.isEmpty()) {
			return null;
		}
		return c.get(0);
	}

	/**
	 * A breakpoint has suspended execution of this thread. Aborts any step
	 * currently in process and notifies listeners of the breakpoint to allow a
	 * vote to determine if the thread should suspend.
	 *
	 * @param breakpoint
	 *            the breakpoint that caused the suspend
	 * @param suspendVote
	 *            current vote before listeners are notified (for example, if a
	 *            step request happens at the same location as a breakpoint, the
	 *            step may have voted to suspend already - this allows a
	 *            conditional breakpoint to avoid evaluation)
	 * @return whether this thread suspended
	 */
	public boolean handleSuspendForBreakpoint(JavaBreakpoint breakpoint,
			boolean suspendVote) {
		int policy = IJavaBreakpoint.SUSPEND_THREAD;
		synchronized (this) {
			if (fClientSuspendRequest) {
				// a request to suspend has overridden the breakpoint request -
				// suspend and
				// ignore the breakpoint
				return true;
			}
			fSuspendVoteInProgress = true;
			addCurrentBreakpoint(breakpoint);
			try {
				policy = breakpoint.getSuspendPolicy();
			} catch (CoreException e) {
				logError(e);
				setRunning(true);
				return false;
			}

			// update state to suspended but don't actually
			// suspend unless a registered listener agrees
			if (policy == IJavaBreakpoint.SUSPEND_VM) {
				((JDIDebugTarget) getDebugTarget())
						.prepareToSuspendByBreakpoint(breakpoint);
			} else {
				setRunning(false);
			}
		}

		try {
			if (!(breakpoint.isTriggerPoint())) {
				if (DebugPlugin.getDefault().getBreakpointManager().hasActiveTriggerPoints()){
					fSuspendVoteInProgress = false;
					return false;
				}

			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		// Evaluate breakpoint condition (if any). The condition is evaluated
		// regardless of the current suspend vote status, since breakpoint
		// listeners
		// should only be notified when a condition is true (i.e. when the
		// breakpoint
		// is really hit).
		if (breakpoint instanceof JavaLineBreakpoint) {
			JavaLineBreakpoint lbp = (JavaLineBreakpoint) breakpoint;
			// evaluate condition unless we're in an evaluation already (bug
			// 284022)
			if (lbp.hasCondition() && !isPerformingEvaluation()) {
				ConditionalBreakpointHandler handler = new ConditionalBreakpointHandler();
				int vote = handler.breakpointHit(this, breakpoint);
				if (vote == IJavaBreakpointListener.DONT_SUSPEND) {
					// condition is false, breakpoint is not hit
					synchronized (this) {
						fSuspendVoteInProgress = false;
						return false;
					}
				}
				if (handler.hasErrors()) {
					// there were errors, suspend and do not notify listeners of
					// hit since
					// they were already notified of compilation/runtime errors
					synchronized (this) {
						fSuspendVoteInProgress = false;
						return true;
					}
				}
				DebugPlugin.getDefault().getBreakpointManager().enableTriggerPoints(null, false);
					// make a note that we auto-disabled the trigger point for this breakpoint.
					// we re enable it at cleanup of JDITarget
			}
		}

		// poll listeners without holding lock on thread
		boolean suspend = true;
		try {
			suspend = JDIDebugPlugin.getDefault().fireBreakpointHit(this,
					breakpoint);
		} finally {
			synchronized (this) {
				fSuspendVoteInProgress = false;
				if (fClientSuspendRequest) {
					// if a client has requested a suspend, then override the
					// vote to suspend
					suspend = true;
				}
			}
		}
		return suspend;
	}

	/**
	 * To be called whenever a Java exception breakpoint is called. This thread will remember the Java exception instance associated with this event
	 * and will answer if suspending should be skipped as per the breakpoint's {@link IJavaExceptionBreakpoint.SuspendOnRecurrenceStrategy suspend on
	 * recurrence} strategy.
	 *
	 * @param breakpoint
	 *            the breakpoint about to trigger
	 * @return either {@code null} to signal that this breakpoint hit is not a recurrence, or one of
	 *         {@link SuspendOnRecurrenceStrategy#RECURRENCE_UNCONFIGURED}, {@link SuspendOnRecurrenceStrategy#SKIP_RECURRENCES}, or
	 *         {@link SuspendOnRecurrenceStrategy#SUSPEND_ALWAYS}.
	 */
	public SuspendOnRecurrenceStrategy shouldSkipExceptionRecurrence(IJavaExceptionBreakpoint breakpoint) {
		if (breakpoint instanceof JavaExceptionBreakpoint) {
			JavaExceptionBreakpoint exceptionBreakpoint = (JavaExceptionBreakpoint) breakpoint;
			try {
				IJavaObject lastException = exceptionBreakpoint.getLastException();
				IJavaObject previousException = fPreviousException;
				if (previousException != null && previousException.equals(lastException)) {
					return exceptionBreakpoint.getSuspendOnRecurrenceStrategy();
				}
				fPreviousException = lastException;
			} catch (CoreException e) {
				// ignore
			}
		}
		return null; // skipping not applicable, since this is no recurrence
	}

	/**
	 * Called after an event set with a breakpoint is done being processed.
	 * Updates thread state based on the result of handling the event set.
	 * Aborts any step in progress and fires a suspend event is suspending.
	 *
	 * @param breakpoint
	 *            the breakpoint that was hit
	 * @param suspend
	 *            whether to suspend
	 * @param queue
	 *            whether to queue events or fire immediately
	 * @param set
	 *            event set handling is associated with
	 */
	public void completeBreakpointHandling(JavaBreakpoint breakpoint,
			boolean suspend, boolean queue, EventSet set) {
		synchronized (this) {
			try {
				int policy = breakpoint.getSuspendPolicy();
				// suspend or resume
				if (suspend) {
					if (policy == IJavaBreakpoint.SUSPEND_VM) {
						((JDIDebugTarget) getDebugTarget())
								.suspendedByBreakpoint(breakpoint, false, set);
					}
					abortStep();
					if (queue) {
						queueSuspendEvent(DebugEvent.BREAKPOINT, set);
					} else {
						fireSuspendEvent(DebugEvent.BREAKPOINT);
					}
				} else {
					if (policy == IJavaBreakpoint.SUSPEND_VM) {
						((JDIDebugTarget) getDebugTarget())
								.cancelSuspendByBreakpoint(breakpoint);
					} else {
						setRunning(true);
						// dispose cached stack frames so we re-retrieve on the
						// next breakpoint
						preserveStackFrames();
					}
				}
			} catch (CoreException e) {
				logError(e);
				setRunning(true);
			}
		}

	}

	@Override
	public boolean isStepping() {
		return getPendingStepHandler() != null;
	}

	@Override
	public boolean isSuspended() {
		return !fRunning && !fTerminated;
	}

	@Override
	public boolean isSystemThread() {
		return fIsSystemThread;
	}

	@Override
	public boolean isDaemon() throws DebugException {
		return fIsDaemon;
	}

	@Override
	public String getThreadGroupName() throws DebugException {
		if (fThreadGroupName == null) {
			ThreadGroupReference tgr = getUnderlyingThreadGroup();

			// bug# 20370
			if (tgr == null) {
				return null;
			}

			try {
				fThreadGroupName = tgr.name();
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIThread_exception_retrieving_thread_group_name,
								e.toString()), e);
				// execution will not reach this line, as
				// #targetRequestFailed will thrown an exception
				return null;
			}
		}
		return fThreadGroupName;
	}

	@Override
	public boolean isTerminated() {
		return fTerminated;
	}

	@Override
	public synchronized boolean isOutOfSynch() throws DebugException {
		if (isSuspended() && ((JDIDebugTarget) getDebugTarget()).hasHCRFailed()) {
			List<IJavaStackFrame> frames = computeStackFrames();
			for(IJavaStackFrame frame : frames) {
				if(((JDIStackFrame)frame).isOutOfSynch()) {
					return true;
				}
			}
			return false;
		}
		// If the thread is not suspended, there's no way to
		// say for certain that it is running out of synch code
		return false;
	}

	@Override
	public boolean mayBeOutOfSynch() {
		if (!isSuspended()) {
			return ((JDIDebugTarget) getDebugTarget()).hasHCRFailed();
		}
		return false;
	}

	/**
	 * Sets whether this thread is terminated
	 *
	 * @param terminated
	 *            whether this thread is terminated
	 */
	protected void setTerminated(boolean terminated) {
		fTerminated = terminated;
	}

	@Override
	public synchronized void resume() throws DebugException {
		if (!isSuspended() && getDebugTarget().isSuspended()) {
			getDebugTarget().resume();
		} else {
			fClientSuspendRequest = false;
			resumeThread(true);
		}
	}

	/**
	 *
	 * Updates the state of this thread, but only fires notification to
	 * listeners if <code>fireNotification</code> is <code>true</code>.
	 *
	 * @see ISuspendResume#resume()
	 * @param fireNotification
	 *            if a resume event should be fired
	 * @throws DebugException
	 *             if an exception occurs trying to resume the thread
	 */
	private synchronized void resumeThread(boolean fireNotification)
			throws DebugException {
		if (!isSuspended() || (isPerformingEvaluation() && !isInvokingMethod())) {
			return;
		}
		try {
			setRunning(true);
			clearMethodResult();
			if (fireNotification) {
				fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			}
			preserveStackFrames();
			fThread.resume();
		} catch (VMDisconnectedException e) {
			disconnected();
		} catch (RuntimeException e) {
			setRunning(false);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIThread_exception_resuming,
					e.toString()), e);
		}
	}

	/**
	 * Sets whether this thread is currently executing. When set to
	 * <code>true</code>, this thread's current breakpoints are cleared.
	 *
	 * @param running
	 *            whether this thread is executing
	 */
	protected void setRunning(boolean running) {
		fRunning = running;
		if (running) {
			synchronized (breakpointAcessLock) {
				fCurrentBreakpoints.clear();
			}
		}
	}

	private void clearMethodResult() {
		setMethodResult(null);
	}

	/**
	 * Preserves stack frames to be used on the next suspend event. Iterates
	 * through all current stack frames, setting their state as invalid. This
	 * method should be called before this thread is resumed, when stack frames
	 * are to be re-used when it later suspends.
	 *
	 * @see #computeStackFrames()
	 */
	protected synchronized void preserveStackFrames() {
		fRefreshChildren = true;
		for(IJavaStackFrame frame : fStackFrames) {
			((JDIStackFrame)frame).setUnderlyingStackFrame(null);
		}
	}

	/**
	 * Disposes stack frames, to be completely re-computed on the next suspend
	 * event. This method should be called before this thread is resumed when
	 * stack frames are not to be re-used on the next suspend.
	 *
	 * @see #computeStackFrames()
	 */
	protected synchronized void disposeStackFrames() {
		fStackFrames.clear();
		fRefreshChildren = true;
	}

	/**
	 * This method is synchronized, such that the step request begins before a
	 * background evaluation can be performed.
	 *
	 * @see IStep#stepInto()
	 */
	@Override
	public void stepInto() throws DebugException {
		synchronized (this) {
			if (!canStepInto()) {
				return;
			}
		}
		StepHandler handler = createStepIntoHandler();
		handler.step();
	}

	/**
	 * This method is synchronized, such that the step request begins before a
	 * background evaluation can be performed.
	 *
	 * @see IStep#stepOver()
	 */
	@Override
	public void stepOver() throws DebugException {
		synchronized (this) {
			if (!canStepOver()) {
				return;
			}
		}
		StepHandler handler = createStepOverHandler();
		handler.step();
	}

	/**
	 * This method is synchronized, such that the step request begins before a
	 * background evaluation can be performed.
	 *
	 * @see IStep#stepReturn()
	 */
	@Override
	public void stepReturn() throws DebugException {
		synchronized (this) {
			if (!canStepReturn()) {
				return;
			}
		}
		StepHandler handler = createStepReturnHandler();
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
	 * In cases where a user-requested step into encounters nothing but filtered
	 * code (static initializers, synthetic methods, etc.), the default JDI
	 * behavior is to put the instruction pointer back where it was before the
	 * step into. This requires a second step to move forward. Since this is
	 * confusing to the user, we do an extra step into in such situations. This
	 * method determines when such an extra step into is necessary. It compares
	 * the current Location to the original Location when the user step into was
	 * initiated. It also makes sure the stack depth now is the same as when the
	 * step was initiated.
	 *
	 * @param location
	 *            the location to consider
	 * @return <code>true</code> if we should do an extra step,
	 *         <code>false</code> otherwise
	 * @throws DebugException
	 *             if an exception occurs
	 */
	protected boolean shouldDoExtraStepInto(Location location)
			throws DebugException {
		if (getOriginalStepKind() != StepRequest.STEP_INTO) {
			return false;
		}
		if (getOriginalStepStackDepth() != getUnderlyingFrameCount()) {
			return false;
		}
		Location origLocation = getOriginalStepLocation();
		if (origLocation == null) {
			return false;
		}
		// We cannot simply check if the two Locations are equal using the
		// equals()
		// method, since this checks the code index within the method. Even if
		// the
		// code indices are different, the line numbers may be the same, in
		// which case
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
	 * Determines if a user did a step into and stepped through filtered code.
	 * In this case, do a step return if the user has requested not to step thru
	 * to an unfiltered location.
	 *
	 * @return <code>true</code> if we should do a step return
	 * @throws DebugException
	 *             if an exception occurs
	 */
	protected boolean shouldDoStepReturn() throws DebugException {
		if (getOriginalStepKind() == StepRequest.STEP_INTO) {
			if ((getOriginalStepStackDepth() + 1) < getUnderlyingFrameCount()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void suspend() throws DebugException {
		// prepare for the suspend request
		prepareForClientSuspend();

		synchronized (this) {
			try {
				// Abort any pending step request
				abortStep();
				suspendUnderlyingThread();
			} catch (RuntimeException e) {
				fClientSuspendRequest = false;
				setRunning(true);
				targetRequestFailed(MessageFormat.format(
						JDIDebugModelMessages.JDIThread_exception_suspending,
						e.toString()), e);
			}
		}
	}

	/**
	 * Prepares to suspend this thread as requested by a client. Terminates any
	 * current evaluation (to stop after next instruction). Waits for any method
	 * invocations to complete.
	 *
	 * @throws DebugException
	 *             if thread does not suspend before timeout
	 */
	protected void prepareForClientSuspend() throws DebugException {
		// note that a suspend request has started
		synchronized (this) {
			// this will abort notification to pending breakpoint listeners
			fClientSuspendRequest = true;
		}

		synchronized (fEvaluationLock) {
			// terminate active evaluation, if any
			if (fEvaluationRunnable != null) {
				if (canTerminateEvaluation()) {
					fEvaluationInterrupted = true;
					((ITerminate) fEvaluationRunnable).terminate();
				}
				// wait for termination to complete
				int timeout = Platform.getPreferencesService().getInt(
						JDIDebugPlugin.getUniqueIdentifier(),
						JDIDebugModel.PREF_REQUEST_TIMEOUT,
						JDIDebugModel.DEF_REQUEST_TIMEOUT,
						null);
				try {
					fEvaluationLock.wait(timeout);
				} catch (InterruptedException e) {
				}
				if (fEvaluationRunnable != null) {
					fClientSuspendRequest = false;
					targetRequestFailed(JDIDebugModelMessages.JDIThread_1, null);
				}
			}
		}

		// first wait for any method invocation in progress to complete its
		// method invocation
		synchronized (fInvocationLock) {
			if (isInvokingMethod()) {
				int timeout = Platform.getPreferencesService().getInt(
						JDIDebugPlugin.getUniqueIdentifier(),
						JDIDebugModel.PREF_REQUEST_TIMEOUT,
						JDIDebugModel.DEF_REQUEST_TIMEOUT,
						null);
				try {
					fInvocationLock.wait(timeout);
				} catch (InterruptedException e) {
				}
				if (isInvokingMethod()) {
					// timeout waiting for invocation to complete, abort
					fClientSuspendRequest = false;
					targetRequestFailed(JDIDebugModelMessages.JDIThread_1, null);
				}
			}
		}
	}

	/**
	 * Suspends the underlying thread asynchronously and fires notification when
	 * the underlying thread is suspended.
	 */
	protected synchronized void suspendUnderlyingThread() {
		if (fIsSuspending) {
			return;
		}
		if (isSuspended()) {
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			return;
		}
		fIsSuspending = true;
		Thread thread = new Thread(() -> {
			try {
				fThread.suspend();
				int timeout = Platform.getPreferencesService().getInt(
						JDIDebugPlugin.getUniqueIdentifier(),
						JDIDebugModel.PREF_REQUEST_TIMEOUT,
						JDIDebugModel.DEF_REQUEST_TIMEOUT,
						null);
				long stop = System.currentTimeMillis() + timeout;
				boolean suspended = isUnderlyingThreadSuspended();
				while (System.currentTimeMillis() < stop && !suspended) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
					}
					suspended = isUnderlyingThreadSuspended();
					if (suspended) {
						break;
					}
				}
				if (!suspended) {
					IStatus status = new Status(
							IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							SUSPEND_TIMEOUT,
							MessageFormat.format(JDIDebugModelMessages.JDIThread_suspend_timeout, timeout),
							null);
					IStatusHandler handler = DebugPlugin.getDefault()
							.getStatusHandler(status);
					if (handler != null) {
						try {
							handler.handleStatus(status, JDIThread.this);
						} catch (CoreException e2) {
						}
					}
				}
				setRunning(false);
				fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			} catch (RuntimeException exception) {
			} finally {
				fIsSuspending = false;
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public boolean isUnderlyingThreadSuspended() {
		return fThread.isSuspended();
	}

	/**
	 * Notifies this thread that it has been suspended due to a VM suspend.
	 */
	protected synchronized void suspendedByVM() {
		setRunning(false);
	}

	/**
	 * Notifies this thread that is about to be resumed due to a VM resume.
	 *
	 * @throws DebugException
	 *             if an exception occurs
	 */
	protected synchronized void resumedByVM() throws DebugException {
		fClientSuspendRequest = false;
		setRunning(true);
		clearMethodResult();
		preserveStackFrames();
		// This method is called *before* the VM is actually resumed.
		// To ensure that all threads will fully resume when the VM
		// is resumed, make sure the suspend count of each thread
		// is no greater than 1. @see Bugs 23328 and 27622
		ThreadReference thread = fThread;
		try {
			while (thread.suspendCount() > 1) {
				thread.resume();
			}
		} catch (ObjectCollectedException e) {
		} catch (VMDisconnectedException e) {
			disconnected();
		} catch (RuntimeException e) {
			setRunning(false);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIThread_exception_resuming,
					e.toString()), e); //
		}
	}

	@Override
	public void terminate() throws DebugException {
		terminateEvaluation();
		getDebugTarget().terminate();
	}

	/**
	 * Drops to the given stack frame
	 *
	 * @param frame
	 *            the stack frame to try dropping to
	 *
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 */
	protected void dropToFrame(IStackFrame frame) throws DebugException {
		JDIDebugTarget target = (JDIDebugTarget) getDebugTarget();
		if (target.canPopFrames()) {
			// JDK 1.4 support
			try {
				// Pop the drop frame and all frames above it
				popFrame(frame);
				stepInto();
			} catch (RuntimeException exception) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIThread_exception_dropping_to_frame,
								exception.toString()),
						exception);
			}
		} else {
			// J9 support
			// This block is synchronized, such that the step request
			// begins before a background evaluation can be performed.
			synchronized (this) {
				StepHandler handler = createDropToFrameHandler(frame);
				handler.step();
			}
		}
	}

	protected void popFrame(IStackFrame frame) throws DebugException {
		JDIDebugTarget target = (JDIDebugTarget) getDebugTarget();
		if (target.canPopFrames()) {
			// JDK 1.4 support
			try {
				// Pop the frame and all frames above it
				StackFrame jdiFrame = null;
				int desiredSize = fStackFrames.size()
						- fStackFrames.indexOf(frame) - 1;
				int lastSize = fStackFrames.size() + 1; // Set up to pass the
														// first test
				int size = fStackFrames.size();
				while (size < lastSize && size > desiredSize) {
					// Keep popping frames until the stack stops getting smaller
					// or popFrame is gone.
					// see Bug 8054
					jdiFrame = ((JDIStackFrame) frame)
							.getUnderlyingStackFrame();
					preserveStackFrames();
					fThread.popFrames(jdiFrame);
					lastSize = size;
					size = computeStackFrames().size();
				}
			} catch (IncompatibleThreadStateException exception) {
				targetRequestFailed(MessageFormat.format(
						JDIDebugModelMessages.JDIThread_exception_popping,
						exception.toString()), exception);
			} catch (InvalidStackFrameException exception) {
				// InvalidStackFrameException can be thrown when all but the
				// deepest frame were popped. Fire a changed notification
				// in case this has occurred.
				fireChangeEvent(DebugEvent.CONTENT);
				targetRequestFailed(exception.toString(), exception);
			} catch (RuntimeException exception) {
				targetRequestFailed(MessageFormat.format(
						JDIDebugModelMessages.JDIThread_exception_popping,
						exception.toString()), exception);
			}
		}
	}

	/**
	 * Steps until the specified stack frame is the top frame. Provides ability
	 * to step over/return in the non-top stack frame. This method is
	 * synchronized, such that the step request begins before a background
	 * evaluation can be performed.
	 *
	 * @param frame
	 *            the stack frame to try and step to
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 */
	protected void stepToFrame(IStackFrame frame) throws DebugException {
		synchronized (this) {
			if (!canStepReturn()) {
				return;
			}
		}
		StepHandler handler = createStepToFrameHandler(frame);
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

	@Override
	public IJavaVariable findVariable(String varName) throws DebugException {
		if (isSuspended()) {
			try {
				IStackFrame[] stackFrames = getStackFrames();
				for (IStackFrame stackFrame : stackFrames) {
					IJavaStackFrame sf = (IJavaStackFrame) stackFrame;
					IJavaVariable var = sf.findVariable(varName);
					if (var != null) {
						return var;
					}
				}
			} catch (DebugException e) {
				// if the thread has since resumed, return null (no need to
				// report error)
				if (e.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
					throw e;
				}
			}
		}
		return null;
	}

	/**
	 * Notification this thread has terminated - update state and fire a
	 * terminate event.
	 */
	protected void terminated() {
		setTerminated(true);
		setRunning(false);
		fireTerminateEvent();
	}

	/**
	 * Returns this thread on the underlying VM which this model thread is a
	 * proxy to.
	 *
	 * @return underlying thread
	 */
	public ThreadReference getUnderlyingThread() {
		return fThread;
	}

	/**
	 * Sets the underlying thread that this model object is a proxy to.
	 *
	 * @param thread
	 *            underlying thread on target VM
	 */
	protected void setUnderlyingThread(ThreadReference thread) {
		fThread = thread;
	}

	/**
	 * Returns this thread's underlying thread group.
	 *
	 * @return thread group
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                <li>Retrieving the underlying thread group is not
	 *                supported on the underlying VM</li>
	 *                </ul>
	 */
	protected ThreadGroupReference getUnderlyingThreadGroup()
			throws DebugException {
		if (fThreadGroup == null) {
			try {
				fThreadGroup = fThread.threadGroup();
			} catch (UnsupportedOperationException e) {
				requestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIThread_exception_retrieving_thread_group,
								e.toString()), e);
				// execution will not reach this line, as
				// #requestFailed will throw an exception
				return null;
			} catch (VMDisconnectedException e) {
				// ignore disconnect
				return null;
			} catch (ObjectCollectedException e) {
				// ignore object collected
				return null;
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIThread_exception_retrieving_thread_group,
								e.toString()), e);
				// execution will not reach this line, as
				// #targetRequestFailed will throw an exception
				return null;
			}
		}
		return fThreadGroup;
	}

	@Override
	public boolean isPerformingEvaluation() {
		return fEvaluationRunnable != null;
	}

	/**
	 * Returns whether this thread is currently performing a method invocation
	 *
	 * @return if the thread is currently invoking a method
	 */
	public boolean isInvokingMethod() {
		return fIsInvokingMethod;
	}

	/**
	 * Returns whether this thread is currently ignoring breakpoints.
	 *
	 * @return if the thread is currently ignoring breakpoints
	 */
	public boolean isIgnoringBreakpoints() {
		return !fHonorBreakpoints || fSuspendVoteInProgress
				|| hasClientRequestedSuspend();
	}

	/**
	 * Returns whether a client has requested the target/thread to suspend.
	 *
	 * @return whether a client has requested the target/thread to suspend
	 */
	public boolean hasClientRequestedSuspend() {
		return fClientSuspendRequest;
	}

	/**
	 * Sets whether this thread is currently invoking a method. Notifies any
	 * threads waiting for the method invocation lock
	 *
	 * @param invoking
	 *            whether this thread is currently invoking a method
	 */
	protected void setInvokingMethod(boolean invoking) {
		synchronized (fInvocationLock) {
			fIsInvokingMethod = invoking;
			if (!invoking) {
				fInvocationLock.notifyAll();
			}
		}
	}

	/**
	 * Sets the step handler currently handling a step request.
	 *
	 * @param handler
	 *            the current step handler, or <code>null</code> if none
	 */
	protected void setPendingStepHandler(StepHandler handler) {
		fStepHandler = handler;
	}

	/**
	 * Returns the step handler currently handling a step request, or
	 * <code>null</code> if none.
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
		 * Initiates a step in the underlying VM by creating a step request of
		 * the appropriate kind (over, into, return), and resuming this thread.
		 * When a step is initiated it is registered with its thread as a
		 * pending step. A pending step could be cancelled if a breakpoint
		 * suspends execution during the step.
		 * <p>
		 * This thread's state is set to running and stepping, and stack frames
		 * are invalidated (but preserved to be re-used when the step
		 * completes). A resume event with a step detail is fired for this
		 * thread.
		 * </p>
		 * Note this method does nothing if this thread has no stack frames.
		 *
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected void step() throws DebugException {
			ISchedulingRule rule = getThreadRule();
			try {
				Job.getJobManager().beginRule(rule, null);
				JDIStackFrame top = (JDIStackFrame) getTopStackFrame();
				if (top == null) {
					return;
				}
				setOriginalStepKind(getStepKind());
				StackFrame frame = top.getUnderlyingStackFrame();
				if (frame == null) {
					return;
				}
				Location location = frame.location();
				setOriginalStepLocation(location);
				setOriginalStepStackDepth(computeStackFrames().size());
				setStepRequest(createStepRequest());
				setPendingStepHandler(this);
				addJDIEventListener(this, getStepRequest());
				setRunning(true);
				clearMethodResult();
				preserveStackFrames();
				fireResumeEvent(getStepDetail());
				invokeThread();
			} finally {
				Job.getJobManager().endRule(rule);
			}
		}

		/**
		 * Resumes the underlying thread to initiate the step. By default the
		 * thread is resumed. Step handlers that require other actions can
		 * override this method.
		 *
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected void invokeThread() throws DebugException {
			try {
				synchronized (JDIThread.this) {
					fClientSuspendRequest = false;
				}
				fThread.resume();
			} catch (RuntimeException e) {
				stepEnd(null);
				fireSuspendEvent(DebugEvent.STEP_END);
				targetRequestFailed(MessageFormat.format(
						JDIDebugModelMessages.JDIThread_exception_stepping,
						e.toString()), e);
			}
		}

		/**
		 * Creates and returns a step request specific to this step handler.
		 * Subclasses must override <code>getStepKind()</code> to return the
		 * kind of step it implements.
		 *
		 * @return step request
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected StepRequest createStepRequest() throws DebugException {
			return createStepRequest(getStepKind());
		}

		/**
		 * Creates and returns a step request of the specified kind.
		 *
		 * @param kind
		 *            of <code>StepRequest.STEP_INTO</code>,
		 *            <code>StepRequest.STEP_OVER</code>,
		 *            <code>StepRequest.STEP_OUT</code>
		 * @return step request
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected StepRequest createStepRequest(int kind) throws DebugException {
			EventRequestManager manager = getEventRequestManager();
			if (manager == null) {
				requestFailed(
						JDIDebugModelMessages.JDIThread_Unable_to_create_step_request___VM_disconnected__1,
						new VMDisconnectedException());
			}
			try {
				StepRequest request = manager.createStepRequest(fThread,
						StepRequest.STEP_LINE, kind);
				request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
				request.addCountFilter(1);
				attachFiltersToStepRequest(request);
				request.enable();

				if (manager.virtualMachine().canGetMethodReturnValues() && showStepResultIsEnabled(getDebugTarget())) {
					if (fCurrentMethodExitRequest != null) {
						removeJDIEventListener(this, fCurrentMethodExitRequest);
						manager.deleteEventRequest(fCurrentMethodExitRequest);
						fCurrentMethodExitRequest = null;
						Thread t = fCurrentMethodExitRequestDisabler;
						if (t != null) {
							t.interrupt();
							fCurrentMethodExitRequestDisabler = null;
						}
					}
					if (fCurrentExceptionRequest != null) {
						removeJDIEventListener(this, fCurrentExceptionRequest);
						manager.deleteEventRequest(fCurrentExceptionRequest);
						fCurrentExceptionRequest = null;
					}
					if (fCurrentMethodEntryRequest != null) {
						removeJDIEventListener(this, fCurrentMethodEntryRequest);
						manager.deleteEventRequest(fCurrentMethodEntryRequest);
						fCurrentMethodEntryRequest = null;
					}
					fStepResultCandidate = null;
					fStepResultTimeoutTriggered.set(false);
					List<IJavaStackFrame> frames = computeStackFrames();
					int frameCount = 0;
					StackFrame currentFrame = null;
					if (!frames.isEmpty()) {
						frameCount = frames.size();
						currentFrame = ((JDIStackFrame) frames.get(0)).getUnderlyingStackFrame();
					} else {
						// can happen, e.g. when step filters are active.
						if (fThread.isSuspended()) {
							try {
								// try to get the required info from the underlying object.
								frameCount = fThread.frameCount();
								currentFrame = fThread.frame(0);
							}
							catch (IncompatibleThreadStateException e) {
								// cannot not happen because of the enclosing isSuspended() check.
								logError(e);
							}
						}
					}
					if (currentFrame != null) {
						MethodExitRequest methodExitRequest = manager.createMethodExitRequest();
						methodExitRequest.addThreadFilter(fThread);
						methodExitRequest.addClassFilter(currentFrame.location().declaringType());

						if (manager.virtualMachine().canUseInstanceFilters()) {
							ObjectReference thisObject = null;
							try {
								thisObject = currentFrame.thisObject();
							} catch (Exception e) {
								// ignore errors here, continue without filtering
							}
							if (thisObject != null) {
								methodExitRequest.addInstanceFilter(thisObject);
							}
						}
						methodExitRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
						methodExitRequest.enable();
						fCurrentMethodExitRequest = methodExitRequest;
						Method method = currentFrame.location().method();

						fStepResultMethod = method;
						fStepReturnTargetFrameCount = frameCount - 1; // depth of the frame that is returned to
						addJDIEventListener(this, methodExitRequest);

						ExceptionRequest exceptionRequest = manager.createExceptionRequest(null, true, false);
						exceptionRequest.addThreadFilter(fThread);
						exceptionRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
						exceptionRequest.enable();
						fCurrentExceptionRequest = exceptionRequest;
						addJDIEventListener(this, exceptionRequest);

						if (kind == StepRequest.STEP_OVER) {
							MethodEntryRequest methodEntryRequest = manager.createMethodEntryRequest();
							methodEntryRequest.addThreadFilter(fThread);
							methodEntryRequest.enable();
							methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
							fCurrentMethodEntryRequest = methodEntryRequest;
							fStepOverLocation = currentFrame.location();
							fStepOverFrameCount = frameCount; // depth of the frame where the step-over is being done
							addJDIEventListener(this, methodEntryRequest);
						}

						int timeout = getStepResultTimeout();
						if (timeout != 0) {
							Runnable r = () -> {
								try {
									if (timeout > 0) {
										Thread.sleep(timeout);
									}
								} catch (InterruptedException e) {
									return;
								}
								fStepResultTimeoutTriggered.set(true);
								if (fCurrentMethodExitRequest == methodExitRequest) {
									try {
										methodExitRequest.disable();
										if (fCurrentExceptionRequest == exceptionRequest) {
											exceptionRequest.disable();
										}
									} catch (Exception e) {
										// ignore
									}
								}
							};
							if (timeout > 0) {
								Thread t = new Thread(r, "JDIThread: MethodExitDisabler"); //$NON-NLS-1$
								t.setDaemon(true);
								t.start();
								fCurrentMethodExitRequestDisabler = t;
							} else {
								// negative timeout: simulate immediate timeout (for testing)
								r.run();
							}
						}
					}
				}
				return request;
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIThread_exception_creating_step_request,
								e.toString()), e);
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
		 *         <code>StepRequest.STEP_OVER</code>,
		 *         <code>StepRequest.STEP_OUT</code>
		 */
		protected abstract int getStepKind();

		/**
		 * Returns the detail for this step event.
		 *
		 * @return one of <code>DebugEvent.STEP_INTO</code>,
		 *         <code>DebugEvent.STEP_OVER</code>,
		 *         <code>DebugEvent.STEP_RETURN</code>
		 */
		protected abstract int getStepDetail();

		/**
		 * Sets the step request created by this handler in the underlying VM.
		 * Set to <code>null<code> when
		 * this handler deletes its request.
		 *
		 * @param request
		 *            step request
		 */
		protected void setStepRequest(StepRequest request) {
			fStepRequest = request;
		}

		/**
		 * Returns the step request created by this handler in the underlying
		 * VM.
		 *
		 * @return step request
		 */
		protected StepRequest getStepRequest() {
			return fStepRequest;
		}

		/**
		 * Deletes this handler's step request from the underlying VM and
		 * removes this handler as an event listener.
		 */
		protected void deleteStepRequest() {
			try {
				if (fCurrentMethodExitRequest != null) {
					removeJDIEventListener(this, fCurrentMethodExitRequest);
					EventRequestManager manager = getEventRequestManager();
					if (manager != null) {
						manager.deleteEventRequest(fCurrentMethodExitRequest);
					}
					fCurrentMethodExitRequest = null;
					Thread t = fCurrentMethodExitRequestDisabler;
					if (t != null) {
						t.interrupt();
						fCurrentMethodExitRequestDisabler = null;
					}
				}
				if (fCurrentExceptionRequest != null) {
					removeJDIEventListener(this, fCurrentExceptionRequest);
					EventRequestManager manager = getEventRequestManager();
					if (manager != null) {
						manager.deleteEventRequest(fCurrentExceptionRequest);
					}
					fCurrentExceptionRequest = null;
				}
				if (fCurrentMethodEntryRequest != null) {
					removeJDIEventListener(this, fCurrentMethodEntryRequest);
					EventRequestManager manager = getEventRequestManager();
					if (manager != null) {
						manager.deleteEventRequest(fCurrentMethodEntryRequest);
					}
					fCurrentMethodEntryRequest = null;
				}
				StepRequest req = getStepRequest();
				if (req != null) {
					removeJDIEventListener(this, req);
					EventRequestManager manager = getEventRequestManager();
					if (manager != null) {
						manager.deleteEventRequest(req);
					}
				}
			} catch (RuntimeException e) {
				logError(e);
			} finally {
				setStepRequest(null);
			}
		}

		/**
		 * If step filters are currently switched on and the current location is
		 * not a filtered location, set all active filters on the step request.
		 *
		 * @param request
		 *            the request to augment
		 */
		protected void attachFiltersToStepRequest(StepRequest request) {

			if (applyStepFilters() && isStepFiltersEnabled()) {
				Location currentLocation = getOriginalStepLocation();
				if (!isSupported(currentLocation)) {
					return;
				}
				// Removed the fix for bug 5587, to address bug 41510
				// //check if the user has already stopped in a filtered
				// location
				// //is so do not filter @see bug 5587
				// ReferenceType type= currentLocation.declaringType();
				// String typeName= type.name();
				String[] activeFilters = getJavaDebugTarget().getStepFilters();
				// for (int i = 0; i < activeFilters.length; i++) {
				// StringMatcher matcher = new StringMatcher(activeFilters[i],
				// false, false);
				// if (matcher.match(typeName)) {
				// return;
				// }
				// }
				if (activeFilters != null) {
					for (String activeFilter : activeFilters) {
						request.addClassExclusionFilter(activeFilter);
					}
				}
			}
		}

		/**
		 * Returns whether this step handler should use step filters when
		 * creating its step request. By default, step filters can be used by
		 * any step request. Subclasses must override if/when required.
		 *
		 * @return whether this step handler should use step filters when
		 *         creating its step request
		 */
		protected boolean applyStepFilters() {
			return true;
		}

		/**
		 * Notification the step request has completed. If the current location
		 * matches one of the user-specified step filter criteria (e.g.,
		 * synthetic methods, static initializers), then continue stepping.
		 *
		 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget, boolean,
		 *      EventSet)
		 */
		@Override
		public boolean handleEvent(Event event, JDIDebugTarget target,
				boolean suspendVote, EventSet eventSet) {
			try {
				if (event instanceof MethodExitEvent) {
					Method stepResultMethod = fStepResultMethod;
					if (stepResultMethod != null) {
						MethodExitEvent methodExitEvent = (MethodExitEvent) event;
						if (methodExitEvent.location().method().equals(stepResultMethod)) {
							fStepResultCandidate = new MethodResult(stepResultMethod, fStepReturnTargetFrameCount, methodExitEvent.returnValue(), ResultType.returned);
						}
					}
					return true;
				}
				if (event instanceof ExceptionEvent) {
					ExceptionEvent exceptionEvent = (ExceptionEvent) event;
					Method stepResultMethod = fStepResultMethod;
					if (stepResultMethod != null) { // should always be true unless some third-party plugins manipulate internal state
						fStepResultCandidate = new MethodResult(stepResultMethod, fStepReturnTargetFrameCount, exceptionEvent.exception(), ResultType.threw);
					}
					return true;
				}
				if (event instanceof MethodEntryEvent) {
					removeJDIEventListener(this, fCurrentMethodEntryRequest);
					EventRequestManager manager = getEventRequestManager();
					if (manager != null) {
						manager.deleteEventRequest(fCurrentMethodEntryRequest);
					}
					fCurrentMethodEntryRequest = null;
					deleteStepRequest();
					createSecondaryStepRequest(StepRequest.STEP_OUT);
					return true;
				}
				StepEvent stepEvent = (StepEvent) event;
				Location currentLocation = stepEvent.location();

				if (fStepResultTimeoutTriggered.get()) {
					Method m = fStepResultMethod;
					if (m != null) {
						setMethodResult(new MethodResult(m, -1, null, ResultType.step_timeout));
					}
					fStepResultMethod = null;
					fStepReturnTargetFrameCount = -1;
					fStepResultCandidate = null;
					fStepResultTimeoutTriggered.set(false);
				} else if (fStepResultCandidate != null) {
					setMethodResult(fStepResultCandidate);
					fStepResultMethod = null;
					fStepReturnTargetFrameCount = -1;
					fStepResultCandidate = null;
					fStepResultTimeoutTriggered.set(false);
				}

				Location stepOverLocation2 = fStepOverLocation;
				if (getStepKind() == StepRequest.STEP_OVER) {
					if (stepOverLocation2 != null && fStepOverFrameCount >= 0) {
						int underlyingFrameCount = getUnderlyingFrameCount();
						if (underlyingFrameCount > fStepOverFrameCount) {
							// sometimes a MethodEntryEvent does not stop the thread but is delivered with another one grouped
							// in an event set. in this situation, multiple step-returns must be done.
							deleteStepRequest();
							createSecondaryStepRequest(StepRequest.STEP_OUT);
							return true;
						}
						if (underlyingFrameCount == fStepOverFrameCount && stepOverLocation2.method().equals(currentLocation.method())) {
							int lineNumber = stepOverLocation2.lineNumber();
							if (lineNumber != -1 && lineNumber == currentLocation.lineNumber()) {
								// line has not changed yet (probably returned from invocation with STEP_OUT)
								deleteStepRequest();
								createSecondaryStepRequest(StepRequest.STEP_OVER);
								return true;
							}
						}
						fStepOverLocation = null;
						fStepOverFrameCount = -1;
					}
				}

				if (!target.isStepThruFilters()) {
					if (shouldDoStepReturn()) {
						deleteStepRequest();
						createSecondaryStepRequest(StepRequest.STEP_OUT);
						return true;
					}
				}
				// if the ending step location is filtered and we did not start
				// from
				// a filtered location, or if we're back where
				// we started on a step into, do another step of the same kind
				if (locationShouldBeFiltered(currentLocation)
						|| shouldDoExtraStepInto(currentLocation)
						|| (getStepKind() == StepRequest.STEP_OVER && isSyntheticAndNotAvailable(currentLocation, stepOverLocation2))) {
					setRunning(true);
					deleteStepRequest();
					createSecondaryStepRequest();
					clearMethodResult();
					return true;
					// otherwise, we're done stepping
				}
				stepEnd(eventSet);
				return false;
			} catch (DebugException e) {
				logError(e);
				stepEnd(eventSet);
				return false;
			}
		}

		@Override
		public void eventSetComplete(Event event, JDIDebugTarget target,
				boolean suspend, EventSet eventSet) {
			// do nothing
		}

		/**
		 * Returns <code>true</code> if the StepEvent's Location is a Method
		 * that the user has indicated (via the step filter preferences) should
		 * be filtered and the step was not initiated from a filtered location.
		 * Returns <code>false</code> otherwise.
		 *
		 * @param location
		 *            the location to check
		 * @return if the given {@link Location} should be filtered
		 * @throws DebugException
		 *             if an exception occurs
		 */
		protected boolean locationShouldBeFiltered(Location location)
				throws DebugException {
			if (applyStepFilters()) {
				Location origLocation = getOriginalStepLocation();
				if (origLocation != null) {
					return !locationIsFiltered(origLocation.method(), true) && locationIsFiltered(location.method(), false);
				}
			}
			return false;
		}

		/**
		 * Returns <code>true</code> if the StepEvent's Location is a Method
		 * that the user has indicated (via the step filter preferences) should
		 * be filtered. Returns <code>false</code> otherwise.
		 *
		 * @param method
		 *            the {@link Method} location to check
		 * @param orig
		 *            <code>true</code> if the {@link Method} {@link Location} is the JDI Location
		 *         from which an original user-requested step began, <code>false</code> otherwise
		 * @return <code>true</code> if the {@link Method} {@link Location}
		 *         should be filtered, <code>false</code> otherwise
		 */
		protected boolean locationIsFiltered(Method method, boolean orig) {
			if (isStepFiltersEnabled()) {
				JDIDebugTarget target = getJavaDebugTarget();
				if ((target.isFilterStaticInitializers() && method.isStaticInitializer())
						|| (target.isFilterSynthetics() && method.isSynthetic())
						|| (target.isFilterConstructors() && method.isConstructor())
						|| (target.isFilterGetters() && JDIMethod.isGetterMethod(method))
						|| (target.isFilterSetters() && JDIMethod.isSetterMethod(method))) {
					return true;
				}
				if(!orig) {
					for (IStepFilter contributedFilter : DebugPlugin.getStepFilters(JDIDebugPlugin.getUniqueIdentifier())) {
						if (contributedFilter.isFiltered(method)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		/**
		 * Cleans up when a step completes.
		 * <ul>
		 * <li>Thread state is set to suspended.</li>
		 * <li>Stepping state is set to false</li>
		 * <li>Stack frames and variables are incrementally updated</li>
		 * <li>The step request is deleted and removed as and event listener</li>
		 * <li>A suspend event is fired</li>
		 * </ul>
		 *
		 * @param set
		 *            the remaining {@link EventSet} to queue
		 */
		protected void stepEnd(EventSet set) {
			setRunning(false);
			deleteStepRequest();
			setPendingStepHandler(null);
			if (set != null) {
				queueSuspendEvent(DebugEvent.STEP_END, set);
			}
		}

		/**
		 * Creates another step request in the underlying thread of the
		 * appropriate kind (over, into, return). This thread will be resumed by
		 * the event dispatcher as this event handler will vote to resume
		 * suspended threads. When a step is initiated it is registered with its
		 * thread as a pending step. A pending step could be cancelled if a
		 * breakpoint suspends execution during the step.
		 *
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected void createSecondaryStepRequest() throws DebugException {
			createSecondaryStepRequest(getStepKind());
		}

		/**
		 * Creates another step request in the underlying thread of the
		 * specified kind (over, into, return). This thread will be resumed by
		 * the event dispatcher as this event handler will vote to resume
		 * suspended threads. When a step is initiated it is registered with its
		 * thread as a pending step. A pending step could be cancelled if a
		 * breakpoint suspends execution during the step.
		 *
		 * @param kind
		 *            of <code>StepRequest.STEP_INTO</code>,
		 *            <code>StepRequest.STEP_OVER</code>,
		 *            <code>StepRequest.STEP_OUT</code>
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected void createSecondaryStepRequest(int kind)
				throws DebugException {
			setStepRequest(createStepRequest(kind));
			setPendingStepHandler(this);
			addJDIEventListener(this, getStepRequest());
		}

		/**
		 * Aborts this step request if active. The step event request is deleted
		 * from the underlying VM.
		 */
		protected void abort() {
			if (getStepRequest() != null) {
				deleteStepRequest();
				setPendingStepHandler(null);
				fStepOverLocation = null;
				fStepOverFrameCount =  -1;
			}
		}

		private boolean isSyntheticAndNotAvailable(Location currentLocation, Location previousLocation) {
			if (previousLocation != null) {
				Method method = previousLocation.method();
				if (method != null && method.isSynthetic()) {
					boolean isLambdaMethod = LambdaUtils.isLambdaMethod(method);
					if (isLambdaMethod) {
						int currentLineNumber = currentLocation.lineNumber();
						if (currentLineNumber == -1) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * Handler for step over requests.
	 */
	class StepOverHandler extends StepHandler {

		@Override
		protected int getStepKind() {
			return StepRequest.STEP_OVER;
		}

		@Override
		protected int getStepDetail() {
			return DebugEvent.STEP_OVER;
		}
	}

	/**
	 * Handler for step into requests.
	 */
	protected class StepIntoHandler extends StepHandler {

		@Override
		protected int getStepKind() {
			return StepRequest.STEP_INTO;
		}

		@Override
		protected int getStepDetail() {
			return DebugEvent.STEP_INTO;
		}

	}

	/**
	 * Handler for step return requests.
	 */
	protected class StepReturnHandler extends StepHandler {

		@Override
		protected boolean locationShouldBeFiltered(Location location)
				throws DebugException {
			// if still at the same depth, do another step return (see bug
			// 38744)
			if (getOriginalStepStackDepth() == getUnderlyingFrameCount()) {
				return true;
			}
			return super.locationShouldBeFiltered(location);
		}

		@Override
		protected int getStepKind() {
			return StepRequest.STEP_OUT;
		}

		@Override
		protected int getStepDetail() {
			return DebugEvent.STEP_RETURN;
		}
	}

	/**
	 * Handler for stepping to a specific stack frame (stepping in the non-top
	 * stack frame). Step returns are performed until a specified stack frame is
	 * reached or the thread is suspended (explicitly, or by a breakpoint).
	 */
	protected class StepToFrameHandler extends StepReturnHandler {

		/**
		 * The number of frames that should be left on the stack
		 */
		private int fRemainingFrames;

		/**
		 * Constructs a step handler to step until the specified stack frame is
		 * reached.
		 *
		 * @param frame
		 *            the stack frame to step to
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected StepToFrameHandler(IStackFrame frame) throws DebugException {
			List<IJavaStackFrame> frames = computeStackFrames();
			setRemainingFrames(frames.size() - frames.indexOf(frame));
		}

		/**
		 * Sets the number of frames that should be remaining on the stack when
		 * done.
		 *
		 * @param num
		 *            number of remaining frames
		 */
		protected void setRemainingFrames(int num) {
			fRemainingFrames = num;
		}

		/**
		 * Returns number of frames that should be remaining on the stack when
		 * done
		 *
		 * @return number of frames that should be left
		 */
		protected int getRemainingFrames() {
			return fRemainingFrames;
		}

		/**
		 * Notification the step request has completed. If in the desired frame,
		 * complete the step request normally. If not in the desired frame,
		 * another step request is created and this thread is resumed.
		 *
		 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget, boolean,
		 *      EventSet)
		 */
		@Override
		public boolean handleEvent(Event event, JDIDebugTarget target,
				boolean suspendVote, EventSet eventSet) {
			try {
				int numFrames = getUnderlyingFrameCount();
				// top of stack should not be null
				if (numFrames <= getRemainingFrames()) {
					stepEnd(eventSet);
					return false;
				}
				// reset running state and keep going
				setRunning(true);
				deleteStepRequest();
				createSecondaryStepRequest();
				clearMethodResult();
				return true;
			} catch (DebugException e) {
				logError(e);
				stepEnd(eventSet);
				return false;
			}
		}
	}

	/**
	 * Handles dropping to a specified frame.
	 */
	protected class DropToFrameHandler extends StepReturnHandler {

		/**
		 * The number of frames to drop off the stack.
		 */
		private int fFramesToDrop;

		/**
		 * Constructs a handler to drop to the specified stack frame.
		 *
		 * @param frame
		 *            the stack frame to drop to
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected DropToFrameHandler(IStackFrame frame) throws DebugException {
			List<IJavaStackFrame> frames = computeStackFrames();
			setFramesToDrop(frames.indexOf(frame));
		}

		/**
		 * Sets the number of frames to pop off the stack.
		 *
		 * @param num
		 *            number of frames to pop
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
		 * To drop a frame or re-enter, the underlying thread is instructed to
		 * do a return. When the frame count is less than zero, the step being
		 * performed is a "step return", so a regular invocation is performed.
		 *
		 * @throws DebugException
		 *             if an exception occurs
		 */
		@Override
		protected void invokeThread() throws DebugException {
			if (getFramesToDrop() < 0) {
				super.invokeThread();
			} else {
				try {
					org.eclipse.jdi.hcr.ThreadReference hcrThread = (org.eclipse.jdi.hcr.ThreadReference) fThread;
					hcrThread.doReturn(null, true);
				} catch (RuntimeException e) {
					stepEnd(null);
					fireSuspendEvent(DebugEvent.STEP_END);
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIThread_exception_while_popping_stack_frame,
									e.toString()), e);
				}
			}
		}

		/**
		 * Notification that the pop has completed. If there are more frames to
		 * pop, keep going, otherwise re-enter the top frame. Returns false, as
		 * this handler will resume this thread with a special invocation (
		 * <code>doReturn</code>).
		 *
		 * @see IJDIEventListener#handleEvent(Event, JDIDebugTarget, boolean,
		 *      EventSet)
		 * @see #invokeThread()
		 */
		@Override
		public boolean handleEvent(Event event, JDIDebugTarget target,
				boolean suspendVote, EventSet eventSet) {
			// pop is complete, update number of frames to drop
			setFramesToDrop(getFramesToDrop() - 1);
			try {
				if (getFramesToDrop() >= -1) {
					deleteStepRequest();
					doSecondaryStep();
				} else {
					stepEnd(eventSet);
				}
			} catch (DebugException e) {
				stepEnd(eventSet);
				logError(e);
			}
			return false;
		}

		/**
		 * Pops a secondary frame off the stack, does a re-enter, or a
		 * step-into.
		 *
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		protected void doSecondaryStep() throws DebugException {
			setStepRequest(createStepRequest());
			setPendingStepHandler(this);
			addJDIEventListener(this, getStepRequest());
			invokeThread();
		}

		/**
		 * Creates and returns a step request. If there are no more frames to
		 * drop, a re-enter request is made. If the re-enter is complete, a
		 * step-into request is created.
		 *
		 * @return step request
		 * @exception DebugException
		 *                if this method fails. Reasons include:
		 *                <ul>
		 *                <li>Failure communicating with the VM. The
		 *                DebugException's status code contains the underlying
		 *                exception responsible for the failure.</li>
		 *                </ul>
		 */
		@Override
		protected StepRequest createStepRequest() throws DebugException {
			EventRequestManager manager = getEventRequestManager();
			if (manager == null) {
				requestFailed(
						JDIDebugModelMessages.JDIThread_Unable_to_create_step_request___VM_disconnected__2,
						new VMDisconnectedException());
			}
			int num = getFramesToDrop();
			if (num > 0) {
				return super.createStepRequest();
			} else if (num == 0) {
				try {
					StepRequest request = ((org.eclipse.jdi.hcr.EventRequestManager) manager).createReenterStepRequest(fThread);
					request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					request.addCountFilter(1);
					request.enable();
					return request;
				} catch (RuntimeException e) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIThread_exception_creating_step_request,
									e.toString()), e);
				}
			} else if (num == -1) {
				try {
					StepRequest request = manager.createStepRequest(fThread, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
					request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					request.addCountFilter(1);
					request.enable();
					return request;
				} catch (RuntimeException e) {
					targetRequestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIThread_exception_creating_step_request,
									e.toString()), e);
				}
			}
			// this line will never be executed, as the try block
			// will either return, or the catch block with throw
			// an exception
			return null;
		}
	}

	@Override
	public boolean hasStackFrames() throws DebugException {
		return isSuspended();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IJavaThread.class) {
			return adapter.cast(this);
		}
		if (adapter == IJavaStackFrame.class) {
			try {
				return adapter.cast(getTopStackFrame());
			} catch (DebugException e) {
				// do nothing if not able to get frame
			}
		}
		return super.getAdapter(adapter);
	}

	@Override
	public boolean hasOwnedMonitors() throws DebugException {
		return isSuspended() && getOwnedMonitors().length > 0;
	}

	@Override
	public IJavaObject[] getOwnedMonitors() throws DebugException {
		try {
			JDIDebugTarget target = (JDIDebugTarget) getDebugTarget();
			List<ObjectReference> ownedMonitors = fThread.ownedMonitors();
			IJavaObject[] javaOwnedMonitors = new IJavaObject[ownedMonitors
					.size()];
			Iterator<ObjectReference> itr = ownedMonitors.iterator();
			int i = 0;
			while (itr.hasNext()) {
				ObjectReference element = itr.next();
				javaOwnedMonitors[i] = new JDIObjectValue(target, element);
				i++;
			}
			return javaOwnedMonitors;
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_43, e);
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_44, e);
		}
		return null;
	}

	@Override
	public IJavaObject getContendedMonitor() throws DebugException {
		try {
			ObjectReference monitor = fThread.currentContendedMonitor();
			if (monitor != null) {
				return new JDIObjectValue((JDIDebugTarget) getDebugTarget(),
						monitor);
			}
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_45, e);
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_46, e);
		}

		return null;
	}

	@Override
	public boolean canStepWithFilters() {
		if (canStepInto()) {
			String[] filters = getJavaDebugTarget().getStepFilters();
			return filters != null && filters.length > 0;
		}
		return false;
	}

	@Override
	public void stepWithFilters() throws DebugException {
		if (!canStepWithFilters()) {
			return;
		}
		stepInto();
	}

	/**
	 * Class which managed the queue of runnable associated with this thread.
	 */
	class ThreadJob extends Job {

		private final Vector<Runnable> fRunnables;

		public ThreadJob() {
			super(JDIDebugModelMessages.JDIThread_39);
			fRunnables = new Vector<>(5);
			setSystem(true);
		}

		public void addRunnable(Runnable runnable) {
			synchronized (fRunnables) {
				fRunnables.add(runnable);
			}
			schedule();
		}

		public boolean isEmpty() {
			return fRunnables.isEmpty();
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			fRunningAsyncJob = true;
			Object[] runnables;
			synchronized (fRunnables) {
				runnables = fRunnables.toArray();
				fRunnables.clear();
			}

			MultiStatus failed = null;
			monitor.beginTask(JDIDebugModelMessages.JDIThread_39, runnables.length);
			int i = 0;
			while (i < runnables.length && !isTerminated()
					&& !monitor.isCanceled()) {
				try {
					((Runnable) runnables[i]).run();
				} catch (Exception e) {
					if (failed == null) {
						failed = new MultiStatus(
								JDIDebugPlugin.getUniqueIdentifier(),
								JDIDebugPlugin.ERROR,
								JDIDebugModelMessages.JDIThread_0, null);
					}
					failed.add(new Status(IStatus.ERROR, JDIDebugPlugin
							.getUniqueIdentifier(), JDIDebugPlugin.ERROR,
							JDIDebugModelMessages.JDIThread_0, e));
				}
				i++;
				monitor.worked(1);
			}
			fRunningAsyncJob = false;
			monitor.done();
			if (failed == null) {
				return Status.OK_STATUS;
			}
			return failed;
		}

		@Override
		public boolean shouldRun() {
			return !isTerminated() && !fRunnables.isEmpty();
		}

		@Override
		public boolean belongsTo(Object family) {
			return JDIThread.class == family || JDIThread.this == family;
		}
	}

	@Override
	public void stop(IJavaObject exception) throws DebugException {
		try {
			fThread.stop(((JDIObjectValue) exception).getUnderlyingObject());
		} catch (InvalidTypeException e) {
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIThread_exception_stoping_thread,
					e.toString()), e);
		}
	}

	@Override
	public IJavaThreadGroup getThreadGroup() throws DebugException {
		ThreadGroupReference group = getUnderlyingThreadGroup();
		if (group != null) {
			return getJavaDebugTarget().findThreadGroup(group);
		}
		return null;
	}

	@Override
	public int getFrameCount() throws DebugException {
		return getUnderlyingFrameCount();
	}

	protected void forceReturn(IJavaValue value) throws DebugException {
		if (!isSuspended()) {
			return;
		}
		try {
			fThread.forceEarlyReturn(((JDIValue) value).getUnderlyingValue());
			stepReturn();
		} catch (VMDisconnectedException e) {
			disconnected();
		} catch (InvalidTypeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_48, e);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_48, e);
		} catch (IncompatibleThreadStateException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_48, e);
		} catch (UnsupportedOperationException e) {
			requestFailed(JDIDebugModelMessages.JDIThread_48, e);
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThread_48, e);
		}
	}

	/**
	 * Implementation of a scheduling rule for this thread, which defines how it
	 * should behave when a request for content job tries to run while the
	 * thread is evaluating
	 *
	 * @since 3.3.0
	 */
	static class SerialPerObjectRule implements ISchedulingRule {

		private Object fObject = null;

		public SerialPerObjectRule(Object lock) {
			fObject = lock;
		}

		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			if (rule instanceof SerialPerObjectRule) {
				SerialPerObjectRule vup = (SerialPerObjectRule) rule;
				return fObject == vup.fObject;
			}
			return false;
		}

	}

	/**
	 * returns the scheduling rule for getting content while evaluations are
	 * running
	 *
	 * @return the <code>ISchedulingRule</code> for this thread
	 *
	 * @since 3.3.0
	 */
	public ISchedulingRule getThreadRule() {
		return new SerialPerObjectRule(this);
	}

	/**
	 * A class prepare has resumed this thread - if the thread was suspended at
	 * startup then fix up the state to running and fire an event to update UI.
	 */
	public synchronized void resumedFromClassPrepare() {
		if (isSuspended()) {
			setRunning(true);
			fireResumeEvent(DebugEvent.CLIENT_REQUEST);
		}
	}

	/**
	 * Returns whether a suspend vote is currently in progress.
	 *
	 * @return whether a suspend vote is currently in progress
	 */
	public boolean isSuspendVoteInProgress() {
		return fSuspendVoteInProgress;
	}

	@Override
	public IJavaObject getThreadObject() throws DebugException {
		return (IJavaObject) JDIValue
				.createValue(getJavaDebugTarget(), fThread);
	}

	protected StepIntoHandler createStepIntoHandler() {
        return new StepIntoHandler();
    }

    protected StepOverHandler createStepOverHandler() {
        return new StepOverHandler();
    }

    protected StepReturnHandler createStepReturnHandler() {
        return new StepReturnHandler();
    }

    protected StepToFrameHandler createStepToFrameHandler(IStackFrame stackFrame) throws DebugException {
        return new StepToFrameHandler(stackFrame);
    }

    protected DropToFrameHandler createDropToFrameHandler(IStackFrame stackFrame) throws DebugException {
        return new DropToFrameHandler(stackFrame);
    }
	public static boolean showStepResultIsEnabled(IDebugTarget debugTarget) {
		if (debugTarget == null || debugTarget.getProcess() == null) {
			return Platform.getPreferencesService().getBoolean(JDIDebugPlugin.getUniqueIdentifier(), JDIDebugModel.PREF_SHOW_STEP_RESULT_REMOTE, false, null);
		}
		return Platform.getPreferencesService().getBoolean(JDIDebugPlugin.getUniqueIdentifier(), JDIDebugModel.PREF_SHOW_STEP_RESULT, true, null);
	}

	public static int getStepResultTimeout() {
		return Platform.getPreferencesService().getInt(JDIDebugPlugin.getUniqueIdentifier(), JDIDebugModel.PREF_SHOW_STEP_TIMEOUT, JDIDebugModel.DEF_SHOW_STEP_TIMEOUT, null);
	}

	protected boolean isSupported(Location currentLocation) {
		if (currentLocation == null) {
			return false;
		}
		return JAVA_STRATUM_CONSTANT.equals(currentLocation.declaringType().defaultStratum());
	}

	public MethodResult getMethodResult() {
		return fMethodResult;
	}

	public void setMethodResult(MethodResult fMethodResult) {
		this.fMethodResult = fMethodResult;
	}

}