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

public class NumericTypesCastTests extends Tests {

	public NumericTypesCastTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// (byte) {byte, char, short, int, long, float, double}

	public void testByteCastByte() throws Throwable {
		try {
		init();
		IValue value = eval("(byte)" + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("(byte) byte : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) byte : wrong result : ", xByteValue, byteValue);

		value = eval("(byte)" + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("(byte) byte : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) byte : wrong result : ", yByteValue, byteValue);
		} finally {
		end();
		}
	}

	public void testByteCastChar() throws Throwable {
		try {
		init();
		IValue value = eval("(byte)" + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("(byte) char : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) char : wrong result : ", (byte) xCharValue, byteValue);

		value = eval("(byte)" + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("(byte) char : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) char : wrong result : ", (byte) yCharValue, byteValue);
		} finally {
		end();
		}
	}

	public void testByteCastShort() throws Throwable {
		try {
		init();
		IValue value = eval("(byte)" + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("(byte) short : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) short : wrong result : ", ( byte) xShortValue, byteValue);

		value = eval("(byte)" + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("(byte) short : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) short : wrong result : ", ( byte) yShortValue, byteValue);
		} finally {
		end();
		}
	}

	public void testByteCastInt() throws Throwable {
		try {
		init();
		IValue value = eval("(byte)" + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("(byte) int : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) int : wrong result : ", ( byte) xIntValue, byteValue);

		value = eval("(byte)" + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("(byte) int : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) int : wrong result : ", ( byte) yIntValue, byteValue);
		} finally {
		end();
		}
	}

	public void testByteCastLong() throws Throwable {
		try {
		init();
		IValue value = eval("(byte)" + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("(byte) long : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) long : wrong result : ", ( byte) xLongValue, byteValue);

		value = eval("(byte)" + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("(byte) long : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) long : wrong result : ", ( byte) yLongValue, byteValue);
		} finally {
		end();
		}
	}

	public void testByteCastFloat() throws Throwable {
		try {
		init();
		IValue value = eval("(byte)" + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("(byte) float : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) float : wrong result : ", ( byte) xFloatValue, byteValue);

		value = eval("(byte)" + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("(byte) float : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) float : wrong result : ", ( byte) yFloatValue, byteValue);
		} finally {
		end();
		}
	}

	public void testByteCastDouble() throws Throwable {
		try {
		init();
		IValue value = eval("(byte)" + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("(byte) double : wrong type : ", "byte", typeName);
		byte byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) double : wrong result : ", ( byte) xDoubleValue, byteValue);

		value = eval("(byte)" + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("(byte) double : wrong type : ", "byte", typeName);
		byteValue = ((IJavaPrimitiveValue)value).getByteValue();
		assertEquals("(byte) double : wrong result : ", ( byte) yDoubleValue, byteValue);
		} finally {
		end();
		}
	}

	// (char) {byte, char, short, int, long, float, double}

	public void testCharCastByte() throws Throwable {
		try {
		init();
		IValue value = eval("(char)" + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("(char) byte : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) byte : wrong result : ", ( char) xByteValue, charValue);

		value = eval("(char)" + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("(char) byte : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) byte : wrong result : ", ( char) yByteValue, charValue);
		} finally {
		end();
		}
	}

	public void testCharCastChar() throws Throwable {
		try {
		init();
		IValue value = eval("(char)" + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("(char) char : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) char : wrong result : ", xCharValue, charValue);

		value = eval("(char)" + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("(char) char : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) char : wrong result : ", yCharValue, charValue);
		} finally {
		end();
		}
	}

	public void testCharCastShort() throws Throwable {
		try {
		init();
		IValue value = eval("(char)" + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("(char) short : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) short : wrong result : ", ( char) xShortValue, charValue);

		value = eval("(char)" + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("(char) short : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) short : wrong result : ", ( char) yShortValue, charValue);
		} finally {
		end();
		}
	}

	public void testCharCastInt() throws Throwable {
		try {
		init();
		IValue value = eval("(char)" + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("(char) int : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) int : wrong result : ", ( char) xIntValue, charValue);

		value = eval("(char)" + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("(char) int : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) int : wrong result : ", ( char) yIntValue, charValue);
		} finally {
		end();
		}
	}

	public void testCharCastLong() throws Throwable {
		try {
		init();
		IValue value = eval("(char)" + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("(char) long : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) long : wrong result : ", ( char) xLongValue, charValue);

		value = eval("(char)" + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("(char) long : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) long : wrong result : ", ( char) yLongValue, charValue);
		} finally {
		end();
		}
	}

	public void testCharCastFloat() throws Throwable {
		try {
		init();
		IValue value = eval("(char)" + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("(char) float : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) float : wrong result : ", ( char) xFloatValue, charValue);

		value = eval("(char)" + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("(char) float : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) float : wrong result : ", ( char) yFloatValue, charValue);
		} finally {
		end();
		}
	}

	public void testCharCastDouble() throws Throwable {
		try {
		init();
		IValue value = eval("(char)" + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("(char) double : wrong type : ", "char", typeName);
		char charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) double : wrong result : ", ( char) xDoubleValue, charValue);

		value = eval("(char)" + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("(char) double : wrong type : ", "char", typeName);
		charValue = ((IJavaPrimitiveValue)value).getCharValue();
		assertEquals("(char) double : wrong result : ", ( char) yDoubleValue, charValue);
		} finally {
		end();
		}
	}

	// (short) {byte, char, short, int, long, float, double}

	public void testShortCastByte() throws Throwable {
		try {
		init();
		IValue value = eval("(short)" + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("(short) byte : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) byte : wrong result : ", xByteValue, shortValue);

		value = eval("(short)" + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("(short) byte : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) byte : wrong result : ", yByteValue, shortValue);
		} finally {
		end();
		}
	}

	public void testShortCastChar() throws Throwable {
		try {
		init();
		IValue value = eval("(short)" + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("(short) char : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) char : wrong result : ", (short) xCharValue, shortValue);

		value = eval("(short)" + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("(short) char : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) char : wrong result : ", (short) yCharValue, shortValue);
		} finally {
		end();
		}
	}

	public void testShortCastShort() throws Throwable {
		try {
		init();
		IValue value = eval("(short)" + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("(short) short : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) short : wrong result : ", xShortValue, shortValue);

		value = eval("(short)" + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("(short) short : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) short : wrong result : ", yShortValue, shortValue);
		} finally {
		end();
		}
	}

	public void testShortCastInt() throws Throwable {
		try {
		init();
		IValue value = eval("(short)" + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("(short) int : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) int : wrong result : ", ( short) xIntValue, shortValue);

		value = eval("(short)" + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("(short) int : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) int : wrong result : ", ( short) yIntValue, shortValue);
		} finally {
		end();
		}
	}

	public void testShortCastLong() throws Throwable {
		try {
		init();
		IValue value = eval("(short)" + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("(short) long : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) long : wrong result : ", ( short) xLongValue, shortValue);

		value = eval("(short)" + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("(short) long : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) long : wrong result : ", ( short) yLongValue, shortValue);
		} finally {
		end();
		}
	}

	public void testShortCastFloat() throws Throwable {
		try {
		init();
		IValue value = eval("(short)" + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("(short) float : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) float : wrong result : ", ( short) xFloatValue, shortValue);

		value = eval("(short)" + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("(short) float : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) float : wrong result : ", ( short) yFloatValue, shortValue);
		} finally {
		end();
		}
	}

	public void testShortCastDouble() throws Throwable {
		try {
		init();
		IValue value = eval("(short)" + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("(short) double : wrong type : ", "short", typeName);
		short shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) double : wrong result : ", ( short) xDoubleValue, shortValue);

		value = eval("(short)" + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("(short) double : wrong type : ", "short", typeName);
		shortValue = ((IJavaPrimitiveValue)value).getShortValue();
		assertEquals("(short) double : wrong result : ", ( short) yDoubleValue, shortValue);
		} finally {
		end();
		}
	}

	// (int) {byte, char, short, int, long, float, double}

	public void testIntCastByte() throws Throwable {
		try {
		init();
		IValue value = eval("(int)" + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("(int) byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) byte : wrong result : ", xByteValue, intValue);

		value = eval("(int)" + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("(int) byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) byte : wrong result : ", yByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntCastChar() throws Throwable {
		try {
		init();
		IValue value = eval("(int)" + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("(int) char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) char : wrong result : ", xCharValue, intValue);

		value = eval("(int)" + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("(int) char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) char : wrong result : ", yCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntCastShort() throws Throwable {
		try {
		init();
		IValue value = eval("(int)" + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("(int) short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) short : wrong result : ", xShortValue, intValue);

		value = eval("(int)" + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("(int) short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) short : wrong result : ", yShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntCastInt() throws Throwable {
		try {
		init();
		IValue value = eval("(int)" + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("(int) int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) int : wrong result : ", xIntValue, intValue);

		value = eval("(int)" + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("(int) int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) int : wrong result : ", yIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntCastLong() throws Throwable {
		try {
		init();
		IValue value = eval("(int)" + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("(int) long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) long : wrong result : ", ( int) xLongValue, intValue);

		value = eval("(int)" + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("(int) long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) long : wrong result : ", ( int) yLongValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntCastFloat() throws Throwable {
		try {
		init();
		IValue value = eval("(int)" + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("(int) float : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) float : wrong result : ", ( int) xFloatValue, intValue);

		value = eval("(int)" + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("(int) float : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) float : wrong result : ", ( int) yFloatValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntCastDouble() throws Throwable {
		try {
		init();
		IValue value = eval("(int)" + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("(int) double : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) double : wrong result : ", ( int) xDoubleValue, intValue);

		value = eval("(int)" + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("(int) double : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("(int) double : wrong result : ", ( int) yDoubleValue, intValue);
		} finally {
		end();
		}
	}

	// (long) {byte, char, short, int, long, float, double}

	public void testLongCastByte() throws Throwable {
		try {
		init();
		IValue value = eval("(long)" + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("(long) byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) byte : wrong result : ", xByteValue, longValue);

		value = eval("(long)" + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("(long) byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) byte : wrong result : ", yByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongCastChar() throws Throwable {
		try {
		init();
		IValue value = eval("(long)" + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("(long) char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) char : wrong result : ", xCharValue, longValue);

		value = eval("(long)" + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("(long) char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) char : wrong result : ", yCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongCastShort() throws Throwable {
		try {
		init();
		IValue value = eval("(long)" + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("(long) short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) short : wrong result : ", xShortValue, longValue);

		value = eval("(long)" + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("(long) short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) short : wrong result : ", yShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongCastInt() throws Throwable {
		try {
		init();
		IValue value = eval("(long)" + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("(long) int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) int : wrong result : ", xIntValue, longValue);

		value = eval("(long)" + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("(long) int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) int : wrong result : ", yIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongCastLong() throws Throwable {
		try {
		init();
		IValue value = eval("(long)" + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("(long) long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) long : wrong result : ", xLongValue, longValue);

		value = eval("(long)" + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("(long) long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) long : wrong result : ", yLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongCastFloat() throws Throwable {
		try {
		init();
		IValue value = eval("(long)" + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("(long) float : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) float : wrong result : ", ( long) xFloatValue, longValue);

		value = eval("(long)" + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("(long) float : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) float : wrong result : ", ( long) yFloatValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongCastDouble() throws Throwable {
		try {
		init();
		IValue value = eval("(long)" + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("(long) double : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) double : wrong result : ", ( long) xDoubleValue, longValue);

		value = eval("(long)" + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("(long) double : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("(long) double : wrong result : ", ( long) yDoubleValue, longValue);
		} finally {
		end();
		}
	}

	// (float) {byte, char, short, int, long, float, double}

	public void testFloatCastByte() throws Throwable {
		try {
		init();
		IValue value = eval("(float)" + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("(float) byte : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) byte : wrong result : ", xByteValue, floatValue, 0);

		value = eval("(float)" + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("(float) byte : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) byte : wrong result : ", yByteValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatCastChar() throws Throwable {
		try {
		init();
		IValue value = eval("(float)" + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("(float) char : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) char : wrong result : ", xCharValue, floatValue, 0);

		value = eval("(float)" + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("(float) char : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) char : wrong result : ", yCharValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatCastShort() throws Throwable {
		try {
		init();
		IValue value = eval("(float)" + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("(float) short : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) short : wrong result : ", xShortValue, floatValue, 0);

		value = eval("(float)" + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("(float) short : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) short : wrong result : ", yShortValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatCastInt() throws Throwable {
		try {
		init();
		IValue value = eval("(float)" + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("(float) int : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) int : wrong result : ", xIntValue, floatValue, 0);

		value = eval("(float)" + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("(float) int : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) int : wrong result : ", yIntValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatCastLong() throws Throwable {
		try {
		init();
		IValue value = eval("(float)" + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("(float) long : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) long : wrong result : ", xLongValue, floatValue, 0);

		value = eval("(float)" + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("(float) long : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) long : wrong result : ", yLongValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatCastFloat() throws Throwable {
		try {
		init();
		IValue value = eval("(float)" + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("(float) float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) float : wrong result : ", xFloatValue, floatValue, 0);

		value = eval("(float)" + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("(float) float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) float : wrong result : ", yFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatCastDouble() throws Throwable {
		try {
		init();
		IValue value = eval("(float)" + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("(float) double : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) double : wrong result : ", ( float) xDoubleValue, floatValue, 0);

		value = eval("(float)" + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("(float) double : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("(float) double : wrong result : ", ( float) yDoubleValue, floatValue, 0);
		} finally {
		end();
		}
	}

	// (double) {byte, char, short, int, long, float, double}

	public void testDoubleCastByte() throws Throwable {
		try {
		init();
		IValue value = eval("(double)" + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("(double) byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) byte : wrong result : ", xByteValue, doubleValue, 0);

		value = eval("(double)" + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("(double) byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) byte : wrong result : ", yByteValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleCastChar() throws Throwable {
		try {
		init();
		IValue value = eval("(double)" + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("(double) char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) char : wrong result : ", xCharValue, doubleValue, 0);

		value = eval("(double)" + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("(double) char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) char : wrong result : ", yCharValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleCastShort() throws Throwable {
		try {
		init();
		IValue value = eval("(double)" + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("(double) short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) short : wrong result : ", xShortValue, doubleValue, 0);

		value = eval("(double)" + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("(double) short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) short : wrong result : ", yShortValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleCastInt() throws Throwable {
		try {
		init();
		IValue value = eval("(double)" + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("(double) int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) int : wrong result : ", xIntValue, doubleValue, 0);

		value = eval("(double)" + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("(double) int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) int : wrong result : ", yIntValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleCastLong() throws Throwable {
		try {
		init();
		IValue value = eval("(double)" + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("(double) long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) long : wrong result : ", xLongValue, doubleValue, 0);

		value = eval("(double)" + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("(double) long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) long : wrong result : ", yLongValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleCastFloat() throws Throwable {
		try {
		init();
		IValue value = eval("(double)" + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("(double) float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) float : wrong result : ", xFloatValue, doubleValue, 0);

		value = eval("(double)" + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("(double) float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) float : wrong result : ", yFloatValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleCastDouble() throws Throwable {
		try {
		init();
		IValue value = eval("(double)" + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("(double) double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) double : wrong result : ", xDoubleValue, doubleValue, 0);

		value = eval("(double)" + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("(double) double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("(double) double : wrong result : ", yDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}


}
