package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * A Java pattern breakpoint is a Java line breakpoint which is installed in 
 * types with a specific source file name (debug attribute) and whose fully
 * qualified name matches a specified pattern.
 * <p>
 * This interface is not intended to be implemented.
 * </p>
 * @since 2.0
 */
public interface IJavaPatternBreakpoint extends IJavaLineBreakpoint {

	/**
	 * Returns the type name pattern in which this breakpoint is installed
	 * 
	 * @return the pattern in which this breakpoint is installed
	 * @exception CoreException is a <code>CoreException</code> is
	 * thrown accessing this breakpoint's underlying marker
	 */
	public String getPattern() throws CoreException;
	
	/**
	 * Returns the simple source file name in which this breakpoint is set.
	 * When this breakpoint specifies a source file name, this breakpoint is
	 * only installed in types whose source file name debug attribute
	 * match this value.
	 * 
	 * @return the simple source file name in which this breakpoint is set
	 * @exception CoreException is a <code>CoreException</code> is
	 * thrown accessing this breakpoint's underlying marker
	 */
	public String getSourceName() throws CoreException;	

}

