package org.eclipse.jdt.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * The type of an array on a virtual machine.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public interface IArrayType extends IType {

	/**
	 * Returns the type of the elements in this array.
	 * 
	 * @return type
	 * @exception EvaluationException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM. The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	IType getComponentType() throws CoreException;
	
	/**
	 * Creates a new array of the same type as this array with the\
	 * given length.
	 * 
	 * @param length the length of the new array
	 * @exception CoreException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM. The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	IArray newArray(int length) throws CoreException;
	
}

