/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class ArrayValueTests extends Tests {

	public ArrayValueTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalArrayTests", 40, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testByteArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayByte + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("byte array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte array length : wrong result : ", xArrayByteValue.length, intValue);

		value = eval(yArrayByte + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte array length : wrong result : ", yArrayByteValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testCharArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayChar + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("char array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char array length : wrong result : ", xArrayCharValue.length, intValue);

		value = eval(yArrayChar + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("char array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char array length : wrong result : ", yArrayCharValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testShortArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayShort + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("short array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short array length : wrong result : ", xArrayShortValue.length, intValue);

		value = eval(yArrayShort + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("short array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short array length : wrong result : ", yArrayShortValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testIntArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayInt + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("int array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array length : wrong result : ", xArrayIntValue.length, intValue);

		value = eval(yArrayInt + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("int array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array length : wrong result : ", yArrayIntValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testLongArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayLong + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("long array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("long array length : wrong result : ", xArrayLongValue.length, intValue);

		value = eval(yArrayLong + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("long array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("long array length : wrong result : ", yArrayLongValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testFloatArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayFloat + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("float array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("float array length : wrong result : ", xArrayFloatValue.length, intValue);

		value = eval(yArrayFloat + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("float array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("float array length : wrong result : ", yArrayFloatValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testDoubleArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayDouble + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("double array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("double array length : wrong result : ", xArrayDoubleValue.length, intValue);

		value = eval(yArrayDouble + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("double array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("double array length : wrong result : ", yArrayDoubleValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testStringArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayString + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("java.lang.String array length : wrong result : ", xArrayStringValue.length, intValue);

		value = eval(yArrayString + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("java.lang.String array length : wrong result : ", yArrayStringValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testBooleanArrayLength() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayBoolean + ".length");
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean array length : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("boolean array length : wrong result : ", xArrayBooleanValue.length, intValue);

		value = eval(yArrayBoolean + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("boolean array length : wrong result : ", yArrayBooleanValue.length, intValue);
		} finally {
		end();
		}
	}

	public void testByteArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayByte + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xArrayByteValue[0], byteValue);

		value = eval(xArrayByte + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xArrayByteValue[1], byteValue);

		value = eval(xArrayByte + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xArrayByteValue[2], byteValue);

		value = eval(yArrayByte + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", yArrayByteValue[0], byteValue);

		value = eval(yArrayByte + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", yArrayByteValue[1], byteValue);

		value = eval(yArrayByte + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", yArrayByteValue[2], byteValue);
		} finally {
		end();
		}
	}

	public void testCharArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayChar + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xArrayCharValue[0], charValue);

		value = eval(xArrayChar + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xArrayCharValue[1], charValue);

		value = eval(xArrayChar + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xArrayCharValue[2], charValue);

		value = eval(yArrayChar + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", yArrayCharValue[0], charValue);

		value = eval(yArrayChar + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", yArrayCharValue[1], charValue);

		value = eval(yArrayChar + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", yArrayCharValue[2], charValue);
		} finally {
		end();
		}
	}

	public void testShortArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayShort + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xArrayShortValue[0], shortValue);

		value = eval(xArrayShort + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xArrayShortValue[1], shortValue);

		value = eval(xArrayShort + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xArrayShortValue[2], shortValue);

		value = eval(yArrayShort + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", yArrayShortValue[0], shortValue);

		value = eval(yArrayShort + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", yArrayShortValue[1], shortValue);

		value = eval(yArrayShort + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", yArrayShortValue[2], shortValue);
		} finally {
		end();
		}
	}

	public void testIntArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayInt + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xArrayIntValue[0], intValue);

		value = eval(xArrayInt + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xArrayIntValue[1], intValue);

		value = eval(xArrayInt + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xArrayIntValue[2], intValue);

		value = eval(yArrayInt + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", yArrayIntValue[0], intValue);

		value = eval(yArrayInt + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", yArrayIntValue[1], intValue);

		value = eval(yArrayInt + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", yArrayIntValue[2], intValue);
		} finally {
		end();
		}
	}

	public void testLongArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayLong + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xArrayLongValue[0], longValue);

		value = eval(xArrayLong + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xArrayLongValue[1], longValue);

		value = eval(xArrayLong + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xArrayLongValue[2], longValue);

		value = eval(yArrayLong + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", yArrayLongValue[0], longValue);

		value = eval(yArrayLong + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", yArrayLongValue[1], longValue);

		value = eval(yArrayLong + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", yArrayLongValue[2], longValue);
		} finally {
		end();
		}
	}

	public void testFloatArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayFloat + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xArrayFloatValue[0], floatValue, 0);

		value = eval(xArrayFloat + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xArrayFloatValue[1], floatValue, 0);

		value = eval(xArrayFloat + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xArrayFloatValue[2], floatValue, 0);

		value = eval(yArrayFloat + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", yArrayFloatValue[0], floatValue, 0);

		value = eval(yArrayFloat + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", yArrayFloatValue[1], floatValue, 0);

		value = eval(yArrayFloat + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", yArrayFloatValue[2], floatValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayDouble + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xArrayDoubleValue[0], doubleValue, 0);

		value = eval(xArrayDouble + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xArrayDoubleValue[1], doubleValue, 0);

		value = eval(xArrayDouble + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xArrayDoubleValue[2], doubleValue, 0);

		value = eval(yArrayDouble + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", yArrayDoubleValue[0], doubleValue, 0);

		value = eval(yArrayDouble + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", yArrayDoubleValue[1], doubleValue, 0);

		value = eval(yArrayDouble + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", yArrayDoubleValue[2], doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testStringArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayString + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xArrayStringValue[0], stringValue);

		value = eval(xArrayString + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xArrayStringValue[1], stringValue);

		value = eval(xArrayString + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xArrayStringValue[2], stringValue);

		value = eval(yArrayString + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", yArrayStringValue[0], stringValue);

		value = eval(yArrayString + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", yArrayStringValue[1], stringValue);

		value = eval(yArrayString + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", yArrayStringValue[2], stringValue);
		} finally {
		end();
		}
	}

	public void testBooleanArrayValue() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayBoolean + "[0]");
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xArrayBooleanValue[0], booleanValue);

		value = eval(xArrayBoolean + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xArrayBooleanValue[1], booleanValue);

		value = eval(xArrayBoolean + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xArrayBooleanValue[2], booleanValue);

		value = eval(yArrayBoolean + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", yArrayBooleanValue[0], booleanValue);

		value = eval(yArrayBoolean + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", yArrayBooleanValue[1], booleanValue);

		value = eval(yArrayBoolean + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", yArrayBooleanValue[2], booleanValue);
		} finally {
		end();
		}
	}


}
