package org.eclipse.jdt.debug.core;

import org.eclipse.jdt.core.IField;

public interface IJavaWatchpoint extends IJavaLineBreakpoint {
	
	/**
	 * Returns whether this watchpoint is an access watchpoint
	 */	
	public boolean isAccess();
	/**
	 * Sets the access attribute of this watchpoint. If access is set to true
	 * and the watchpoint is disabled, enable the watchpoint. If both access and 
	 * modification are false, disable the watchpoint.
	 */
	public void setAccess(boolean access);	
	/**
	 * Toggle the access attribute of this watchpoint
	 */
	public void toggleAccess();
	/**
	 * Returns whether this watchpoint is a modification watchpoint
	 */		
	public boolean isModification();
	/**
	 * Sets the modification attribute of this watchpoint. If modification is set to true
	 * and the watchpoint is disabled, enable the watchpoint. If both access and 
	 * modification are false, disable the watchpoint.
	 */
	public void setModification(boolean modification);	
	/**
	 * Toggle the modification attribute of this watchpoint
	 */	
	public void toggleModification();
	/**
	 * Generate the field associated with this watchpoint
	 */
	public IField getField();	
}

