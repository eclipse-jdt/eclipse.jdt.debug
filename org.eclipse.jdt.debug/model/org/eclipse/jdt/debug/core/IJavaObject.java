package org.eclipse.jdt.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;

/**
 * A Java object is a Java value that references
 * an object on the target VM.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IJavaValue
 */
public interface IJavaObject extends IJavaValue {
	
	/**
	 * Returns the result of sending the specified message to this object
	 * with the given arguments in the specified thread. The given
	 * thread is resumed to perform the method invocation, and this
	 * method does not return until the method invocation is complete.
	 * Resuming the specified thread can result in breakpoints being
	 * hit, infinite loops, and deadlock.
	 * 
	 * @param selector the selector of the method to be invoked
	 * @param signature the JNI style signature of the method to be invoked
	 * @param args the arguments of the method, which can be
	 * 	<code>null</code> or emtpy if there are none
	 * @param thread the thread in which to invoke the method
	 * @param superSend <code>true</code> if the method lookup should 
	 *  begin in this object's superclass
	 * @return the result of invoking the method
	 * @exception DebugException if this method fails. Reasons include:<ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This object does not implement the specified method</li>
	 * <li>An exception occurs while invoking the specified method</li>
	 * </ul>
	 */
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, boolean superSend) throws DebugException;	
	/**
	 * Returns a variable representing the field in this object
	 * with the given name, or <code>null</code> if there is no
	 * field with the given name, or the name is ambiguous.
	 * 
	 * @param name field name
	 * @param superField whether or not to get the field in the superclass
	 *  of this objects.
	 * @return the variable representing the field, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public IJavaFieldVariable getField(String name, boolean superField) throws DebugException;
	/**
	 * Returns a variable representing the field in this object
	 * with the given name, or <code>null</code> if there is no
	 * field with the given name, or the name is ambiguous.
	 * 
	 * @param name field name
	 * @param typeSignature the name of the type in which the field
	 *  is defined
	 * @return the variable representing the field, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public IJavaFieldVariable getField(String name, String typeSignature) throws DebugException;
}


