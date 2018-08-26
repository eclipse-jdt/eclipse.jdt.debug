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

public class TestsArrays extends Tests {
	/**
	 * Constructor for TypeHierarchy.
	 * @param name
	 */
	public TestsArrays(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalArrayTests", 40, 1, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
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

}
