package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * An object, primitive data type, or array, on a Java virtual machine.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see org.eclipse.debug.core.model.IValue
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
	 * 
	 * @since 2.0
	 */
	public IJavaType getJavaType() throws DebugException;
	
}


