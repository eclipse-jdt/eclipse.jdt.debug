package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * A target pattern breakpoint is a Java line breakpoint which is installed in
 * classes with a specific source file name (debug attribute) and whose fully
 * qualified name matches a specified pattern per target.
 * The {target, class name pattern} pairs are not persisted with this breakpoint, as 
 * targets are transient. Clients that use this type of breakpoint are intended
 * to be breakpoint listeners that set a pattern per target as each breakpoint
 * is added to a target.
 * <p>
 * This interface is not intended to be implemented.
 * </p>
 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener
 * @since 2.0
 */
public interface IJavaTargetPatternBreakpoint extends IJavaLineBreakpoint {

	/**
	 * Returns the class name pattern in which this breakpoint is to be
	 * installed for the given target
	 * 
	 * @param target debug target
	 * @return the class name pattern in which this breakpoint is installed
	 */
	public String getPattern(IJavaDebugTarget target);
	
	/** 
	 * Sets the class name pattern for the specified target. This breakpoint
	 * will be installed in all types matching the given pattern in the
	 * specified target.
	 * 
	 * @param target debug target
	 * @param pattern class name pattern
	 * @exception CoreException if changing the pattern for this breakpoint fails
	 */
	public void setPattern(IJavaDebugTarget target, String pattern) throws CoreException;

	/**
	 * Returns the simple source file name in which this breakpoint is set.
	 * When this breakpoint specifies a source file name, this breakpoint is
	 * only installed in classes whose source file name debug attribute
	 * match this value.
	 * 
	 * @return the simple source file name in which this breakpoint is set
	 * @exception CoreException is a <code>CoreException</code> is
	 * thrown accessing this breakpoint's underlying marker
	 */
	public String getSourceName() throws CoreException;	
}

