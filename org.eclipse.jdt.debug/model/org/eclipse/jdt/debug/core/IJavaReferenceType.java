/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.core;

import org.eclipse.debug.core.DebugException;

/**
 * Represents the type of an object in a virtual machine - including classes,
 * interfaces and array types.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 3.0
 */
public interface IJavaReferenceType extends IJavaType {
	
	/**
	 * Returns a variable representing the static field in this type
	 * with the given name, or <code>null</code> if there is no
	 * field with the given name, or the name is ambiguous.
	 * 
	 * @param name field name
	 * @return the variable representing the static field, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	public IJavaFieldVariable getField(String name) throws DebugException;	
	
	/**
	 * Returns the class object associated with this type.
	 * 
	 * @return the class object associated with this type
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	public IJavaClassObject getClassObject() throws DebugException;
	
	/**
	 * Returns a collection of strata available for this type.
	 * 
	 * @return a collection of strata available for this type
	 * @throws DebugException if unable to retrive available strata
	 */
	public String[] getAvailableStrata() throws DebugException;
	
	/**
	 * Returns the default stratum for this type.
	 * 
	 * @return the default stratum for this type
	 * @throws DebugException if unable to retrieve the default stratum
	 */
	public String getDefaultStratum() throws DebugException;
	
	/**
	 * Returns a collection of the names of the fields declared in this type.
	 * 
	 * @return a collection of the names of the field declared in this type
	 * @throws DebugException if unable to retrieve declared field names
	 */
	public String[] getDeclaredFieldNames() throws DebugException;
	
	/**
	 * Returns a collection of the names of all of the fields declared in this
	 * type, all of its superclasses, implemented interfaces and super interfaces.
	 * 
	 * @return a collection of the names of all of the fields declared in this
	 * type, all of its superclasses, implemented interfaces and super interfaces
	 * @throws DebugException if unable to retrieve field names
	 */
	public String[] getAllFieldNames() throws DebugException;
    
    /**
     * Returns the class loader object that loaded the class corresponding to this
     * type.
     *  
     * @return the class loader object that loaded the class corresponding to this
     *   type
     * @throws DebugException
     * @since 3.1
     */
    public IJavaObject getClassLoaderObject() throws DebugException;
    
	/**
	 * Returns the generic signature as defined in the JVM
	 * specification for this type.
	 * Returns <code>null</code> if this type is not a generic type.
	 *
	 * @return signature, or <code>null</code> if generic signature not available
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li><ul>
	 * @since 3.1
	 */
    public String getGenericSignature() throws DebugException;
	
}
