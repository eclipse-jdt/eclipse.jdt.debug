package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.core.IField;

/**
 * A breakpoint on a field. If a watchpoint is an access watchpoint,
 * it will suspend execution when its field is accessed. If a watchpoint
 * is a modification watchpoint, it will suspend execution when its field
 * is modified.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 2.0
 */
public interface IJavaWatchpoint extends IJavaLineBreakpoint {
	
	/**
	 * Returns whether this watchpoint is an access watchpoint
	 * 
	 * @return whether this is an access watchpoint
	 * @exception CoreException if unable to access the property
	 * 	on this breakpoint's underlying marker
	 */	
	public boolean isAccess() throws CoreException;
	/**
	 * Sets whether this breakpoint will suspend execution when its associated
	 * field is accessed. If true and this watchpoint is disabled, this watchpoint
	 * is automatically enabled. If both access and modification are false,
	 * this watchpoint is automatically disabled.
	 * 
	 * @param access whether to suspend on field access
	 * @exception CoreException if unable to set the property
	 * 	on this breakpoint's underlying marker
	 */
	public void setAccess(boolean access) throws CoreException;
	/**
	 * Returns whether this watchpoint is a modification watchpoint
	 * 
	 * @return whether this is a modification watchpoint
	 * @exception CoreException if unable to access the property
	 * 	on this breakpoint's underlying marker
	 */		
	public boolean isModification() throws CoreException;
	/**
	 * Sets whether this breakpoint will suspend execution when its associated
	 * field is modified. If true and this watchpoint is disabled, this watchpoint
	 * is automatically enabled. If both access and modification are false,
	 * this watchpoint is automatically disabled.
	 * 
	 * @param modification whether to suspend on field modification
	 * @exception CoreException if unable to set the property on
	 * 	this breakpoint's underlying marker
	 */
	public void setModification(boolean modification) throws CoreException;	
	/**
	 * Returns the name of the field associated with this watchpoint
	 * 
	 * @return field the name of the field on which this watchpoint is installed
	 * @exception CoreException if unable to access the property on
	 * 	this breakpoint's underlying marker
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

