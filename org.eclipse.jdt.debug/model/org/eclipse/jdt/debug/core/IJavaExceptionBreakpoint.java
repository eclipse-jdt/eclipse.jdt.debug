package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;

/**
 * A breakpoint that suspends execution when a corresponding exception
 * is thrown in a target VM. An exception breakpoint can be configured
 * to suspend execution when the corresponding exception is thrown in
 * a caught or uncaught location. As well, the location can be filtered
 * inclusively or exclusively by type name patterns.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 2.0
 */
public interface IJavaExceptionBreakpoint extends IJavaBreakpoint {
	/**
	 * Returns whether this breakpoint suspends execution when the
	 * associated exception is thrown in a caught location (i.e. in
	 * a try/catch statement).
	 * 
	 * @return <code>true</code> if this is a caught exception
	 *  breakpoint
	 * @exception CoreException if unable to access the property from
	 * 	this breakpoint's underlying marker
	 */
	public boolean isCaught() throws CoreException;
	/**
	 * Returns whether this breakpoint suspends execution when the
	 * associated exception is thrown in an uncaught location (i.e. not
	 * caught by a try/catch statement).
	 * 
	 * @return <code>true</code> if this is an uncaught exception
	 *  breakpoint.
	 * @exception CoreException if unable to access the property from
	 * 	this breakpoint's underlying marker
	 */
	public boolean isUncaught() throws CoreException;		
	/**
	 * Sets whether this breakpoint suspends execution when the associated
	 * exception is thrown in a caught location (i.e. in a try/catch
	 * statement).
	 *
	 * @param caught whether or not this breakpoint suspends execution when the
	 *  associated exception is thrown in a caught location
	 * @exception CoreException if unable to set the property on
	 * 	this breakpoint's underlying marker
	 */
	public void setCaught(boolean caught) throws CoreException;
	/**
	 * Sets whether this breakpoint suspends execution when the associated
	 * exception is thrown in an uncaught location.
	 * 
	 * @param uncaught whether or not this breakpoint suspends execution when the
	 *  associated exception is thrown in an uncaught location
	 * @exception CoreException if unable to set the property
	 * 	on this breakpoint's underlying marker
	 */	
	public void setUncaught(boolean uncaught) throws CoreException;
	/**
	 * Returns whether the exception associated with this breakpoint is a
	 * checked exception (i.e. compiler detected, not a runtime exception)
	 * 
	 * @return <code>true</code> if the exception associated with this breakpoint
	 *  is a checked exception
	 * @exception CoreException if unable to access the property from
	 * 	this breakpoint's underlying marker
	 */
	public boolean isChecked() throws CoreException;
	
	/**
	 * Returns the fully qualified type name of the exception that
	 * last caused this breakpoint to suspend, of <code>null</code>
	 * if this breakpoint has not caused a thread to suspend. Note
	 * that this name may be a subtype of the exception that this
	 * breakpoint is associated with.
	 * 
	 * @return fully qualified exception name or <code>null</code>
	 */
	public String getExceptionTypeName();
	
	/**
	 * Sets the filters that will define the scope for the associated exception.
	 * Filters are a collection of strings of type name prefixes.
	 * Default packages should be specified as the empty string.
	 * 
	 * @param filters the array of filters to apply
	 * @param inclusive whether or not to apply the filters as inclusive or exclusive
	 * @exception CoreException if unable to set the property on 
	 *  this breakpoint's underlying marker
	 */
	public void setFilters(String[] filters, boolean inclusive) throws CoreException;
	
	/**
	 * Returns the filters that define the scope for the associated exception.
	 * Filters are a collection of strings of type name prefixes.
	 * 
	 * @return the array of defined filters
	 * @exception CoreException if unable to access the property on
	 *  this breakpoint's underlying marker
	 */
	public String[] getFilters() throws CoreException;
	
	/**
	 * Returns whether to apply any filters as inclusive or exclusive.
	 * @return <code>true<code> if the filters are applied as inclusive,
	 *  <code>false</code> if the filters are applied as exclusive
	 * @exception CoreException if unable to access the property on 
	 *  this breakpoint's underlying marker
	 */
	public boolean isInclusiveFiltered() throws CoreException;
}

