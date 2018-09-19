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

public class QualifiedFieldValueTests extends Tests {

	public QualifiedFieldValueTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalTypeTests", 76, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testByteFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte field value : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte field value : wrong result : ", xFieldByteValue, byteValue);

		value = eval("foo." + yFieldByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte field value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte field value : wrong result : ", yFieldByteValue, byteValue);
		} finally {
		end();
		}
	}

	public void testCharFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char field value : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char field value : wrong result : ", xFieldCharValue, charValue);

		value = eval("foo." + yFieldChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char field value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char field value : wrong result : ", yFieldCharValue, charValue);
		} finally {
		end();
		}
	}

	public void testShortFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short field value : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short field value : wrong result : ", xFieldShortValue, shortValue);

		value = eval("foo." + yFieldShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short field value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short field value : wrong result : ", yFieldShortValue, shortValue);
		} finally {
		end();
		}
	}

	public void testIntFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int field value : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int field value : wrong result : ", xFieldIntValue, intValue);

		value = eval("foo." + yFieldInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int field value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int field value : wrong result : ", yFieldIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testLongFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long field value : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long field value : wrong result : ", xFieldLongValue, longValue);

		value = eval("foo." + yFieldLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long field value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long field value : wrong result : ", yFieldLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testFloatFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float field value : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float field value : wrong result : ", xFieldFloatValue, floatValue, 0);

		value = eval("foo." + yFieldFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float field value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float field value : wrong result : ", yFieldFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double field value : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double field value : wrong result : ", xFieldDoubleValue, doubleValue, 0);

		value = eval("foo." + yFieldDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double field value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double field value : wrong result : ", yFieldDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testStringFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String field value : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String field value : wrong result : ", xFieldStringValue, stringValue);

		value = eval("foo." + yFieldString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String field value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String field value : wrong result : ", yFieldStringValue, stringValue);
		} finally {
		end();
		}
	}

	public void testBooleanFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("foo." + xFieldBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean field value : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean field value : wrong result : ", xFieldBooleanValue, booleanValue);

		value = eval("foo." + yFieldBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean field value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean field value : wrong result : ", yFieldBooleanValue, booleanValue);
		} finally {
		end();
		}
	}


}
