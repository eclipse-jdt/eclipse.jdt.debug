package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;

/**
 * Java watchpoints are an extension of java line breakpoints which
 * apply to a specific field. If the watchpoint is an access watchpoint,
 * it will suspend execution when its field is accessed. If the watchpoint
 * is a modification watchpoint, it will suspend execution when its field
 * is modified.
 * 
 * Clients are not intended to implement this interface.
 */
public interface IJavaWatchpoint extends IJavaLineBreakpoint {
	
	/**
	 * Returns whether this watchpoint is an access watchpoint
	 * 
	 * @return <code>true</code> if this is an access watchpoint
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */	
	public boolean isAccess() throws CoreException;
	/**
	 * Sets the access attribute of this watchpoint. If access is set to true
	 * and the watchpoint is disabled, enable the watchpoint. If both access and 
	 * modification are false, disable the watchpoint.
	 * 
	 * @param access whether or not this watchpoint will be an access watchpoint
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setAccess(boolean access) throws CoreException;
	/**
	 * Returns whether this watchpoint is a modification watchpoint
	 * 
	 * @return <code>true</code> if this is a modification watchpoint
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */		
	public boolean isModification() throws CoreException;
	/**
	 * Sets the modification attribute of this watchpoint. If modification is set to true
	 * and the watchpoint is disabled, enable the watchpoint. If both access and 
	 * modification are false, disable the watchpoint.
	 * 
	 * @param modification whether or not this watchpoint will be a
	 *  modification watchpoint
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setModification(boolean modification) throws CoreException;	
	/**
	 * Generate the field associated with this watchpoint
	 * 
	 * @return field the field on which this watchpoint is installed
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public IField getField() throws CoreException;	
	
	/**
	 * Returns whether this breakpoint last suspended in this target for an access
	 * (<code>true</code>) or modification (<code>false</code>).
	 * 
	 * @return true if this watchpoint last suspended in this target for an access
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public boolean isAccessSuspend(IJavaDebugTarget target);
}

