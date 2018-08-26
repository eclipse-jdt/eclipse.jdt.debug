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

public class ArrayAssignmentTests extends Tests {

	public ArrayAssignmentTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalArrayTests", 40, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testByteArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayByte + "[0]" + equalOp +xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte array assignment : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array assignment : wrong result : ", xByteValue, byteValue);
		value = eval(xArrayByte + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xByteValue, byteValue);

		value = eval(xArrayByte + "[1]" + equalOp +yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte array assignment : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array assignment : wrong result : ", yByteValue, byteValue);
		value = eval(xArrayByte + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", yByteValue, byteValue);

		value = eval(xArrayByte + "[2]" + equalOp +xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte array assignment : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array assignment : wrong result : ", xByteValue, byteValue);
		value = eval(xArrayByte + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xByteValue, byteValue);

		value = eval(yArrayByte + "[0]" + equalOp +yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte array assignment : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array assignment : wrong result : ", yByteValue, byteValue);
		value = eval(yArrayByte + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", yByteValue, byteValue);

		value = eval(yArrayByte + "[1]" + equalOp +xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte array assignment : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array assignment : wrong result : ", xByteValue, byteValue);
		value = eval(yArrayByte + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", xByteValue, byteValue);

		value = eval(yArrayByte + "[2]" + equalOp +yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte array assignment : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array assignment : wrong result : ", yByteValue, byteValue);
		value = eval(yArrayByte + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("byte array value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte array value : wrong result : ", yByteValue, byteValue);
		} finally {
		end();
		}
	}

	public void testCharArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayChar + "[0]" + equalOp +xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char array assignment : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array assignment : wrong result : ", xCharValue, charValue);
		value = eval(xArrayChar + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xCharValue, charValue);

		value = eval(xArrayChar + "[1]" + equalOp +yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char array assignment : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array assignment : wrong result : ", yCharValue, charValue);
		value = eval(xArrayChar + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", yCharValue, charValue);

		value = eval(xArrayChar + "[2]" + equalOp +xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char array assignment : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array assignment : wrong result : ", xCharValue, charValue);
		value = eval(xArrayChar + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xCharValue, charValue);

		value = eval(yArrayChar + "[0]" + equalOp +yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char array assignment : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array assignment : wrong result : ", yCharValue, charValue);
		value = eval(yArrayChar + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", yCharValue, charValue);

		value = eval(yArrayChar + "[1]" + equalOp +xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char array assignment : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array assignment : wrong result : ", xCharValue, charValue);
		value = eval(yArrayChar + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", xCharValue, charValue);

		value = eval(yArrayChar + "[2]" + equalOp +yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char array assignment : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array assignment : wrong result : ", yCharValue, charValue);
		value = eval(yArrayChar + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("char array value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char array value : wrong result : ", yCharValue, charValue);
		} finally {
		end();
		}
	}

	public void testShortArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayShort + "[0]" + equalOp +xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short array assignment : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array assignment : wrong result : ", xShortValue, shortValue);
		value = eval(xArrayShort + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xShortValue, shortValue);

		value = eval(xArrayShort + "[1]" + equalOp +yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short array assignment : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array assignment : wrong result : ", yShortValue, shortValue);
		value = eval(xArrayShort + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", yShortValue, shortValue);

		value = eval(xArrayShort + "[2]" + equalOp +xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short array assignment : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array assignment : wrong result : ", xShortValue, shortValue);
		value = eval(xArrayShort + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xShortValue, shortValue);

		value = eval(yArrayShort + "[0]" + equalOp +yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short array assignment : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array assignment : wrong result : ", yShortValue, shortValue);
		value = eval(yArrayShort + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", yShortValue, shortValue);

		value = eval(yArrayShort + "[1]" + equalOp +xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short array assignment : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array assignment : wrong result : ", xShortValue, shortValue);
		value = eval(yArrayShort + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", xShortValue, shortValue);

		value = eval(yArrayShort + "[2]" + equalOp +yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short array assignment : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array assignment : wrong result : ", yShortValue, shortValue);
		value = eval(yArrayShort + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("short array value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short array value : wrong result : ", yShortValue, shortValue);
		} finally {
		end();
		}
	}

	public void testIntArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayInt + "[0]" + equalOp +xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int array assignment : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array assignment : wrong result : ", xIntValue, intValue);
		value = eval(xArrayInt + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xIntValue, intValue);

		value = eval(xArrayInt + "[1]" + equalOp +yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int array assignment : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array assignment : wrong result : ", yIntValue, intValue);
		value = eval(xArrayInt + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", yIntValue, intValue);

		value = eval(xArrayInt + "[2]" + equalOp +xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int array assignment : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array assignment : wrong result : ", xIntValue, intValue);
		value = eval(xArrayInt + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xIntValue, intValue);

		value = eval(yArrayInt + "[0]" + equalOp +yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int array assignment : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array assignment : wrong result : ", yIntValue, intValue);
		value = eval(yArrayInt + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", yIntValue, intValue);

		value = eval(yArrayInt + "[1]" + equalOp +xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int array assignment : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array assignment : wrong result : ", xIntValue, intValue);
		value = eval(yArrayInt + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", xIntValue, intValue);

		value = eval(yArrayInt + "[2]" + equalOp +yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int array assignment : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array assignment : wrong result : ", yIntValue, intValue);
		value = eval(yArrayInt + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("int array value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int array value : wrong result : ", yIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testLongArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayLong + "[0]" + equalOp +xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long array assignment : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array assignment : wrong result : ", xLongValue, longValue);
		value = eval(xArrayLong + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xLongValue, longValue);

		value = eval(xArrayLong + "[1]" + equalOp +yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long array assignment : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array assignment : wrong result : ", yLongValue, longValue);
		value = eval(xArrayLong + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", yLongValue, longValue);

		value = eval(xArrayLong + "[2]" + equalOp +xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long array assignment : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array assignment : wrong result : ", xLongValue, longValue);
		value = eval(xArrayLong + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xLongValue, longValue);

		value = eval(yArrayLong + "[0]" + equalOp +yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long array assignment : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array assignment : wrong result : ", yLongValue, longValue);
		value = eval(yArrayLong + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", yLongValue, longValue);

		value = eval(yArrayLong + "[1]" + equalOp +xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long array assignment : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array assignment : wrong result : ", xLongValue, longValue);
		value = eval(yArrayLong + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", xLongValue, longValue);

		value = eval(yArrayLong + "[2]" + equalOp +yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long array assignment : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array assignment : wrong result : ", yLongValue, longValue);
		value = eval(yArrayLong + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("long array value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long array value : wrong result : ", yLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testFloatArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayFloat + "[0]" + equalOp +xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float array assignment : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array assignment : wrong result : ", xFloatValue, floatValue, 0);
		value = eval(xArrayFloat + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xFloatValue, floatValue, 0);

		value = eval(xArrayFloat + "[1]" + equalOp +yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float array assignment : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array assignment : wrong result : ", yFloatValue, floatValue, 0);
		value = eval(xArrayFloat + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", yFloatValue, floatValue, 0);

		value = eval(xArrayFloat + "[2]" + equalOp +xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float array assignment : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array assignment : wrong result : ", xFloatValue, floatValue, 0);
		value = eval(xArrayFloat + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xFloatValue, floatValue, 0);

		value = eval(yArrayFloat + "[0]" + equalOp +yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float array assignment : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array assignment : wrong result : ", yFloatValue, floatValue, 0);
		value = eval(yArrayFloat + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", yFloatValue, floatValue, 0);

		value = eval(yArrayFloat + "[1]" + equalOp +xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float array assignment : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array assignment : wrong result : ", xFloatValue, floatValue, 0);
		value = eval(yArrayFloat + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", xFloatValue, floatValue, 0);

		value = eval(yArrayFloat + "[2]" + equalOp +yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float array assignment : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array assignment : wrong result : ", yFloatValue, floatValue, 0);
		value = eval(yArrayFloat + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("float array value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float array value : wrong result : ", yFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayDouble + "[0]" + equalOp +xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double array assignment : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array assignment : wrong result : ", xDoubleValue, doubleValue, 0);
		value = eval(xArrayDouble + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xDoubleValue, doubleValue, 0);

		value = eval(xArrayDouble + "[1]" + equalOp +yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double array assignment : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array assignment : wrong result : ", yDoubleValue, doubleValue, 0);
		value = eval(xArrayDouble + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", yDoubleValue, doubleValue, 0);

		value = eval(xArrayDouble + "[2]" + equalOp +xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double array assignment : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array assignment : wrong result : ", xDoubleValue, doubleValue, 0);
		value = eval(xArrayDouble + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xDoubleValue, doubleValue, 0);

		value = eval(yArrayDouble + "[0]" + equalOp +yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double array assignment : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array assignment : wrong result : ", yDoubleValue, doubleValue, 0);
		value = eval(yArrayDouble + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", yDoubleValue, doubleValue, 0);

		value = eval(yArrayDouble + "[1]" + equalOp +xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double array assignment : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array assignment : wrong result : ", xDoubleValue, doubleValue, 0);
		value = eval(yArrayDouble + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", xDoubleValue, doubleValue, 0);

		value = eval(yArrayDouble + "[2]" + equalOp +yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double array assignment : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array assignment : wrong result : ", yDoubleValue, doubleValue, 0);
		value = eval(yArrayDouble + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("double array value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double array value : wrong result : ", yDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testStringArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayString + "[0]" + equalOp +xString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array assignment : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array assignment : wrong result : ", xStringValue, stringValue);
		value = eval(xArrayString + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xStringValue, stringValue);

		value = eval(xArrayString + "[1]" + equalOp +yString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array assignment : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array assignment : wrong result : ", yStringValue, stringValue);
		value = eval(xArrayString + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", yStringValue, stringValue);

		value = eval(xArrayString + "[2]" + equalOp +xString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array assignment : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array assignment : wrong result : ", xStringValue, stringValue);
		value = eval(xArrayString + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xStringValue, stringValue);

		value = eval(yArrayString + "[0]" + equalOp +yString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array assignment : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array assignment : wrong result : ", yStringValue, stringValue);
		value = eval(yArrayString + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", yStringValue, stringValue);

		value = eval(yArrayString + "[1]" + equalOp +xString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array assignment : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array assignment : wrong result : ", xStringValue, stringValue);
		value = eval(yArrayString + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", xStringValue, stringValue);

		value = eval(yArrayString + "[2]" + equalOp +yString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array assignment : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array assignment : wrong result : ", yStringValue, stringValue);
		value = eval(yArrayString + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String array value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String array value : wrong result : ", yStringValue, stringValue);
		} finally {
		end();
		}
	}

	public void testBooleanArrayAssignment() throws Throwable {
		try {
		init();
		IValue value = eval(xArrayBoolean + "[0]" + equalOp +xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean array assignment : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array assignment : wrong result : ", xBooleanValue, booleanValue);
		value = eval(xArrayBoolean + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xBooleanValue, booleanValue);

		value = eval(xArrayBoolean + "[1]" + equalOp +yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array assignment : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array assignment : wrong result : ", yBooleanValue, booleanValue);
		value = eval(xArrayBoolean + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", yBooleanValue, booleanValue);

		value = eval(xArrayBoolean + "[2]" + equalOp +xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array assignment : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array assignment : wrong result : ", xBooleanValue, booleanValue);
		value = eval(xArrayBoolean + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xBooleanValue, booleanValue);

		value = eval(yArrayBoolean + "[0]" + equalOp +yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array assignment : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array assignment : wrong result : ", yBooleanValue, booleanValue);
		value = eval(yArrayBoolean + "[0]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", yBooleanValue, booleanValue);

		value = eval(yArrayBoolean + "[1]" + equalOp +xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array assignment : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array assignment : wrong result : ", xBooleanValue, booleanValue);
		value = eval(yArrayBoolean + "[1]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", xBooleanValue, booleanValue);

		value = eval(yArrayBoolean + "[2]" + equalOp +yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array assignment : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array assignment : wrong result : ", yBooleanValue, booleanValue);
		value = eval(yArrayBoolean + "[2]");
		typeName = value.getReferenceTypeName();
		assertEquals("boolean array value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean array value : wrong result : ", yBooleanValue, booleanValue);
		} finally {
		end();
		}
	}


}
