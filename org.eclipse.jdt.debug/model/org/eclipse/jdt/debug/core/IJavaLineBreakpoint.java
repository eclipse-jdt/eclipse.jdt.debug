package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

/**
 * Java line breakpoints are java breakpoints that suspend execution
 * when a particular line of code is reached.
 * 
 * Clients are not intended to implement this interface
 */
public interface IJavaLineBreakpoint extends IJavaBreakpoint, ILineBreakpoint {	
	/**
	 * Returns the member this breakpoint is located in.
	 * 
	 * @return the member this breakpoint is located in
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public IMember getMember() throws CoreException;
}

