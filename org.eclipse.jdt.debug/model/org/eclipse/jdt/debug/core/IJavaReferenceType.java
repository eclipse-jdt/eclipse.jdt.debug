/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.core;

import org.eclipse.debug.core.DebugException;

/**
 * Represents the type of an object in a virtual machine - including classes,
 * interfaces and array types.
 * 
 * @since 3.0
 */
public interface IJavaReferenceType extends IJavaType {
	
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

}
