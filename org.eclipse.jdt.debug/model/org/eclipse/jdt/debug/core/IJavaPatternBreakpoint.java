package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * A line breakpoint installed in types associated with a specific source file
 * (based on source file name debug attribute) and whose fully
 * qualified name matches a specified pattern.
 * <p>
 * This interface is not intended to be implemented.
 * </p>
 * @since 2.0
 */
public interface IJavaPatternBreakpoint extends IJavaLineBreakpoint {

	/**
	 * Returns the type name pattern this breakpoint uses to identify types
	 * in which to install itself.
	 * 
	 * @return the type name pattern this breakpoint uses to identify types
	 *  in which to install itself
	 * @exception CoreException if unable to access the property from
	 *  this breakpoint's underlying marker
	 */
	public String getPattern() throws CoreException;
	
	/**
	 * Returns the source file name in which this breakpoint is set.
	 * When this breakpoint specifies a source file name, this breakpoint is
	 * only installed in types whose source file name debug attribute
	 * match this value.
	 * 
	 * @return the source file name in which this breakpoint is set
	 * @exception CoreException if unable to access the property from
	 *  this breakpoint's underlying marker
	 */
	public String getSourceName() throws CoreException;	

}

