package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;

public interface IJavaExceptionBreakpoint extends IJavaBreakpoint {
	/**
	 * Returns the <code>CAUGHT</code> attribute of the given breakpoint
	 * or <code>false</code> if the attribute is not set.
	 */
	public boolean isCaught();
	/**
	 * Toggle the caught state of this breakpoint
	 */
	public void toggleCaught() throws CoreException;
	/**
	 * Returns the <code>UNCAUGHT</code> attribute of the given breakpoint
	 * or <code>false</code> if the attribute is not set.
	 */
	public boolean isUncaught();
	/**
	 * Toggle the caught state of this breakpoint
	 */
	public void toggleUncaught() throws CoreException;		
	/**
	 * Returns whether the given breakpoint breaks on checked exceptions.
	 */
	public boolean isChecked();
	/**
	 * Toggle the checked state of this breakpoint
	 */
	public void toggleChecked() throws CoreException;
	/**
	 * Returns the text that should be displayed for the marker
	 * associated with this breakpoint.
	 */
	public String getMarkerText(boolean qualified);	
}

