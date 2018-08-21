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

public class IntAssignmentOperatorsTests extends Tests {

	public IntAssignmentOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// int += {byte, char, short, int, long, float, double}

	public void testIntPlusAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar += xByteValue;
		IValue value = eval(xVarInt + plusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar += yByteValue;
		value = eval(xVarInt + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar += xByteValue;
		value = eval(yVarInt + plusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar += yByteValue;
		value = eval(yVarInt + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntPlusAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar += xCharValue;
		IValue value = eval(xVarInt + plusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar += yCharValue;
		value = eval(xVarInt + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar += xCharValue;
		value = eval(yVarInt + plusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar += yCharValue;
		value = eval(yVarInt + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntPlusAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar += xShortValue;
		IValue value = eval(xVarInt + plusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar += yShortValue;
		value = eval(xVarInt + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar += xShortValue;
		value = eval(yVarInt + plusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar += yShortValue;
		value = eval(yVarInt + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
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

	public void testIntPlusAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar += xLongValue;
		IValue value = eval(xVarInt + plusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar += yLongValue;
		value = eval(xVarInt + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar += xLongValue;
		value = eval(yVarInt + plusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar += yLongValue;
		value = eval(yVarInt + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntPlusAssignmentFloat() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar += xFloatValue;
		IValue value = eval(xVarInt + plusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment float : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar += yFloatValue;
		value = eval(xVarInt + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar += xFloatValue;
		value = eval(yVarInt + plusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar += yFloatValue;
		value = eval(yVarInt + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntPlusAssignmentDouble() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar += xDoubleValue;
		IValue value = eval(xVarInt + plusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment double : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar += yDoubleValue;
		value = eval(xVarInt + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar += xDoubleValue;
		value = eval(yVarInt + plusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar += yDoubleValue;
		value = eval(yVarInt + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int plusAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plusAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int -= {byte, char, short, int, long, float, double}

	public void testIntMinusAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar -= xByteValue;
		IValue value = eval(xVarInt + minusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar -= yByteValue;
		value = eval(xVarInt + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar -= xByteValue;
		value = eval(yVarInt + minusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar -= yByteValue;
		value = eval(yVarInt + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar -= xCharValue;
		IValue value = eval(xVarInt + minusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar -= yCharValue;
		value = eval(xVarInt + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar -= xCharValue;
		value = eval(yVarInt + minusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar -= yCharValue;
		value = eval(yVarInt + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar -= xShortValue;
		IValue value = eval(xVarInt + minusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar -= yShortValue;
		value = eval(xVarInt + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar -= xShortValue;
		value = eval(yVarInt + minusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar -= yShortValue;
		value = eval(yVarInt + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar -= xIntValue;
		IValue value = eval(xVarInt + minusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar -= yIntValue;
		value = eval(xVarInt + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar -= xIntValue;
		value = eval(yVarInt + minusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar -= yIntValue;
		value = eval(yVarInt + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar -= xLongValue;
		IValue value = eval(xVarInt + minusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar -= yLongValue;
		value = eval(xVarInt + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar -= xLongValue;
		value = eval(yVarInt + minusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar -= yLongValue;
		value = eval(yVarInt + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusAssignmentFloat() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar -= xFloatValue;
		IValue value = eval(xVarInt + minusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment float : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar -= yFloatValue;
		value = eval(xVarInt + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar -= xFloatValue;
		value = eval(yVarInt + minusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar -= yFloatValue;
		value = eval(yVarInt + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusAssignmentDouble() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar -= xDoubleValue;
		IValue value = eval(xVarInt + minusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment double : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar -= yDoubleValue;
		value = eval(xVarInt + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar -= xDoubleValue;
		value = eval(yVarInt + minusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar -= yDoubleValue;
		value = eval(yVarInt + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int minusAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minusAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int *= {byte, char, short, int, long, float, double}

	public void testIntMultiplyAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar *= xByteValue;
		IValue value = eval(xVarInt + multiplyAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar *= yByteValue;
		value = eval(xVarInt + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar *= xByteValue;
		value = eval(yVarInt + multiplyAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar *= yByteValue;
		value = eval(yVarInt + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar *= xCharValue;
		IValue value = eval(xVarInt + multiplyAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar *= yCharValue;
		value = eval(xVarInt + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar *= xCharValue;
		value = eval(yVarInt + multiplyAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar *= yCharValue;
		value = eval(yVarInt + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar *= xShortValue;
		IValue value = eval(xVarInt + multiplyAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar *= yShortValue;
		value = eval(xVarInt + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar *= xShortValue;
		value = eval(yVarInt + multiplyAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar *= yShortValue;
		value = eval(yVarInt + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar *= xIntValue;
		IValue value = eval(xVarInt + multiplyAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar *= yIntValue;
		value = eval(xVarInt + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar *= xIntValue;
		value = eval(yVarInt + multiplyAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar *= yIntValue;
		value = eval(yVarInt + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar *= xLongValue;
		IValue value = eval(xVarInt + multiplyAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar *= yLongValue;
		value = eval(xVarInt + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar *= xLongValue;
		value = eval(yVarInt + multiplyAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar *= yLongValue;
		value = eval(yVarInt + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyAssignmentFloat() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar *= xFloatValue;
		IValue value = eval(xVarInt + multiplyAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment float : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar *= yFloatValue;
		value = eval(xVarInt + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar *= xFloatValue;
		value = eval(yVarInt + multiplyAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar *= yFloatValue;
		value = eval(yVarInt + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyAssignmentDouble() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar *= xDoubleValue;
		IValue value = eval(xVarInt + multiplyAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment double : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar *= yDoubleValue;
		value = eval(xVarInt + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar *= xDoubleValue;
		value = eval(yVarInt + multiplyAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar *= yDoubleValue;
		value = eval(yVarInt + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiplyAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiplyAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int /= {byte, char, short, int, long, float, double}

	public void testIntDivideAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar /= xByteValue;
		IValue value = eval(xVarInt + divideAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar /= yByteValue;
		value = eval(xVarInt + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar /= xByteValue;
		value = eval(yVarInt + divideAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar /= yByteValue;
		value = eval(yVarInt + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar /= xCharValue;
		IValue value = eval(xVarInt + divideAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar /= yCharValue;
		value = eval(xVarInt + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar /= xCharValue;
		value = eval(yVarInt + divideAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar /= yCharValue;
		value = eval(yVarInt + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar /= xShortValue;
		IValue value = eval(xVarInt + divideAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar /= yShortValue;
		value = eval(xVarInt + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar /= xShortValue;
		value = eval(yVarInt + divideAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar /= yShortValue;
		value = eval(yVarInt + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar /= xIntValue;
		IValue value = eval(xVarInt + divideAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar /= yIntValue;
		value = eval(xVarInt + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar /= xIntValue;
		value = eval(yVarInt + divideAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar /= yIntValue;
		value = eval(yVarInt + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar /= xLongValue;
		IValue value = eval(xVarInt + divideAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar /= yLongValue;
		value = eval(xVarInt + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar /= xLongValue;
		value = eval(yVarInt + divideAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar /= yLongValue;
		value = eval(yVarInt + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideAssignmentFloat() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar /= xFloatValue;
		IValue value = eval(xVarInt + divideAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment float : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar /= yFloatValue;
		value = eval(xVarInt + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar /= xFloatValue;
		value = eval(yVarInt + divideAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar /= yFloatValue;
		value = eval(yVarInt + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideAssignmentDouble() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar /= xDoubleValue;
		IValue value = eval(xVarInt + divideAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment double : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar /= yDoubleValue;
		value = eval(xVarInt + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar /= xDoubleValue;
		value = eval(yVarInt + divideAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar /= yDoubleValue;
		value = eval(yVarInt + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int divideAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divideAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int %= {byte, char, short, int, long, float, double}

	public void testIntRemainderAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar %= xByteValue;
		IValue value = eval(xVarInt + remainderAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar %= yByteValue;
		value = eval(xVarInt + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar %= xByteValue;
		value = eval(yVarInt + remainderAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar %= yByteValue;
		value = eval(yVarInt + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar %= xCharValue;
		IValue value = eval(xVarInt + remainderAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar %= yCharValue;
		value = eval(xVarInt + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar %= xCharValue;
		value = eval(yVarInt + remainderAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar %= yCharValue;
		value = eval(yVarInt + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar %= xShortValue;
		IValue value = eval(xVarInt + remainderAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar %= yShortValue;
		value = eval(xVarInt + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar %= xShortValue;
		value = eval(yVarInt + remainderAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar %= yShortValue;
		value = eval(yVarInt + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar %= xIntValue;
		IValue value = eval(xVarInt + remainderAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar %= yIntValue;
		value = eval(xVarInt + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar %= xIntValue;
		value = eval(yVarInt + remainderAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar %= yIntValue;
		value = eval(yVarInt + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar %= xLongValue;
		IValue value = eval(xVarInt + remainderAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar %= yLongValue;
		value = eval(xVarInt + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar %= xLongValue;
		value = eval(yVarInt + remainderAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar %= yLongValue;
		value = eval(yVarInt + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderAssignmentFloat() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar %= xFloatValue;
		IValue value = eval(xVarInt + remainderAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment float : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar %= yFloatValue;
		value = eval(xVarInt + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment float : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar %= xFloatValue;
		value = eval(yVarInt + remainderAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar %= yFloatValue;
		value = eval(yVarInt + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment float : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderAssignmentDouble() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar %= xDoubleValue;
		IValue value = eval(xVarInt + remainderAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment double : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar %= yDoubleValue;
		value = eval(xVarInt + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment double : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar %= xDoubleValue;
		value = eval(yVarInt + remainderAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar %= yDoubleValue;
		value = eval(yVarInt + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainderAssignment double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainderAssignment double : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int <<= {byte, char, short, int, long, float, double}

	public void testIntLeftShiftAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar <<= xByteValue;
		IValue value = eval(xVarInt + leftShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar <<= yByteValue;
		value = eval(xVarInt + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar <<= xByteValue;
		value = eval(yVarInt + leftShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar <<= yByteValue;
		value = eval(yVarInt + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntLeftShiftAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar <<= xCharValue;
		IValue value = eval(xVarInt + leftShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar <<= yCharValue;
		value = eval(xVarInt + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar <<= xCharValue;
		value = eval(yVarInt + leftShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar <<= yCharValue;
		value = eval(yVarInt + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntLeftShiftAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar <<= xShortValue;
		IValue value = eval(xVarInt + leftShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar <<= yShortValue;
		value = eval(xVarInt + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar <<= xShortValue;
		value = eval(yVarInt + leftShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar <<= yShortValue;
		value = eval(yVarInt + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntLeftShiftAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar <<= xIntValue;
		IValue value = eval(xVarInt + leftShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar <<= yIntValue;
		value = eval(xVarInt + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar <<= xIntValue;
		value = eval(yVarInt + leftShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar <<= yIntValue;
		value = eval(yVarInt + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntLeftShiftAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar <<= xLongValue;
		IValue value = eval(xVarInt + leftShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar <<= yLongValue;
		value = eval(xVarInt + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar <<= xLongValue;
		value = eval(yVarInt + leftShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar <<= yLongValue;
		value = eval(yVarInt + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShiftAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int >>= {byte, char, short, int, long, float, double}

	public void testIntRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>= xByteValue;
		IValue value = eval(xVarInt + rightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>= yByteValue;
		value = eval(xVarInt + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>= xByteValue;
		value = eval(yVarInt + rightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>= yByteValue;
		value = eval(yVarInt + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>= xCharValue;
		IValue value = eval(xVarInt + rightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>= yCharValue;
		value = eval(xVarInt + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>= xCharValue;
		value = eval(yVarInt + rightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>= yCharValue;
		value = eval(yVarInt + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>= xShortValue;
		IValue value = eval(xVarInt + rightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>= yShortValue;
		value = eval(xVarInt + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>= xShortValue;
		value = eval(yVarInt + rightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>= yShortValue;
		value = eval(yVarInt + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>= xIntValue;
		IValue value = eval(xVarInt + rightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>= yIntValue;
		value = eval(xVarInt + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>= xIntValue;
		value = eval(yVarInt + rightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>= yIntValue;
		value = eval(yVarInt + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>= xLongValue;
		IValue value = eval(xVarInt + rightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>= yLongValue;
		value = eval(xVarInt + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>= xLongValue;
		value = eval(yVarInt + rightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>= yLongValue;
		value = eval(yVarInt + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShiftAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int >>>= {byte, char, short, int, long, float, double}

	public void testIntUnsignedRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>>= xByteValue;
		IValue value = eval(xVarInt + unsignedRightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>>= yByteValue;
		value = eval(xVarInt + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>>= xByteValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>>= yByteValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntUnsignedRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>>= xCharValue;
		IValue value = eval(xVarInt + unsignedRightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>>= yCharValue;
		value = eval(xVarInt + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>>= xCharValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>>= yCharValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntUnsignedRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>>= xShortValue;
		IValue value = eval(xVarInt + unsignedRightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>>= yShortValue;
		value = eval(xVarInt + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>>= xShortValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>>= yShortValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntUnsignedRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>>= xIntValue;
		IValue value = eval(xVarInt + unsignedRightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>>= yIntValue;
		value = eval(xVarInt + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>>= xIntValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>>= yIntValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntUnsignedRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar >>>= xLongValue;
		IValue value = eval(xVarInt + unsignedRightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar >>>= yLongValue;
		value = eval(xVarInt + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar >>>= xLongValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar >>>= yLongValue;
		value = eval(yVarInt + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShiftAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShiftAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int |= {byte, char, short, int, long, float, double}

	public void testIntOrAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar |= xByteValue;
		IValue value = eval(xVarInt + orAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar |= yByteValue;
		value = eval(xVarInt + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar |= xByteValue;
		value = eval(yVarInt + orAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar |= yByteValue;
		value = eval(yVarInt + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntOrAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar |= xCharValue;
		IValue value = eval(xVarInt + orAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar |= yCharValue;
		value = eval(xVarInt + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar |= xCharValue;
		value = eval(yVarInt + orAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar |= yCharValue;
		value = eval(yVarInt + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntOrAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar |= xShortValue;
		IValue value = eval(xVarInt + orAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar |= yShortValue;
		value = eval(xVarInt + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar |= xShortValue;
		value = eval(yVarInt + orAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar |= yShortValue;
		value = eval(yVarInt + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntOrAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar |= xIntValue;
		IValue value = eval(xVarInt + orAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar |= yIntValue;
		value = eval(xVarInt + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar |= xIntValue;
		value = eval(yVarInt + orAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar |= yIntValue;
		value = eval(yVarInt + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntOrAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar |= xLongValue;
		IValue value = eval(xVarInt + orAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar |= yLongValue;
		value = eval(xVarInt + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar |= xLongValue;
		value = eval(yVarInt + orAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar |= yLongValue;
		value = eval(yVarInt + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int orAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int orAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int &= {byte, char, short, int, long, float, double}

	public void testIntAndAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar &= xByteValue;
		IValue value = eval(xVarInt + andAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar &= yByteValue;
		value = eval(xVarInt + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar &= xByteValue;
		value = eval(yVarInt + andAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar &= yByteValue;
		value = eval(yVarInt + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntAndAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar &= xCharValue;
		IValue value = eval(xVarInt + andAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar &= yCharValue;
		value = eval(xVarInt + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar &= xCharValue;
		value = eval(yVarInt + andAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar &= yCharValue;
		value = eval(yVarInt + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntAndAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar &= xShortValue;
		IValue value = eval(xVarInt + andAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar &= yShortValue;
		value = eval(xVarInt + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar &= xShortValue;
		value = eval(yVarInt + andAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar &= yShortValue;
		value = eval(yVarInt + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntAndAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar &= xIntValue;
		IValue value = eval(xVarInt + andAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar &= yIntValue;
		value = eval(xVarInt + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar &= xIntValue;
		value = eval(yVarInt + andAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar &= yIntValue;
		value = eval(yVarInt + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntAndAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar &= xLongValue;
		IValue value = eval(xVarInt + andAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar &= yLongValue;
		value = eval(xVarInt + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar &= xLongValue;
		value = eval(yVarInt + andAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar &= yLongValue;
		value = eval(yVarInt + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int andAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int andAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// int ^= {byte, char, short, int, long, float, double}

	public void testIntXorAssignmentByte() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar ^= xByteValue;
		IValue value = eval(xVarInt + xorAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar ^= yByteValue;
		value = eval(xVarInt + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment byte : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar ^= xByteValue;
		value = eval(yVarInt + xorAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar ^= yByteValue;
		value = eval(yVarInt + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment byte : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntXorAssignmentChar() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar ^= xCharValue;
		IValue value = eval(xVarInt + xorAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar ^= yCharValue;
		value = eval(xVarInt + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment char : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar ^= xCharValue;
		value = eval(yVarInt + xorAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar ^= yCharValue;
		value = eval(yVarInt + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment char : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntXorAssignmentShort() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar ^= xShortValue;
		IValue value = eval(xVarInt + xorAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar ^= yShortValue;
		value = eval(xVarInt + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment short : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar ^= xShortValue;
		value = eval(yVarInt + xorAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar ^= yShortValue;
		value = eval(yVarInt + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment short : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntXorAssignmentInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar ^= xIntValue;
		IValue value = eval(xVarInt + xorAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar ^= yIntValue;
		value = eval(xVarInt + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment int : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar ^= xIntValue;
		value = eval(yVarInt + xorAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar ^= yIntValue;
		value = eval(yVarInt + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment int : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testIntXorAssignmentLong() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		tmpxVar ^= xLongValue;
		IValue value = eval(xVarInt + xorAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		tmpxVar ^= yLongValue;
		value = eval(xVarInt + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment long : wrong result : ", tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		tmpyVar ^= xLongValue;
		value = eval(yVarInt + xorAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);

		tmpyVar ^= yLongValue;
		value = eval(yVarInt + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int xorAssignment long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xorAssignment long : wrong result : ", tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}


}
