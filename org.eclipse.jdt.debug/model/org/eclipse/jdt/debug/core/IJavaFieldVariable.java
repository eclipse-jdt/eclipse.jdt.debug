package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;

/**
 * A variable that contains the value of an instance or class variable.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see org.eclipse.debug.core.model.IVariable
 * @since 2.0
 */
public interface IJavaFieldVariable extends IJavaVariable {
	
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
	 * Returns the type that declares this variable.
	 * 
	 * @return the type that decalares this variable
	 */
	public IJavaType getDeclaringType();	
	

}


