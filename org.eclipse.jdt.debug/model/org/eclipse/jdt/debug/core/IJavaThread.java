package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.core.resources.IMarker;
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
public interface IJavaThread extends IThread, IJavaEvaluate {
	
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
	IVariable findVariable(String variableName) throws DebugException;

}