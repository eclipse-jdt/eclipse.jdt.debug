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
 * An interface an object implements on a Java debug target.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see IJavaValue
 * @since 2.0
 */
public interface IJavaInterfaceType  extends IJavaType {
		
	/**
	 * Returns a variable representing the static field in this interface
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
	 * Returns the class object associated with this interface type.
	 * 
	 * @return the class object associated with this interface type
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	public IJavaClassObject getClassObject() throws DebugException;
}

