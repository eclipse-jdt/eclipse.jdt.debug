package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;

/**
 * A Java debug target is an extension of a regular debug
 * target, providing support specific to the JDI debug model.
 * A Java debug target is also available as an adapter from
 * debug targets originating from the JDI debug model.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IDebugTarget
 * @see org.eclipse.core.runtime.IAdaptable 
 */

public interface IJavaDebugTarget extends IDebugTarget {
	/**
	 * Searches for and returns a variable with the given name,
	 * or <code>null</code> if unable to resolve a variable with the name.
	 * <p>
	 * Variable lookup works only when a debug target has one or more
	 * threads suspended. Lookup is performed in each suspended thread,
	 * returning the first successful match, or <code>null</code> if no
	 * match if found. If this debug target has no suspended threads,
	 * <code>null</code> is returned.
	 * </p>
	 * @param variableName name of the variable
	 * @return a variable with the given name, or <code>null</code> if none
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	public abstract IVariable findVariable(String variableName) throws DebugException;
	
	/**
	 * Returns the type from this debug target with the given fully
	 * qualified name, or <code>null</code> of no type with the given
	 * name is loaded.
	 * 
	 * @param name fully qualified name of type, for example
	 * 	<code>java.lang.String</code>
	 * @return the type with the given name, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	public abstract IJavaType getJavaType(String name) throws DebugException;

	/**
	 * Returns an <code>IJavaValue</code> for the given boolean. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value a boolean from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(boolean value);	

	/**
	 * Returns an <code>IJavaValue</code> for the given byte. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value a byte from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(byte value);	

	/**
	 * Returns an <code>IJavaValue</code> for the given char. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value a char from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(char value);
	
	/**
	 * Returns an <code>IJavaValue</code> for the given double. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value a double from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(double value);	
	
	/**
	 * Returns an <code>IJavaValue</code> for the given float. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value a float from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(float value);		
				
	/**
	 * Returns an <code>IJavaValue</code> for the given int. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value an int from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(int value);
	
	/**
	 * Returns an <code>IJavaValue</code> for the given long. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value a long from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(long value);	
	
	/**
	 * Returns an <code>IJavaValue</code> for the given short. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value a short from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(short value);
	
	/**
	 * Returns an <code>IJavaValue</code> for the given string. This
	 * value can be used for setting and comparing against a value 
	 * retrieved from this debug target.
	 * 
	 * @param value a string from which to create a value
	 * @return the equivalent <code>IJavaValue</code>
	 */	
	public abstract IJavaValue newValue(String value);		
	
	/**
	 * Returns an <code>IJavaValue</code> that represents
	 * <code>null</code>. This value can be used for setting
	 * and comparing against a value retrieved from this debug target.
	 * 
	 * @return the null <code>IJavaValue</code>
	 */	
	public abstract IJavaValue nullValue();
	
	/**
	 * Returns an <code>IJavaValue</code> that represents
	 * <code>void</code>. This value can be used for setting
	 * and comparing against a value retrieved from this debug target.
	 * 
	 * @return the void <code>IJavaValue</code>
	 */	
	public abstract IJavaValue voidValue();
	/**
	 * Returns whether any of the threads associated with this debug target
	 * are running code in the VM that is out of synch with the code
	 * in the workspace.
	 * 
	 * @return whether this debug target is out of synch with the VM.
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public abstract boolean isOutOfSynch() throws DebugException;
	/**
	 * Returns whether any of the threads associated with this debug target
	 * may be running code in the VM that is out of synch with the code
	 * in the workspace.
	 * 
	 * @return whether this debug target may be out of synch with the VM.
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public abstract boolean mayBeOutOfSynch() throws DebugException;
	
	/**
	 * Returns whether this target supports hot code replace.
	 * 
	 * @return whether this target supports hot code replace
	 */
	public boolean supportsHotCodeReplace();
	
	/**
	 * Returns whether step filters are enabled
	 * in this target.
	 * 
	 * @return whether step filters are enabled
	 * in this target
	 */
	public abstract boolean isStepFiltersEnabled();
	
	/**
	 * Sets whether step filters are enabled in this
	 * target.
	 * 
	 * @param enabled whether step filters are enabled in this
	 * target
	 */
	public abstract void setStepFiltersEnabled(boolean enabled);
	
	/**
	 * Returns whether synthetic methods are filtered
	 * when stepping.
	 * 
	 * @return whether synthetic methods are filtered
	 * when stepping
	 */
	public abstract boolean isFilterSynthetics();
	
	/**
	 * Sets whether synthetic methods are filtered
	 * when stepping.
	 * 
	 * @param filter whether to synthetic methods are filtered
	 * when stepping
	 */
	public abstract void setFilterSynthetics(boolean filter);
	
	/**
	 * Returns whether static initializers are filtered
	 * when stepping.
	 * 
	 * @return whether static initializers are filtered
	 * when stepping
	 */
	public abstract boolean isFilterStaticInitializers();
	
	/**
	 * Sets whether to filter static initializers when
	 * stepping.
	 * 
	 * @param filter whether to filter static initializers when
	 * stepping
	 */
	public abstract void setFilterStaticInitializers(boolean filter);
	
	/**
	 * Returns whether constructors are filtered when stepping.
	 * 
	 * @return whether constructors are filtered when stepping
	 */
	public abstract boolean isFilterConstructors();
	
	/**
	 * Sets whether to filter constructors when stepping.
	 * 
	 * @param filter whether to filter constructors when stepping
	 */
	public abstract void setFilterConstructors(boolean filter);
	
	/**
	 * Returns the list of active step filters in this target.
	 * The list is a collection of Strings. Each filter is the 
	 * fully qualified name/pattern of a type/package to filter
	 * when stepping. For example <code>java.lang.*</code> or
	 * <code>java.lang.String</code>.
	 * 
	 * @return the list of active step filters, or <code>null</code>
	 */
	public abstract String[] getStepFilters();
	
	/**
	 * Sets the list of active step filters in this target.
	 * The list is a collection of Strings. Each filter is the 
	 * fully qualified name/pattern of a type/package to filter
	 * when stepping. For example <code>java.lang.*</code> or
	 * <code>java.lang.String</code>.
	 * 
	 * @param list active step filters, or <code>null</code>
	 */
	public abstract void setStepFilters(String[] list);
	
}