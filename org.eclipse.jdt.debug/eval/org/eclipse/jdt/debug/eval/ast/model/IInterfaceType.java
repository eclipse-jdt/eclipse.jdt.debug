package org.eclipse.jdt.debug.eval.ast.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
 
/**
 * The class of an object on a virtual machine.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IInterfaceType extends IType {	
	/**
	 * Returns the (static) field of this class with the specified name,
	 * or <code>null</code> if none exists.
	 * 
	 * @return the field of this class with the specified name, or
	 *  <code>null</code>
	 * @exception CoreException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	IVariable getField(String name) throws CoreException;	
	
	/**
	 * Returns the fully qualified name of this class.
	 * 
	 * @return the fully qualified name of this class
	 */
	String getName() throws CoreException;
	
	/**
	 * Returns the Class object for this type
	 */
	IObject getClassObject() throws CoreException;
}

