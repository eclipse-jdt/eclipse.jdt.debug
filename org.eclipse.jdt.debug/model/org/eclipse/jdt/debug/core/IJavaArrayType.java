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
 * The type of an array on a Java debug target.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see IJavaValue
 * @since 2.0
 */

public interface IJavaArrayType extends IJavaReferenceType {

	/**
	 * Returns a new instance of an array of this type,
	 * with the specified length.
	 *
	 * @param size the length of the new array
	 * @return a new array of the specified length
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	public IJavaArray newInstance(int size) throws DebugException;

	/**
	 * Returns the type of the elements in this array.
	 *
	 * @return type
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM. The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	public IJavaType getComponentType() throws DebugException;

}

