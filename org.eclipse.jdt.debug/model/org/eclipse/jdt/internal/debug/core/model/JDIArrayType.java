/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;


import java.text.MessageFormat;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Type;

/**
 * The type of an array
 */
public class JDIArrayType extends JDIReferenceType implements IJavaArrayType {

	/**
	 * Cosntructs a new array type on the given target referencing
	 * the specified array type.
	 */
	public JDIArrayType(JDIDebugTarget target, ArrayType type) {
		super(target, type);
	}
	/**
	 * @see IJavaArrayType#newInstance(int)
	 */
	public IJavaArray newInstance(int size) throws DebugException {
		try {
			ArrayReference ar = ((ArrayType)getUnderlyingType()).newInstance(size);
			return (IJavaArray)JDIValue.createValue(getDebugTarget(), ar);
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayType_exception_while_creating_new_instance_of_array, new String[] {e.toString()}), e); //$NON-NLS-1$
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
			Type type = ((ArrayType)getUnderlyingType()).componentType();
			return JDIType.createType(getDebugTarget(), type);
		} catch (ClassNotLoadedException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayType_exception_while_retrieving_component_type_of_array, new String[] {e.toString()}), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.JDIArrayType_exception_while_retrieving_component_type_of_array, new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		// execution will not reach this line as
		// an exception will be thrown
		return null;
	}

}

