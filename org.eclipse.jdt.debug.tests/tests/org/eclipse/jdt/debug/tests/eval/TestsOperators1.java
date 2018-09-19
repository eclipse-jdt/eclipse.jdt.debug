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

public class TestsOperators1 extends Tests {
	/**
	 * Constructor for TypeHierarchy.
	 * @param name
	 */
	public TestsOperators1(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalSimpleTests", 37, 1, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testIntPlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus int : wrong result : ", xIntValue + yIntValue, intValue);

		value = eval(yInt + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus int : wrong result : ", yIntValue + xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testStringPlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus java.lang.String : wrong result : ", xStringValue + yStringValue, stringValue);

		value = eval(yString + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus java.lang.String : wrong result : ", yStringValue + xStringValue, stringValue);
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

}
