package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 * A Java variable is an extension of a regular variable,
 * providing support specific to the JDI debug model.
 * A Java variable is also available as an adapter from
 * variables originating for the JDI debug model.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.model.IVariable
 * @see org.eclipse.core.runtime.IAdaptable
 */
public interface IJavaVariable extends IVariable, IJavaModifiers {
	
	/**
	 * Returns whether this variable is declared as transient.
	 *
	 * @return whether this variable has been declared as transient
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	public boolean isTransient() throws DebugException;
	/**
	 * Returns whether this variable is declared as volatile.
	 * 
	 * @return whether this variable has been declared as volatile
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	public boolean isVolatile() throws DebugException;
	
	/**
	 * Returns the JNI-style signature for the declared type of this
	 * variable, or <code>null</code> if the type associated with the
	 * signature is not yet loaded in the target VM.
	 *
	 * @return signature, or <code>null</code> if not accessible
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The type associated with the signature is not yet loaded</li></ul>
	 */
	public String getSignature() throws DebugException;
	
	/**
	 * Returns the declared type of this variable.
	 * 
	 * @return the declared type of this variable
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The type associated with the signature is not yet loaded</li></ul>
	 */
	public IJavaType getJavaType() throws DebugException;	
}


