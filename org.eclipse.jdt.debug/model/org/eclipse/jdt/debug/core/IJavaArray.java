package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * A Java array is a Java value that references
 * an array on the target VM.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaValue
 */

public interface IJavaArray extends IJavaObject {
	
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

