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

public class LocalVarValueTests extends Tests {

	public LocalVarValueTests(String arg) {
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
		IValue value = eval(xVarByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", xVarByteValue, byteValue);

		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", yVarByteValue, byteValue);
		} finally {
		end();
		}
	}

	public void testChar() throws Throwable {
		try {
		init();
		IValue value = eval(xVarChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", xVarCharValue, charValue);

		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", yVarCharValue, charValue);
		} finally {
		end();
		}
	}

	public void testShort() throws Throwable {
		try {
		init();
		IValue value = eval(xVarShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", xVarShortValue, shortValue);

		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", yVarShortValue, shortValue);
		} finally {
		end();
		}
	}

	public void testInt() throws Throwable {
		try {
		init();
		IValue value = eval(xVarInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", xVarIntValue, intValue);

		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", yVarIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testLong() throws Throwable {
		try {
		init();
		IValue value = eval(xVarLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", xVarLongValue, longValue);

		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", yVarLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xVarFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", xVarFloatValue, floatValue, 0);

		value = eval(yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", yVarFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xVarDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", xVarDoubleValue, doubleValue, 0);

		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", yVarDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testString() throws Throwable {
		try {
		init();
		IValue value = eval(xVarString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", xVarStringValue, stringValue);

		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", yVarStringValue, stringValue);
		} finally {
		end();
		}
	}

	public void testBoolean() throws Throwable {
		try {
		init();
		IValue value = eval(xVarBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", xVarBooleanValue, booleanValue);

		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", yVarBooleanValue, booleanValue);
		} finally {
		end();
		}
	}


}
