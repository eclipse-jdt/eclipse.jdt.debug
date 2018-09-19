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
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class StringPlusAssignmentOpTests extends Tests {

	public StringPlusAssignmentOpTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// java.lang.String += {byte, char, short, int, long, java.lang.String, null}

	public void testStringPlusAssignmentByte() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xByteValue;
		IValue value = eval(xVarString + plusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment byte : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment byte : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yByteValue;
		value = eval(xVarString + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment byte : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment byte : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xByteValue;
		value = eval(yVarString + plusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment byte : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment byte : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yByteValue;
		value = eval(yVarString + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment byte : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment byte : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);
		} finally {
		end();
		}
	}

	public void testStringPlusAssignmentChar() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xCharValue;
		IValue value = eval(xVarString + plusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment char : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment char : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yCharValue;
		value = eval(xVarString + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment char : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment char : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xCharValue;
		value = eval(yVarString + plusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment char : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment char : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yCharValue;
		value = eval(yVarString + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment char : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment char : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);
		} finally {
		end();
		}
	}

	public void testStringPlusAssignmentShort() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xShortValue;
		IValue value = eval(xVarString + plusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment short : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment short : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yShortValue;
		value = eval(xVarString + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment short : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment short : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xShortValue;
		value = eval(yVarString + plusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment short : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment short : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yShortValue;
		value = eval(yVarString + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment short : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment short : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);
		} finally {
		end();
		}
	}

	public void testStringPlusAssignmentInt() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xIntValue;
		IValue value = eval(xVarString + plusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment int : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment int : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yIntValue;
		value = eval(xVarString + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment int : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment int : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xIntValue;
		value = eval(yVarString + plusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment int : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment int : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yIntValue;
		value = eval(yVarString + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment int : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment int : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);
		} finally {
		end();
		}
	}

	public void testStringPlusAssignmentLong() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xLongValue;
		IValue value = eval(xVarString + plusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment long : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment long : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yLongValue;
		value = eval(xVarString + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment long : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment long : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xLongValue;
		value = eval(yVarString + plusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment long : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment long : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yLongValue;
		value = eval(yVarString + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment long : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment long : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);
		} finally {
		end();
		}
	}

	public void testStringPlusAssignmentFloat() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xFloatValue;
		IValue value = eval(xVarString + plusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment float : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment float : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yFloatValue;
		value = eval(xVarString + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment float : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment float : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xFloatValue;
		value = eval(yVarString + plusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment float : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment float : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yFloatValue;
		value = eval(yVarString + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment float : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment float : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);
		} finally {
		end();
		}
	}

	public void testStringPlusAssignmentDouble() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xDoubleValue;
		IValue value = eval(xVarString + plusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment double : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment double : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yDoubleValue;
		value = eval(xVarString + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment double : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment double : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xDoubleValue;
		value = eval(yVarString + plusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment double : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment double : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yDoubleValue;
		value = eval(yVarString + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment double : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment double : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);
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

	public void testStringPlusAssignmentNull() throws Throwable {
		try {
		init();
		java.lang.String tmpxVar = xVarStringValue;
		tmpxVar += xNullValue;
		IValue value = eval(xVarString + plusAssignmentOp + xNull);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment null : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment null : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		tmpxVar += yNullValue;
		value = eval(xVarString + plusAssignmentOp + yNull);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment null : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment null : wrong result : ", tmpxVar, stringValue);
		value = eval(xVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpxVar, stringValue);

		java.lang.String tmpyVar = yVarStringValue;
		tmpyVar += xNullValue;
		value = eval(yVarString + plusAssignmentOp + xNull);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment null : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment null : wrong result : ", tmpyVar, stringValue);
		value = eval(yVarString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String local variable value : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String local variable value : wrong result : ", tmpyVar, stringValue);

		tmpyVar += yNullValue;
		value = eval(yVarString + plusAssignmentOp + yNull);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plusAssignment null : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plusAssignment null : wrong result : ", tmpyVar, stringValue);
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
