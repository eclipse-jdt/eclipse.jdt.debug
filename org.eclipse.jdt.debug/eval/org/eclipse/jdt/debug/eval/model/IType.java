package org.eclipse.jdt.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * The type of a value on a virtual machine - a primitive
 * data type, class, interface, or array.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IType {
	/**
	 * Returns the JNI-style signature for this type.
	 *
	 * @return signature
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	String getSignature() throws CoreException;
		
	/**
	 * Returns the name of this type, for example
	 * <code>java.lang.String</code>.
	 *
	 * @return name
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	String getName() throws CoreException;		
}

