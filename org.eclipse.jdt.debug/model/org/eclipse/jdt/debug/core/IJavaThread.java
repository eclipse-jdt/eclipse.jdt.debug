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
 * A Java thread is an extension of a regular thread,
 * providing support specific to the JDI debug model.
 * A Java thread is also available as an adapter from
 * threads originating from the JDI debug model.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.model.IThread
 * @see org.eclipse.core.runtime.IAdaptable 
 */
public interface IJavaThread extends IThread {
	
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
	 * @return whether this thread is out of synch with the VM.
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	boolean isOutOfSynch() throws DebugException;
	/**
	 * Returns whether this thread may be running code in the VM that
	 * is out of synch with the code in the workspace.
	 * 
	 * @return whether this thread may be out of synch with the VM.
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	boolean mayBeOutOfSynch() throws DebugException;	
	/**
	 * Returns whether this thread is currently performing
	 * an evaluation.
	 * 
	 * @return whether this thread is currently performing
	 * 	an evaluation
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
	 * <code>DebugEvent.EVALUATION_READ_ONLY</code>).
	 * When the specified timeout period (milliseconds) has passed and the 
	 * evaluation has not completed, a resume event is fired with a detail
	 * code of <code>UNSPECIFIED</code>, admitting that the given thread has been
	 * resumed.
	 * 
	 * @param evaluation the evalation to perform
	 * @param monitor progress monitor (may be <code>null</code>
	 * @param evaluationDetail one of <code>DebugEvent.EVALUATION</code> or
	 *  <code>DebugEvent.EVALUATION_READ_ONLY</code>
	 * @param timeout the number of milliseconds to wait for the evaluation to
	 *  complete before firing a resume event on the given thread
	 * @exception DebugException if an exception occurs performing
	 *  the evaluation
	 */
	public void runEvaluation(IEvaluationRunnable evaluation, IProgressMonitor monitor, int evaluationDetail) throws DebugException; 

}