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

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Type;

/**
 * The type of an array
 */
public class JDIArrayType extends JDIReferenceType implements IJavaArrayType {

	/**
	 * Constructs a new array type on the given target referencing the specified
	 * array type.
	 */
	public JDIArrayType(JDIDebugTarget target, ArrayType type) {
		super(target, type);
	}

	/**
	 * @see IJavaArrayType#newInstance(int)
	 */
	public IJavaArray newInstance(int size) throws DebugException {
		try {
			ArrayReference ar = ((ArrayType) getUnderlyingType())
					.newInstance(size);
			return (IJavaArray) JDIValue.createValue(getJavaDebugTarget(), ar);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayType_exception_while_creating_new_instance_of_array,
							e.toString()), e);
		}
		// execution will not reach this line as
		// an exception will be thrown
		return null;
	}

	/**
	 * @see IJavaArray#getComponentType()
	 */
	public IJavaType getComponentType() throws DebugException {
		try {
			Type type = ((ArrayType) getUnderlyingType()).componentType();
			return JDIType.createType(getJavaDebugTarget(), type);
		} catch (ClassNotLoadedException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayType_exception_while_retrieving_component_type_of_array,
							e.toString()), e);
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIArrayType_exception_while_retrieving_component_type_of_array,
							e.toString()), e);
		}
		// execution will not reach this line as
		// an exception will be thrown
		return null;
	}

}
