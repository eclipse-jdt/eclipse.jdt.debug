package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
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
	 * Sets whether this breakpoint will suspend execution when its associated
	 * field is accessed. If true and this watchpoint is disabled, this watchpoint
	 * is enabled. If both access and modification are false, disable this watchpoint.
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
	 * Sets whether this breakpoint will suspend execution when its associated
	 * field is modified. If true and this watchpoint is disabled, this watchpoint
	 * is enabled. If both access and modification are false, disable this watchpoint.
	 * 
	 * @param modification whether or not this watchpoint will be a
	 *  modification watchpoint
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setModification(boolean modification) throws CoreException;	
	/**
	 * Returns the name of the field associated with this watchpoint
	 * 
	 * @return field the name of the field on which this watchpoint is installed
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public String getFieldName() throws CoreException;	
	
	/**
	 * Returns whether this breakpoint last suspended in this target due to an access
	 * (<code>true</code>) or modification (<code>false</code>).
	 * 
	 * @return <code>true</code> if this watchpoint last suspended the given
	 *  target due to a field access; <code>false</code> if this watchpoint last
	 *  suspended the given target due to a modification access or if this
	 *  watchpoint hasn't suspended the given target.
	 */
	public boolean isAccessSuspend(IDebugTarget target);
}

