package org.eclipse.jdt.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;

public interface IJavaWatchpoint extends IJavaLineBreakpoint {
	
	/**
	 * Returns whether this watchpoint is an access watchpoint
	 */	
	public boolean isAccess() throws CoreException;
	/**
	 * Sets the access attribute of this watchpoint. If access is set to true
	 * and the watchpoint is disabled, enable the watchpoint. If both access and 
	 * modification are false, disable the watchpoint.
	 */
	public void setAccess(boolean access) throws CoreException;
	/**
	 * Returns whether this watchpoint is a modification watchpoint
	 */		
	public boolean isModification() throws CoreException;
	/**
	 * Sets the modification attribute of this watchpoint. If modification is set to true
	 * and the watchpoint is disabled, enable the watchpoint. If both access and 
	 * modification are false, disable the watchpoint.
	 */
	public void setModification(boolean modification) throws CoreException;	
	/**
	 * Generate the field associated with this watchpoint
	 */
	public IField getField() throws CoreException;	
	
	/**
	 * Returns whether this breakpoint last suspended for an access (<code>true</code>)
	 * or modification (<code>false</code>).
	 */
	public boolean isAccessSuspend();
}

