package org.eclipse.jdt.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * A Java value is an extension of a regular value,
 * providing support specific to the JDI debug model.
 * A Java value is also available as an adapter from
 * values originating from the JDI debug model.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.model.IValue
 * @see org.eclipse.core.runtime.IAdaptable
 */
public interface IJavaValue extends IValue {
	/**
	 * Returns the JNI-style signature for the type of this
	 * value, or <code>null</code> if the type associated with the
	 * signature is not yet loaded in the target VM, or the value
	 * is <code>null</code>.
	 *
	 * @return signature, or <code>null</code> if not accessible
	 * @exception DebugException if unable to retrive this
	 *   value's signature on the target 
	 */
	public String getSignature() throws DebugException;
	
	/**
	 * Returns the length of this array, if this value is associated
	 * with an array type, or -1 if it is not.
	 *
	 * @return arrayLength, or -1 if this value is not an array
	 * @exception DebugException if unable to determine the length
	 *   of this value's associatd array
	 */
	public int getArrayLength() throws DebugException;
	
	/**
	 * Evaluates and returns the result of sending the message
	 * <code>toString()</code> to this value. If this value
	 * represents a primitive data type, the returned value
	 * is the same as that returned from <code>getValueString()</code>.
	 * The evaluation is performed in the thread that this value
	 * orginiated from. If the thread is not suspended, an exception
	 * is thrown.
	 *
	 * @exception DebugException if unable to perform the evaluation,
	 * 	or if an exception occurrs while performing the evaluation
	 */
	public String evaluateToString() throws DebugException;
}


