package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;

public interface IJavaExceptionBreakpoint extends IJavaBreakpoint {
	/**
	 * Returns the <code>CAUGHT</code> attribute of this breakpoint
	 * or <code>false</code> if the attribute is not set.
	 */
	public boolean isCaught() throws CoreException;
	/**
	 * Returns the <code>UNCAUGHT</code> attribute of this breakpoint
	 * or <code>false</code> if the attribute is not set.
	 */
	public boolean isUncaught() throws CoreException;		
	/**
	 * Set the <code>CAUGHT</code> attribute of this breakpoint
	 */
	public void setCaught(boolean caught) throws CoreException;
	/**
	 * Set the <code>UNCAUGHT</code> attribute of this breakpoint
	 */	
	public void setUncaught(boolean uncaught) throws CoreException;
	/**
	 * Returns whether this breakpoint breaks on checked exceptions.
	 */
	public boolean isChecked() throws CoreException;
}

