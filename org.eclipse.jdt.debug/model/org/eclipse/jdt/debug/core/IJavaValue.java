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
 * values originating from the JDI debug model.
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
	 * @deprecated use <code>getJavaType().getSignature()</code>
	 */
	public String getSignature() throws DebugException;
	
	/**
	 * Returns the length of this array, if this value is associated
	 * with an array type, or -1 otherwise.
	 *
	 * @return arrayLength, or -1 if this value is not an array
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 */
	public int getArrayLength() throws DebugException;
	
	/**
	 * Evaluates and returns the result of sending the message
	 * <code>toString()</code> to this value. If this value
	 * represents a primitive data type, the returned value
	 * is the same as that returned from <code>getValueString()</code>.
	 * The evaluation is performed in the specified thread.
	 * If the thread is not suspended, an exception is thrown.
	 *
	 * @param thread the thread used to perform the evaluation
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li></ul>
	 * @deprecated use <code>sendMessage(String, String, IJavaValue[], IJavaThread)</code>
	 */
	public String evaluateToString(IJavaThread thread) throws DebugException;
	
	/**
	 * Returns the type of this vlaue.
	 * 
	 * @return the type of this value
	 */
	public IJavaType getJavaType() throws DebugException;
	
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
	 * <li>This value does not represent an object (i.e. this value 
	 * represents a primitive data type or array). </li>
	 * <li>An exception occurrs while invoking the specified method</li>
	 * </ul>
	 */
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, boolean superSend) throws DebugException;	
}


