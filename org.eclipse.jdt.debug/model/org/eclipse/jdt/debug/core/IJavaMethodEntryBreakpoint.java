package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;

/**
 * Method entry breakpoints are java line breakpoints that
 * suspend execution when a specific method is entered.
 * 
 * Clients are not intended to implement this interface.
 */
public interface IJavaMethodEntryBreakpoint extends IJavaLineBreakpoint {

	/**
	 * Returns the name of the method this breakpoint is
	 * located in.
	 * 
	 * @return the name of the method this breakpoint is
	 *  located in
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public String getMethodName() throws CoreException;
	
	/**
	 * Returns the signature of the method this breakpoint is
	 * located in.
	 * 
	 * @return the signature of the method this breakpoint is
	 *  located in
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public String getMethodSignature() throws CoreException;	
}

