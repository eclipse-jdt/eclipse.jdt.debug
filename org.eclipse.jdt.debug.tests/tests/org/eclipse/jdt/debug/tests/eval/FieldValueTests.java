/*******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class FieldValueTests extends Tests {

	public FieldValueTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalTypeTests",63,2);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testByteFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte field value : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte field value : wrong result : ", xFieldByteValue, byteValue);

		value = eval("" + yFieldByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte field value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte field value : wrong result : ", yFieldByteValue, byteValue);
		} finally {;
		end();
		}
	}

	public void testCharFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char field value : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char field value : wrong result : ", xFieldCharValue, charValue);

		value = eval("" + yFieldChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char field value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char field value : wrong result : ", yFieldCharValue, charValue);
		} finally {;
		end();
		}
	}

	public void testShortFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short field value : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short field value : wrong result : ", xFieldShortValue, shortValue);

		value = eval("" + yFieldShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short field value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short field value : wrong result : ", yFieldShortValue, shortValue);
		} finally {;
		end();
		}
	}

	public void testIntFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int field value : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int field value : wrong result : ", xFieldIntValue, intValue);

		value = eval("" + yFieldInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int field value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int field value : wrong result : ", yFieldIntValue, intValue);
		} finally {;
		end();
		}
	}

	public void testLongFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long field value : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long field value : wrong result : ", xFieldLongValue, longValue);

		value = eval("" + yFieldLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long field value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long field value : wrong result : ", yFieldLongValue, longValue);
		} finally {;
		end();
		}
	}

	public void testFloatFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float field value : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float field value : wrong result : ", xFieldFloatValue, floatValue, 0);

		value = eval("" + yFieldFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float field value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float field value : wrong result : ", yFieldFloatValue, floatValue, 0);
		} finally {;
		end();
		}
	}

	public void testDoubleFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double field value : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double field value : wrong result : ", xFieldDoubleValue, doubleValue, 0);

		value = eval("" + yFieldDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double field value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double field value : wrong result : ", yFieldDoubleValue, doubleValue, 0);
		} finally {;
		end();
		}
	}

	public void testStringFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String field value : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String field value : wrong result : ", xFieldStringValue, stringValue);

		value = eval("" + yFieldString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String field value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String field value : wrong result : ", yFieldStringValue, stringValue);
		} finally {;
		end();
		}
	}

	public void testBooleanFieldValue() throws Throwable {
		try {
		init();
		IValue value = eval("" + xFieldBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean field value : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean field value : wrong result : ", xFieldBooleanValue, booleanValue);

		value = eval("" + yFieldBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean field value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean field value : wrong result : ", yFieldBooleanValue, booleanValue);
		} finally {;
		end();
		}
	}


}
