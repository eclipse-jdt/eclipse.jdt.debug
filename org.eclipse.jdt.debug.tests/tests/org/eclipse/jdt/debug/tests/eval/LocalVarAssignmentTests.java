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

public class LocalVarAssignmentTests extends Tests {

	public LocalVarAssignmentTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 40, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	//

	public void testByte() throws Throwable {
		try {
		init();
		IValue value = eval(xVarByte + equalOp +xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte local variable assignment : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable assignment : wrong result : ", xByteValue, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", xByteValue, byteValue);

		value = eval(xVarByte + equalOp +yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable assignment : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable assignment : wrong result : ", yByteValue, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", yByteValue, byteValue);

		value = eval(yVarByte + equalOp +xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable assignment : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable assignment : wrong result : ", xByteValue, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", xByteValue, byteValue);

		value = eval(yVarByte + equalOp +yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable assignment : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable assignment : wrong result : ", yByteValue, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", yByteValue, byteValue);
		} finally {
		end();
		}
	}

	public void testChar() throws Throwable {
		try {
		init();
		IValue value = eval(xVarChar + equalOp +xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char local variable assignment : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable assignment : wrong result : ", xCharValue, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", xCharValue, charValue);

		value = eval(xVarChar + equalOp +yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable assignment : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable assignment : wrong result : ", yCharValue, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", yCharValue, charValue);

		value = eval(yVarChar + equalOp +xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable assignment : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable assignment : wrong result : ", xCharValue, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", xCharValue, charValue);

		value = eval(yVarChar + equalOp +yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable assignment : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable assignment : wrong result : ", yCharValue, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", yCharValue, charValue);
		} finally {
		end();
		}
	}

	public void testShort() throws Throwable {
		try {
		init();
		IValue value = eval(xVarShort + equalOp +xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short local variable assignment : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable assignment : wrong result : ", xShortValue, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", xShortValue, shortValue);

		value = eval(xVarShort + equalOp +yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable assignment : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable assignment : wrong result : ", yShortValue, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", yShortValue, shortValue);

		value = eval(yVarShort + equalOp +xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable assignment : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable assignment : wrong result : ", xShortValue, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", xShortValue, shortValue);

		value = eval(yVarShort + equalOp +yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable assignment : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable assignment : wrong result : ", yShortValue, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", yShortValue, shortValue);
		} finally {
		end();
		}
	}

	public void testInt() throws Throwable {
		try {
		init();
		IValue value = eval(xVarInt + equalOp +xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int local variable assignment : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable assignment : wrong result : ", xIntValue, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", xIntValue, intValue);

		value = eval(xVarInt + equalOp +yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable assignment : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable assignment : wrong result : ", yIntValue, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", yIntValue, intValue);

		value = eval(yVarInt + equalOp +xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable assignment : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable assignment : wrong result : ", xIntValue, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", xIntValue, intValue);

		value = eval(yVarInt + equalOp +yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable assignment : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable assignment : wrong result : ", yIntValue, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", yIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testLong() throws Throwable {
		try {
		init();
		IValue value = eval(xVarLong + equalOp +xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long local variable assignment : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable assignment : wrong result : ", xLongValue, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", xLongValue, longValue);

		value = eval(xVarLong + equalOp +yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable assignment : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable assignment : wrong result : ", yLongValue, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", yLongValue, longValue);

		value = eval(yVarLong + equalOp +xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable assignment : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable assignment : wrong result : ", xLongValue, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", xLongValue, longValue);

		value = eval(yVarLong + equalOp +yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable assignment : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable assignment : wrong result : ", yLongValue, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", yLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xVarFloat + equalOp +xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float local variable assignment : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable assignment : wrong result : ", xFloatValue, floatValue, 0);
		value = eval(xVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", xFloatValue, floatValue, 0);

		value = eval(xVarFloat + equalOp +yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable assignment : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable assignment : wrong result : ", yFloatValue, floatValue, 0);
		value = eval(xVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", yFloatValue, floatValue, 0);

		value = eval(yVarFloat + equalOp +xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable assignment : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable assignment : wrong result : ", xFloatValue, floatValue, 0);
		value = eval(yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", xFloatValue, floatValue, 0);

		value = eval(yVarFloat + equalOp +yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable assignment : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable assignment : wrong result : ", yFloatValue, floatValue, 0);
		value = eval(yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", yFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xVarDouble + equalOp +xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double local variable assignment : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable assignment : wrong result : ", xDoubleValue, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", xDoubleValue, doubleValue, 0);

		value = eval(xVarDouble + equalOp +yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable assignment : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable assignment : wrong result : ", yDoubleValue, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", yDoubleValue, doubleValue, 0);

		value = eval(yVarDouble + equalOp +xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable assignment : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable assignment : wrong result : ", xDoubleValue, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", xDoubleValue, doubleValue, 0);

		value = eval(yVarDouble + equalOp +yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable assignment : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable assignment : wrong result : ", yDoubleValue, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", yDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testString() throws Throwable {
		try {
		init();
		IValue value = eval(xVarString + equalOp +xString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable assignment : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable assignment : wrong result : ", xStringValue, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", xStringValue, stringValue);

		value = eval(xVarString + equalOp +yString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable assignment : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable assignment : wrong result : ", yStringValue, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", yStringValue, stringValue);

		value = eval(yVarString + equalOp +xString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable assignment : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable assignment : wrong result : ", xStringValue, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", xStringValue, stringValue);

		value = eval(yVarString + equalOp +yString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable assignment : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable assignment : wrong result : ", yStringValue, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", yStringValue, stringValue);
		} finally {
		end();
		}
	}

	public void testBoolean() throws Throwable {
		try {
		init();
		IValue value = eval(xVarBoolean + equalOp +xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable assignment : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable assignment : wrong result : ", xBooleanValue, booleanValue);
		value = eval(xVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", xBooleanValue, booleanValue);

		value = eval(xVarBoolean + equalOp +yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable assignment : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable assignment : wrong result : ", yBooleanValue, booleanValue);
		value = eval(xVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", yBooleanValue, booleanValue);

		value = eval(yVarBoolean + equalOp +xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable assignment : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable assignment : wrong result : ", xBooleanValue, booleanValue);
		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", xBooleanValue, booleanValue);

		value = eval(yVarBoolean + equalOp +yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable assignment : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable assignment : wrong result : ", yBooleanValue, booleanValue);
		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", yBooleanValue, booleanValue);
		} finally {
		end();
		}
	}


}
