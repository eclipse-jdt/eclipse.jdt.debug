package org.eclipse.jdt.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;

/**
 * A Java debug target is an extension of a regular debug
 * target, providing support specific to the JDI debug model.
 * A Java debug target is also available as an adapter from
 * debug targets originating for the JDI debug model.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IDebugTarget
 * @see org.eclipse.core.runtime.IAdaptable 
 */

public interface IJavaDebugTarget extends IDebugTarget {
	/**
	 * Searches for and returns a variable with the given name,
	 * or <code>null</code> if unable to resolve a variable with the name.
	 * <p>
	 * Variable lookup works only when a debug target has one or more
	 * threads suspended. Lookup is performed in each suspended thread,
	 * returning the first successful match. If this debug target has no
	 * suspended threads, <code>null</code> is returned.
	 * </p>
	 * @param variableName name of the variable
	 * @return a variable with the given name, or <code>null</code> if none
	 * @exception DebugException if an exception occurrs while searching
	 *   for the varialble on the target
	 */
	IVariable findVariable(String variableName) throws DebugException;

}