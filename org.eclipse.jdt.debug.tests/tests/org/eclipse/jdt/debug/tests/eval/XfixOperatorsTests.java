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

public class XfixOperatorsTests extends Tests {

	public XfixOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// {++, --} byte
	// byte {++, --}

	public void testPrefixPlusPlusByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		IValue value = eval(prefixPlusPlusOp + xVarByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("prefixPlusPlus byte : wrong result : ", ++tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		value = eval(prefixPlusPlusOp + yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("prefixPlusPlus byte : wrong result : ", ++tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testPrefixMinusMinusByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		IValue value = eval(prefixMinusMinusOp + xVarByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("prefixMinusMinus byte : wrong result : ", --tmpxVar, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		value = eval(prefixMinusMinusOp + yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("prefixMinusMinus byte : wrong result : ", --tmpyVar, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testPostfixPlusPlusByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		IValue value = eval(xVarByte + postfixPlusPlusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte postfixPlusPlus : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte postfixPlusPlus : wrong result : ", tmpxVar++, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		value = eval(yVarByte + postfixPlusPlusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("byte postfixPlusPlus : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte postfixPlusPlus : wrong result : ", tmpyVar++, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	public void testPostfixMinusMinusByte() throws Throwable {
		try {
		init();
		byte tmpxVar = xVarByteValue;
		IValue value = eval(xVarByte + postfixMinusMinusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte postfixMinusMinus : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte postfixMinusMinus : wrong result : ", tmpxVar--, byteValue);
		value = eval(xVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpxVar, byteValue);

		byte tmpyVar = yVarByteValue;
		value = eval(yVarByte + postfixMinusMinusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("byte postfixMinusMinus : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte postfixMinusMinus : wrong result : ", tmpyVar--, byteValue);
		value = eval(yVarByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte local variable value : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("byte local variable value : wrong result : ", tmpyVar, byteValue);
		} finally {
		end();
		}
	}

	// {++, --} char
	// char {++, --}

	public void testPrefixPlusPlusChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		IValue value = eval(prefixPlusPlusOp + xVarChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("prefixPlusPlus char : wrong result : ", ++tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		value = eval(prefixPlusPlusOp + yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("prefixPlusPlus char : wrong result : ", ++tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testPrefixMinusMinusChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		IValue value = eval(prefixMinusMinusOp + xVarChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("prefixMinusMinus char : wrong result : ", --tmpxVar, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		value = eval(prefixMinusMinusOp + yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("prefixMinusMinus char : wrong result : ", --tmpyVar, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testPostfixPlusPlusChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		IValue value = eval(xVarChar + postfixPlusPlusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("char postfixPlusPlus : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char postfixPlusPlus : wrong result : ", tmpxVar++, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		value = eval(yVarChar + postfixPlusPlusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("char postfixPlusPlus : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char postfixPlusPlus : wrong result : ", tmpyVar++, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	public void testPostfixMinusMinusChar() throws Throwable {
		try {
		init();
		char tmpxVar = xVarCharValue;
		IValue value = eval(xVarChar + postfixMinusMinusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("char postfixMinusMinus : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char postfixMinusMinus : wrong result : ", tmpxVar--, charValue);
		value = eval(xVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpxVar, charValue);

		char tmpyVar = yVarCharValue;
		value = eval(yVarChar + postfixMinusMinusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("char postfixMinusMinus : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char postfixMinusMinus : wrong result : ", tmpyVar--, charValue);
		value = eval(yVarChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char local variable value : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("char local variable value : wrong result : ", tmpyVar, charValue);
		} finally {
		end();
		}
	}

	// {++, --} short
	// short {++, --}

	public void testPrefixPlusPlusShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		IValue value = eval(prefixPlusPlusOp + xVarShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("prefixPlusPlus short : wrong result : ", ++tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		value = eval(prefixPlusPlusOp + yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("prefixPlusPlus short : wrong result : ", ++tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testPrefixMinusMinusShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		IValue value = eval(prefixMinusMinusOp + xVarShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("prefixMinusMinus short : wrong result : ", --tmpxVar, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		value = eval(prefixMinusMinusOp + yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("prefixMinusMinus short : wrong result : ", --tmpyVar, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testPostfixPlusPlusShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		IValue value = eval(xVarShort + postfixPlusPlusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("short postfixPlusPlus : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short postfixPlusPlus : wrong result : ", tmpxVar++, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		value = eval(yVarShort + postfixPlusPlusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("short postfixPlusPlus : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short postfixPlusPlus : wrong result : ", tmpyVar++, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	public void testPostfixMinusMinusShort() throws Throwable {
		try {
		init();
		short tmpxVar = xVarShortValue;
		IValue value = eval(xVarShort + postfixMinusMinusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("short postfixMinusMinus : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short postfixMinusMinus : wrong result : ", tmpxVar--, shortValue);
		value = eval(xVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpxVar, shortValue);

		short tmpyVar = yVarShortValue;
		value = eval(yVarShort + postfixMinusMinusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("short postfixMinusMinus : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short postfixMinusMinus : wrong result : ", tmpyVar--, shortValue);
		value = eval(yVarShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short local variable value : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("short local variable value : wrong result : ", tmpyVar, shortValue);
		} finally {
		end();
		}
	}

	// {++, --} int
	// int {++, --}

	public void testPrefixPlusPlusInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		IValue value = eval(prefixPlusPlusOp + xVarInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("prefixPlusPlus int : wrong result : ", ++tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		value = eval(prefixPlusPlusOp + yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("prefixPlusPlus int : wrong result : ", ++tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testPrefixMinusMinusInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		IValue value = eval(prefixMinusMinusOp + xVarInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("prefixMinusMinus int : wrong result : ", --tmpxVar, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		value = eval(prefixMinusMinusOp + yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("prefixMinusMinus int : wrong result : ", --tmpyVar, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testPostfixPlusPlusInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		IValue value = eval(xVarInt + postfixPlusPlusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("int postfixPlusPlus : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int postfixPlusPlus : wrong result : ", tmpxVar++, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		value = eval(yVarInt + postfixPlusPlusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("int postfixPlusPlus : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int postfixPlusPlus : wrong result : ", tmpyVar++, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	public void testPostfixMinusMinusInt() throws Throwable {
		try {
		init();
		int tmpxVar = xVarIntValue;
		IValue value = eval(xVarInt + postfixMinusMinusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("int postfixMinusMinus : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int postfixMinusMinus : wrong result : ", tmpxVar--, intValue);
		value = eval(xVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpxVar, intValue);

		int tmpyVar = yVarIntValue;
		value = eval(yVarInt + postfixMinusMinusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("int postfixMinusMinus : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int postfixMinusMinus : wrong result : ", tmpyVar--, intValue);
		value = eval(yVarInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int local variable value : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int local variable value : wrong result : ", tmpyVar, intValue);
		} finally {
		end();
		}
	}

	// {++, --} long
	// long {++, --}

	public void testPrefixPlusPlusLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		IValue value = eval(prefixPlusPlusOp + xVarLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("prefixPlusPlus long : wrong result : ", ++tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		value = eval(prefixPlusPlusOp + yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("prefixPlusPlus long : wrong result : ", ++tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testPrefixMinusMinusLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		IValue value = eval(prefixMinusMinusOp + xVarLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("prefixMinusMinus long : wrong result : ", --tmpxVar, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		value = eval(prefixMinusMinusOp + yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("prefixMinusMinus long : wrong result : ", --tmpyVar, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testPostfixPlusPlusLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		IValue value = eval(xVarLong + postfixPlusPlusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("long postfixPlusPlus : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long postfixPlusPlus : wrong result : ", tmpxVar++, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		value = eval(yVarLong + postfixPlusPlusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("long postfixPlusPlus : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long postfixPlusPlus : wrong result : ", tmpyVar++, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	public void testPostfixMinusMinusLong() throws Throwable {
		try {
		init();
		long tmpxVar = xVarLongValue;
		IValue value = eval(xVarLong + postfixMinusMinusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("long postfixMinusMinus : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long postfixMinusMinus : wrong result : ", tmpxVar--, longValue);
		value = eval(xVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpxVar, longValue);

		long tmpyVar = yVarLongValue;
		value = eval(yVarLong + postfixMinusMinusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("long postfixMinusMinus : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long postfixMinusMinus : wrong result : ", tmpyVar--, longValue);
		value = eval(yVarLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long local variable value : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long local variable value : wrong result : ", tmpyVar, longValue);
		} finally {
		end();
		}
	}

	// {++, --} float
	// float {++, --}

	public void testPrefixPlusPlusFloat() throws Throwable {
		try {
		init();
		float tmpxVar = xVarFloatValue;
		IValue value = eval(prefixPlusPlusOp + xVarFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("prefixPlusPlus float : wrong result : ", ++tmpxVar, floatValue, 0);
		value = eval(xVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", tmpxVar, floatValue, 0);

		float tmpyVar = yVarFloatValue;
		value = eval(prefixPlusPlusOp + yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("prefixPlusPlus float : wrong result : ", ++tmpyVar, floatValue, 0);
		value = eval(yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", tmpyVar, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testPrefixMinusMinusFloat() throws Throwable {
		try {
		init();
		float tmpxVar = xVarFloatValue;
		IValue value = eval(prefixMinusMinusOp + xVarFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("prefixMinusMinus float : wrong result : ", --tmpxVar, floatValue, 0);
		value = eval(xVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", tmpxVar, floatValue, 0);

		float tmpyVar = yVarFloatValue;
		value = eval(prefixMinusMinusOp + yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("prefixMinusMinus float : wrong result : ", --tmpyVar, floatValue, 0);
		value = eval(yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", tmpyVar, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testPostfixPlusPlusFloat() throws Throwable {
		try {
		init();
		float tmpxVar = xVarFloatValue;
		IValue value = eval(xVarFloat + postfixPlusPlusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("float postfixPlusPlus : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float postfixPlusPlus : wrong result : ", tmpxVar++, floatValue, 0);
		value = eval(xVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", tmpxVar, floatValue, 0);

		float tmpyVar = yVarFloatValue;
		value = eval(yVarFloat + postfixPlusPlusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("float postfixPlusPlus : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float postfixPlusPlus : wrong result : ", tmpyVar++, floatValue, 0);
		value = eval(yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", tmpyVar, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testPostfixMinusMinusFloat() throws Throwable {
		try {
		init();
		float tmpxVar = xVarFloatValue;
		IValue value = eval(xVarFloat + postfixMinusMinusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("float postfixMinusMinus : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float postfixMinusMinus : wrong result : ", tmpxVar--, floatValue, 0);
		value = eval(xVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", tmpxVar, floatValue, 0);

		float tmpyVar = yVarFloatValue;
		value = eval(yVarFloat + postfixMinusMinusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("float postfixMinusMinus : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float postfixMinusMinus : wrong result : ", tmpyVar--, floatValue, 0);
		value = eval(yVarFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float local variable value : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float local variable value : wrong result : ", tmpyVar, floatValue, 0);
		} finally {
		end();
		}
	}

	// {++, --} double
	// double {++, --}

	public void testPrefixPlusPlusDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		IValue value = eval(prefixPlusPlusOp + xVarDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("prefixPlusPlus double : wrong result : ", ++tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		value = eval(prefixPlusPlusOp + yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixPlusPlus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("prefixPlusPlus double : wrong result : ", ++tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testPrefixMinusMinusDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		IValue value = eval(prefixMinusMinusOp + xVarDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("prefixMinusMinus double : wrong result : ", --tmpxVar, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		value = eval(prefixMinusMinusOp + yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("prefixMinusMinus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("prefixMinusMinus double : wrong result : ", --tmpyVar, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testPostfixPlusPlusDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		IValue value = eval(xVarDouble + postfixPlusPlusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("double postfixPlusPlus : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double postfixPlusPlus : wrong result : ", tmpxVar++, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		value = eval(yVarDouble + postfixPlusPlusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("double postfixPlusPlus : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double postfixPlusPlus : wrong result : ", tmpyVar++, doubleValue, 0);
		value = eval(yVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpyVar, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testPostfixMinusMinusDouble() throws Throwable {
		try {
		init();
		double tmpxVar = xVarDoubleValue;
		IValue value = eval(xVarDouble + postfixMinusMinusOp);
		String typeName = value.getReferenceTypeName();
		assertEquals("double postfixMinusMinus : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double postfixMinusMinus : wrong result : ", tmpxVar--, doubleValue, 0);
		value = eval(xVarDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double local variable value : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double local variable value : wrong result : ", tmpxVar, doubleValue, 0);

		double tmpyVar = yVarDoubleValue;
		value = eval(yVarDouble + postfixMinusMinusOp);
		typeName = value.getReferenceTypeName();
		assertEquals("double postfixMinusMinus : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double postfixMinusMinus : wrong result : ", tmpyVar--, doubleValue, 0);
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
