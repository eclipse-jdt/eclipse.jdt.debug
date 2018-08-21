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

public class DoubleAssignmentOperatorsTests extends Tests {

	public DoubleAssignmentOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// double += {byte, char, short, int, long, float, double}

	public void testDoublePlusAssignmentByte() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar += xByteValue;
		IValue value = eval(xVarDouble + plusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar += yByteValue;
		value = eval(xVarDouble + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar += xByteValue;
		value = eval(yVarDouble + plusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar += yByteValue;
		value = eval(yVarDouble + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusAssignmentChar() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar += xCharValue;
		IValue value = eval(xVarDouble + plusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar += yCharValue;
		value = eval(xVarDouble + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar += xCharValue;
		value = eval(yVarDouble + plusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar += yCharValue;
		value = eval(yVarDouble + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusAssignmentShort() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar += xShortValue;
		IValue value = eval(xVarDouble + plusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar += yShortValue;
		value = eval(xVarDouble + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar += xShortValue;
		value = eval(yVarDouble + plusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar += yShortValue;
		value = eval(yVarDouble + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusAssignmentInt() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar += xIntValue;
		IValue value = eval(xVarDouble + plusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar += yIntValue;
		value = eval(xVarDouble + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar += xIntValue;
		value = eval(yVarDouble + plusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar += yIntValue;
		value = eval(yVarDouble + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusAssignmentLong() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar += xLongValue;
		IValue value = eval(xVarDouble + plusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar += yLongValue;
		value = eval(xVarDouble + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar += xLongValue;
		value = eval(yVarDouble + plusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar += yLongValue;
		value = eval(yVarDouble + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusAssignmentFloat() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar += xFloatValue;
		IValue value = eval(xVarDouble + plusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar += yFloatValue;
		value = eval(xVarDouble + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar += xFloatValue;
		value = eval(yVarDouble + plusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar += yFloatValue;
		value = eval(yVarDouble + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusAssignmentDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar += xDoubleValue;
		IValue value = eval(xVarDouble + plusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar += yDoubleValue;
		value = eval(xVarDouble + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar += xDoubleValue;
		value = eval(yVarDouble + plusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar += yDoubleValue;
		value = eval(yVarDouble + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double plusAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plusAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	// double -= {byte, char, short, int, long, float, double}

	public void testDoubleMinusAssignmentByte() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar -= xByteValue;
		IValue value = eval(xVarDouble + minusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar -= yByteValue;
		value = eval(xVarDouble + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar -= xByteValue;
		value = eval(yVarDouble + minusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar -= yByteValue;
		value = eval(yVarDouble + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusAssignmentChar() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar -= xCharValue;
		IValue value = eval(xVarDouble + minusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar -= yCharValue;
		value = eval(xVarDouble + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar -= xCharValue;
		value = eval(yVarDouble + minusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar -= yCharValue;
		value = eval(yVarDouble + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusAssignmentShort() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar -= xShortValue;
		IValue value = eval(xVarDouble + minusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar -= yShortValue;
		value = eval(xVarDouble + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar -= xShortValue;
		value = eval(yVarDouble + minusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar -= yShortValue;
		value = eval(yVarDouble + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusAssignmentInt() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar -= xIntValue;
		IValue value = eval(xVarDouble + minusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar -= yIntValue;
		value = eval(xVarDouble + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar -= xIntValue;
		value = eval(yVarDouble + minusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar -= yIntValue;
		value = eval(yVarDouble + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusAssignmentLong() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar -= xLongValue;
		IValue value = eval(xVarDouble + minusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar -= yLongValue;
		value = eval(xVarDouble + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar -= xLongValue;
		value = eval(yVarDouble + minusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar -= yLongValue;
		value = eval(yVarDouble + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusAssignmentFloat() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar -= xFloatValue;
		IValue value = eval(xVarDouble + minusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar -= yFloatValue;
		value = eval(xVarDouble + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar -= xFloatValue;
		value = eval(yVarDouble + minusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar -= yFloatValue;
		value = eval(yVarDouble + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusAssignmentDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar -= xDoubleValue;
		IValue value = eval(xVarDouble + minusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar -= yDoubleValue;
		value = eval(xVarDouble + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar -= xDoubleValue;
		value = eval(yVarDouble + minusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar -= yDoubleValue;
		value = eval(yVarDouble + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double minusAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minusAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	// double *= {byte, char, short, int, long, float, double}

	public void testDoubleMultiplyAssignmentByte() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar *= xByteValue;
		IValue value = eval(xVarDouble + multiplyAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar *= yByteValue;
		value = eval(xVarDouble + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar *= xByteValue;
		value = eval(yVarDouble + multiplyAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar *= yByteValue;
		value = eval(yVarDouble + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyAssignmentChar() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar *= xCharValue;
		IValue value = eval(xVarDouble + multiplyAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar *= yCharValue;
		value = eval(xVarDouble + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar *= xCharValue;
		value = eval(yVarDouble + multiplyAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar *= yCharValue;
		value = eval(yVarDouble + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyAssignmentShort() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar *= xShortValue;
		IValue value = eval(xVarDouble + multiplyAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar *= yShortValue;
		value = eval(xVarDouble + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar *= xShortValue;
		value = eval(yVarDouble + multiplyAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar *= yShortValue;
		value = eval(yVarDouble + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyAssignmentInt() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar *= xIntValue;
		IValue value = eval(xVarDouble + multiplyAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar *= yIntValue;
		value = eval(xVarDouble + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar *= xIntValue;
		value = eval(yVarDouble + multiplyAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar *= yIntValue;
		value = eval(yVarDouble + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyAssignmentLong() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar *= xLongValue;
		IValue value = eval(xVarDouble + multiplyAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar *= yLongValue;
		value = eval(xVarDouble + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar *= xLongValue;
		value = eval(yVarDouble + multiplyAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar *= yLongValue;
		value = eval(yVarDouble + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyAssignmentFloat() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar *= xFloatValue;
		IValue value = eval(xVarDouble + multiplyAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar *= yFloatValue;
		value = eval(xVarDouble + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar *= xFloatValue;
		value = eval(yVarDouble + multiplyAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar *= yFloatValue;
		value = eval(yVarDouble + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyAssignmentDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar *= xDoubleValue;
		IValue value = eval(xVarDouble + multiplyAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar *= yDoubleValue;
		value = eval(xVarDouble + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar *= xDoubleValue;
		value = eval(yVarDouble + multiplyAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar *= yDoubleValue;
		value = eval(yVarDouble + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiplyAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiplyAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	// double /= {byte, char, short, int, long, float, double}

	public void testDoubleDivideAssignmentByte() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar /= xByteValue;
		IValue value = eval(xVarDouble + divideAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar /= yByteValue;
		value = eval(xVarDouble + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar /= xByteValue;
		value = eval(yVarDouble + divideAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar /= yByteValue;
		value = eval(yVarDouble + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideAssignmentChar() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar /= xCharValue;
		IValue value = eval(xVarDouble + divideAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar /= yCharValue;
		value = eval(xVarDouble + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar /= xCharValue;
		value = eval(yVarDouble + divideAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar /= yCharValue;
		value = eval(yVarDouble + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideAssignmentShort() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar /= xShortValue;
		IValue value = eval(xVarDouble + divideAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar /= yShortValue;
		value = eval(xVarDouble + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar /= xShortValue;
		value = eval(yVarDouble + divideAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar /= yShortValue;
		value = eval(yVarDouble + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideAssignmentInt() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar /= xIntValue;
		IValue value = eval(xVarDouble + divideAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar /= yIntValue;
		value = eval(xVarDouble + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar /= xIntValue;
		value = eval(yVarDouble + divideAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar /= yIntValue;
		value = eval(yVarDouble + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideAssignmentLong() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar /= xLongValue;
		IValue value = eval(xVarDouble + divideAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar /= yLongValue;
		value = eval(xVarDouble + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar /= xLongValue;
		value = eval(yVarDouble + divideAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar /= yLongValue;
		value = eval(yVarDouble + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideAssignmentFloat() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar /= xFloatValue;
		IValue value = eval(xVarDouble + divideAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar /= yFloatValue;
		value = eval(xVarDouble + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar /= xFloatValue;
		value = eval(yVarDouble + divideAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar /= yFloatValue;
		value = eval(yVarDouble + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideAssignmentDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar /= xDoubleValue;
		IValue value = eval(xVarDouble + divideAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar /= yDoubleValue;
		value = eval(xVarDouble + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar /= xDoubleValue;
		value = eval(yVarDouble + divideAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar /= yDoubleValue;
		value = eval(yVarDouble + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double divideAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divideAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	// double %= {byte, char, short, int, long, float, double}

	public void testDoubleRemainderAssignmentByte() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar %= xByteValue;
		IValue value = eval(xVarDouble + remainderAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar %= yByteValue;
		value = eval(xVarDouble + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment byte : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar %= xByteValue;
		value = eval(yVarDouble + remainderAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar %= yByteValue;
		value = eval(yVarDouble + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment byte : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderAssignmentChar() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar %= xCharValue;
		IValue value = eval(xVarDouble + remainderAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar %= yCharValue;
		value = eval(xVarDouble + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment char : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar %= xCharValue;
		value = eval(yVarDouble + remainderAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar %= yCharValue;
		value = eval(yVarDouble + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment char : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderAssignmentShort() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar %= xShortValue;
		IValue value = eval(xVarDouble + remainderAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar %= yShortValue;
		value = eval(xVarDouble + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment short : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar %= xShortValue;
		value = eval(yVarDouble + remainderAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar %= yShortValue;
		value = eval(yVarDouble + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment short : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderAssignmentInt() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar %= xIntValue;
		IValue value = eval(xVarDouble + remainderAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar %= yIntValue;
		value = eval(xVarDouble + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment int : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar %= xIntValue;
		value = eval(yVarDouble + remainderAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar %= yIntValue;
		value = eval(yVarDouble + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment int : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderAssignmentLong() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar %= xLongValue;
		IValue value = eval(xVarDouble + remainderAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar %= yLongValue;
		value = eval(xVarDouble + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment long : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar %= xLongValue;
		value = eval(yVarDouble + remainderAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar %= yLongValue;
		value = eval(yVarDouble + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment long : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderAssignmentFloat() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar %= xFloatValue;
		IValue value = eval(xVarDouble + remainderAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar %= yFloatValue;
		value = eval(xVarDouble + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment float : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar %= xFloatValue;
		value = eval(yVarDouble + remainderAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar %= yFloatValue;
		value = eval(yVarDouble + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment float : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderAssignmentDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		tmpxVar %= xDoubleValue;
		IValue value = eval(xVarDouble + remainderAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		tmpxVar %= yDoubleValue;
		value = eval(xVarDouble + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment double : wrong result : ", tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		tmpyVar %= xDoubleValue;
		value = eval(yVarDouble + remainderAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);

		tmpyVar %= yDoubleValue;
		value = eval(yVarDouble + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainderAssignment double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainderAssignment double : wrong result : ", tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}


}
