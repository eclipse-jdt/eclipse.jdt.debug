/*******************************************************************************
 *  Copyright (c) 2002, 2008 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class ByteOperatorsTests extends Tests {

	public ByteOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// byte + {byte, char, short, int, long, float, double}

	public void testBytePlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + plusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte plus byte : wrong result : ", xByteValue + yByteValue, intValue);

		value = eval(yByte + plusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte plus byte : wrong result : ", yByteValue + xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testBytePlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + plusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte plus char : wrong result : ", xByteValue + yCharValue, intValue);

		value = eval(yByte + plusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte plus char : wrong result : ", yByteValue + xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testBytePlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + plusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte plus short : wrong result : ", xByteValue + yShortValue, intValue);

		value = eval(yByte + plusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte plus short : wrong result : ", yByteValue + xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testBytePlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte plus int : wrong result : ", xByteValue + yIntValue, intValue);

		value = eval(yByte + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte plus int : wrong result : ", yByteValue + xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testBytePlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + plusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte plus long : wrong result : ", xByteValue + yLongValue, longValue);

		value = eval(yByte + plusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte plus long : wrong result : ", yByteValue + xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testBytePlusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + plusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte plus float : wrong result : ", xByteValue + yFloatValue, floatValue, 0);

		value = eval(yByte + plusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte plus float : wrong result : ", yByteValue + xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testBytePlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + plusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte plus double : wrong result : ", xByteValue + yDoubleValue, doubleValue, 0);

		value = eval(yByte + plusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte plus double : wrong result : ", yByteValue + xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testBytePlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("byte plus java.lang.String : wrong result : ", xByteValue + yStringValue, stringValue);

		value = eval(yByte + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("byte plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("byte plus java.lang.String : wrong result : ", yByteValue + xStringValue, stringValue);
		} finally {
		end();
		}
	}

	// byte - {byte, char, short, int, long, float, double}

	public void testByteMinusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + minusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte minus byte : wrong result : ", xByteValue - yByteValue, intValue);

		value = eval(yByte + minusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte minus byte : wrong result : ", yByteValue - xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteMinusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + minusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte minus char : wrong result : ", xByteValue - yCharValue, intValue);

		value = eval(yByte + minusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte minus char : wrong result : ", yByteValue - xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteMinusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + minusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte minus short : wrong result : ", xByteValue - yShortValue, intValue);

		value = eval(yByte + minusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte minus short : wrong result : ", yByteValue - xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteMinusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + minusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte minus int : wrong result : ", xByteValue - yIntValue, intValue);

		value = eval(yByte + minusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte minus int : wrong result : ", yByteValue - xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteMinusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + minusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte minus long : wrong result : ", xByteValue - yLongValue, longValue);

		value = eval(yByte + minusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte minus long : wrong result : ", yByteValue - xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testByteMinusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + minusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte minus float : wrong result : ", xByteValue - yFloatValue, floatValue, 0);

		value = eval(yByte + minusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte minus float : wrong result : ", yByteValue - xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testByteMinusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + minusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte minus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte minus double : wrong result : ", xByteValue - yDoubleValue, doubleValue, 0);

		value = eval(yByte + minusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte minus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte minus double : wrong result : ", yByteValue - xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// byte * {byte, char, short, int, long, float, double}

	public void testByteMultiplyByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + multiplyOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiply byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte multiply byte : wrong result : ", xByteValue * yByteValue, intValue);

		value = eval(yByte + multiplyOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiply byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte multiply byte : wrong result : ", yByteValue * xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + multiplyOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiply char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte multiply char : wrong result : ", xByteValue * yCharValue, intValue);

		value = eval(yByte + multiplyOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiply char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte multiply char : wrong result : ", yByteValue * xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + multiplyOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiply short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte multiply short : wrong result : ", xByteValue * yShortValue, intValue);

		value = eval(yByte + multiplyOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiply short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte multiply short : wrong result : ", yByteValue * xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + multiplyOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiply int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte multiply int : wrong result : ", xByteValue * yIntValue, intValue);

		value = eval(yByte + multiplyOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiply int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte multiply int : wrong result : ", yByteValue * xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + multiplyOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiply long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte multiply long : wrong result : ", xByteValue * yLongValue, longValue);

		value = eval(yByte + multiplyOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiply long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte multiply long : wrong result : ", yByteValue * xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testByteMultiplyFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + multiplyOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiply float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte multiply float : wrong result : ", xByteValue * yFloatValue, floatValue, 0);

		value = eval(yByte + multiplyOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiply float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte multiply float : wrong result : ", yByteValue * xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testByteMultiplyDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + multiplyOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte multiply double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte multiply double : wrong result : ", xByteValue * yDoubleValue, doubleValue, 0);

		value = eval(yByte + multiplyOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte multiply double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte multiply double : wrong result : ", yByteValue * xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// byte / {byte, char, short, int, long, float, double}

	public void testByteDivideByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + divideOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divide byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte divide byte : wrong result : ", xByteValue / yByteValue, intValue);

		value = eval(yByte + divideOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divide byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte divide byte : wrong result : ", yByteValue / xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteDivideChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + divideOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divide char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte divide char : wrong result : ", xByteValue / yCharValue, intValue);

		value = eval(yByte + divideOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divide char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte divide char : wrong result : ", yByteValue / xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteDivideShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + divideOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divide short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte divide short : wrong result : ", xByteValue / yShortValue, intValue);

		value = eval(yByte + divideOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divide short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte divide short : wrong result : ", yByteValue / xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteDivideInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + divideOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divide int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte divide int : wrong result : ", xByteValue / yIntValue, intValue);

		value = eval(yByte + divideOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divide int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte divide int : wrong result : ", yByteValue / xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteDivideLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + divideOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divide long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte divide long : wrong result : ", xByteValue / yLongValue, longValue);

		value = eval(yByte + divideOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divide long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte divide long : wrong result : ", yByteValue / xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testByteDivideFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + divideOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divide float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte divide float : wrong result : ", xByteValue / yFloatValue, floatValue, 0);

		value = eval(yByte + divideOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divide float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte divide float : wrong result : ", yByteValue / xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testByteDivideDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + divideOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte divide double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte divide double : wrong result : ", xByteValue / yDoubleValue, doubleValue, 0);

		value = eval(yByte + divideOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte divide double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte divide double : wrong result : ", yByteValue / xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// byte % {byte, char, short, int, long, float, double}

	public void testByteRemainderByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + remainderOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainder byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte remainder byte : wrong result : ", xByteValue % yByteValue, intValue);

		value = eval(yByte + remainderOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainder byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte remainder byte : wrong result : ", yByteValue % xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + remainderOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainder char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte remainder char : wrong result : ", xByteValue % yCharValue, intValue);

		value = eval(yByte + remainderOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainder char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte remainder char : wrong result : ", yByteValue % xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + remainderOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainder short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte remainder short : wrong result : ", xByteValue % yShortValue, intValue);

		value = eval(yByte + remainderOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainder short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte remainder short : wrong result : ", yByteValue % xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + remainderOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainder int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte remainder int : wrong result : ", xByteValue % yIntValue, intValue);

		value = eval(yByte + remainderOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainder int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte remainder int : wrong result : ", yByteValue % xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + remainderOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainder long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte remainder long : wrong result : ", xByteValue % yLongValue, longValue);

		value = eval(yByte + remainderOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainder long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte remainder long : wrong result : ", yByteValue % xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testByteRemainderFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + remainderOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainder float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte remainder float : wrong result : ", xByteValue % yFloatValue, floatValue, 0);

		value = eval(yByte + remainderOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainder float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("byte remainder float : wrong result : ", yByteValue % xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testByteRemainderDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + remainderOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte remainder double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte remainder double : wrong result : ", xByteValue % yDoubleValue, doubleValue, 0);

		value = eval(yByte + remainderOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte remainder double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("byte remainder double : wrong result : ", yByteValue % xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// byte > {byte, char, short, int, long, float, double}

	public void testByteGreaterByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greater byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater byte : wrong result : ", xByteValue > yByteValue, booleanValue);

		value = eval(yByte + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater byte : wrong result : ", yByteValue > xByteValue, booleanValue);

		value = eval(xByte + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater byte : wrong result : ", xByteValue > xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greater char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater char : wrong result : ", xByteValue > yCharValue, booleanValue);

		value = eval(yByte + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater char : wrong result : ", yByteValue > xCharValue, booleanValue);

		value = eval(xByte + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater char : wrong result : ", xByteValue > xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greater short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater short : wrong result : ", xByteValue > yShortValue, booleanValue);

		value = eval(yByte + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater short : wrong result : ", yByteValue > xShortValue, booleanValue);

		value = eval(xByte + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater short : wrong result : ", xByteValue > xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greater int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater int : wrong result : ", xByteValue > yIntValue, booleanValue);

		value = eval(yByte + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater int : wrong result : ", yByteValue > xIntValue, booleanValue);

		value = eval(xByte + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater int : wrong result : ", xByteValue > xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greater long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater long : wrong result : ", xByteValue > yLongValue, booleanValue);

		value = eval(yByte + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater long : wrong result : ", yByteValue > xLongValue, booleanValue);

		value = eval(xByte + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater long : wrong result : ", xByteValue > xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greater float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater float : wrong result : ", xByteValue > yFloatValue, booleanValue);

		value = eval(yByte + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater float : wrong result : ", yByteValue > xFloatValue, booleanValue);

		value = eval(xByte + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater float : wrong result : ", xByteValue > xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greater double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater double : wrong result : ", xByteValue > yDoubleValue, booleanValue);

		value = eval(yByte + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater double : wrong result : ", yByteValue > xDoubleValue, booleanValue);

		value = eval(xByte + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greater double : wrong result : ", xByteValue > xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// byte >= {byte, char, short, int, long, float, double}

	public void testByteGreaterEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual byte : wrong result : ", xByteValue >= yByteValue, booleanValue);

		value = eval(yByte + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual byte : wrong result : ", yByteValue >= xByteValue, booleanValue);

		value = eval(xByte + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual byte : wrong result : ", xByteValue >= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual char : wrong result : ", xByteValue >= yCharValue, booleanValue);

		value = eval(yByte + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual char : wrong result : ", yByteValue >= xCharValue, booleanValue);

		value = eval(xByte + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual char : wrong result : ", xByteValue >= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual short : wrong result : ", xByteValue >= yShortValue, booleanValue);

		value = eval(yByte + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual short : wrong result : ", yByteValue >= xShortValue, booleanValue);

		value = eval(xByte + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual short : wrong result : ", xByteValue >= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual int : wrong result : ", xByteValue >= yIntValue, booleanValue);

		value = eval(yByte + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual int : wrong result : ", yByteValue >= xIntValue, booleanValue);

		value = eval(xByte + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual int : wrong result : ", xByteValue >= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual long : wrong result : ", xByteValue >= yLongValue, booleanValue);

		value = eval(yByte + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual long : wrong result : ", yByteValue >= xLongValue, booleanValue);

		value = eval(xByte + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual long : wrong result : ", xByteValue >= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual float : wrong result : ", xByteValue >= yFloatValue, booleanValue);

		value = eval(yByte + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual float : wrong result : ", yByteValue >= xFloatValue, booleanValue);

		value = eval(xByte + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual float : wrong result : ", xByteValue >= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteGreaterEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + greaterEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual double : wrong result : ", xByteValue >= yDoubleValue, booleanValue);

		value = eval(yByte + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual double : wrong result : ", yByteValue >= xDoubleValue, booleanValue);

		value = eval(xByte + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte greaterEqual double : wrong result : ", xByteValue >= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// byte < {byte, char, short, int, long, float, double}

	public void testByteLessByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte less byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less byte : wrong result : ", xByteValue < yByteValue, booleanValue);

		value = eval(yByte + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less byte : wrong result : ", yByteValue < xByteValue, booleanValue);

		value = eval(xByte + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less byte : wrong result : ", xByteValue < xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte less char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less char : wrong result : ", xByteValue < yCharValue, booleanValue);

		value = eval(yByte + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less char : wrong result : ", yByteValue < xCharValue, booleanValue);

		value = eval(xByte + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less char : wrong result : ", xByteValue < xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte less short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less short : wrong result : ", xByteValue < yShortValue, booleanValue);

		value = eval(yByte + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less short : wrong result : ", yByteValue < xShortValue, booleanValue);

		value = eval(xByte + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less short : wrong result : ", xByteValue < xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte less int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less int : wrong result : ", xByteValue < yIntValue, booleanValue);

		value = eval(yByte + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less int : wrong result : ", yByteValue < xIntValue, booleanValue);

		value = eval(xByte + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less int : wrong result : ", xByteValue < xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte less long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less long : wrong result : ", xByteValue < yLongValue, booleanValue);

		value = eval(yByte + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less long : wrong result : ", yByteValue < xLongValue, booleanValue);

		value = eval(xByte + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less long : wrong result : ", xByteValue < xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte less float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less float : wrong result : ", xByteValue < yFloatValue, booleanValue);

		value = eval(yByte + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less float : wrong result : ", yByteValue < xFloatValue, booleanValue);

		value = eval(xByte + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less float : wrong result : ", xByteValue < xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte less double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less double : wrong result : ", xByteValue < yDoubleValue, booleanValue);

		value = eval(yByte + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less double : wrong result : ", yByteValue < xDoubleValue, booleanValue);

		value = eval(xByte + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte less double : wrong result : ", xByteValue < xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// byte <= {byte, char, short, int, long, float, double}

	public void testByteLessEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual byte : wrong result : ", xByteValue <= yByteValue, booleanValue);

		value = eval(yByte + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual byte : wrong result : ", yByteValue <= xByteValue, booleanValue);

		value = eval(xByte + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual byte : wrong result : ", xByteValue <= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual char : wrong result : ", xByteValue <= yCharValue, booleanValue);

		value = eval(yByte + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual char : wrong result : ", yByteValue <= xCharValue, booleanValue);

		value = eval(xByte + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual char : wrong result : ", xByteValue <= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual short : wrong result : ", xByteValue <= yShortValue, booleanValue);

		value = eval(yByte + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual short : wrong result : ", yByteValue <= xShortValue, booleanValue);

		value = eval(xByte + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual short : wrong result : ", xByteValue <= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual int : wrong result : ", xByteValue <= yIntValue, booleanValue);

		value = eval(yByte + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual int : wrong result : ", yByteValue <= xIntValue, booleanValue);

		value = eval(xByte + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual int : wrong result : ", xByteValue <= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual long : wrong result : ", xByteValue <= yLongValue, booleanValue);

		value = eval(yByte + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual long : wrong result : ", yByteValue <= xLongValue, booleanValue);

		value = eval(xByte + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual long : wrong result : ", xByteValue <= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual float : wrong result : ", xByteValue <= yFloatValue, booleanValue);

		value = eval(yByte + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual float : wrong result : ", yByteValue <= xFloatValue, booleanValue);

		value = eval(xByte + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual float : wrong result : ", xByteValue <= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteLessEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + lessEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual double : wrong result : ", xByteValue <= yDoubleValue, booleanValue);

		value = eval(yByte + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual double : wrong result : ", yByteValue <= xDoubleValue, booleanValue);

		value = eval(xByte + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte lessEqual double : wrong result : ", xByteValue <= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// byte == {byte, char, short, int, long, float, double}

	public void testByteEqualEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + equalEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual byte : wrong result : ", xByteValue == yByteValue, booleanValue);

		value = eval(yByte + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual byte : wrong result : ", yByteValue == xByteValue, booleanValue);

		value = eval(xByte + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual byte : wrong result : ", true, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteEqualEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + equalEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual char : wrong result : ", xByteValue == yCharValue, booleanValue);

		value = eval(yByte + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual char : wrong result : ", yByteValue == xCharValue, booleanValue);

		value = eval(xByte + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual char : wrong result : ", xByteValue == xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteEqualEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + equalEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual short : wrong result : ", xByteValue == yShortValue, booleanValue);

		value = eval(yByte + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual short : wrong result : ", yByteValue == xShortValue, booleanValue);

		value = eval(xByte + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual short : wrong result : ", xByteValue == xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteEqualEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + equalEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual int : wrong result : ", xByteValue == yIntValue, booleanValue);

		value = eval(yByte + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual int : wrong result : ", yByteValue == xIntValue, booleanValue);

		value = eval(xByte + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual int : wrong result : ", xByteValue == xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteEqualEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + equalEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual long : wrong result : ", xByteValue == yLongValue, booleanValue);

		value = eval(yByte + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual long : wrong result : ", yByteValue == xLongValue, booleanValue);

		value = eval(xByte + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual long : wrong result : ", xByteValue == xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteEqualEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + equalEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual float : wrong result : ", xByteValue == yFloatValue, booleanValue);

		value = eval(yByte + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual float : wrong result : ", yByteValue == xFloatValue, booleanValue);

		value = eval(xByte + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual float : wrong result : ", xByteValue == xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteEqualEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + equalEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual double : wrong result : ", xByteValue == yDoubleValue, booleanValue);

		value = eval(yByte + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual double : wrong result : ", yByteValue == xDoubleValue, booleanValue);

		value = eval(xByte + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte equalEqual double : wrong result : ", xByteValue == xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// byte != {byte, char, short, int, long, float, double}

	public void testByteNotEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + notEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual byte : wrong result : ", xByteValue != yByteValue, booleanValue);

		value = eval(yByte + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual byte : wrong result : ", yByteValue != xByteValue, booleanValue);

		value = eval(xByte + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual byte : wrong result : ", false, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteNotEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + notEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual char : wrong result : ", xByteValue != yCharValue, booleanValue);

		value = eval(yByte + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual char : wrong result : ", yByteValue != xCharValue, booleanValue);

		value = eval(xByte + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual char : wrong result : ", xByteValue != xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteNotEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + notEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual short : wrong result : ", xByteValue != yShortValue, booleanValue);

		value = eval(yByte + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual short : wrong result : ", yByteValue != xShortValue, booleanValue);

		value = eval(xByte + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual short : wrong result : ", xByteValue != xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteNotEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + notEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual int : wrong result : ", xByteValue != yIntValue, booleanValue);

		value = eval(yByte + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual int : wrong result : ", yByteValue != xIntValue, booleanValue);

		value = eval(xByte + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual int : wrong result : ", xByteValue != xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteNotEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + notEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual long : wrong result : ", xByteValue != yLongValue, booleanValue);

		value = eval(yByte + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual long : wrong result : ", yByteValue != xLongValue, booleanValue);

		value = eval(xByte + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual long : wrong result : ", xByteValue != xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteNotEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + notEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual float : wrong result : ", xByteValue != yFloatValue, booleanValue);

		value = eval(yByte + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual float : wrong result : ", yByteValue != xFloatValue, booleanValue);

		value = eval(xByte + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual float : wrong result : ", xByteValue != xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testByteNotEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + notEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual double : wrong result : ", xByteValue != yDoubleValue, booleanValue);

		value = eval(yByte + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual double : wrong result : ", yByteValue != xDoubleValue, booleanValue);

		value = eval(xByte + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("byte notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("byte notEqual double : wrong result : ", xByteValue != xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// byte << {byte, char, short, int, long}

	public void testByteLeftShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + leftShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift byte : wrong result : ", xByteValue << yByteValue, intValue);

		value = eval(yByte + leftShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift byte : wrong result : ", yByteValue << xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteLeftShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + leftShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift char : wrong result : ", xByteValue << yCharValue, intValue);

		value = eval(yByte + leftShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift char : wrong result : ", yByteValue << xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteLeftShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + leftShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift short : wrong result : ", xByteValue << yShortValue, intValue);

		value = eval(yByte + leftShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift short : wrong result : ", yByteValue << xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteLeftShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + leftShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift int : wrong result : ", xByteValue << yIntValue, intValue);

		value = eval(yByte + leftShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift int : wrong result : ", yByteValue << xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteLeftShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + leftShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift long : wrong result : ", xByteValue << yLongValue, intValue);

		value = eval(yByte + leftShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte leftShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte leftShift long : wrong result : ", yByteValue << xLongValue, intValue);
		} finally {
		end();
		}
	}

	// byte >> {byte, char, short, int, long}

	public void testByteRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + rightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift byte : wrong result : ", xByteValue >> yByteValue, intValue);

		value = eval(yByte + rightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift byte : wrong result : ", yByteValue >> xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + rightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift char : wrong result : ", xByteValue >> yCharValue, intValue);

		value = eval(yByte + rightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift char : wrong result : ", yByteValue >> xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + rightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift short : wrong result : ", xByteValue >> yShortValue, intValue);

		value = eval(yByte + rightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift short : wrong result : ", yByteValue >> xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + rightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift int : wrong result : ", xByteValue >> yIntValue, intValue);

		value = eval(yByte + rightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift int : wrong result : ", yByteValue >> xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + rightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift long : wrong result : ", xByteValue >> yLongValue, intValue);

		value = eval(yByte + rightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte rightShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte rightShift long : wrong result : ", yByteValue >> xLongValue, intValue);
		} finally {
		end();
		}
	}

	// byte >>> {byte, char, short, int, long}

	public void testByteUnsignedRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + unsignedRightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift byte : wrong result : ", xByteValue >>> yByteValue, intValue);

		value = eval(yByte + unsignedRightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift byte : wrong result : ", yByteValue >>> xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteUnsignedRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + unsignedRightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift char : wrong result : ", xByteValue >>> yCharValue, intValue);

		value = eval(yByte + unsignedRightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift char : wrong result : ", yByteValue >>> xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteUnsignedRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + unsignedRightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift short : wrong result : ", xByteValue >>> yShortValue, intValue);

		value = eval(yByte + unsignedRightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift short : wrong result : ", yByteValue >>> xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteUnsignedRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + unsignedRightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift int : wrong result : ", xByteValue >>> yIntValue, intValue);

		value = eval(yByte + unsignedRightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift int : wrong result : ", yByteValue >>> xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteUnsignedRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + unsignedRightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift long : wrong result : ", xByteValue >>> yLongValue, intValue);

		value = eval(yByte + unsignedRightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte unsignedRightShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte unsignedRightShift long : wrong result : ", yByteValue >>> xLongValue, intValue);
		} finally {
		end();
		}
	}

	// byte | {byte, char, short, int, long}

	public void testByteOrByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + orOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte or byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte or byte : wrong result : ", xByteValue | yByteValue, intValue);

		value = eval(yByte + orOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte or byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte or byte : wrong result : ", yByteValue | xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteOrChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + orOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte or char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte or char : wrong result : ", xByteValue | yCharValue, intValue);

		value = eval(yByte + orOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte or char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte or char : wrong result : ", yByteValue | xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteOrShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + orOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte or short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte or short : wrong result : ", xByteValue | yShortValue, intValue);

		value = eval(yByte + orOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte or short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte or short : wrong result : ", yByteValue | xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteOrInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + orOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte or int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte or int : wrong result : ", xByteValue | yIntValue, intValue);

		value = eval(yByte + orOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte or int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte or int : wrong result : ", yByteValue | xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteOrLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + orOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte or long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte or long : wrong result : ", xByteValue | yLongValue, longValue);

		value = eval(yByte + orOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte or long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte or long : wrong result : ", yByteValue | xLongValue, longValue);
		} finally {
		end();
		}
	}

	// byte & {byte, char, short, int, long}

	public void testByteAndByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + andOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte and byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte and byte : wrong result : ", xByteValue & yByteValue, intValue);

		value = eval(yByte + andOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte and byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte and byte : wrong result : ", yByteValue & xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteAndChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + andOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte and char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte and char : wrong result : ", xByteValue & yCharValue, intValue);

		value = eval(yByte + andOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte and char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte and char : wrong result : ", yByteValue & xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteAndShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + andOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte and short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte and short : wrong result : ", xByteValue & yShortValue, intValue);

		value = eval(yByte + andOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte and short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte and short : wrong result : ", yByteValue & xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteAndInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + andOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte and int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte and int : wrong result : ", xByteValue & yIntValue, intValue);

		value = eval(yByte + andOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte and int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte and int : wrong result : ", yByteValue & xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteAndLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + andOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte and long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte and long : wrong result : ", xByteValue & yLongValue, longValue);

		value = eval(yByte + andOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte and long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte and long : wrong result : ", yByteValue & xLongValue, longValue);
		} finally {
		end();
		}
	}

	// byte ^ {byte, char, short, int, long}

	public void testByteXorByte() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + xorOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xor byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte xor byte : wrong result : ", xByteValue ^ yByteValue, intValue);

		value = eval(yByte + xorOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xor byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte xor byte : wrong result : ", yByteValue ^ xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteXorChar() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + xorOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xor char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte xor char : wrong result : ", xByteValue ^ yCharValue, intValue);

		value = eval(yByte + xorOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xor char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte xor char : wrong result : ", yByteValue ^ xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteXorShort() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + xorOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xor short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte xor short : wrong result : ", xByteValue ^ yShortValue, intValue);

		value = eval(yByte + xorOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xor short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte xor short : wrong result : ", yByteValue ^ xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteXorInt() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + xorOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xor int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte xor int : wrong result : ", xByteValue ^ yIntValue, intValue);

		value = eval(yByte + xorOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xor int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("byte xor int : wrong result : ", yByteValue ^ xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testByteXorLong() throws Throwable {
		try {
		init();
		IValue value = eval(xByte + xorOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("byte xor long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte xor long : wrong result : ", xByteValue ^ yLongValue, longValue);

		value = eval(yByte + xorOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("byte xor long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("byte xor long : wrong result : ", yByteValue ^ xLongValue, longValue);
		} finally {
		end();
		}
	}

	// + byte

	public void testPlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(plusOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("plus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("plus byte : wrong result : ", + xByteValue, intValue);

		value = eval(plusOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("plus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("plus byte : wrong result : ", + yByteValue, intValue);
		} finally {
		end();
		}
	}

	// - byte

	public void testMinusByte() throws Throwable {
		try {
		init();
		IValue value = eval(minusOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("minus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("minus byte : wrong result : ", - xByteValue, intValue);

		value = eval(minusOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("minus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("minus byte : wrong result : ", - yByteValue, intValue);
		} finally {
		end();
		}
	}

	// ~ byte

	public void testTwiddleByte() throws Throwable {
		try {
		init();
		IValue value = eval(twiddleOp + xByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("twiddle byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("twiddle byte : wrong result : ", ~ xByteValue, intValue);

		value = eval(twiddleOp + yByte);
		typeName = value.getReferenceTypeName();
		assertEquals("twiddle byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("twiddle byte : wrong result : ", ~ yByteValue, intValue);
		} finally {
		end();
		}
	}


}
