package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;

public interface IJavaPatternBreakpoint extends IJavaLineBreakpoint {

	/**
	 * Returns the pattern in which this breakpoint is installed
	 */
	public String getPattern() throws CoreException;

}

