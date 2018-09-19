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

public class ArrayInitializationTests extends Tests {

	public ArrayInitializationTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalArrayTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testByteArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayByte + equalOp + "new byte[]{" + xByte + ", " + yByte + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("byte array assignment : wrong type : ", "byte[]", typeName);
		int intValue = 0;
		byte byteValue = yByteValue;
		value = eval(xArrayByte + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte array length : wrong result : ", 2, intValue);
		value = eval(xArrayByte + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xByteValue, byteValue);
		value = eval(xArrayByte + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", yByteValue, byteValue);

		value = eval(yArrayByte + equalOp + "new byte[]{" + xByte + ", " + yByte + ", " + xByte + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array assignment : wrong type : ", "byte[]", typeName);
		value = eval(yArrayByte + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte array length : wrong result : ", 3, intValue);
		value = eval(yArrayByte + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xByteValue, byteValue);
		value = eval(yArrayByte + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", yByteValue, byteValue);
		value = eval(yArrayByte + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xByteValue, byteValue);
		} finally {
		end();
		}
	}

	public void testCharArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayChar + equalOp + "new char[]{" + xChar + ", " + yChar + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("char array assignment : wrong type : ", "char[]", typeName);
		int intValue = 0;
		char charValue = yCharValue;
		value = eval(xArrayChar + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("char array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char array length : wrong result : ", 2, intValue);
		value = eval(xArrayChar + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xCharValue, charValue);
		value = eval(xArrayChar + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", yCharValue, charValue);

		value = eval(yArrayChar + equalOp + "new char[]{" + xChar + ", " + yChar + ", " + xChar + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("char array assignment : wrong type : ", "char[]", typeName);
		value = eval(yArrayChar + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("char array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char array length : wrong result : ", 3, intValue);
		value = eval(yArrayChar + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xCharValue, charValue);
		value = eval(yArrayChar + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", yCharValue, charValue);
		value = eval(yArrayChar + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xCharValue, charValue);
		} finally {
		end();
		}
	}

	public void testShortArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayShort + equalOp + "new short[]{" + xShort + ", " + yShort + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("short array assignment : wrong type : ", "short[]", typeName);
		int intValue = 0;
		short shortValue = yShortValue;
		value = eval(xArrayShort + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("short array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short array length : wrong result : ", 2, intValue);
		value = eval(xArrayShort + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xShortValue, shortValue);
		value = eval(xArrayShort + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", yShortValue, shortValue);

		value = eval(yArrayShort + equalOp + "new short[]{" + xShort + ", " + yShort + ", " + xShort + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("short array assignment : wrong type : ", "short[]", typeName);
		value = eval(yArrayShort + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("short array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short array length : wrong result : ", 3, intValue);
		value = eval(yArrayShort + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xShortValue, shortValue);
		value = eval(yArrayShort + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", yShortValue, shortValue);
		value = eval(yArrayShort + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xShortValue, shortValue);
		} finally {
		end();
		}
	}

	public void testIntArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayInt + equalOp + "new int[]{" + xInt + ", " + yInt + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("int array assignment : wrong type : ", "int[]", typeName);
		int intValue = 0;
		value = eval(xArrayInt + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("int array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array length : wrong result : ", 2, intValue);
		value = eval(xArrayInt + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xIntValue, intValue);
		value = eval(xArrayInt + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", yIntValue, intValue);

		value = eval(yArrayInt + equalOp + "new int[]{" + xInt + ", " + yInt + ", " + xInt + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("int array assignment : wrong type : ", "int[]", typeName);
		value = eval(yArrayInt + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("int array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array length : wrong result : ", 3, intValue);
		value = eval(yArrayInt + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xIntValue, intValue);
		value = eval(yArrayInt + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", yIntValue, intValue);
		value = eval(yArrayInt + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testLongArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayLong + equalOp + "new long[]{" + xLong + ", " + yLong + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("long array assignment : wrong type : ", "long[]", typeName);
		int intValue = 0;
		long longValue = yLongValue;
		value = eval(xArrayLong + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("long array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("long array length : wrong result : ", 2, intValue);
		value = eval(xArrayLong + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xLongValue, longValue);
		value = eval(xArrayLong + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", yLongValue, longValue);

		value = eval(yArrayLong + equalOp + "new long[]{" + xLong + ", " + yLong + ", " + xLong + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("long array assignment : wrong type : ", "long[]", typeName);
		value = eval(yArrayLong + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("long array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("long array length : wrong result : ", 3, intValue);
		value = eval(yArrayLong + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xLongValue, longValue);
		value = eval(yArrayLong + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", yLongValue, longValue);
		value = eval(yArrayLong + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testFloatArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayFloat + equalOp + "new float[]{" + xFloat + ", " + yFloat + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("float array assignment : wrong type : ", "float[]", typeName);
		int intValue = 0;
		float floatValue = yFloatValue;
		value = eval(xArrayFloat + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("float array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("float array length : wrong result : ", 2, intValue);
		value = eval(xArrayFloat + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xFloatValue, floatValue, 0);
		value = eval(xArrayFloat + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", yFloatValue, floatValue, 0);

		value = eval(yArrayFloat + equalOp + "new float[]{" + xFloat + ", " + yFloat + ", " + xFloat + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("float array assignment : wrong type : ", "float[]", typeName);
		value = eval(yArrayFloat + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("float array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("float array length : wrong result : ", 3, intValue);
		value = eval(yArrayFloat + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xFloatValue, floatValue, 0);
		value = eval(yArrayFloat + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", yFloatValue, floatValue, 0);
		value = eval(yArrayFloat + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayDouble + equalOp + "new double[]{" + xDouble + ", " + yDouble + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("double array assignment : wrong type : ", "double[]", typeName);
		int intValue = 0;
		double doubleValue = yDoubleValue;
		value = eval(xArrayDouble + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("double array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("double array length : wrong result : ", 2, intValue);
		value = eval(xArrayDouble + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xDoubleValue, doubleValue, 0);
		value = eval(xArrayDouble + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", yDoubleValue, doubleValue, 0);

		value = eval(yArrayDouble + equalOp + "new double[]{" + xDouble + ", " + yDouble + ", " + xDouble + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("double array assignment : wrong type : ", "double[]", typeName);
		value = eval(yArrayDouble + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("double array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("double array length : wrong result : ", 3, intValue);
		value = eval(yArrayDouble + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xDoubleValue, doubleValue, 0);
		value = eval(yArrayDouble + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", yDoubleValue, doubleValue, 0);
		value = eval(yArrayDouble + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testStringArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayString + equalOp + "new java.lang.String[]{" + xString + ", " + yString + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array assignment : wrong type : ", "java.lang.String[]", typeName);
		int intValue = 0;
		java.lang.String stringValue = yStringValue;
		value = eval(xArrayString + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("java.lang.String array length : wrong result : ", 2, intValue);
		value = eval(xArrayString + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xStringValue, stringValue);
		value = eval(xArrayString + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", yStringValue, stringValue);

		value = eval(yArrayString + equalOp + "new java.lang.String[]{" + xString + ", " + yString + ", " + xString + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array assignment : wrong type : ", "java.lang.String[]", typeName);
		value = eval(yArrayString + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("java.lang.String array length : wrong result : ", 3, intValue);
		value = eval(yArrayString + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xStringValue, stringValue);
		value = eval(yArrayString + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", yStringValue, stringValue);
		value = eval(yArrayString + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xStringValue, stringValue);
		} finally {
		end();
		}
	}

	public void testBooleanArrayAllocation() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayBoolean + equalOp + "new boolean[]{" + xBoolean + ", " + yBoolean + "}");
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean array assignment : wrong type : ", "boolean[]", typeName);
		int intValue = 0;
		boolean booleanValue = yBooleanValue;
		value = eval(xArrayBoolean + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("boolean array length : wrong result : ", 2, intValue);
		value = eval(xArrayBoolean + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xBooleanValue, booleanValue);
		value = eval(xArrayBoolean + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", yBooleanValue, booleanValue);

		value = eval(yArrayBoolean + equalOp + "new boolean[]{" + xBoolean + ", " + yBoolean + ", " + xBoolean + "}");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array assignment : wrong type : ", "boolean[]", typeName);
		value = eval(yArrayBoolean + ".length");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array length : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("boolean array length : wrong result : ", 3, intValue);
		value = eval(yArrayBoolean + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xBooleanValue, booleanValue);
		value = eval(yArrayBoolean + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", yBooleanValue, booleanValue);
		value = eval(yArrayBoolean + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xBooleanValue, booleanValue);
		} finally {
		end();
		}
	}


}
