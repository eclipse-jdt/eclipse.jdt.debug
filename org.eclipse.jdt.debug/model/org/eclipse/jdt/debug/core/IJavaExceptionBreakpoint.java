package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;

/**
 * Java exception breakpoints are Java breakpoints that suspend
 * execution when an exception is thrown. If the breakpoint is
 * a caught exception breakpoint, it will suspend execution when
 * the associated exception is thrown and caught.If the breakpoint
 * is an uncaught exception breakpoint, it will suspend execution
 * when the associated exception is thrown and not caught.
 * 
 * Clients are not intended to implement this interface.
 */
public interface IJavaExceptionBreakpoint extends IJavaBreakpoint {
	/**
	 * Returns whether this breakpoint suspends execution when the
	 * associated exception is thrown and caught.
	 * 
	 * @return <code>true</code> if this is a caught exception
	 *  breakpoint
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public boolean isCaught() throws CoreException;
	/**
	 * Returns whether this breakpoint suspends execution when the
	 * associated exception is thrown and not caught.
	 * 
	 * @return <code>true</code> if this is an uncaught exception
	 *  breakpoint.
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public boolean isUncaught() throws CoreException;		
	/**
	 * Sets whether this breakpoint suspends execution when the associated
	 * exception is thrown and caught.
	 *
	 * @param caught whether or not this breakpoint suspends execution when the
	 *  associated exception is thrown and caught
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public void setCaught(boolean caught) throws CoreException;
	/**
	 * Sets whether this breakpoint suspends execution when the associated
	 * exception is thrown and not caught.
	 * 
	 * @param uncaught whether or not this breakpoint suspends execution when the
	 *  associated exception is thrown and not caught
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */	
	public void setUncaught(boolean uncaught) throws CoreException;
	/**
	 * Returns whether the exception associated with this breakpoint is a
	 * checked exception (i.e. compiler detected, not a runtime exception)
	 * 
	 * @return <code>true</code> if the exception associated with this breakpoint
	 *  is a checked exception
	 * @exception CoreException if a <code>CoreException</code> is
	 * 	thrown accessing this breakpoint's underlying marker
	 */
	public boolean isChecked() throws CoreException;
	
	/**
	 * Returns the fully qualified type name of the exception that
	 * last caused this breakpoint to suspend, of <code>null</code>
	 * if this breakpoint has not causes a thread to suspend. Note
	 * that this name may be a subtype of the exception that this
	 * breakpoint is associated with.
	 * 
	 * @return fully qualified exception name or <code>null</code>
	 */
	public String getExceptionTypeName();
	
	/**
	 * Sets the filters that will define the scope for the associated exception.
	 * 
	 * @param filters the array of filters to apply
	 * @param inclusive whether or not to apply the filters as inclusive or exclusive
	 * @exception CoreException if a <code>CoreException</code> is 
	 * thrown accessing this breakpoint's underlying marker
	 */
	public void setFilters(String[] filters, boolean inclusive) throws CoreException;
	
	/**
	 * Returns the filters that define the scope for the associated exception.
	 * 
	 * @return the array of defined filters
	 * @exception CoreException if a <code>CoreException</code> is 
	 * thrown accessing this breakpoint's underlying marker
	 */
	public String[] getFilters() throws CoreException;
	
	/**
	 * Returns whether or not to apply any filters as
	 * inclusive or exclusive.
	 * @return <code>true<code> if the filters are applied as inclusive
	 * @exception CoreException if a <code>CoreException</code> is 
	 * thrown accessing this breakpoint's underlying marker
	 */
	public boolean isInclusiveFiltered() throws CoreException;
}

