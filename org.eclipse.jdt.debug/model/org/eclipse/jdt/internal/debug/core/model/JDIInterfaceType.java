/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper Steen Møller <jesper@selskabet.org> - Bug 430839
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.Value;

/**
 * The interface of an object in a debug target.
 */
public class JDIInterfaceType extends JDIReferenceType implements
		IJavaInterfaceType {

	/**
	 * Constructs a new interface type on the given target referencing the
	 * specified interface type.
	 */
	public JDIInterfaceType(JDIDebugTarget target, InterfaceType type) {
		super(target, type);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaInterfaceType#getImplementors()
	 */
	@Override
	public IJavaClassType[] getImplementors() throws DebugException {
		try {
			List<ClassType> implementorList = ((InterfaceType) getUnderlyingType())
					.implementors();
			List<JDIType> javaClassTypeList = new ArrayList<>(implementorList.size());
			Iterator<ClassType> iterator = implementorList.iterator();
			while (iterator.hasNext()) {
				ClassType classType = iterator.next();
				if (classType != null) {
					javaClassTypeList.add(JDIType.createType(
							getJavaDebugTarget(), classType));
				}
			}
			IJavaClassType[] javaClassTypeArray = new IJavaClassType[javaClassTypeList
					.size()];
			javaClassTypeArray = javaClassTypeList
					.toArray(javaClassTypeArray);
			return javaClassTypeArray;
		} catch (RuntimeException re) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIClassType_exception_while_retrieving_superclass,
							re.toString()), re);
		}
		return new IJavaClassType[0];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaInterfaceType#getSubInterfaces()
	 */
	@Override
	public IJavaInterfaceType[] getSubInterfaces() throws DebugException {
		try {
			List<InterfaceType> subList = ((InterfaceType) getUnderlyingType())
					.subinterfaces();
			List<JDIType> javaInterfaceTypeList = new ArrayList<>(subList.size());
			Iterator<InterfaceType> iterator = subList.iterator();
			while (iterator.hasNext()) {
				InterfaceType interfaceType = iterator.next();
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
	 * @see org.eclipse.jdt.debug.core.IJavaInterfaceType#getSuperInterfaces()
	 */
	@Override
	public IJavaInterfaceType[] getSuperInterfaces() throws DebugException {
		try {
			List<InterfaceType> superList = ((InterfaceType) getUnderlyingType())
					.superinterfaces();
			List<JDIType> javaInterfaceTypeList = new ArrayList<>(superList.size());
			Iterator<InterfaceType> iterator = superList.iterator();
			while (iterator.hasNext()) {
				InterfaceType interfaceType = iterator.next();
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
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaInterfaceType#sendMessage(java.lang.String,
	 * java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[],
	 * org.eclipse.jdt.debug.core.IJavaThread)
	 */
	@Override
	public IJavaValue sendMessage(String selector, String signature,
			IJavaValue[] args, IJavaThread thread) throws DebugException {
		if (getUnderlyingType() instanceof InterfaceType) {
			InterfaceType iface = (InterfaceType) getUnderlyingType();
			JDIThread javaThread = (JDIThread) thread;
			List<Value> arguments = convertArguments(args);
			Method method = null;
			try {
				List<Method> methods = iface.methodsByName(selector, signature);
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
			Value result = javaThread.invokeMethod(iface, method, arguments);
			return JDIValue.createValue(getJavaDebugTarget(), result);
		}
		requestFailed(
				JDIDebugModelMessages.JDIClassType_Type_is_not_a_class_type,
				null);
		// execution will not fall through to here,
		// as #requestFailed will throw an exception
		return null;
	}

}
