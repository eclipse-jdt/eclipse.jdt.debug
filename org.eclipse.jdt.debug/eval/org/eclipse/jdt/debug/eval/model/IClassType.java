package org.eclipse.jdt.debug.eval.model;

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
public interface IClassType extends IType {	
	/**
	 * Returns a new instance of this class by invoking the
	 * constructor with the specified signature and arguments. 
	 * 
	 * @param signature the JNI style signature of the cosntructor to
	 *  be invoked
	 * @param args the arguments of the constructor, which
	 * 	can be <code>null</code> or emtpy if there are none
	 * @param thread the thread in which to construct the new object
	 * @return the result of invoking the constructor
	 * @exception EvaluationException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This class does not implement the specified constructor</li>
	 * <li>An exception occurrs on the target VM while invoking the
	 *  specified constructor</li>
	 * </ul>
	 */
	IObject newInstance(String signature, IValue[] args, IThread thread) throws CoreException;	
	
	/**
	 * Returns the result of sending the specified (static) message to
	 * this class with the given arguments.
	 * 
	 * @param selector the selector of the method to be invoked
	 * @param signature the JNI style signature of the method to be invoked
	 * @param args the arguments of the method, which can be
	 * 	<code>null</code> or emtpy if there are none
	 * @param thread the thread in which to perform the message send
	 * @return the result of invoking the method
	 * @exception CoreException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This class does not implement the specified method</li>
	 * <li>An exception occurrs on the target VM while invoking the
	 *  specified method</li>
	 * </ul>
	 */
	IValue sendMessage(String selector, String signature, IValue[] args, IThread thread) throws CoreException;		
	
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

