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
import org.eclipse.debug.core.model.IIndexedValue;

/**
 * A value referencing an array on a target VM.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see IJavaValue
 * @since 2.0
 */

public interface IJavaArray extends IJavaObject, IIndexedValue {
	
	/**
	 * Returns the values contained in this array.
	 * 
	 * @return the values contained in this array
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	public IJavaValue[] getValues() throws DebugException;
		
	/**
	 * Returns the value at the given index in 
	 * this array.
	 * 
	 * @param index the index of the value to return
	 * @return the value at the given index
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 * @exception java.lang.IndexOutOfBoundsException if the index is 
	 *  not within the bounds of this array.
	 */
	public IJavaValue getValue(int index) throws DebugException;
	
	/**
	 * Returns the length of this array.
	 * 
	 * @return the length of this array
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul
	 */
	public int getLength() throws DebugException;
	
	/**
	 * Sets the value at the given index to the specified
	 * value.
	 * 
	 * @param index the index at which to assign a new value
	 * @param value the new value
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The given value is not compatible with the type of this
	 * array</li>
	 * </ul>
	 * @exception java.lang.IndexOutOfBoundsException if the index is 
	 *  not within the bounds of this array.
	 */
	public void setValue(int index, IJavaValue value) throws DebugException;

}

