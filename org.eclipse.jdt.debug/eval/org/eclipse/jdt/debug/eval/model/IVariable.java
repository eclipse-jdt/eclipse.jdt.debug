package org.eclipse.jdt.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * A variable on a virtual machine - local variable, argument,
 * or field of an object.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public interface IVariable {
	
	/**
	 * Returns the (declared) type of this variable.
	 * 
	 * @return type
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	IType getType() throws CoreException;


	/**
	 * Returns the current value of this variable.
	 * 
	 * @return value
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	IValue getValue() throws CoreException;
	
	/**
	 * Sets the value of this variable.
	 * 
	 * @param value the new value
	 * @exception CoreException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The given value is of an uncompatible type, or did not
	 * orginate from the same virtual machine as this variable.</li>
	 * </ul>
	 */
	void setValue(IValue vale) throws CoreException;	
	
	/**
	 * Returns the name of this variable.
	 * 
	 * @return the name of this variable
	 * @exception CoreException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	String getName() throws CoreException;
}

