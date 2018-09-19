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

public class ShortAssignmentOperatorsTests extends Tests {

	public ShortAssignmentOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// short += {byte, char, short, int, long, float, double}

	public void testShortPlusAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar += xByteValue;
		IValue value = eval(xVarShort + plusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar += yByteValue;
		value = eval(xVarShort + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar += xByteValue;
		value = eval(yVarShort + plusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar += yByteValue;
		value = eval(yVarShort + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortPlusAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar += xCharValue;
		IValue value = eval(xVarShort + plusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar += yCharValue;
		value = eval(xVarShort + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar += xCharValue;
		value = eval(yVarShort + plusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar += yCharValue;
		value = eval(yVarShort + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortPlusAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar += xShortValue;
		IValue value = eval(xVarShort + plusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar += yShortValue;
		value = eval(xVarShort + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar += xShortValue;
		value = eval(yVarShort + plusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar += yShortValue;
		value = eval(yVarShort + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortPlusAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar += xIntValue;
		IValue value = eval(xVarShort + plusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar += yIntValue;
		value = eval(xVarShort + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar += xIntValue;
		value = eval(yVarShort + plusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar += yIntValue;
		value = eval(yVarShort + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortPlusAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar += xLongValue;
		IValue value = eval(xVarShort + plusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar += yLongValue;
		value = eval(xVarShort + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar += xLongValue;
		value = eval(yVarShort + plusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar += yLongValue;
		value = eval(yVarShort + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortPlusAssignmentFloat() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar += xFloatValue;
		IValue value = eval(xVarShort + plusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment float : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar += yFloatValue;
		value = eval(xVarShort + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar += xFloatValue;
		value = eval(yVarShort + plusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar += yFloatValue;
		value = eval(yVarShort + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortPlusAssignmentDouble() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar += xDoubleValue;
		IValue value = eval(xVarShort + plusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment double : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar += yDoubleValue;
		value = eval(xVarShort + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar += xDoubleValue;
		value = eval(yVarShort + plusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar += yDoubleValue;
		value = eval(yVarShort + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short plusAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short plusAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short -= {byte, char, short, int, long, float, double}

	public void testShortMinusAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar -= xByteValue;
		IValue value = eval(xVarShort + minusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar -= yByteValue;
		value = eval(xVarShort + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar -= xByteValue;
		value = eval(yVarShort + minusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar -= yByteValue;
		value = eval(yVarShort + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMinusAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar -= xCharValue;
		IValue value = eval(xVarShort + minusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar -= yCharValue;
		value = eval(xVarShort + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar -= xCharValue;
		value = eval(yVarShort + minusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar -= yCharValue;
		value = eval(yVarShort + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMinusAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar -= xShortValue;
		IValue value = eval(xVarShort + minusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar -= yShortValue;
		value = eval(xVarShort + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar -= xShortValue;
		value = eval(yVarShort + minusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar -= yShortValue;
		value = eval(yVarShort + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMinusAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar -= xIntValue;
		IValue value = eval(xVarShort + minusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar -= yIntValue;
		value = eval(xVarShort + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar -= xIntValue;
		value = eval(yVarShort + minusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar -= yIntValue;
		value = eval(yVarShort + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMinusAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar -= xLongValue;
		IValue value = eval(xVarShort + minusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar -= yLongValue;
		value = eval(xVarShort + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar -= xLongValue;
		value = eval(yVarShort + minusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar -= yLongValue;
		value = eval(yVarShort + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMinusAssignmentFloat() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar -= xFloatValue;
		IValue value = eval(xVarShort + minusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment float : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar -= yFloatValue;
		value = eval(xVarShort + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar -= xFloatValue;
		value = eval(yVarShort + minusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar -= yFloatValue;
		value = eval(yVarShort + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMinusAssignmentDouble() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar -= xDoubleValue;
		IValue value = eval(xVarShort + minusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment double : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar -= yDoubleValue;
		value = eval(xVarShort + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar -= xDoubleValue;
		value = eval(yVarShort + minusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar -= yDoubleValue;
		value = eval(yVarShort + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short minusAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short minusAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short *= {byte, char, short, int, long, float, double}

	public void testShortMultiplyAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar *= xByteValue;
		IValue value = eval(xVarShort + multiplyAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar *= yByteValue;
		value = eval(xVarShort + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar *= xByteValue;
		value = eval(yVarShort + multiplyAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar *= yByteValue;
		value = eval(yVarShort + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar *= xCharValue;
		IValue value = eval(xVarShort + multiplyAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar *= yCharValue;
		value = eval(xVarShort + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar *= xCharValue;
		value = eval(yVarShort + multiplyAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar *= yCharValue;
		value = eval(yVarShort + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar *= xShortValue;
		IValue value = eval(xVarShort + multiplyAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar *= yShortValue;
		value = eval(xVarShort + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar *= xShortValue;
		value = eval(yVarShort + multiplyAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar *= yShortValue;
		value = eval(yVarShort + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar *= xIntValue;
		IValue value = eval(xVarShort + multiplyAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar *= yIntValue;
		value = eval(xVarShort + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar *= xIntValue;
		value = eval(yVarShort + multiplyAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar *= yIntValue;
		value = eval(yVarShort + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar *= xLongValue;
		IValue value = eval(xVarShort + multiplyAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar *= yLongValue;
		value = eval(xVarShort + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar *= xLongValue;
		value = eval(yVarShort + multiplyAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar *= yLongValue;
		value = eval(yVarShort + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyAssignmentFloat() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar *= xFloatValue;
		IValue value = eval(xVarShort + multiplyAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment float : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar *= yFloatValue;
		value = eval(xVarShort + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar *= xFloatValue;
		value = eval(yVarShort + multiplyAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar *= yFloatValue;
		value = eval(yVarShort + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyAssignmentDouble() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar *= xDoubleValue;
		IValue value = eval(xVarShort + multiplyAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment double : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar *= yDoubleValue;
		value = eval(xVarShort + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar *= xDoubleValue;
		value = eval(yVarShort + multiplyAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar *= yDoubleValue;
		value = eval(yVarShort + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiplyAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short multiplyAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short /= {byte, char, short, int, long, float, double}

	public void testShortDivideAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar /= xByteValue;
		IValue value = eval(xVarShort + divideAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar /= yByteValue;
		value = eval(xVarShort + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar /= xByteValue;
		value = eval(yVarShort + divideAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar /= yByteValue;
		value = eval(yVarShort + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortDivideAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar /= xCharValue;
		IValue value = eval(xVarShort + divideAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar /= yCharValue;
		value = eval(xVarShort + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar /= xCharValue;
		value = eval(yVarShort + divideAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar /= yCharValue;
		value = eval(yVarShort + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortDivideAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar /= xShortValue;
		IValue value = eval(xVarShort + divideAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar /= yShortValue;
		value = eval(xVarShort + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar /= xShortValue;
		value = eval(yVarShort + divideAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar /= yShortValue;
		value = eval(yVarShort + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortDivideAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar /= xIntValue;
		IValue value = eval(xVarShort + divideAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar /= yIntValue;
		value = eval(xVarShort + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar /= xIntValue;
		value = eval(yVarShort + divideAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar /= yIntValue;
		value = eval(yVarShort + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortDivideAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar /= xLongValue;
		IValue value = eval(xVarShort + divideAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar /= yLongValue;
		value = eval(xVarShort + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar /= xLongValue;
		value = eval(yVarShort + divideAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar /= yLongValue;
		value = eval(yVarShort + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortDivideAssignmentFloat() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar /= xFloatValue;
		IValue value = eval(xVarShort + divideAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment float : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar /= yFloatValue;
		value = eval(xVarShort + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar /= xFloatValue;
		value = eval(yVarShort + divideAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar /= yFloatValue;
		value = eval(yVarShort + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortDivideAssignmentDouble() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar /= xDoubleValue;
		IValue value = eval(xVarShort + divideAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment double : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar /= yDoubleValue;
		value = eval(xVarShort + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar /= xDoubleValue;
		value = eval(yVarShort + divideAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar /= yDoubleValue;
		value = eval(yVarShort + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short divideAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short divideAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short %= {byte, char, short, int, long, float, double}

	public void testShortRemainderAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar %= xByteValue;
		IValue value = eval(xVarShort + remainderAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar %= yByteValue;
		value = eval(xVarShort + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar %= xByteValue;
		value = eval(yVarShort + remainderAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar %= yByteValue;
		value = eval(yVarShort + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar %= xCharValue;
		IValue value = eval(xVarShort + remainderAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar %= yCharValue;
		value = eval(xVarShort + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar %= xCharValue;
		value = eval(yVarShort + remainderAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar %= yCharValue;
		value = eval(yVarShort + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar %= xShortValue;
		IValue value = eval(xVarShort + remainderAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar %= yShortValue;
		value = eval(xVarShort + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar %= xShortValue;
		value = eval(yVarShort + remainderAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar %= yShortValue;
		value = eval(yVarShort + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar %= xIntValue;
		IValue value = eval(xVarShort + remainderAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar %= yIntValue;
		value = eval(xVarShort + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar %= xIntValue;
		value = eval(yVarShort + remainderAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar %= yIntValue;
		value = eval(yVarShort + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar %= xLongValue;
		IValue value = eval(xVarShort + remainderAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar %= yLongValue;
		value = eval(xVarShort + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar %= xLongValue;
		value = eval(yVarShort + remainderAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar %= yLongValue;
		value = eval(yVarShort + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderAssignmentFloat() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar %= xFloatValue;
		IValue value = eval(xVarShort + remainderAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment float : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar %= yFloatValue;
		value = eval(xVarShort + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment float : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar %= xFloatValue;
		value = eval(yVarShort + remainderAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar %= yFloatValue;
		value = eval(yVarShort + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment float : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderAssignmentDouble() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar %= xDoubleValue;
		IValue value = eval(xVarShort + remainderAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment double : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar %= yDoubleValue;
		value = eval(xVarShort + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment double : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar %= xDoubleValue;
		value = eval(yVarShort + remainderAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar %= yDoubleValue;
		value = eval(yVarShort + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainderAssignment double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short remainderAssignment double : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short <<= {byte, char, short, int, long, float, double}

	public void testShortLeftShiftAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar <<= xByteValue;
		IValue value = eval(xVarShort + leftShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar <<= yByteValue;
		value = eval(xVarShort + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar <<= xByteValue;
		value = eval(yVarShort + leftShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar <<= yByteValue;
		value = eval(yVarShort + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortLeftShiftAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar <<= xCharValue;
		IValue value = eval(xVarShort + leftShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar <<= yCharValue;
		value = eval(xVarShort + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar <<= xCharValue;
		value = eval(yVarShort + leftShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar <<= yCharValue;
		value = eval(yVarShort + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortLeftShiftAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar <<= xShortValue;
		IValue value = eval(xVarShort + leftShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar <<= yShortValue;
		value = eval(xVarShort + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar <<= xShortValue;
		value = eval(yVarShort + leftShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar <<= yShortValue;
		value = eval(yVarShort + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortLeftShiftAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar <<= xIntValue;
		IValue value = eval(xVarShort + leftShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar <<= yIntValue;
		value = eval(xVarShort + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar <<= xIntValue;
		value = eval(yVarShort + leftShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar <<= yIntValue;
		value = eval(yVarShort + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortLeftShiftAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar <<= xLongValue;
		IValue value = eval(xVarShort + leftShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar <<= yLongValue;
		value = eval(xVarShort + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar <<= xLongValue;
		value = eval(yVarShort + leftShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar <<= yLongValue;
		value = eval(yVarShort + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short leftShiftAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short >>= {byte, char, short, int, long, float, double}

	public void testShortRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>= xByteValue;
		IValue value = eval(xVarShort + rightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>= yByteValue;
		value = eval(xVarShort + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>= xByteValue;
		value = eval(yVarShort + rightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>= yByteValue;
		value = eval(yVarShort + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>= xCharValue;
		IValue value = eval(xVarShort + rightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>= yCharValue;
		value = eval(xVarShort + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>= xCharValue;
		value = eval(yVarShort + rightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>= yCharValue;
		value = eval(yVarShort + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>= xShortValue;
		IValue value = eval(xVarShort + rightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>= yShortValue;
		value = eval(xVarShort + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>= xShortValue;
		value = eval(yVarShort + rightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>= yShortValue;
		value = eval(yVarShort + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>= xIntValue;
		IValue value = eval(xVarShort + rightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>= yIntValue;
		value = eval(xVarShort + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>= xIntValue;
		value = eval(yVarShort + rightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>= yIntValue;
		value = eval(yVarShort + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>= xLongValue;
		IValue value = eval(xVarShort + rightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>= yLongValue;
		value = eval(xVarShort + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>= xLongValue;
		value = eval(yVarShort + rightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>= yLongValue;
		value = eval(yVarShort + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short rightShiftAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short >>>= {byte, char, short, int, long, float, double}

	public void testShortUnsignedRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>>= xByteValue;
		IValue value = eval(xVarShort + unsignedRightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>>= yByteValue;
		value = eval(xVarShort + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>>= xByteValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>>= yByteValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortUnsignedRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>>= xCharValue;
		IValue value = eval(xVarShort + unsignedRightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>>= yCharValue;
		value = eval(xVarShort + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>>= xCharValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>>= yCharValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortUnsignedRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>>= xShortValue;
		IValue value = eval(xVarShort + unsignedRightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>>= yShortValue;
		value = eval(xVarShort + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>>= xShortValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>>= yShortValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortUnsignedRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>>= xIntValue;
		IValue value = eval(xVarShort + unsignedRightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>>= yIntValue;
		value = eval(xVarShort + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>>= xIntValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>>= yIntValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortUnsignedRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar >>>= xLongValue;
		IValue value = eval(xVarShort + unsignedRightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar >>>= yLongValue;
		value = eval(xVarShort + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar >>>= xLongValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar >>>= yLongValue;
		value = eval(yVarShort + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShiftAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short unsignedRightShiftAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short |= {byte, char, short, int, long, float, double}

	public void testShortOrAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar |= xByteValue;
		IValue value = eval(xVarShort + orAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar |= yByteValue;
		value = eval(xVarShort + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar |= xByteValue;
		value = eval(yVarShort + orAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar |= yByteValue;
		value = eval(yVarShort + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortOrAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar |= xCharValue;
		IValue value = eval(xVarShort + orAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar |= yCharValue;
		value = eval(xVarShort + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar |= xCharValue;
		value = eval(yVarShort + orAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar |= yCharValue;
		value = eval(yVarShort + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortOrAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar |= xShortValue;
		IValue value = eval(xVarShort + orAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar |= yShortValue;
		value = eval(xVarShort + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar |= xShortValue;
		value = eval(yVarShort + orAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar |= yShortValue;
		value = eval(yVarShort + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortOrAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar |= xIntValue;
		IValue value = eval(xVarShort + orAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar |= yIntValue;
		value = eval(xVarShort + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar |= xIntValue;
		value = eval(yVarShort + orAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar |= yIntValue;
		value = eval(yVarShort + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortOrAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar |= xLongValue;
		IValue value = eval(xVarShort + orAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar |= yLongValue;
		value = eval(xVarShort + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar |= xLongValue;
		value = eval(yVarShort + orAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar |= yLongValue;
		value = eval(yVarShort + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short orAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short orAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short &= {byte, char, short, int, long, float, double}

	public void testShortAndAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar &= xByteValue;
		IValue value = eval(xVarShort + andAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar &= yByteValue;
		value = eval(xVarShort + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar &= xByteValue;
		value = eval(yVarShort + andAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar &= yByteValue;
		value = eval(yVarShort + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortAndAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar &= xCharValue;
		IValue value = eval(xVarShort + andAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar &= yCharValue;
		value = eval(xVarShort + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar &= xCharValue;
		value = eval(yVarShort + andAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar &= yCharValue;
		value = eval(yVarShort + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortAndAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar &= xShortValue;
		IValue value = eval(xVarShort + andAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar &= yShortValue;
		value = eval(xVarShort + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar &= xShortValue;
		value = eval(yVarShort + andAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar &= yShortValue;
		value = eval(yVarShort + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortAndAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar &= xIntValue;
		IValue value = eval(xVarShort + andAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar &= yIntValue;
		value = eval(xVarShort + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar &= xIntValue;
		value = eval(yVarShort + andAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar &= yIntValue;
		value = eval(yVarShort + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortAndAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar &= xLongValue;
		IValue value = eval(xVarShort + andAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar &= yLongValue;
		value = eval(xVarShort + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar &= xLongValue;
		value = eval(yVarShort + andAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar &= yLongValue;
		value = eval(yVarShort + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short andAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short andAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// short ^= {byte, char, short, int, long, float, double}

	public void testShortXorAssignmentByte() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar ^= xByteValue;
		IValue value = eval(xVarShort + xorAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar ^= yByteValue;
		value = eval(xVarShort + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment byte : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar ^= xByteValue;
		value = eval(yVarShort + xorAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar ^= yByteValue;
		value = eval(yVarShort + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment byte : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortXorAssignmentChar() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar ^= xCharValue;
		IValue value = eval(xVarShort + xorAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar ^= yCharValue;
		value = eval(xVarShort + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment char : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar ^= xCharValue;
		value = eval(yVarShort + xorAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar ^= yCharValue;
		value = eval(yVarShort + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment char : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortXorAssignmentShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar ^= xShortValue;
		IValue value = eval(xVarShort + xorAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar ^= yShortValue;
		value = eval(xVarShort + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment short : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar ^= xShortValue;
		value = eval(yVarShort + xorAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar ^= yShortValue;
		value = eval(yVarShort + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment short : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortXorAssignmentInt() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar ^= xIntValue;
		IValue value = eval(xVarShort + xorAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar ^= yIntValue;
		value = eval(xVarShort + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment int : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar ^= xIntValue;
		value = eval(yVarShort + xorAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar ^= yIntValue;
		value = eval(yVarShort + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment int : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testShortXorAssignmentLong() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		tmpxVar ^= xLongValue;
		IValue value = eval(xVarShort + xorAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		tmpxVar ^= yLongValue;
		value = eval(xVarShort + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment long : wrong result : ", tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		tmpyVar ^= xLongValue;
		value = eval(yVarShort + xorAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);

		tmpyVar ^= yLongValue;
		value = eval(yVarShort + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short xorAssignment long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short xorAssignment long : wrong result : ", tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}


}
