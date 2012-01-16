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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

/**
 * The class of an object in a debug target.
 */
public class JDIClassType extends JDIReferenceType implements IJavaClassType {

	/**
	 * Constructs a new class type on the given target referencing the specified
	 * class type.
	 */
	public JDIClassType(JDIDebugTarget target, ClassType type) {
		super(target, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaClassType#newInstance(java.lang.String,
	 * org.eclipse.jdt.debug.core.IJavaValue[],
	 * org.eclipse.jdt.debug.core.IJavaThread)
	 */
	public IJavaObject newInstance(String signature, IJavaValue[] args,
			IJavaThread thread) throws DebugException {
		if (getUnderlyingType() instanceof ClassType) {
			ClassType clazz = (ClassType) getUnderlyingType();
			JDIThread javaThread = (JDIThread) thread;
			List<Value> arguments = convertArguments(args);
			Method method = null;
			try {
				List<Method> methods = clazz.methodsByName("<init>", signature); //$NON-NLS-1$
				if (methods.isEmpty()) {
					requestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIClassType_Type_does_not_implement_cosntructor,
									signature), null);
				} else {
					method = methods.get(0);
				}
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIClassType_exception_while_performing_method_lookup_for_constructor,
								e.toString(), signature), e);
			}
			ObjectReference result = javaThread.newInstance(clazz, method,
					arguments);
			return (IJavaObject) JDIValue.createValue(getJavaDebugTarget(),
					result);
		}
		requestFailed(
				JDIDebugModelMessages.JDIClassType_Type_is_not_a_class_type,
				null);
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaClassType#sendMessage(java.lang.String,
	 * java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[],
	 * org.eclipse.jdt.debug.core.IJavaThread)
	 */
	public IJavaValue sendMessage(String selector, String signature,
			IJavaValue[] args, IJavaThread thread) throws DebugException {
		if (getUnderlyingType() instanceof ClassType) {
			ClassType clazz = (ClassType) getUnderlyingType();
			JDIThread javaThread = (JDIThread) thread;
			List<Value> arguments = convertArguments(args);
			Method method = null;
			try {
				List<Method> methods = clazz.methodsByName(selector, signature);
				if (methods.isEmpty()) {
					requestFailed(
							MessageFormat.format(
									JDIDebugModelMessages.JDIClassType_Type_does_not_implement_selector,
									selector, signature), null);
				} else {
					method = methods.get(0);
				}
			} catch (RuntimeException e) {
				targetRequestFailed(
						MessageFormat.format(
								JDIDebugModelMessages.JDIClassType_exception_while_performing_method_lookup_for_selector,
								e.toString(), selector, signature), e);
			}
			Value result = javaThread.invokeMethod(clazz, null, method,
					arguments, false);
			return JDIValue.createValue(getJavaDebugTarget(), result);
		}
		requestFailed(
				JDIDebugModelMessages.JDIClassType_Type_is_not_a_class_type,
				null);
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}

	/**
	 * Utility method to convert argument array to an argument list.
	 * 
	 * @param args
	 *            array of arguments, as <code>IJavaValue</code>s, possibly
	 *            <code>null</code> or empty
	 * @return a list of underlying <code>Value</code>s
	 */
	protected List<Value> convertArguments(IJavaValue[] args) {
		List<Value> arguments = null;
		if (args == null) {
			arguments = Collections.EMPTY_LIST;
		} else {
			arguments = new ArrayList<Value>(args.length);
			for (IJavaValue arg : args) {
				arguments.add(((JDIValue) arg).getUnderlyingValue());
			}
		}
		return arguments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaClassType#getSuperclass()
	 */
	public IJavaClassType getSuperclass() throws DebugException {
		try {
			ClassType superclazz = ((ClassType) getUnderlyingType())
					.superclass();
			if (superclazz != null) {
				return (IJavaClassType) JDIType.createType(
						getJavaDebugTarget(), superclazz);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIClassType_exception_while_retrieving_superclass,
							e.toString()), e);
		}
		// it is possible to return null
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaClassType#getAllInterfaces()
	 */
	public IJavaInterfaceType[] getAllInterfaces() throws DebugException {
		try {
			List<InterfaceType> interfaceList = ((ClassType) getUnderlyingType())
					.allInterfaces();
			List<JDIType> javaInterfaceTypeList = new ArrayList<JDIType>(interfaceList.size());
			for(InterfaceType interfaceType : interfaceList) {
				if (interfaceType != null) {
					javaInterfaceTypeList.add(JDIType.createType(
							getJavaDebugTarget(), interfaceType));
				}
			}
			IJavaInterfaceType[] javaInterfaceTypeArray = new IJavaInterfaceType[javaInterfaceTypeList
					.size()];
			javaInterfaceTypeArray = javaInterfaceTypeList
					.toArray(javaInterfaceTypeArray);
			return javaInterfaceTypeArray;
		} catch (RuntimeException re) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIClassType_exception_while_retrieving_superclass,
							re.toString()), re);
		}
		return new IJavaInterfaceType[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaClassType#getInterfaces()
	 */
	public IJavaInterfaceType[] getInterfaces() throws DebugException {
		try {
			List<InterfaceType> interfaceList = ((ClassType) getUnderlyingType()).interfaces();
			List<JDIType> javaInterfaceTypeList = new ArrayList<JDIType>(interfaceList.size());
			for(InterfaceType interfaceType : interfaceList) {
				if (interfaceType != null) {
					javaInterfaceTypeList.add(JDIType.createType(
							getJavaDebugTarget(), interfaceType));
				}
			}
			IJavaInterfaceType[] javaInterfaceTypeArray = new IJavaInterfaceType[javaInterfaceTypeList
					.size()];
			javaInterfaceTypeArray = javaInterfaceTypeList
					.toArray(javaInterfaceTypeArray);
			return javaInterfaceTypeArray;
		} catch (RuntimeException re) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIClassType_exception_while_retrieving_superclass,
							re.toString()), re);
		}
		return new IJavaInterfaceType[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaClassType#isEnum()
	 */
	public boolean isEnum() {
		return ((ClassType) getReferenceType()).isEnum();
	}

}
