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

import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class TestsOperators2 extends Tests {
	/**
	 * Constructor for TypeHierarchy.
	 * @param name
	 */
	public TestsOperators2(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalSimpleTests", 37, 1, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
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

	public void testIntPlusAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar += xIntValue;
		IValue value = eval(xVarInt + plusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar += yIntValue;
		value = eval(xVarInt + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar += xIntValue;
		value = eval(yVarInt + plusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar += yIntValue;
		value = eval(yVarInt + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testStringPlusAssignmentString() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xStringValue;
		IValue value = eval(xVarString + plusAssignmentOp + xString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment java.lang.String : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yStringValue;
		value = eval(xVarString + plusAssignmentOp + yString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment java.lang.String : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xStringValue;
		value = eval(yVarString + plusAssignmentOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment java.lang.String : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yStringValue;
		value = eval(yVarString + plusAssignmentOp + yString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment java.lang.String : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);
		} finally {
		end();
		}
	}

}
