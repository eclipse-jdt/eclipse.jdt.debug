package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * A java pattern breakpoint is a java line breakpoint which is installed in all 
 * classes whose fully qualified name matches a specified pattern.
 * 
 * This interface is not intended to be implemented.
 */
public interface IJavaPatternBreakpoint extends IJavaLineBreakpoint {

	/**
	 * Returns the pattern in which this breakpoint is installed
	 * 
	 * @return the pattern in which this breakpoint is installed
	 * @exception CoreException is a <code>CoreException</code> is
	 * thrown accessing this breakpoint's underlying marker
	 */
	public String getPattern() throws CoreException;

}

