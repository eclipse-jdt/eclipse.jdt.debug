package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;

/**
 * A thread in a Java virtual machine.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see org.eclipse.debug.core.model.IThread
 */
public interface IJavaThread extends IThread {
	
	/**
	 * Status code indicating a request failed because a thread
	 * was not suspended.
	 */
	public static final int ERR_THREAD_NOT_SUSPENDED = 100;
	
	/**
	 * Status code indicating a request to perform a message send
	 * failed because a thread was already performing a message send.
	 * 
	 * @see IJavaObject#sendMessage(String, String, IJavaValue[], IJavaThread, boolean)
	 * @see IJavaClassType#sendMessage(String, String, IJavaValue[], IJavaThread)
	 * @see IJavaClassType#newInstance(String, IJavaValue[], IJavaThread)
	 */
	public static final int ERR_NESTED_METHOD_INVOCATION = 101;	

	/**
	 * Status code indicating a request to perform a message send
	 * failed because a thread was not suspended by a step or
	 * breakpoint event. When a thread is suspended explicitly via
	 * the <code>suspend()</code> method, it is not able to perform
	 * method invocations (this is a JDI limitation).
	 * 
	 * @see IJavaObject#sendMessage(String, String, IJavaValue[], IJavaThread, boolean)
	 * @see IJavaClassType#sendMessage(String, String, IJavaValue[], IJavaThread)
	 * @see IJavaClassType#newInstance(String, IJavaValue[], IJavaThread)
	 */
	public static final int ERR_INCOMPATIBLE_THREAD_STATE = 102;
		
	/**
	 * Returns whether this thread is a system thread.
	 *
	 * @return whether this thread is a system thread
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	boolean isSystemThread() throws DebugException;
	/**
	 * Returns whether any of the stack frames associated with this thread
	 * are running code in the VM that is out of synch with the code
	 * in the workspace.
	 * 
	 * @return whether this thread is out of synch with the workspace.
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * @since 2.0
	 */
	boolean isOutOfSynch() throws DebugException;
	/**
	 * Returns whether this thread may be running code in the VM that
	 * is out of synch with the code in the workspace.
	 * 
	 * @return whether this thread may be out of synch with the workspace.
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * @since 2.0
	 */
	boolean mayBeOutOfSynch() throws DebugException;	
	/**
	 * Returns whether this thread is currently performing
	 * an evaluation.
	 * 
	 * @return whether this thread is currently performing
	 * 	an evaluation
	 * @since 2.0
	 */
	boolean isPerformingEvaluation();
	/**
	 * Returns the name of the thread group this thread belongs to,
	 * or <code>null</code> if none.
	 *
	 * @return thread group name, or <code>null</code> if none
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	String getThreadGroupName() throws DebugException;
	
	/**
	 * Returns a variable with the given name, or <code>null</code> if
	 * unable to resolve a variable with the name, or if this
	 * thread is not currently suspended.
	 * <p>
	 * Variable lookup works only when a thread is suspended.
	 * Lookup is performed in all stack frames, in a top-down
	 * order, returning the first successful match, or <code>null</code>
	 * if no match is found.
	 * </p>
	 * @param variableName the name of the variable to search for
	 * @return a variable, or <code>null</code> if none
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	IJavaVariable findVariable(String variableName) throws DebugException;
	
	/**
	 * Invokes the given evaluation with the specfied progress
	 * monitor. This thread fires a resume event
	 * when the evaluation begins, and a suspend event when the evaluation
	 * completes or throws an exception. The events are given a detail
	 * as specified by <code>evaluationDetail</code> (one of
	 * <code>DebugEvent.EVALUATION</code> or
	 * <code>DebugEvent.EVALUATION_IMPLICIT</code>).
	 * 
	 * @param evaluation the evalation to perform
	 * @param monitor progress monitor (may be <code>null</code>
	 * @param evaluationDetail one of <code>DebugEvent.EVALUATION</code> or
	 *  <code>DebugEvent.EVALUATION_IMPLICIT</code>
	 * @param hitBreakpoints whether or not breakpoints should be honored
	 *  in this thread during the evaluation. If <code>false</code>, breakpoints
	 *  hit in this thread during the evaluation will be ignored.
	 * @exception DebugException if an exception occurs performing
	 *  the evaluation
	 * @since 2.0
	 */
	public void runEvaluation(IEvaluationRunnable evaluation, IProgressMonitor monitor, int evaluationDetail, boolean hitBreakpoints) throws DebugException; 
	
	/**
	 * Attempts to terminate the currently executing <code>IEvaluationRunnable</code>
	 * in this thread, if any. 
	 * 
	 * Evaluations may be composed of a series of instructions.
	 * Terminating an evaluation means stopping the evaluation after
	 * the current instruction completes. A single instruction (such as a method invocation)
	 * cannot be interrupted.
	 * 
	 * @exception DebugException if an exception occurs while
	 *  terminating the evaluation.
	 * @since 2.1
	 */
	public void terminateEvaluation() throws DebugException;
	/**
	 * Returns whether the currently executing <code>IEvaluationRunnable</code>
	 * supports termination. An IEvaluationRunnable supports termination
	 * if it implements <code>ITerminate</code>
	 * 
	 * @return whether the current evaluation supports termination
	 * @since 2.1
	 */
	public boolean canTerminateEvaluation();
}