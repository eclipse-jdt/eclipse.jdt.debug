package org.eclipse.jdt.debug.eval.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * A object on a virtual machine.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public interface IObject extends IValue {
	
	/**
	 * Returns the field of this object with the specified name.
	 * Static and instance fields are available from this method.
	 * 
	 * @return the field of this object with the specified name
	 * @exception EvaluationException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	IVariable getField(String name, boolean superField) throws CoreException;
	
	IVariable getField(String name, String typeSignature) throws CoreException;

	/**
	 * Returns the result of sending the specified message to
	 * this object with the given arguments.
	 * 
	 * @param selector the selector of the method to be invoked
	 * @param signature the JNI style signature of the method to be invoked
	 * @param args the arguments of the method, which can be
	 * 	<code>null</code> or emtpy if there are none
	 * @param superSend whether method lookup will being in this object's
	 * 	superclass
	 * @param thread the thread in which to perform the message send
	 * @return the result of invoking the method
	 * @exception EvaluationException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The exception's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This class does not implement the specified method</li>
	 * <li>An exception occurrs on the target VM while invoking the
	 *  specified method</li>
	 * </ul>
	 */
	IValue sendMessage(String selector, String signature, IValue[] args, boolean superSend, IThread thread) throws CoreException;	

}

