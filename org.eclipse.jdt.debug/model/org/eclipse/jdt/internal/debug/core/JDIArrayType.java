package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Type;

import java.text.MessageFormat;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;
 
/**
 * The type of an array
 */
public class JDIArrayType extends JDIType implements IJavaArrayType {

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
			getDebugTarget().targetRequestFailed(MessageFormat.format("{0} occurred while creating new instance of array.", new String[] {e.toString()}), e);
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
			return JDIType.createType((JDIDebugTarget)getDebugTarget(), type);
		} catch (ClassNotLoadedException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format("{0} occurred while retrieving component type of array.", new String[] {e.toString()}), e);
		} catch (RuntimeException e) {
			getDebugTarget().targetRequestFailed(MessageFormat.format("{0} occurred while retrieving component type of array.", new String[] {e.toString()}), e);
		}
		// execution will not reach this line as
		// an exception will be thrown
		return null;
	}
}

