package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * A Java value is an extension of a regular value,
 * providing support specific to the JDI debug model.
 * A Java value is also available as an adapter from
 * values originating from the JDI debug model. A value
 * represents an object, primitive data type or array.
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
	 * value, or <code>null</code> if the value is <code>null</code>.
	 *
	 * @return signature, or <code>null</code> if signature is <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The type associated with the signature is not yet loaded</li></ul>
	 */
	public String getSignature() throws DebugException;
		
	/**
	 * Returns the type of this value, or <code>null</code>
	 * if this value represents the <code>null</code> value
	 * 
	 * @return the type of this value, or <code>null</code>
	 * if this value represents the <code>null</code> value
	 */
	public IJavaType getJavaType() throws DebugException;
	
}


