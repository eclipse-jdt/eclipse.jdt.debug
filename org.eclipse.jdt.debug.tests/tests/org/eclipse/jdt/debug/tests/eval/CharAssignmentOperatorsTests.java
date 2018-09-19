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

public class CharAssignmentOperatorsTests extends Tests {

	public CharAssignmentOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// char += {byte, char, short, int, long, float, double}

	public void testCharPlusAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar += xByteValue;
		IValue value = eval(xVarChar + plusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar += yByteValue;
		value = eval(xVarChar + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar += xByteValue;
		value = eval(yVarChar + plusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar += yByteValue;
		value = eval(yVarChar + plusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharPlusAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar += xCharValue;
		IValue value = eval(xVarChar + plusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar += yCharValue;
		value = eval(xVarChar + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar += xCharValue;
		value = eval(yVarChar + plusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar += yCharValue;
		value = eval(yVarChar + plusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharPlusAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar += xShortValue;
		IValue value = eval(xVarChar + plusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar += yShortValue;
		value = eval(xVarChar + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar += xShortValue;
		value = eval(yVarChar + plusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar += yShortValue;
		value = eval(yVarChar + plusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharPlusAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar += xIntValue;
		IValue value = eval(xVarChar + plusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar += yIntValue;
		value = eval(xVarChar + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar += xIntValue;
		value = eval(yVarChar + plusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar += yIntValue;
		value = eval(yVarChar + plusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharPlusAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar += xLongValue;
		IValue value = eval(xVarChar + plusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar += yLongValue;
		value = eval(xVarChar + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar += xLongValue;
		value = eval(yVarChar + plusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar += yLongValue;
		value = eval(yVarChar + plusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharPlusAssignmentFloat() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar += xFloatValue;
		IValue value = eval(xVarChar + plusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment float : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar += yFloatValue;
		value = eval(xVarChar + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar += xFloatValue;
		value = eval(yVarChar + plusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar += yFloatValue;
		value = eval(yVarChar + plusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharPlusAssignmentDouble() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar += xDoubleValue;
		IValue value = eval(xVarChar + plusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment double : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar += yDoubleValue;
		value = eval(xVarChar + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar += xDoubleValue;
		value = eval(yVarChar + plusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar += yDoubleValue;
		value = eval(yVarChar + plusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char plusAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char plusAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char -= {byte, char, short, int, long, float, double}

	public void testCharMinusAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar -= xByteValue;
		IValue value = eval(xVarChar + minusAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar -= yByteValue;
		value = eval(xVarChar + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar -= xByteValue;
		value = eval(yVarChar + minusAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar -= yByteValue;
		value = eval(yVarChar + minusAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMinusAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar -= xCharValue;
		IValue value = eval(xVarChar + minusAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar -= yCharValue;
		value = eval(xVarChar + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar -= xCharValue;
		value = eval(yVarChar + minusAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar -= yCharValue;
		value = eval(yVarChar + minusAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMinusAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar -= xShortValue;
		IValue value = eval(xVarChar + minusAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar -= yShortValue;
		value = eval(xVarChar + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar -= xShortValue;
		value = eval(yVarChar + minusAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar -= yShortValue;
		value = eval(yVarChar + minusAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMinusAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar -= xIntValue;
		IValue value = eval(xVarChar + minusAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar -= yIntValue;
		value = eval(xVarChar + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar -= xIntValue;
		value = eval(yVarChar + minusAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar -= yIntValue;
		value = eval(yVarChar + minusAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMinusAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar -= xLongValue;
		IValue value = eval(xVarChar + minusAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar -= yLongValue;
		value = eval(xVarChar + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar -= xLongValue;
		value = eval(yVarChar + minusAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar -= yLongValue;
		value = eval(yVarChar + minusAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMinusAssignmentFloat() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar -= xFloatValue;
		IValue value = eval(xVarChar + minusAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment float : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar -= yFloatValue;
		value = eval(xVarChar + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar -= xFloatValue;
		value = eval(yVarChar + minusAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar -= yFloatValue;
		value = eval(yVarChar + minusAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMinusAssignmentDouble() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar -= xDoubleValue;
		IValue value = eval(xVarChar + minusAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment double : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar -= yDoubleValue;
		value = eval(xVarChar + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar -= xDoubleValue;
		value = eval(yVarChar + minusAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar -= yDoubleValue;
		value = eval(yVarChar + minusAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char minusAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char minusAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char *= {byte, char, short, int, long, float, double}

	public void testCharMultiplyAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar *= xByteValue;
		IValue value = eval(xVarChar + multiplyAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar *= yByteValue;
		value = eval(xVarChar + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar *= xByteValue;
		value = eval(yVarChar + multiplyAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar *= yByteValue;
		value = eval(yVarChar + multiplyAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar *= xCharValue;
		IValue value = eval(xVarChar + multiplyAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar *= yCharValue;
		value = eval(xVarChar + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar *= xCharValue;
		value = eval(yVarChar + multiplyAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar *= yCharValue;
		value = eval(yVarChar + multiplyAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar *= xShortValue;
		IValue value = eval(xVarChar + multiplyAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar *= yShortValue;
		value = eval(xVarChar + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar *= xShortValue;
		value = eval(yVarChar + multiplyAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar *= yShortValue;
		value = eval(yVarChar + multiplyAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar *= xIntValue;
		IValue value = eval(xVarChar + multiplyAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar *= yIntValue;
		value = eval(xVarChar + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar *= xIntValue;
		value = eval(yVarChar + multiplyAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar *= yIntValue;
		value = eval(yVarChar + multiplyAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar *= xLongValue;
		IValue value = eval(xVarChar + multiplyAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar *= yLongValue;
		value = eval(xVarChar + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar *= xLongValue;
		value = eval(yVarChar + multiplyAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar *= yLongValue;
		value = eval(yVarChar + multiplyAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyAssignmentFloat() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar *= xFloatValue;
		IValue value = eval(xVarChar + multiplyAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment float : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar *= yFloatValue;
		value = eval(xVarChar + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar *= xFloatValue;
		value = eval(yVarChar + multiplyAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar *= yFloatValue;
		value = eval(yVarChar + multiplyAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyAssignmentDouble() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar *= xDoubleValue;
		IValue value = eval(xVarChar + multiplyAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment double : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar *= yDoubleValue;
		value = eval(xVarChar + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar *= xDoubleValue;
		value = eval(yVarChar + multiplyAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar *= yDoubleValue;
		value = eval(yVarChar + multiplyAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiplyAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char multiplyAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char /= {byte, char, short, int, long, float, double}

	public void testCharDivideAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar /= xByteValue;
		IValue value = eval(xVarChar + divideAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar /= yByteValue;
		value = eval(xVarChar + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar /= xByteValue;
		value = eval(yVarChar + divideAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar /= yByteValue;
		value = eval(yVarChar + divideAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharDivideAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar /= xCharValue;
		IValue value = eval(xVarChar + divideAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar /= yCharValue;
		value = eval(xVarChar + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar /= xCharValue;
		value = eval(yVarChar + divideAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar /= yCharValue;
		value = eval(yVarChar + divideAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharDivideAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar /= xShortValue;
		IValue value = eval(xVarChar + divideAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar /= yShortValue;
		value = eval(xVarChar + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar /= xShortValue;
		value = eval(yVarChar + divideAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar /= yShortValue;
		value = eval(yVarChar + divideAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharDivideAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar /= xIntValue;
		IValue value = eval(xVarChar + divideAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar /= yIntValue;
		value = eval(xVarChar + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar /= xIntValue;
		value = eval(yVarChar + divideAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar /= yIntValue;
		value = eval(yVarChar + divideAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharDivideAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar /= xLongValue;
		IValue value = eval(xVarChar + divideAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar /= yLongValue;
		value = eval(xVarChar + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar /= xLongValue;
		value = eval(yVarChar + divideAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar /= yLongValue;
		value = eval(yVarChar + divideAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharDivideAssignmentFloat() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar /= xFloatValue;
		IValue value = eval(xVarChar + divideAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment float : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar /= yFloatValue;
		value = eval(xVarChar + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar /= xFloatValue;
		value = eval(yVarChar + divideAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar /= yFloatValue;
		value = eval(yVarChar + divideAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharDivideAssignmentDouble() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar /= xDoubleValue;
		IValue value = eval(xVarChar + divideAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment double : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar /= yDoubleValue;
		value = eval(xVarChar + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar /= xDoubleValue;
		value = eval(yVarChar + divideAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar /= yDoubleValue;
		value = eval(yVarChar + divideAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char divideAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char divideAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char %= {byte, char, short, int, long, float, double}

	public void testCharRemainderAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar %= xByteValue;
		IValue value = eval(xVarChar + remainderAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar %= yByteValue;
		value = eval(xVarChar + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar %= xByteValue;
		value = eval(yVarChar + remainderAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar %= yByteValue;
		value = eval(yVarChar + remainderAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar %= xCharValue;
		IValue value = eval(xVarChar + remainderAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar %= yCharValue;
		value = eval(xVarChar + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar %= xCharValue;
		value = eval(yVarChar + remainderAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar %= yCharValue;
		value = eval(yVarChar + remainderAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar %= xShortValue;
		IValue value = eval(xVarChar + remainderAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar %= yShortValue;
		value = eval(xVarChar + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar %= xShortValue;
		value = eval(yVarChar + remainderAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar %= yShortValue;
		value = eval(yVarChar + remainderAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar %= xIntValue;
		IValue value = eval(xVarChar + remainderAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar %= yIntValue;
		value = eval(xVarChar + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar %= xIntValue;
		value = eval(yVarChar + remainderAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar %= yIntValue;
		value = eval(yVarChar + remainderAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar %= xLongValue;
		IValue value = eval(xVarChar + remainderAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar %= yLongValue;
		value = eval(xVarChar + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar %= xLongValue;
		value = eval(yVarChar + remainderAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar %= yLongValue;
		value = eval(yVarChar + remainderAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderAssignmentFloat() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar %= xFloatValue;
		IValue value = eval(xVarChar + remainderAssignmentOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment float : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar %= yFloatValue;
		value = eval(xVarChar + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment float : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar %= xFloatValue;
		value = eval(yVarChar + remainderAssignmentOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar %= yFloatValue;
		value = eval(yVarChar + remainderAssignmentOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment float : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderAssignmentDouble() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar %= xDoubleValue;
		IValue value = eval(xVarChar + remainderAssignmentOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment double : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar %= yDoubleValue;
		value = eval(xVarChar + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment double : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar %= xDoubleValue;
		value = eval(yVarChar + remainderAssignmentOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar %= yDoubleValue;
		value = eval(yVarChar + remainderAssignmentOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainderAssignment double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char remainderAssignment double : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char <<= {byte, char, short, int, long, float, double}

	public void testCharLeftShiftAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar <<= xByteValue;
		IValue value = eval(xVarChar + leftShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar <<= yByteValue;
		value = eval(xVarChar + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar <<= xByteValue;
		value = eval(yVarChar + leftShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar <<= yByteValue;
		value = eval(yVarChar + leftShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharLeftShiftAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar <<= xCharValue;
		IValue value = eval(xVarChar + leftShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar <<= yCharValue;
		value = eval(xVarChar + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar <<= xCharValue;
		value = eval(yVarChar + leftShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar <<= yCharValue;
		value = eval(yVarChar + leftShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharLeftShiftAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar <<= xShortValue;
		IValue value = eval(xVarChar + leftShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar <<= yShortValue;
		value = eval(xVarChar + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar <<= xShortValue;
		value = eval(yVarChar + leftShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar <<= yShortValue;
		value = eval(yVarChar + leftShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharLeftShiftAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar <<= xIntValue;
		IValue value = eval(xVarChar + leftShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar <<= yIntValue;
		value = eval(xVarChar + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar <<= xIntValue;
		value = eval(yVarChar + leftShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar <<= yIntValue;
		value = eval(yVarChar + leftShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharLeftShiftAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar <<= xLongValue;
		IValue value = eval(xVarChar + leftShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar <<= yLongValue;
		value = eval(xVarChar + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar <<= xLongValue;
		value = eval(yVarChar + leftShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar <<= yLongValue;
		value = eval(yVarChar + leftShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char leftShiftAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char >>= {byte, char, short, int, long, float, double}

	public void testCharRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>= xByteValue;
		IValue value = eval(xVarChar + rightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>= yByteValue;
		value = eval(xVarChar + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>= xByteValue;
		value = eval(yVarChar + rightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>= yByteValue;
		value = eval(yVarChar + rightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>= xCharValue;
		IValue value = eval(xVarChar + rightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>= yCharValue;
		value = eval(xVarChar + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>= xCharValue;
		value = eval(yVarChar + rightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>= yCharValue;
		value = eval(yVarChar + rightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>= xShortValue;
		IValue value = eval(xVarChar + rightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>= yShortValue;
		value = eval(xVarChar + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>= xShortValue;
		value = eval(yVarChar + rightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>= yShortValue;
		value = eval(yVarChar + rightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>= xIntValue;
		IValue value = eval(xVarChar + rightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>= yIntValue;
		value = eval(xVarChar + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>= xIntValue;
		value = eval(yVarChar + rightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>= yIntValue;
		value = eval(yVarChar + rightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>= xLongValue;
		IValue value = eval(xVarChar + rightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>= yLongValue;
		value = eval(xVarChar + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>= xLongValue;
		value = eval(yVarChar + rightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>= yLongValue;
		value = eval(yVarChar + rightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char rightShiftAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char >>>= {byte, char, short, int, long, float, double}

	public void testCharUnsignedRightShiftAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>>= xByteValue;
		IValue value = eval(xVarChar + unsignedRightShiftAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>>= yByteValue;
		value = eval(xVarChar + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>>= xByteValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>>= yByteValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharUnsignedRightShiftAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>>= xCharValue;
		IValue value = eval(xVarChar + unsignedRightShiftAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>>= yCharValue;
		value = eval(xVarChar + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>>= xCharValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>>= yCharValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharUnsignedRightShiftAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>>= xShortValue;
		IValue value = eval(xVarChar + unsignedRightShiftAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>>= yShortValue;
		value = eval(xVarChar + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>>= xShortValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>>= yShortValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharUnsignedRightShiftAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>>= xIntValue;
		IValue value = eval(xVarChar + unsignedRightShiftAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>>= yIntValue;
		value = eval(xVarChar + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>>= xIntValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>>= yIntValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharUnsignedRightShiftAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar >>>= xLongValue;
		IValue value = eval(xVarChar + unsignedRightShiftAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar >>>= yLongValue;
		value = eval(xVarChar + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar >>>= xLongValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar >>>= yLongValue;
		value = eval(yVarChar + unsignedRightShiftAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShiftAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char unsignedRightShiftAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char |= {byte, char, short, int, long, float, double}

	public void testCharOrAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar |= xByteValue;
		IValue value = eval(xVarChar + orAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar |= yByteValue;
		value = eval(xVarChar + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar |= xByteValue;
		value = eval(yVarChar + orAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar |= yByteValue;
		value = eval(yVarChar + orAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharOrAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar |= xCharValue;
		IValue value = eval(xVarChar + orAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar |= yCharValue;
		value = eval(xVarChar + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar |= xCharValue;
		value = eval(yVarChar + orAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar |= yCharValue;
		value = eval(yVarChar + orAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharOrAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar |= xShortValue;
		IValue value = eval(xVarChar + orAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar |= yShortValue;
		value = eval(xVarChar + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar |= xShortValue;
		value = eval(yVarChar + orAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar |= yShortValue;
		value = eval(yVarChar + orAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharOrAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar |= xIntValue;
		IValue value = eval(xVarChar + orAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar |= yIntValue;
		value = eval(xVarChar + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar |= xIntValue;
		value = eval(yVarChar + orAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar |= yIntValue;
		value = eval(yVarChar + orAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharOrAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar |= xLongValue;
		IValue value = eval(xVarChar + orAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar |= yLongValue;
		value = eval(xVarChar + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar |= xLongValue;
		value = eval(yVarChar + orAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar |= yLongValue;
		value = eval(yVarChar + orAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char orAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char orAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char &= {byte, char, short, int, long, float, double}

	public void testCharAndAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar &= xByteValue;
		IValue value = eval(xVarChar + andAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar &= yByteValue;
		value = eval(xVarChar + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar &= xByteValue;
		value = eval(yVarChar + andAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar &= yByteValue;
		value = eval(yVarChar + andAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharAndAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar &= xCharValue;
		IValue value = eval(xVarChar + andAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar &= yCharValue;
		value = eval(xVarChar + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar &= xCharValue;
		value = eval(yVarChar + andAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar &= yCharValue;
		value = eval(yVarChar + andAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharAndAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar &= xShortValue;
		IValue value = eval(xVarChar + andAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar &= yShortValue;
		value = eval(xVarChar + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar &= xShortValue;
		value = eval(yVarChar + andAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar &= yShortValue;
		value = eval(yVarChar + andAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharAndAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar &= xIntValue;
		IValue value = eval(xVarChar + andAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar &= yIntValue;
		value = eval(xVarChar + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar &= xIntValue;
		value = eval(yVarChar + andAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar &= yIntValue;
		value = eval(yVarChar + andAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharAndAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar &= xLongValue;
		IValue value = eval(xVarChar + andAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar &= yLongValue;
		value = eval(xVarChar + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar &= xLongValue;
		value = eval(yVarChar + andAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar &= yLongValue;
		value = eval(yVarChar + andAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char andAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char andAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// char ^= {byte, char, short, int, long, float, double}

	public void testCharXorAssignmentByte() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar ^= xByteValue;
		IValue value = eval(xVarChar + xorAssignmentOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar ^= yByteValue;
		value = eval(xVarChar + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment byte : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar ^= xByteValue;
		value = eval(yVarChar + xorAssignmentOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar ^= yByteValue;
		value = eval(yVarChar + xorAssignmentOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment byte : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharXorAssignmentChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar ^= xCharValue;
		IValue value = eval(xVarChar + xorAssignmentOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar ^= yCharValue;
		value = eval(xVarChar + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment char : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar ^= xCharValue;
		value = eval(yVarChar + xorAssignmentOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar ^= yCharValue;
		value = eval(yVarChar + xorAssignmentOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment char : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharXorAssignmentShort() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar ^= xShortValue;
		IValue value = eval(xVarChar + xorAssignmentOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar ^= yShortValue;
		value = eval(xVarChar + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment short : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar ^= xShortValue;
		value = eval(yVarChar + xorAssignmentOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar ^= yShortValue;
		value = eval(yVarChar + xorAssignmentOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment short : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharXorAssignmentInt() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar ^= xIntValue;
		IValue value = eval(xVarChar + xorAssignmentOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar ^= yIntValue;
		value = eval(xVarChar + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment int : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar ^= xIntValue;
		value = eval(yVarChar + xorAssignmentOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar ^= yIntValue;
		value = eval(yVarChar + xorAssignmentOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment int : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testCharXorAssignmentLong() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		tmpxVar ^= xLongValue;
		IValue value = eval(xVarChar + xorAssignmentOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		tmpxVar ^= yLongValue;
		value = eval(xVarChar + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment long : wrong result : ", tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		tmpyVar ^= xLongValue;
		value = eval(yVarChar + xorAssignmentOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);

		tmpyVar ^= yLongValue;
		value = eval(yVarChar + xorAssignmentOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char xorAssignment long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char xorAssignment long : wrong result : ", tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}


}
