package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.internal.debug.core.JavaBreakpoint;

/**
 * A Java thread is an extension of a regular thread,
 * providing support specific to the JDI debug model.
 * A Java thread is also available as an adapter from
 * threads originating for the JDI debug model.
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
	 * @exception DebugException if unable to determine if this
	 *   thread is a system thread
	 */
	boolean isSystemThread() throws DebugException;
	
	/**
	 * Returns the name of the thread group this thread belongs to.
	 *
	 * @return thread group name
	 * @exception DebugException if unable retrieve this thread's
	 *	group name from the target
	 */
	String getThreadGroupName() throws DebugException;
	
	/**
	 * Returns the breakpoint that caused this thread to suspend,
	 * or <code>null</code> if this thread is not suspended or
	 * was not suspended by a breakpoint.
	 *
	 * @return breakpoint that caused suspend, or <code>null</code> if none
	 */
	JavaBreakpoint getBreakpoint();
	
	/**
	 * Returns a variable with the given name, or <code>null</code> if
	 * unable to resolve a variable with the name.
	 * <p>
	 * Variable lookup works only when this thread is suspended.
	 * Lookup is performed in all stack frames, in a top-down
	 * order, returning the first successful match.
	 * </p>
	 * @param variableName the name of the variable to search for
	 * @return a variable, or <code>null</code> if none
	 * @exception DebugException if an exception occurrs while searching
	 *    for the variable 
	 */
	IVariable findVariable(String variableName) throws DebugException;

}