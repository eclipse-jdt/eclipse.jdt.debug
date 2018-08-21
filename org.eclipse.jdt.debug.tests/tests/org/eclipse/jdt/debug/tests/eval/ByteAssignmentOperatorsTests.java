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

public class ByteAssignmentOperatorsTests extends Tests {

	public ByteAssignmentOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// byte += {byte, char, short, int, long, float, double}

	public void testBytePlusAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar += xByteValue;
		IValue value = eval(xVarByte + plusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar += yByteValue;
		value = eval(xVarByte + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar += xByteValue;
		value = eval(yVarByte + plusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar += yByteValue;
		value = eval(yVarByte + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testBytePlusAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar += xCharValue;
		IValue value = eval(xVarByte + plusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar += yCharValue;
		value = eval(xVarByte + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar += xCharValue;
		value = eval(yVarByte + plusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar += yCharValue;
		value = eval(yVarByte + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testBytePlusAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar += xShortValue;
		IValue value = eval(xVarByte + plusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar += yShortValue;
		value = eval(xVarByte + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar += xShortValue;
		value = eval(yVarByte + plusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar += yShortValue;
		value = eval(yVarByte + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testBytePlusAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar += xIntValue;
		IValue value = eval(xVarByte + plusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar += yIntValue;
		value = eval(xVarByte + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar += xIntValue;
		value = eval(yVarByte + plusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar += yIntValue;
		value = eval(yVarByte + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testBytePlusAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar += xLongValue;
		IValue value = eval(xVarByte + plusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar += yLongValue;
		value = eval(xVarByte + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar += xLongValue;
		value = eval(yVarByte + plusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar += yLongValue;
		value = eval(yVarByte + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testBytePlusAssignmentFloat() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar += xFloatValue;
		IValue value = eval(xVarByte + plusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment float : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar += yFloatValue;
		value = eval(xVarByte + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar += xFloatValue;
		value = eval(yVarByte + plusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar += yFloatValue;
		value = eval(yVarByte + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testBytePlusAssignmentDouble() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar += xDoubleValue;
		IValue value = eval(xVarByte + plusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment double : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar += yDoubleValue;
		value = eval(xVarByte + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar += xDoubleValue;
		value = eval(yVarByte + plusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar += yDoubleValue;
		value = eval(yVarByte + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plusAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte plusAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte -= {byte, char, short, int, long, float, double}

	public void testByteMinusAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar -= xByteValue;
		IValue value = eval(xVarByte + minusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar -= yByteValue;
		value = eval(xVarByte + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar -= xByteValue;
		value = eval(yVarByte + minusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar -= yByteValue;
		value = eval(yVarByte + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMinusAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar -= xCharValue;
		IValue value = eval(xVarByte + minusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar -= yCharValue;
		value = eval(xVarByte + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar -= xCharValue;
		value = eval(yVarByte + minusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar -= yCharValue;
		value = eval(yVarByte + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMinusAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar -= xShortValue;
		IValue value = eval(xVarByte + minusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar -= yShortValue;
		value = eval(xVarByte + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar -= xShortValue;
		value = eval(yVarByte + minusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar -= yShortValue;
		value = eval(yVarByte + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMinusAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar -= xIntValue;
		IValue value = eval(xVarByte + minusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar -= yIntValue;
		value = eval(xVarByte + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar -= xIntValue;
		value = eval(yVarByte + minusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar -= yIntValue;
		value = eval(yVarByte + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMinusAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar -= xLongValue;
		IValue value = eval(xVarByte + minusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar -= yLongValue;
		value = eval(xVarByte + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar -= xLongValue;
		value = eval(yVarByte + minusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar -= yLongValue;
		value = eval(yVarByte + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMinusAssignmentFloat() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar -= xFloatValue;
		IValue value = eval(xVarByte + minusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment float : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar -= yFloatValue;
		value = eval(xVarByte + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar -= xFloatValue;
		value = eval(yVarByte + minusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar -= yFloatValue;
		value = eval(yVarByte + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMinusAssignmentDouble() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar -= xDoubleValue;
		IValue value = eval(xVarByte + minusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment double : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar -= yDoubleValue;
		value = eval(xVarByte + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar -= xDoubleValue;
		value = eval(yVarByte + minusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar -= yDoubleValue;
		value = eval(yVarByte + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minusAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte minusAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte *= {byte, char, short, int, long, float, double}

	public void testByteMultiplyAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar *= xByteValue;
		IValue value = eval(xVarByte + multiplyAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar *= yByteValue;
		value = eval(xVarByte + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar *= xByteValue;
		value = eval(yVarByte + multiplyAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar *= yByteValue;
		value = eval(yVarByte + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar *= xCharValue;
		IValue value = eval(xVarByte + multiplyAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar *= yCharValue;
		value = eval(xVarByte + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar *= xCharValue;
		value = eval(yVarByte + multiplyAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar *= yCharValue;
		value = eval(yVarByte + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar *= xShortValue;
		IValue value = eval(xVarByte + multiplyAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar *= yShortValue;
		value = eval(xVarByte + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar *= xShortValue;
		value = eval(yVarByte + multiplyAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar *= yShortValue;
		value = eval(yVarByte + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar *= xIntValue;
		IValue value = eval(xVarByte + multiplyAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar *= yIntValue;
		value = eval(xVarByte + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar *= xIntValue;
		value = eval(yVarByte + multiplyAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar *= yIntValue;
		value = eval(yVarByte + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar *= xLongValue;
		IValue value = eval(xVarByte + multiplyAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar *= yLongValue;
		value = eval(xVarByte + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar *= xLongValue;
		value = eval(yVarByte + multiplyAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar *= yLongValue;
		value = eval(yVarByte + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyAssignmentFloat() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar *= xFloatValue;
		IValue value = eval(xVarByte + multiplyAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment float : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar *= yFloatValue;
		value = eval(xVarByte + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar *= xFloatValue;
		value = eval(yVarByte + multiplyAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar *= yFloatValue;
		value = eval(yVarByte + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyAssignmentDouble() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar *= xDoubleValue;
		IValue value = eval(xVarByte + multiplyAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment double : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar *= yDoubleValue;
		value = eval(xVarByte + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar *= xDoubleValue;
		value = eval(yVarByte + multiplyAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar *= yDoubleValue;
		value = eval(yVarByte + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiplyAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte multiplyAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte /= {byte, char, short, int, long, float, double}

	public void testByteDivideAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar /= xByteValue;
		IValue value = eval(xVarByte + divideAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar /= yByteValue;
		value = eval(xVarByte + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar /= xByteValue;
		value = eval(yVarByte + divideAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar /= yByteValue;
		value = eval(yVarByte + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteDivideAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar /= xCharValue;
		IValue value = eval(xVarByte + divideAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar /= yCharValue;
		value = eval(xVarByte + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar /= xCharValue;
		value = eval(yVarByte + divideAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar /= yCharValue;
		value = eval(yVarByte + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteDivideAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar /= xShortValue;
		IValue value = eval(xVarByte + divideAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar /= yShortValue;
		value = eval(xVarByte + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar /= xShortValue;
		value = eval(yVarByte + divideAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar /= yShortValue;
		value = eval(yVarByte + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteDivideAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar /= xIntValue;
		IValue value = eval(xVarByte + divideAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar /= yIntValue;
		value = eval(xVarByte + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar /= xIntValue;
		value = eval(yVarByte + divideAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar /= yIntValue;
		value = eval(yVarByte + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteDivideAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar /= xLongValue;
		IValue value = eval(xVarByte + divideAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar /= yLongValue;
		value = eval(xVarByte + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar /= xLongValue;
		value = eval(yVarByte + divideAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar /= yLongValue;
		value = eval(yVarByte + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteDivideAssignmentFloat() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar /= xFloatValue;
		IValue value = eval(xVarByte + divideAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment float : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar /= yFloatValue;
		value = eval(xVarByte + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar /= xFloatValue;
		value = eval(yVarByte + divideAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar /= yFloatValue;
		value = eval(yVarByte + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteDivideAssignmentDouble() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar /= xDoubleValue;
		IValue value = eval(xVarByte + divideAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment double : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar /= yDoubleValue;
		value = eval(xVarByte + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar /= xDoubleValue;
		value = eval(yVarByte + divideAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar /= yDoubleValue;
		value = eval(yVarByte + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divideAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte divideAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte %= {byte, char, short, int, long, float, double}

	public void testByteRemainderAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar %= xByteValue;
		IValue value = eval(xVarByte + remainderAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar %= yByteValue;
		value = eval(xVarByte + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar %= xByteValue;
		value = eval(yVarByte + remainderAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar %= yByteValue;
		value = eval(yVarByte + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar %= xCharValue;
		IValue value = eval(xVarByte + remainderAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar %= yCharValue;
		value = eval(xVarByte + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar %= xCharValue;
		value = eval(yVarByte + remainderAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar %= yCharValue;
		value = eval(yVarByte + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar %= xShortValue;
		IValue value = eval(xVarByte + remainderAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar %= yShortValue;
		value = eval(xVarByte + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar %= xShortValue;
		value = eval(yVarByte + remainderAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar %= yShortValue;
		value = eval(yVarByte + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar %= xIntValue;
		IValue value = eval(xVarByte + remainderAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar %= yIntValue;
		value = eval(xVarByte + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar %= xIntValue;
		value = eval(yVarByte + remainderAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar %= yIntValue;
		value = eval(yVarByte + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar %= xLongValue;
		IValue value = eval(xVarByte + remainderAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar %= yLongValue;
		value = eval(xVarByte + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar %= xLongValue;
		value = eval(yVarByte + remainderAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar %= yLongValue;
		value = eval(yVarByte + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderAssignmentFloat() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar %= xFloatValue;
		IValue value = eval(xVarByte + remainderAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment float : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar %= yFloatValue;
		value = eval(xVarByte + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment float : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar %= xFloatValue;
		value = eval(yVarByte + remainderAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar %= yFloatValue;
		value = eval(yVarByte + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment float : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderAssignmentDouble() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar %= xDoubleValue;
		IValue value = eval(xVarByte + remainderAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment double : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar %= yDoubleValue;
		value = eval(xVarByte + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment double : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar %= xDoubleValue;
		value = eval(yVarByte + remainderAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar %= yDoubleValue;
		value = eval(yVarByte + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainderAssignment double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte remainderAssignment double : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte <<= {byte, char, short, int, long, float, double}

	public void testByteLeftShiftAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar <<= xByteValue;
		IValue value = eval(xVarByte + leftShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar <<= yByteValue;
		value = eval(xVarByte + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar <<= xByteValue;
		value = eval(yVarByte + leftShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar <<= yByteValue;
		value = eval(yVarByte + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteLeftShiftAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar <<= xCharValue;
		IValue value = eval(xVarByte + leftShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar <<= yCharValue;
		value = eval(xVarByte + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar <<= xCharValue;
		value = eval(yVarByte + leftShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar <<= yCharValue;
		value = eval(yVarByte + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteLeftShiftAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar <<= xShortValue;
		IValue value = eval(xVarByte + leftShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar <<= yShortValue;
		value = eval(xVarByte + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar <<= xShortValue;
		value = eval(yVarByte + leftShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar <<= yShortValue;
		value = eval(yVarByte + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteLeftShiftAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar <<= xIntValue;
		IValue value = eval(xVarByte + leftShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar <<= yIntValue;
		value = eval(xVarByte + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar <<= xIntValue;
		value = eval(yVarByte + leftShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar <<= yIntValue;
		value = eval(yVarByte + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteLeftShiftAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar <<= xLongValue;
		IValue value = eval(xVarByte + leftShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar <<= yLongValue;
		value = eval(xVarByte + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar <<= xLongValue;
		value = eval(yVarByte + leftShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar <<= yLongValue;
		value = eval(yVarByte + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte leftShiftAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte >>= {byte, char, short, int, long, float, double}

	public void testByteRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>= xByteValue;
		IValue value = eval(xVarByte + rightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>= yByteValue;
		value = eval(xVarByte + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>= xByteValue;
		value = eval(yVarByte + rightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>= yByteValue;
		value = eval(yVarByte + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>= xCharValue;
		IValue value = eval(xVarByte + rightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>= yCharValue;
		value = eval(xVarByte + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>= xCharValue;
		value = eval(yVarByte + rightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>= yCharValue;
		value = eval(yVarByte + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>= xShortValue;
		IValue value = eval(xVarByte + rightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>= yShortValue;
		value = eval(xVarByte + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>= xShortValue;
		value = eval(yVarByte + rightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>= yShortValue;
		value = eval(yVarByte + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>= xIntValue;
		IValue value = eval(xVarByte + rightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>= yIntValue;
		value = eval(xVarByte + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>= xIntValue;
		value = eval(yVarByte + rightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>= yIntValue;
		value = eval(yVarByte + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>= xLongValue;
		IValue value = eval(xVarByte + rightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>= yLongValue;
		value = eval(xVarByte + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>= xLongValue;
		value = eval(yVarByte + rightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>= yLongValue;
		value = eval(yVarByte + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte rightShiftAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte >>>= {byte, char, short, int, long, float, double}

	public void testByteUnsignedRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>>= xByteValue;
		IValue value = eval(xVarByte + unsignedRightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>>= yByteValue;
		value = eval(xVarByte + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>>= xByteValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>>= yByteValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteUnsignedRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>>= xCharValue;
		IValue value = eval(xVarByte + unsignedRightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>>= yCharValue;
		value = eval(xVarByte + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>>= xCharValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>>= yCharValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteUnsignedRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>>= xShortValue;
		IValue value = eval(xVarByte + unsignedRightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>>= yShortValue;
		value = eval(xVarByte + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>>= xShortValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>>= yShortValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteUnsignedRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>>= xIntValue;
		IValue value = eval(xVarByte + unsignedRightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>>= yIntValue;
		value = eval(xVarByte + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>>= xIntValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>>= yIntValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteUnsignedRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar >>>= xLongValue;
		IValue value = eval(xVarByte + unsignedRightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar >>>= yLongValue;
		value = eval(xVarByte + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar >>>= xLongValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar >>>= yLongValue;
		value = eval(yVarByte + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShiftAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte unsignedRightShiftAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte |= {byte, char, short, int, long, float, double}

	public void testByteOrAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar |= xByteValue;
		IValue value = eval(xVarByte + orAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar |= yByteValue;
		value = eval(xVarByte + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar |= xByteValue;
		value = eval(yVarByte + orAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar |= yByteValue;
		value = eval(yVarByte + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteOrAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar |= xCharValue;
		IValue value = eval(xVarByte + orAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar |= yCharValue;
		value = eval(xVarByte + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar |= xCharValue;
		value = eval(yVarByte + orAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar |= yCharValue;
		value = eval(yVarByte + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteOrAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar |= xShortValue;
		IValue value = eval(xVarByte + orAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar |= yShortValue;
		value = eval(xVarByte + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar |= xShortValue;
		value = eval(yVarByte + orAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar |= yShortValue;
		value = eval(yVarByte + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteOrAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar |= xIntValue;
		IValue value = eval(xVarByte + orAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar |= yIntValue;
		value = eval(xVarByte + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar |= xIntValue;
		value = eval(yVarByte + orAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar |= yIntValue;
		value = eval(yVarByte + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteOrAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar |= xLongValue;
		IValue value = eval(xVarByte + orAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar |= yLongValue;
		value = eval(xVarByte + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar |= xLongValue;
		value = eval(yVarByte + orAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar |= yLongValue;
		value = eval(yVarByte + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte orAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte orAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte &= {byte, char, short, int, long, float, double}

	public void testByteAndAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar &= xByteValue;
		IValue value = eval(xVarByte + andAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar &= yByteValue;
		value = eval(xVarByte + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar &= xByteValue;
		value = eval(yVarByte + andAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar &= yByteValue;
		value = eval(yVarByte + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteAndAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar &= xCharValue;
		IValue value = eval(xVarByte + andAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar &= yCharValue;
		value = eval(xVarByte + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar &= xCharValue;
		value = eval(yVarByte + andAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar &= yCharValue;
		value = eval(yVarByte + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteAndAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar &= xShortValue;
		IValue value = eval(xVarByte + andAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar &= yShortValue;
		value = eval(xVarByte + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar &= xShortValue;
		value = eval(yVarByte + andAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar &= yShortValue;
		value = eval(yVarByte + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteAndAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar &= xIntValue;
		IValue value = eval(xVarByte + andAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar &= yIntValue;
		value = eval(xVarByte + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar &= xIntValue;
		value = eval(yVarByte + andAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar &= yIntValue;
		value = eval(yVarByte + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteAndAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar &= xLongValue;
		IValue value = eval(xVarByte + andAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar &= yLongValue;
		value = eval(xVarByte + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar &= xLongValue;
		value = eval(yVarByte + andAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar &= yLongValue;
		value = eval(yVarByte + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte andAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte andAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// byte ^= {byte, char, short, int, long, float, double}

	public void testByteXorAssignmentByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar ^= xByteValue;
		IValue value = eval(xVarByte + xorAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar ^= yByteValue;
		value = eval(xVarByte + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment byte : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar ^= xByteValue;
		value = eval(yVarByte + xorAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar ^= yByteValue;
		value = eval(yVarByte + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment byte : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteXorAssignmentChar() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar ^= xCharValue;
		IValue value = eval(xVarByte + xorAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar ^= yCharValue;
		value = eval(xVarByte + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment char : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar ^= xCharValue;
		value = eval(yVarByte + xorAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar ^= yCharValue;
		value = eval(yVarByte + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment char : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteXorAssignmentShort() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar ^= xShortValue;
		IValue value = eval(xVarByte + xorAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar ^= yShortValue;
		value = eval(xVarByte + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment short : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar ^= xShortValue;
		value = eval(yVarByte + xorAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar ^= yShortValue;
		value = eval(yVarByte + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment short : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteXorAssignmentInt() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar ^= xIntValue;
		IValue value = eval(xVarByte + xorAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar ^= yIntValue;
		value = eval(xVarByte + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment int : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar ^= xIntValue;
		value = eval(yVarByte + xorAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar ^= yIntValue;
		value = eval(yVarByte + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment int : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testByteXorAssignmentLong() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		tmpxVar ^= xLongValue;
		IValue value = eval(xVarByte + xorAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		tmpxVar ^= yLongValue;
		value = eval(xVarByte + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment long : wrong result : ", tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		tmpyVar ^= xLongValue;
		value = eval(yVarByte + xorAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);

		tmpyVar ^= yLongValue;
		value = eval(yVarByte + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xorAssignment long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte xorAssignment long : wrong result : ", tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}


}
