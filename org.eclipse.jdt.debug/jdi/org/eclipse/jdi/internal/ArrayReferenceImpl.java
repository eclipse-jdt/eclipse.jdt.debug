package org.eclipse.jdi.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpObjectID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class ArrayReferenceImpl extends ObjectReferenceImpl implements ArrayReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.ARRAY_TAG;
	
	/**
	 * Creates new ArrayReferenceImpl.
	 */
	public ArrayReferenceImpl(VirtualMachineImpl vmImpl, JdwpObjectID objectID) {
		super("ArrayReference", vmImpl, objectID); //$NON-NLS-1$
	}
	
	/**
	 * @returns tag.
	 */
	public byte getTag() {
		return tag;
	}
	
	/**
	 * @returns Returns an array component value.
	 */
	public Value getValue(int index) throws IndexOutOfBoundsException {
		return (Value)getValues(index, 1).get(0);
	}
	
	/**
	 * @returns Returns all of the components in this array.
	 */
	public List getValues() {
		return getValues(0, -1);
	}
	
	/**
	 * @returns Returns a range of array components.
	 */
	public List getValues(int firstIndex, int length) throws IndexOutOfBoundsException {
		// Negative length indicates all elements.
		if (length < 0)
			length = length();
			
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);	// arrayObject
			writeInt(firstIndex, "firstIndex", outData); //$NON-NLS-1$
			writeInt(length, "length", outData); //$NON-NLS-1$
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.AR_GET_VALUES, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.INVALID_INDEX:
					throw new IndexOutOfBoundsException(JDIMessages.getString("ArrayReferenceImpl.Invalid_index_of_array_reference_given_1")); //$NON-NLS-1$
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			
			DataInputStream replyData = replyPacket.dataInStream();
	
			/* NOTE: The JDWP documentation is not clear on this: it turns out that the following is received from the VM:
			 * - type tag;
			 * - length of array;
			 * - values of elements.
			 */

			int type = readByte("type", JdwpID.tagMap(), replyData); //$NON-NLS-1$
			int readLength = readInt("length", replyData); //$NON-NLS-1$
			// See also ValueImpl.
			switch(type) {
				// Multidimensional array.
				case ArrayReferenceImpl.tag:
				// Object references.
				case ClassLoaderReferenceImpl.tag:
				case ClassObjectReferenceImpl.tag:
				case StringReferenceImpl.tag:
				case ObjectReferenceImpl.tag:
				case ThreadGroupReferenceImpl.tag:
				case ThreadReferenceImpl.tag:
					return readObjectSequence(readLength, replyData);

				// Primitive type.
				case BooleanValueImpl.tag:
				case ByteValueImpl.tag:
				case CharValueImpl.tag:
				case DoubleValueImpl.tag:
				case FloatValueImpl.tag:
				case IntegerValueImpl.tag:
				case LongValueImpl.tag:
				case ShortValueImpl.tag:
					return readPrimitiveSequence(readLength, type, replyData);

				case VoidValueImpl.tag:
				case 0:
				default:
					throw new InternalException(JDIMessages.getString("ArrayReferenceImpl.Invalid_ArrayReference_Value_tag_encountered___2") + type); //$NON-NLS-1$
			}
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @returns Returns sequence of object reference values.
	 */
	private List readObjectSequence(int length, DataInputStream in) throws IOException {
		List elements = new ArrayList(length);
		for (int i = 0; i < length; i++) {
			ValueImpl value = ObjectReferenceImpl.readObjectRefWithTag(this, in);
			elements.add(value);
		}
		return elements;
	}
	
	/**
	 * @returns Returns sequence of values of primitive type.
	 */
	private List readPrimitiveSequence(int length, int type, DataInputStream in) throws IOException {
		List elements = new ArrayList(length);
		for (int i = 0; i < length; i++) {
			ValueImpl value = ValueImpl.readWithoutTag(this, type, in);
			elements.add(value);
		}
		return elements;
	}
	
	/**
	 * @returns Returns the number of components in this array.
	 */
	public int length() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.AR_LENGTH, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			return readInt("length", replyData); //$NON-NLS-1$
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * Replaces an array component with another value.
	 */
	public void setValue(int index, Value value) throws InvalidTypeException, ClassNotLoadedException {
		ArrayList list = new ArrayList(1);
		list.add(value);
		setValues(index, list, 0, 1);
	}
	
	/**
	 * Replaces all array components with other values.
	 */
	public void setValues(List values) throws InvalidTypeException, ClassNotLoadedException {
		setValues(0, values, 0, -1);
	}
	
	/**
	 * Replaces a range of array components with other values.
	 */
	public void setValues(int index, List values, int srcIndex, int length) throws InvalidTypeException, ClassNotLoadedException {
		// Negative length indicates all elements.
		if (length < 0) {
			length = length() - index;
		} else if (index + length > length()) {
			throw new IndexOutOfBoundsException(JDIMessages.getString("ArrayReferenceImpl.Attempted_to_set_more_values_in_array_than_length_of_array_3")); //$NON-NLS-1$
		}

		// Check if enough values are given.
		if (values.size() < srcIndex + length) {
			throw new IndexOutOfBoundsException(JDIMessages.getString("ArrayReferenceImpl.Attempted_to_set_more_values_in_array_than_given_4")); //$NON-NLS-1$
		}

		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			writeInt(index, "index", outData); //$NON-NLS-1$
			writeInt(length, "length", outData); //$NON-NLS-1$
			String componentSignature= ((ArrayTypeImpl) referenceType()).componentSignature();
			for (int i = srcIndex; i < srcIndex + length; i++) {
				ValueImpl value = (ValueImpl)values.get(i);
				if (value != null) {
					value= convertPrimitiveValue(value, componentSignature);
					checkVM(value);
					((ValueImpl)value).write(this, outData);
				} else {
					ValueImpl.writeNull(this, outData);
				}
			}
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.AR_SET_VALUES, outBytes);
			switch (replyPacket.errorCode()) {
				case JdwpReplyPacket.TYPE_MISMATCH:
					throw new InvalidTypeException();
				case JdwpReplyPacket.INVALID_CLASS:
					throw new ClassNotLoadedException(type().name());
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}
	
	/**
	 * Converts the given primitive value to a value of the given primitive signature.
	 */
	private ValueImpl convertPrimitiveValue(ValueImpl value, String signature) {
		if (!(value instanceof PrimitiveValue)) {
			return value;
		}
		PrimitiveValueImpl primitiveValue = (PrimitiveValueImpl) value;
		switch (signature.charAt(0)) {
			case 'B' :
				return new ByteValueImpl(virtualMachineImpl(), new Byte(primitiveValue.byteValue()));
			case 'C' :
				return new CharValueImpl(virtualMachineImpl(), new Character(primitiveValue.charValue()));
			case 'S' :
				return new ShortValueImpl(virtualMachineImpl(), new Short(primitiveValue.shortValue()));
			case 'I' :
				return new IntegerValueImpl(virtualMachineImpl(), new Integer(primitiveValue.intValue()));
			case 'J' :
				return new LongValueImpl(virtualMachineImpl(), new Long(primitiveValue.longValue()));
			case 'F' :
				return new FloatValueImpl(virtualMachineImpl(), new Float(primitiveValue.floatValue()));
			case 'D' :
				return new DoubleValueImpl(virtualMachineImpl(), new Double(primitiveValue.doubleValue()));
		}
		return value;
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	public String toString() {
		try {
			StringBuffer buf = new StringBuffer(type().name());
			// Insert length of string between (last) square braces.
			buf.insert(buf.length() - 1, length());
			// Append space and idString.
			buf.append(' ');
			buf.append(idString());
			return buf.toString();
		} catch (ObjectCollectedException e) {
			return JDIMessages.getString("ArrayReferenceImpl.(Garbage_Collected)_ArrayReference_5") + "[" + length() + "] " + idString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (Exception e) {
			return fDescription;
		}
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ArrayReferenceImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpObjectID ID = new JdwpObjectID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("arrayReference", ID.value()); //$NON-NLS-1$
		}

		if (ID.isNull()) {
			return null;
		}
			
		ArrayReferenceImpl mirror = new ArrayReferenceImpl(vmImpl, ID);
		return mirror;
	}
}