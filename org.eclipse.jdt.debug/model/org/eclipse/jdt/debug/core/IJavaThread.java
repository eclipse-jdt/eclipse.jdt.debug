package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

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
	 * Returns the name of the thread group this thread belongs to.
	 *
	 * @return thread group name
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
	 * When the specified timeout period (milliseconds) has passed and the 
	 * evaluation has not completed, a resume event is fired with a detail
	 * code of <code>UNSPECIFIED</code>, admitting that the given thread has been
	 * resumed.
	 * 
	 * @param evaluation the evalation to perform
	 * @param monitor progress monitor (may be <code>null</code>
	 * @param evaluationDetail one of <code>DebugEvent.EVALUATION</code> or
	 *  <code>DebugEvent.EVALUATION_IMPLICIT</code>
	 * @param timeout the number of milliseconds to wait for the evaluation to
	 *  complete before firing a resume event on the given thread
	 * @exception DebugException if an exception occurs performing
	 *  the evaluation
	 * @since 2.0
	 */
	public void runEvaluation(IEvaluationRunnable evaluation, IProgressMonitor monitor, int evaluationDetail) throws DebugException; 

}