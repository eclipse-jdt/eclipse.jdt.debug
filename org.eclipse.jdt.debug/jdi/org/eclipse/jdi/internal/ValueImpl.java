package org.eclipse.jdi.internal;

/**********************************************************************
Copyright (c) 2000, 2001, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpObjectID;

import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class ValueImpl extends MirrorImpl implements Value {
	/**
	 * Creates new ValueImpl.
	 */
	protected ValueImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}

	/**
	 * @returns type of value.
	 */
	public abstract Type type();

	/**
	 * @returns type of value.
	 */
	public abstract byte getTag();
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ValueImpl readWithTag(MirrorImpl target, DataInputStream in) throws IOException {
		byte tag = target.readByte("object tag", JdwpID.tagMap(), in); //$NON-NLS-1$
	   	return readWithoutTag(target, tag, in);
	}
	
	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ValueImpl readWithoutTag(MirrorImpl target, int type, DataInputStream in) throws IOException {	
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		// See also ArrayReference Impl.
		switch(type) {
			case ArrayReferenceImpl.tag:
				return ArrayReferenceImpl.read(target, in);
			case ClassLoaderReferenceImpl.tag:
				return ClassLoaderReferenceImpl.read(target, in);
			case ClassObjectReferenceImpl.tag:
				return ClassObjectReferenceImpl.read(target, in);
			case StringReferenceImpl.tag:
				return StringReferenceImpl.read(target, in);
			case ObjectReferenceImpl.tag:
				return ObjectReferenceImpl.readObjectRefWithoutTag(target, in);
			case ThreadGroupReferenceImpl.tag:
				return ThreadGroupReferenceImpl.read(target, in);
			case ThreadReferenceImpl.tag:
				return ThreadReferenceImpl.read(target, in);
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
			case VoidValueImpl.tag:
				return new VoidValueImpl(vmImpl);
			case 0:
				return null;
			default:
				throw new InternalException(JDIMessages.getString("ValueImpl.Invalid_Value_tag_encountered___1") + type); //$NON-NLS-1$
		}
	}

	/**
	 * Writes value with value tag.
	 */
	public void writeWithTag(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeByte(getTag(), "tag", JdwpID.tagMap(), out); //$NON-NLS-1$
		write(target, out);
	}

	/**
	 * Writes value without value tag.
	 */
	public abstract void write(MirrorImpl target, DataOutputStream out) throws IOException;
	
	/**
	 * Writes null value without value tag.
	 */
	public static void writeNull(MirrorImpl target, DataOutputStream out) throws IOException {
		JdwpObjectID nullID = new JdwpObjectID(target.virtualMachineImpl());
		nullID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("objectReference", nullID.value()); //$NON-NLS-1$
	}
	
	/**
	 * Writes null value with value tag.
	 */
	public static void writeNullWithTag(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeByte(ObjectReferenceImpl.tag, "tag", JdwpID.tagMap(), out); //$NON-NLS-1$
		writeNull(target, out);
	}

	/**
	 * Check the type and the vm of each values, according to the associated type.
	 * For primitive values, convert the value for match the given type if needed.
	 * The two list must have the same size.
	 * @return the (converted) values.
	 * @see checkValue(Value, Type, VirtualMachineImpl)
	 */
	protected static List checkValues(List values, List types, VirtualMachineImpl vm) throws InvalidTypeException {
		List result= new ArrayList(values.size());
		Iterator iterValues= values.iterator();
		Iterator iterTypes= types.iterator();
		while (iterValues.hasNext()) {
			Value value= (Value)iterValues.next();
			Type type= (Type)iterTypes.next();
			result.add(checkValue(value, type, vm));
		}
		return result;
	}

	/**
	 * Check the type and the vm of the given value.
	 * In case of primitive value, the value is converted if needed.
	 * @return the (converted) value.
	 * @throws InvalidTypeException if the value and the type are not both
	 * primitive or not primitive or, in case of primitive, if the given
	 * value is no assignment compatible with the given type.
	 * @see checkPrimitiveValue(PrimitiveValueImpl, PrimitiveTypeImpl, PrimitiveTypeImpl)	 */
	protected static ValueImpl checkValue(Value value, Type type, VirtualMachineImpl vm) throws InvalidTypeException {
		if (value == null) {
			if (type instanceof PrimitiveType) {
				throw new InvalidTypeException(JDIMessages.getString("ValueImpl.Type_of_the_value_not_compatible_with_the_expected_type._1")); //$NON-NLS-1$
			}
			return null;
		} else {
			vm.checkVM(value);
			TypeImpl valueType= (TypeImpl)value.type();
			if ((type instanceof PrimitiveType) ^ (valueType instanceof PrimitiveType)) {
				throw new InvalidTypeException("Type of the value not compatible with the expected type."); //$NON-NLS-1$
			}
			if (type instanceof PrimitiveType) {
				return checkPrimitiveValue((PrimitiveValueImpl) value, (PrimitiveTypeImpl) valueType, (PrimitiveTypeImpl) type);
			} else {
				return (ValueImpl)value;
			}
		}
	}

	/**
	 * Check the type of the given value, and convert the value to the given
	 * type if needed (see Java Language Spec, section 5.2).
	 * @return the (converted) value.
	 * @throws InvalidTypeException if the given value is no assignment
	 * compatible with the given type.
	 */
	protected static ValueImpl checkPrimitiveValue(PrimitiveValueImpl value, PrimitiveTypeImpl valueType, PrimitiveTypeImpl type) throws InvalidTypeException {
		char valueTypeSignature= valueType.signature().charAt(0);
		char typeSignature= type.signature().charAt(0);
		if (valueTypeSignature == typeSignature) {
			return value;
		}
		VirtualMachineImpl vm= value.virtualMachineImpl();
		switch (typeSignature) {
			case 'D':
				if (valueTypeSignature != 'Z') {
					return new DoubleValueImpl(vm, new Double(value.doubleValue()));
				}
				break;
			case 'F':
				if (valueTypeSignature != 'Z' && valueTypeSignature != 'D') {
					return new FloatValueImpl(vm, new Float(value.floatValue()));
				}
				break;
			case 'J':
				if (valueTypeSignature != 'Z' && valueTypeSignature != 'D' && valueTypeSignature != 'F') {
					return new LongValueImpl(vm, new Long(value.longValue()));
				}
				break;
			case 'I':
				if (valueTypeSignature == 'B' || valueTypeSignature == 'C' || valueTypeSignature == 'S') {
					return new IntegerValueImpl(vm, new Integer(value.intValue()));
				}
				break;
			case 'S':
				if (valueTypeSignature == 'B') {
					return new ShortValueImpl(vm, new Short(value.shortValue()));
				}
				break;
		}
		throw new InvalidTypeException("Type of the value not compatible with the expected type."); //$NON-NLS-1$
	}
}
