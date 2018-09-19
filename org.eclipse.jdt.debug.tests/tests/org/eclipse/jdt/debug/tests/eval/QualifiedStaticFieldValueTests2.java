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

public class QualifiedStaticFieldValueTests2 extends Tests {

	public QualifiedStaticFieldValueTests2(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalTypeTests", 76, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testByteStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte field value : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte field value : wrong result : ", xStaticFieldByteValue, byteValue);

		value = eval("EvalTypeTests." + yStaticFieldByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte field value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte field value : wrong result : ", yStaticFieldByteValue, byteValue);
		} finally {
		end();
		}
	}

	public void testCharStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char field value : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char field value : wrong result : ", xStaticFieldCharValue, charValue);

		value = eval("EvalTypeTests." + yStaticFieldChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char field value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char field value : wrong result : ", yStaticFieldCharValue, charValue);
		} finally {
		end();
		}
	}

	public void testShortStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short field value : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short field value : wrong result : ", xStaticFieldShortValue, shortValue);

		value = eval("EvalTypeTests." + yStaticFieldShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short field value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short field value : wrong result : ", yStaticFieldShortValue, shortValue);
		} finally {
		end();
		}
	}

	public void testIntStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int field value : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int field value : wrong result : ", xStaticFieldIntValue, intValue);

		value = eval("EvalTypeTests." + yStaticFieldInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int field value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int field value : wrong result : ", yStaticFieldIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testLongStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long field value : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long field value : wrong result : ", xStaticFieldLongValue, longValue);

		value = eval("EvalTypeTests." + yStaticFieldLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long field value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long field value : wrong result : ", yStaticFieldLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testFloatStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float field value : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float field value : wrong result : ", xStaticFieldFloatValue, floatValue, 0);

		value = eval("EvalTypeTests." + yStaticFieldFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float field value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float field value : wrong result : ", yStaticFieldFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double field value : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double field value : wrong result : ", xStaticFieldDoubleValue, doubleValue, 0);

		value = eval("EvalTypeTests." + yStaticFieldDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double field value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double field value : wrong result : ", yStaticFieldDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testStringStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String field value : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String field value : wrong result : ", xStaticFieldStringValue, stringValue);

		value = eval("EvalTypeTests." + yStaticFieldString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String field value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String field value : wrong result : ", yStaticFieldStringValue, stringValue);
		} finally {
		end();
		}
	}

	public void testBooleanStaticFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("EvalTypeTests." + xStaticFieldBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean field value : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean field value : wrong result : ", xStaticFieldBooleanValue, booleanValue);

		value = eval("EvalTypeTests." + yStaticFieldBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean field value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean field value : wrong result : ", yStaticFieldBooleanValue, booleanValue);
		} finally {
		end();
		}
	}


}
