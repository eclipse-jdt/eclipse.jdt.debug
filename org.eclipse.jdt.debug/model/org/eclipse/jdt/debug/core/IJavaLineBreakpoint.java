package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILineBreakpoint;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

public interface IJavaLineBreakpoint extends IJavaBreakpoint, ILineBreakpoint {	
	/**
	 * Returns the text that should be displayed for the marker
	 * associated with this breakpoint.
	 */
	public String getMarkerText(boolean qualified, String memberString);
	/**
	 * Returns the smallest determinable <code>IMember</code> the given breakpoint is installed in.
	 */
	public IMember getMember();
	/**
	 * Returns the method the given breakpoint is installed in
	 * or <code>null</code> if a method cannot be resolved.
	 */
	public IMethod getMethod();	
	/**
	 * Returns the <code>HIT_COUNT</code> attribute of the given breakpoint
	 * or -1 if the attribute is not set.
	 */
	public int getHitCount();
	/**
	 * Sets the <code>HIT_COUNT</code> attribute of the given breakpoint,
	 * and resets the <code>EXPIRED</code> attribute to false (since, if
	 * the hit count is changed, the breakpoint should no longer be expired).
	 */
	public void setHitCount(int count) throws CoreException;	
}

