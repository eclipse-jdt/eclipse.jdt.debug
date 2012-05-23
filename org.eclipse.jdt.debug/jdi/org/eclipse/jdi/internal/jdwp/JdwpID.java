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
package org.eclipse.jdi.internal.jdwp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdi.internal.VirtualMachineImpl;

/**
 * From this class all Java Debug Wire Protocol (JDWP) IDs declared by the JDWP
 * specification are derived.
 * 
 */
public abstract class JdwpID {
	/** Tag Constants. */
	public static final byte NULL_TAG = 91; // Used for tagged null values.
	public static final byte ARRAY_TAG = 91; // '[' - an array object (objectID
												// size).
	public static final byte BYTE_TAG = 66; // 'B' - a byte value (1 byte).
	public static final byte CHAR_TAG = 67; // 'C' - a character value (2
											// bytes).
	public static final byte OBJECT_TAG = 76; // 'L' - an object (objectID
												// size).
	public static final byte FLOAT_TAG = 70; // 'F' - a float value (4 bytes).
	public static final byte DOUBLE_TAG = 68; // 'D' - a double value (8 bytes).
	public static final byte INT_TAG = 73; // 'I' - an int value (4 bytes).
	public static final byte LONG_TAG = 74; // 'J' - a long value (8 bytes).
	public static final byte SHORT_TAG = 83; // 'S' - a short value (2 bytes).
	public static final byte VOID_TAG = 86; // 'V' - a void value (no bytes).
	public static final byte BOOLEAN_TAG = 90; // 'Z' - a boolean value (1
												// byte).
	public static final byte STRING_TAG = 115; // 's' - a String object
												// (objectID size).
	public static final byte THREAD_TAG = 116; // 't' - a Thread object
												// (objectID size).
	public static final byte THREAD_GROUP_TAG = 103; // 'g' - a ThreadGroup
														// object (objectID
														// size).
	public static final byte CLASS_LOADER_TAG = 108; // 'l' - a ClassLoader
														// object (objectID
														// size).
	public static final byte CLASS_OBJECT_TAG = 99; // 'c' - a class object
													// object (objectID size).

	/** TypeTag Constants. */
	public static final byte TYPE_TAG_CLASS = 1; // ReferenceType is a class.
	public static final byte TYPE_TAG_INTERFACE = 2; // ReferenceType is an
														// interface.
	public static final byte TYPE_TAG_ARRAY = 3; // ReferenceType is an array.

	/** Mapping of command codes to strings. */
	private static HashMap<Integer, String> fTagMap = null;
	private static HashMap<Integer, String> fTypeTagMap = null;

	/** Jdwp representation of null ID. */
	protected static final int VALUE_NULL = 0;

	/** The value of the ID */
	protected long fValue = VALUE_NULL;
	/**
	 * The virtual machine of the mirror object that uses this ID (needed for ID
	 * sizes.
	 */
	protected VirtualMachineImpl fVirtualMachine;

	/**
	 * Creates new JdwpID.
	 */
	public JdwpID(VirtualMachineImpl vmImpl) {
		fVirtualMachine = vmImpl;
	}

	/**
	 * @return Returns true if two IDs refer to the same entity in the target
	 *         VM.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		return object instanceof JdwpID && fValue == ((JdwpID) object).fValue;
	}

	/**
	 * @return Returns a has code for this object.
	 * @see java.lang.Object#hashCode
	 */
	@Override
	public int hashCode() {
		return (int) fValue;
	}

	/**
	 * @return Returns value of ID.
	 */
	public final long value() {
		return fValue;
	}

	/**
	 * @return Returns string representation.
	 */
	@Override
	public String toString() {
		return new Long(fValue).toString();
	}

	/**
	 * @return Returns VM specific size of ID.
	 */
	protected abstract int getSize();

	/**
	 * @return Returns true if ID is null.
	 */
	public abstract boolean isNull();

	/**
	 * Reads ID.
	 */
	public void read(DataInputStream inStream) throws IOException {
		fValue = 0;
		int size = getSize();
		for (int i = 0; i < size; i++) {
			int b = inStream.readUnsignedByte(); // Note that the byte must be
													// treated as unsigned.
			fValue = fValue << 8 | b;
		}
	}

	/**
	 * Writes ID.
	 */
	public void write(DataOutputStream outStream) throws IOException {
		int size = getSize();
		for (int i = size - 1; i >= 0; i--) {
			byte b = (byte) (fValue >>> 8 * i); // Note that >>> must be used
												// because fValue must be
												// treated as unsigned.
			outStream.write(b);
		}
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fTagMap != null)
			return;

		java.lang.reflect.Field[] fields = JdwpID.class.getDeclaredFields();
		fTagMap = new HashMap<Integer, String>();
		fTypeTagMap = new HashMap<Integer, String>();
		for (Field field : fields) {
			if ((field.getModifiers() & java.lang.reflect.Modifier.PUBLIC) == 0
					|| (field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0
					|| (field.getModifiers() & java.lang.reflect.Modifier.FINAL) == 0)
				continue;

			try {
				String name = field.getName();
				Integer intValue = new Integer(field.getInt(null));
				if (name.startsWith("TYPE_TAG_")) { //$NON-NLS-1$
					name = name.substring(9);
					fTypeTagMap.put(intValue, name);
				} else if (name.endsWith("_TAG")) { //$NON-NLS-1$
					fTagMap.put(intValue, name);
				}
			} catch (IllegalAccessException e) {
				// Will not occur for own class.
			} catch (IllegalArgumentException e) {
				// Should not occur.
				// We should take care that all public static final constants
				// in this class are numbers that are convertible to int.
			}
		}
	}

	/**
	 * @return Returns a map with string representations of tags.
	 */
	public static Map<Integer, String> tagMap() {
		getConstantMaps();
		return fTagMap;
	}

	/**
	 * @return Returns a map with string representations of type tags.
	 */
	public static Map<Integer, String> typeTagMap() {
		getConstantMaps();
		return fTypeTagMap;
	}
}
