package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;

public interface IJavaMethodEntryBreakpoint extends IJavaLineBreakpoint {

	/**
	 * Returns the method this entry breakpoint is located in.
	 * 
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown while accessing the underlying marker attribute
	 */
	public IMethod getMethod() throws CoreException;
}

