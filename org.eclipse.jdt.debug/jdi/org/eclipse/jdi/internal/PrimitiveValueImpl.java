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
package org.eclipse.jdi.internal;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.sun.jdi.InternalException;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class PrimitiveValueImpl extends ValueImpl implements PrimitiveValue, Comparable {
	/** Primitive value in wrapper. */
	Object fValue;
	
	/**
	 * Creates new ValueImpl.
	 */
	public PrimitiveValueImpl(String description, VirtualMachineImpl vmImpl, Object value) {
		super(description, vmImpl);
		fValue = value;
	}
	
	/**
	 * @return Returns Primitive Value converted to required type.
	 */
	public boolean booleanValue() {
		if (fValue instanceof Boolean)
			return ((Boolean)fValue).booleanValue();
		else if (fValue instanceof Character)
			return ((Character)fValue).charValue() != 0;
		else return ((Number)fValue).doubleValue() != 0;
	}
	
	/**
	 * @return Returns Primitive Value converted to required type.
	 */
	public char charValue() {
		if (fValue instanceof Boolean)
			return ((Boolean)fValue).booleanValue() ? (char)1 : (char)0;
		else if (fValue instanceof Character)
			return ((Character)fValue).charValue();
		else return (char)((Number)fValue).intValue();
	}

	/**
	 * @return Returns Primitive Value converted to required type.
	 */
	public byte byteValue() {
		if (fValue instanceof Boolean)
			return ((Boolean)fValue).booleanValue() ? (byte)1 : (byte)0;
		else if (fValue instanceof Character)
			return (byte)((Character)fValue).charValue();
		else return ((Number)fValue).byteValue();
	}

	/**
	 * @return Returns Primitive Value converted to required type.
	 */
	public double doubleValue() {
		if (fValue instanceof Boolean)
			return ((Boolean)fValue).booleanValue() ? (double)1 : (double)0;
		else if (fValue instanceof Character)
			return ((Character)fValue).charValue();
		else return ((Number)fValue).doubleValue();
	}

	/**
	 * @return Returns Primitive Value converted to required type.
	 */
	public float floatValue() {
		if (fValue instanceof Boolean)
			return ((Boolean)fValue).booleanValue() ? (float)1 : (float)0;
		else if (fValue instanceof Character)
			return ((Character)fValue).charValue();
		else return ((Number)fValue).floatValue();
	}

	/**
	 * @return Returns Primitive Value converted to required type.
	 */
	public int intValue() {
		if (fValue instanceof Boolean)
			return ((Boolean)fValue).booleanValue() ? (int)1 : (int)0;
		else if (fValue instanceof Character)
			return ((Character)fValue).charValue();
		else return ((Number)fValue).intValue();
	}

	/**
	 * @return Returns Primitive Value converted to required type.
	 */
	public long longValue() {
		if (fValue instanceof Boolean)
			return ((Boolean)fValue).booleanValue() ? (long)1 : (long)0;
		else if (fValue instanceof Character)
			return ((Character)fValue).charValue();
		else return ((Number)fValue).longValue();
	}

	/**
	 * @return Returns Primitive Value converted to required type.
	 */
	public short shortValue() {
		if (fValue instanceof Boolean)
			return ((Boolean)fValue).booleanValue() ? (short)1 : (short)0;
		else if (fValue instanceof Character)
			return (short)((Character)fValue).charValue();
		else return ((Number)fValue).shortValue();
	}
	
	/**
	 * @return Returns true if two values are equal.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		return object != null && object.getClass().equals(this.getClass()) && fValue.equals(((PrimitiveValueImpl)object).fValue);
	}
	
	/**
	 * @return Returns a has code for this object.
	 * @see java.lang.Object#hashCode
	 */
	public int hashCode() {
		return fValue.hashCode();
 	}
	
	/**
	 * Compares this object with the specified object for order.
	 * Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     * 
     * May throw a ClassCastException if obj is not comparable. This is in accordance
     * with Java 1.4 compareTo(Object) methods.
	 */
	public int compareTo(Object obj) {
		if (fValue instanceof Character)
			return ((Character)fValue).compareTo((Character) obj);
		else if (fValue instanceof Byte)
			return ((Byte)fValue).compareTo((Byte) obj);
		else if (fValue instanceof Double)
			return ((Double)fValue).compareTo((Double) obj);
		else if (fValue instanceof Float)
			return ((Float)fValue).compareTo((Float) obj);
		else if (fValue instanceof Integer)
			return ((Integer)fValue).compareTo((Integer) obj);
		else if (fValue instanceof Long)
			return ((Long)fValue).compareTo((Long) obj);
		else if (fValue instanceof Short)
			return ((Short)fValue).compareTo((Short) obj);
			
		throw new InternalException(JDIMessages.PrimitiveValueImpl_Invalid_Primitive_Value_encountered_1); 
		
	}
	
	/**
	 * @return Returns description of Mirror object.
	 */
	public String toString() {
		return fValue.toString();
	}

	/**
	 * Writes value without value tag.
	 */
	public abstract void write(MirrorImpl target, DataOutputStream out) throws IOException;
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static PrimitiveValueImpl readWithoutTag(MirrorImpl target, PrimitiveType type, DataInputStream in) throws IOException {	
		switch (((PrimitiveTypeImpl)type).tag()) {
			case 0:
				return null;
			case BooleanValueImpl.tag:
				return BooleanValueImpl.read(target, in);
			case ByteValueImpl.tag:
				return ByteValueImpl.read(target, in);
			case CharValueImpl.tag:
				return CharValueImpl.read(target, in);
			case DoubleValueImpl.tag:
				return DoubleValueImpl.read(target, in);
			case FloatValueImpl.tag:
				return FloatValueImpl.read(target, in);
			case IntegerValueImpl.tag:
				return IntegerValueImpl.read(target, in);
			case LongValueImpl.tag:
				return LongValueImpl.read(target, in);
			case ShortValueImpl.tag:
				return ShortValueImpl.read(target, in);
		}
		throw new InternalException(JDIMessages.PrimitiveValueImpl_Invalid_Primitive_Value_tag_encountered___2 + type); 
	}
}
