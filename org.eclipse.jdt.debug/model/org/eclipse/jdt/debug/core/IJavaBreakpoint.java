package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IType;

public interface IJavaBreakpoint extends IBreakpoint {
	/**
	 * Returns whether this breakpoint is installed in at least
	 * one debug target.
	 * 
	 * @return whether this breakpoint is installed
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown while accessing the underlying marker attribute
	 */
	public boolean isInstalled() throws CoreException;
	/**
	 * Returns the type the given breakpoint is located in,
	 * or <code>null</code> a type cannot be resolved.
	 * 
	 * @return the type this breakpoint is located in, or <code>null</code>
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown while accessing the underlying marker attribute
	 */
	public IType getType() throws CoreException;
	/**
	 * Returns the hit count attribute of this breakpoint,
	 * or -1 if the attribute is not set.
	 * 
	 * @return this breakpoint's hit count, or -1
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown while accessing the underlying marker attribute
	 */
	public int getHitCount() throws CoreException;
	/**
	 * Sets the hit count attribute of this breakpoint,
	 * and resets the expired attribute to false (since, if
	 * the hit count is changed, the breakpoint should no longer be expired).
	 * 
	 * @param count the new hit count
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown while accessing the underlying marker attribute
	 */
	public void setHitCount(int count) throws CoreException;	

}

