package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

public interface IJavaLineBreakpoint extends IJavaBreakpoint, ILineBreakpoint {	
	/**
	 * Returns the smallest determinable member this breakpoint is
	 * located in.
	 * 
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown while accessing the underlying marker attribute
	 */
	public IMember getMember() throws CoreException;
}

