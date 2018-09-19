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

public class LongAssignmentOperatorsTests extends Tests {

	public LongAssignmentOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// long += {byte, char, short, int, long, float, double}

	public void testLongPlusAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar += xByteValue;
		IValue value = eval(xVarLong + plusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar += yByteValue;
		value = eval(xVarLong + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar += xByteValue;
		value = eval(yVarLong + plusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar += yByteValue;
		value = eval(yVarLong + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar += xCharValue;
		IValue value = eval(xVarLong + plusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar += yCharValue;
		value = eval(xVarLong + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar += xCharValue;
		value = eval(yVarLong + plusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar += yCharValue;
		value = eval(yVarLong + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar += xShortValue;
		IValue value = eval(xVarLong + plusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar += yShortValue;
		value = eval(xVarLong + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar += xShortValue;
		value = eval(yVarLong + plusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar += yShortValue;
		value = eval(yVarLong + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar += xIntValue;
		IValue value = eval(xVarLong + plusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar += yIntValue;
		value = eval(xVarLong + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar += xIntValue;
		value = eval(yVarLong + plusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar += yIntValue;
		value = eval(yVarLong + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar += xLongValue;
		IValue value = eval(xVarLong + plusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar += yLongValue;
		value = eval(xVarLong + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar += xLongValue;
		value = eval(yVarLong + plusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar += yLongValue;
		value = eval(yVarLong + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusAssignmentFloat() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar += xFloatValue;
		IValue value = eval(xVarLong + plusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment float : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar += yFloatValue;
		value = eval(xVarLong + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar += xFloatValue;
		value = eval(yVarLong + plusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar += yFloatValue;
		value = eval(yVarLong + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusAssignmentDouble() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar += xDoubleValue;
		IValue value = eval(xVarLong + plusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment double : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar += yDoubleValue;
		value = eval(xVarLong + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar += xDoubleValue;
		value = eval(yVarLong + plusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar += yDoubleValue;
		value = eval(yVarLong + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long plusAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plusAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long -= {byte, char, short, int, long, float, double}

	public void testLongMinusAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar -= xByteValue;
		IValue value = eval(xVarLong + minusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar -= yByteValue;
		value = eval(xVarLong + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar -= xByteValue;
		value = eval(yVarLong + minusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar -= yByteValue;
		value = eval(yVarLong + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar -= xCharValue;
		IValue value = eval(xVarLong + minusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar -= yCharValue;
		value = eval(xVarLong + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar -= xCharValue;
		value = eval(yVarLong + minusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar -= yCharValue;
		value = eval(yVarLong + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar -= xShortValue;
		IValue value = eval(xVarLong + minusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar -= yShortValue;
		value = eval(xVarLong + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar -= xShortValue;
		value = eval(yVarLong + minusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar -= yShortValue;
		value = eval(yVarLong + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar -= xIntValue;
		IValue value = eval(xVarLong + minusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar -= yIntValue;
		value = eval(xVarLong + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar -= xIntValue;
		value = eval(yVarLong + minusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar -= yIntValue;
		value = eval(yVarLong + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar -= xLongValue;
		IValue value = eval(xVarLong + minusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar -= yLongValue;
		value = eval(xVarLong + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar -= xLongValue;
		value = eval(yVarLong + minusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar -= yLongValue;
		value = eval(yVarLong + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusAssignmentFloat() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar -= xFloatValue;
		IValue value = eval(xVarLong + minusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment float : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar -= yFloatValue;
		value = eval(xVarLong + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar -= xFloatValue;
		value = eval(yVarLong + minusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar -= yFloatValue;
		value = eval(yVarLong + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusAssignmentDouble() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar -= xDoubleValue;
		IValue value = eval(xVarLong + minusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment double : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar -= yDoubleValue;
		value = eval(xVarLong + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar -= xDoubleValue;
		value = eval(yVarLong + minusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar -= yDoubleValue;
		value = eval(yVarLong + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long minusAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minusAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long *= {byte, char, short, int, long, float, double}

	public void testLongMultiplyAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar *= xByteValue;
		IValue value = eval(xVarLong + multiplyAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar *= yByteValue;
		value = eval(xVarLong + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar *= xByteValue;
		value = eval(yVarLong + multiplyAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar *= yByteValue;
		value = eval(yVarLong + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar *= xCharValue;
		IValue value = eval(xVarLong + multiplyAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar *= yCharValue;
		value = eval(xVarLong + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar *= xCharValue;
		value = eval(yVarLong + multiplyAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar *= yCharValue;
		value = eval(yVarLong + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar *= xShortValue;
		IValue value = eval(xVarLong + multiplyAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar *= yShortValue;
		value = eval(xVarLong + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar *= xShortValue;
		value = eval(yVarLong + multiplyAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar *= yShortValue;
		value = eval(yVarLong + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar *= xIntValue;
		IValue value = eval(xVarLong + multiplyAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar *= yIntValue;
		value = eval(xVarLong + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar *= xIntValue;
		value = eval(yVarLong + multiplyAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar *= yIntValue;
		value = eval(yVarLong + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar *= xLongValue;
		IValue value = eval(xVarLong + multiplyAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar *= yLongValue;
		value = eval(xVarLong + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar *= xLongValue;
		value = eval(yVarLong + multiplyAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar *= yLongValue;
		value = eval(yVarLong + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyAssignmentFloat() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar *= xFloatValue;
		IValue value = eval(xVarLong + multiplyAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment float : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar *= yFloatValue;
		value = eval(xVarLong + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar *= xFloatValue;
		value = eval(yVarLong + multiplyAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar *= yFloatValue;
		value = eval(yVarLong + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyAssignmentDouble() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar *= xDoubleValue;
		IValue value = eval(xVarLong + multiplyAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment double : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar *= yDoubleValue;
		value = eval(xVarLong + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar *= xDoubleValue;
		value = eval(yVarLong + multiplyAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar *= yDoubleValue;
		value = eval(yVarLong + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiplyAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiplyAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long /= {byte, char, short, int, long, float, double}

	public void testLongDivideAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar /= xByteValue;
		IValue value = eval(xVarLong + divideAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar /= yByteValue;
		value = eval(xVarLong + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar /= xByteValue;
		value = eval(yVarLong + divideAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar /= yByteValue;
		value = eval(yVarLong + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar /= xCharValue;
		IValue value = eval(xVarLong + divideAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar /= yCharValue;
		value = eval(xVarLong + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar /= xCharValue;
		value = eval(yVarLong + divideAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar /= yCharValue;
		value = eval(yVarLong + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar /= xShortValue;
		IValue value = eval(xVarLong + divideAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar /= yShortValue;
		value = eval(xVarLong + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar /= xShortValue;
		value = eval(yVarLong + divideAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar /= yShortValue;
		value = eval(yVarLong + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar /= xIntValue;
		IValue value = eval(xVarLong + divideAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar /= yIntValue;
		value = eval(xVarLong + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar /= xIntValue;
		value = eval(yVarLong + divideAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar /= yIntValue;
		value = eval(yVarLong + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar /= xLongValue;
		IValue value = eval(xVarLong + divideAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar /= yLongValue;
		value = eval(xVarLong + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar /= xLongValue;
		value = eval(yVarLong + divideAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar /= yLongValue;
		value = eval(yVarLong + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideAssignmentFloat() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar /= xFloatValue;
		IValue value = eval(xVarLong + divideAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment float : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar /= yFloatValue;
		value = eval(xVarLong + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar /= xFloatValue;
		value = eval(yVarLong + divideAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar /= yFloatValue;
		value = eval(yVarLong + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideAssignmentDouble() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar /= xDoubleValue;
		IValue value = eval(xVarLong + divideAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment double : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar /= yDoubleValue;
		value = eval(xVarLong + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar /= xDoubleValue;
		value = eval(yVarLong + divideAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar /= yDoubleValue;
		value = eval(yVarLong + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long divideAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divideAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long %= {byte, char, short, int, long, float, double}

	public void testLongRemainderAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar %= xByteValue;
		IValue value = eval(xVarLong + remainderAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar %= yByteValue;
		value = eval(xVarLong + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar %= xByteValue;
		value = eval(yVarLong + remainderAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar %= yByteValue;
		value = eval(yVarLong + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar %= xCharValue;
		IValue value = eval(xVarLong + remainderAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar %= yCharValue;
		value = eval(xVarLong + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar %= xCharValue;
		value = eval(yVarLong + remainderAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar %= yCharValue;
		value = eval(yVarLong + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar %= xShortValue;
		IValue value = eval(xVarLong + remainderAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar %= yShortValue;
		value = eval(xVarLong + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar %= xShortValue;
		value = eval(yVarLong + remainderAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar %= yShortValue;
		value = eval(yVarLong + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar %= xIntValue;
		IValue value = eval(xVarLong + remainderAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar %= yIntValue;
		value = eval(xVarLong + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar %= xIntValue;
		value = eval(yVarLong + remainderAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar %= yIntValue;
		value = eval(yVarLong + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar %= xLongValue;
		IValue value = eval(xVarLong + remainderAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar %= yLongValue;
		value = eval(xVarLong + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar %= xLongValue;
		value = eval(yVarLong + remainderAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar %= yLongValue;
		value = eval(yVarLong + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderAssignmentFloat() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar %= xFloatValue;
		IValue value = eval(xVarLong + remainderAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment float : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar %= yFloatValue;
		value = eval(xVarLong + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment float : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar %= xFloatValue;
		value = eval(yVarLong + remainderAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar %= yFloatValue;
		value = eval(yVarLong + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment float : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderAssignmentDouble() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar %= xDoubleValue;
		IValue value = eval(xVarLong + remainderAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment double : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar %= yDoubleValue;
		value = eval(xVarLong + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment double : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar %= xDoubleValue;
		value = eval(yVarLong + remainderAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar %= yDoubleValue;
		value = eval(yVarLong + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainderAssignment double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainderAssignment double : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long <<= {byte, char, short, int, long, float, double}

	public void testLongLeftShiftAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar <<= xByteValue;
		IValue value = eval(xVarLong + leftShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar <<= yByteValue;
		value = eval(xVarLong + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar <<= xByteValue;
		value = eval(yVarLong + leftShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar <<= yByteValue;
		value = eval(yVarLong + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongLeftShiftAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar <<= xCharValue;
		IValue value = eval(xVarLong + leftShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar <<= yCharValue;
		value = eval(xVarLong + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar <<= xCharValue;
		value = eval(yVarLong + leftShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar <<= yCharValue;
		value = eval(yVarLong + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongLeftShiftAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar <<= xShortValue;
		IValue value = eval(xVarLong + leftShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar <<= yShortValue;
		value = eval(xVarLong + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar <<= xShortValue;
		value = eval(yVarLong + leftShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar <<= yShortValue;
		value = eval(yVarLong + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongLeftShiftAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar <<= xIntValue;
		IValue value = eval(xVarLong + leftShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar <<= yIntValue;
		value = eval(xVarLong + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar <<= xIntValue;
		value = eval(yVarLong + leftShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar <<= yIntValue;
		value = eval(yVarLong + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongLeftShiftAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar <<= xLongValue;
		IValue value = eval(xVarLong + leftShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar <<= yLongValue;
		value = eval(xVarLong + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar <<= xLongValue;
		value = eval(yVarLong + leftShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar <<= yLongValue;
		value = eval(yVarLong + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShiftAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long >>= {byte, char, short, int, long, float, double}

	public void testLongRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>= xByteValue;
		IValue value = eval(xVarLong + rightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>= yByteValue;
		value = eval(xVarLong + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>= xByteValue;
		value = eval(yVarLong + rightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>= yByteValue;
		value = eval(yVarLong + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>= xCharValue;
		IValue value = eval(xVarLong + rightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>= yCharValue;
		value = eval(xVarLong + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>= xCharValue;
		value = eval(yVarLong + rightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>= yCharValue;
		value = eval(yVarLong + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>= xShortValue;
		IValue value = eval(xVarLong + rightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>= yShortValue;
		value = eval(xVarLong + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>= xShortValue;
		value = eval(yVarLong + rightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>= yShortValue;
		value = eval(yVarLong + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>= xIntValue;
		IValue value = eval(xVarLong + rightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>= yIntValue;
		value = eval(xVarLong + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>= xIntValue;
		value = eval(yVarLong + rightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>= yIntValue;
		value = eval(yVarLong + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>= xLongValue;
		IValue value = eval(xVarLong + rightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>= yLongValue;
		value = eval(xVarLong + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>= xLongValue;
		value = eval(yVarLong + rightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>= yLongValue;
		value = eval(yVarLong + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShiftAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long >>>= {byte, char, short, int, long, float, double}

	public void testLongUnsignedRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>>= xByteValue;
		IValue value = eval(xVarLong + unsignedRightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>>= yByteValue;
		value = eval(xVarLong + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>>= xByteValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>>= yByteValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongUnsignedRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>>= xCharValue;
		IValue value = eval(xVarLong + unsignedRightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>>= yCharValue;
		value = eval(xVarLong + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>>= xCharValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>>= yCharValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongUnsignedRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>>= xShortValue;
		IValue value = eval(xVarLong + unsignedRightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>>= yShortValue;
		value = eval(xVarLong + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>>= xShortValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>>= yShortValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongUnsignedRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>>= xIntValue;
		IValue value = eval(xVarLong + unsignedRightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>>= yIntValue;
		value = eval(xVarLong + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>>= xIntValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>>= yIntValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongUnsignedRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar >>>= xLongValue;
		IValue value = eval(xVarLong + unsignedRightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar >>>= yLongValue;
		value = eval(xVarLong + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar >>>= xLongValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar >>>= yLongValue;
		value = eval(yVarLong + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShiftAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShiftAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long |= {byte, char, short, int, long, float, double}

	public void testLongOrAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar |= xByteValue;
		IValue value = eval(xVarLong + orAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar |= yByteValue;
		value = eval(xVarLong + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar |= xByteValue;
		value = eval(yVarLong + orAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar |= yByteValue;
		value = eval(yVarLong + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongOrAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar |= xCharValue;
		IValue value = eval(xVarLong + orAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar |= yCharValue;
		value = eval(xVarLong + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar |= xCharValue;
		value = eval(yVarLong + orAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar |= yCharValue;
		value = eval(yVarLong + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongOrAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar |= xShortValue;
		IValue value = eval(xVarLong + orAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar |= yShortValue;
		value = eval(xVarLong + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar |= xShortValue;
		value = eval(yVarLong + orAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar |= yShortValue;
		value = eval(yVarLong + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongOrAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar |= xIntValue;
		IValue value = eval(xVarLong + orAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar |= yIntValue;
		value = eval(xVarLong + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar |= xIntValue;
		value = eval(yVarLong + orAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar |= yIntValue;
		value = eval(yVarLong + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongOrAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar |= xLongValue;
		IValue value = eval(xVarLong + orAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar |= yLongValue;
		value = eval(xVarLong + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar |= xLongValue;
		value = eval(yVarLong + orAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar |= yLongValue;
		value = eval(yVarLong + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long orAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long orAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long &= {byte, char, short, int, long, float, double}

	public void testLongAndAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar &= xByteValue;
		IValue value = eval(xVarLong + andAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar &= yByteValue;
		value = eval(xVarLong + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar &= xByteValue;
		value = eval(yVarLong + andAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar &= yByteValue;
		value = eval(yVarLong + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongAndAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar &= xCharValue;
		IValue value = eval(xVarLong + andAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar &= yCharValue;
		value = eval(xVarLong + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar &= xCharValue;
		value = eval(yVarLong + andAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar &= yCharValue;
		value = eval(yVarLong + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongAndAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar &= xShortValue;
		IValue value = eval(xVarLong + andAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar &= yShortValue;
		value = eval(xVarLong + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar &= xShortValue;
		value = eval(yVarLong + andAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar &= yShortValue;
		value = eval(yVarLong + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongAndAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar &= xIntValue;
		IValue value = eval(xVarLong + andAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar &= yIntValue;
		value = eval(xVarLong + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar &= xIntValue;
		value = eval(yVarLong + andAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar &= yIntValue;
		value = eval(yVarLong + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongAndAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar &= xLongValue;
		IValue value = eval(xVarLong + andAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar &= yLongValue;
		value = eval(xVarLong + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar &= xLongValue;
		value = eval(yVarLong + andAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar &= yLongValue;
		value = eval(yVarLong + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long andAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long andAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// long ^= {byte, char, short, int, long, float, double}

	public void testLongXorAssignmentByte() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar ^= xByteValue;
		IValue value = eval(xVarLong + xorAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar ^= yByteValue;
		value = eval(xVarLong + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment byte : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar ^= xByteValue;
		value = eval(yVarLong + xorAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar ^= yByteValue;
		value = eval(yVarLong + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment byte : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongXorAssignmentChar() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar ^= xCharValue;
		IValue value = eval(xVarLong + xorAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar ^= yCharValue;
		value = eval(xVarLong + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment char : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar ^= xCharValue;
		value = eval(yVarLong + xorAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar ^= yCharValue;
		value = eval(yVarLong + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment char : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongXorAssignmentShort() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar ^= xShortValue;
		IValue value = eval(xVarLong + xorAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar ^= yShortValue;
		value = eval(xVarLong + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment short : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar ^= xShortValue;
		value = eval(yVarLong + xorAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar ^= yShortValue;
		value = eval(yVarLong + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment short : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongXorAssignmentInt() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar ^= xIntValue;
		IValue value = eval(xVarLong + xorAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar ^= yIntValue;
		value = eval(xVarLong + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment int : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar ^= xIntValue;
		value = eval(yVarLong + xorAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar ^= yIntValue;
		value = eval(yVarLong + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment int : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testLongXorAssignmentLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		tmpxVar ^= xLongValue;
		IValue value = eval(xVarLong + xorAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		tmpxVar ^= yLongValue;
		value = eval(xVarLong + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment long : wrong result : ", tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		tmpyVar ^= xLongValue;
		value = eval(yVarLong + xorAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);

		tmpyVar ^= yLongValue;
		value = eval(yVarLong + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long xorAssignment long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xorAssignment long : wrong result : ", tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}


}
