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
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;

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
	public IJavaClassType[] getImplementors() throws DebugException {
		try {
			List<ClassType> implementorList = ((InterfaceType) getUnderlyingType())
					.implementors();
			List<JDIType> javaClassTypeList = new ArrayList<JDIType>(implementorList.size());
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
	public IJavaInterfaceType[] getSubInterfaces() throws DebugException {
		try {
			List<InterfaceType> subList = ((InterfaceType) getUnderlyingType())
					.subinterfaces();
			List<JDIType> javaInterfaceTypeList = new ArrayList<JDIType>(subList.size());
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
	public IJavaInterfaceType[] getSuperInterfaces() throws DebugException {
		try {
			List<InterfaceType> superList = ((InterfaceType) getUnderlyingType())
					.superinterfaces();
			List<JDIType> javaInterfaceTypeList = new ArrayList<JDIType>(superList.size());
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

}
