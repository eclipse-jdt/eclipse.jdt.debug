/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Type;

/**
 * A type of an object or primitive data type in a debug target.
 */
public class JDIType extends JDIDebugElement implements IJavaType {

	/**
	 * Underlying type on target VM
	 */
	private Type fType;

	/**
	 * Constructs a new type based on the specified underlying type, in the
	 * given debug target
	 * 
	 * @param target
	 *            the debug target this type originated from
	 * @param type
	 *            underlying type on the target VM
	 */
	protected JDIType(JDIDebugTarget target, Type type) {
		super(target);
		setUnderlyingType(type);
	}

	/**
	 * Throws a new debug exception with the given status code.
	 * 
	 * @param message
	 *            Failure message
	 * @param e
	 *            Exception that has occurred (<code>can be null</code>)
	 * @param code
	 *            status code
	 * @throws DebugException
	 *             a new exception with given status code
	 */
	@Override
	public void requestFailed(String message, Throwable e, int code)
			throws DebugException {
		throwDebugException(message, code, e);
	}

	/**
	 * Throws a debug exception with the given message, error code, and
	 * underlying exception.
	 */
	@Override
	protected void throwDebugException(String message, int code,
			Throwable exception) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR,
				JDIDebugModel.getPluginIdentifier(), code, message, exception));
	}

	/**
	 * Throws a new debug exception with a status code of
	 * <code>TARGET_REQUEST_FAILED</code> with the given underlying exception.
	 * If the underlying exception is not a JDI exception, the original
	 * exception is thrown.
	 * 
	 * @param message
	 *            Failure message
	 * @param e
	 *            underlying exception that has occurred
	 * @throws DebugException
	 *             The exception with a status code of
	 *             <code>TARGET_REQUEST_FAILED</code>
	 */
	@Override
	public void targetRequestFailed(String message, RuntimeException e)
			throws DebugException {
		if (e == null
				|| e.getClass().getName().startsWith("com.sun.jdi") || e instanceof TimeoutException) { //$NON-NLS-1$
			requestFailed(message, e, DebugException.TARGET_REQUEST_FAILED);
		} else {
			throw e;
		}
	}

	/**
	 * Creates the appropriate kind of type, based on the specialized type.
	 */
	public static JDIType createType(JDIDebugTarget target, Type type) {
		if (type instanceof ArrayType) {
			return new JDIArrayType(target, (ArrayType) type);
		}
		if (type instanceof ClassType) {
			return new JDIClassType(target, (ClassType) type);
		}
		if (type instanceof InterfaceType) {
			return new JDIInterfaceType(target, (InterfaceType) type);
		}
		return new JDIType(target, type);
	}

	/**
	 * @see IJavaType#getSignature()
	 */
	public String getSignature() throws DebugException {
		try {
			return getUnderlyingType().signature();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIType_exception_while_retrieving_signature,
							e.toString()), e);
			// execution will not reach this line as
			// #targetRequestFailed will throw an exception
			return null;
		}
	}

	/**
	 * Returns the underlying type on the VM.
	 * 
	 * @return the underlying type on the VM
	 */
	public Type getUnderlyingType() {
		return fType;
	}

	/**
	 * Sets the underlying type on the VM.
	 * 
	 * @param type
	 *            the underlying type on the VM
	 */
	protected void setUnderlyingType(Type type) {
		fType = type;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getUnderlyingType().toString();
	}

	/**
	 * @see IJavaType#getName()
	 */
	public String getName() throws DebugException {
		try {
			return getUnderlyingType().name();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIType_exception_while_retrieving_type_name,
							e.toString()), e);
		}
		// execution will not fall through as an exception
		// will be thrown by the catch block
		return null;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		return object instanceof JDIType
				&& fType.equals(((JDIType) object).fType);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fType.hashCode();
	}

}
