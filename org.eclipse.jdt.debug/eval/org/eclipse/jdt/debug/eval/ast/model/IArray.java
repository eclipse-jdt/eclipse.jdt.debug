package org.eclipse.jdt.debug.eval.ast.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * An array on a virtual machine.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public interface IArray extends IObject {
	
	/**
	 * Returns the length of this array.
	 * 
	 * @return length
	 * @exception EvaluationException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM. The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	int getLength() throws CoreException;
	
	/**
	 * Returns the value at the given index in this array.
	 * 
	 * @param index index in this array
	 * @return value at the specified index
	 * @exception EvaluationException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM. The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	IValue getValue(int index) throws CoreException;
	
	/**
	 * Sets the value at the given index in this array.
	 * 
	 * @param index index in this array
	 * @param new value for the specified index
	 * @exception CoreException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM. The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	void setValue(int index, IValue value) throws CoreException;	

}

