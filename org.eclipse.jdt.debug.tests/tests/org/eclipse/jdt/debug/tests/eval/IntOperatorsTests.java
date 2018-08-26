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

public class IntOperatorsTests extends Tests {

	public IntOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// int + {byte, char, short, int, long, float, double}

	public void testIntPlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus byte : wrong result : ", xIntValue + yByteValue, intValue);

		value = eval(yInt + plusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus byte : wrong result : ", yIntValue + xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntPlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus char : wrong result : ", xIntValue + yCharValue, intValue);

		value = eval(yInt + plusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus char : wrong result : ", yIntValue + xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntPlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus short : wrong result : ", xIntValue + yShortValue, intValue);

		value = eval(yInt + plusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus short : wrong result : ", yIntValue + xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntPlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus int : wrong result : ", xIntValue + yIntValue, intValue);

		value = eval(yInt + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int plus int : wrong result : ", yIntValue + xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntPlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int plus long : wrong result : ", xIntValue + yLongValue, longValue);

		value = eval(yInt + plusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int plus long : wrong result : ", yIntValue + xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testIntPlusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int plus float : wrong result : ", xIntValue + yFloatValue, floatValue, 0);

		value = eval(yInt + plusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int plus float : wrong result : ", yIntValue + xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testIntPlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int plus double : wrong result : ", xIntValue + yDoubleValue, doubleValue, 0);

		value = eval(yInt + plusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int plus double : wrong result : ", yIntValue + xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testIntPlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("int plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("int plus java.lang.String : wrong result : ", xIntValue + yStringValue, stringValue);

		value = eval(yInt + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("int plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("int plus java.lang.String : wrong result : ", yIntValue + xStringValue, stringValue);
		} finally {
		end();
		}
	}

	// int - {byte, char, short, int, long, float, double}

	public void testIntMinusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + minusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minus byte : wrong result : ", xIntValue - yByteValue, intValue);

		value = eval(yInt + minusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int minus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minus byte : wrong result : ", yIntValue - xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + minusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minus char : wrong result : ", xIntValue - yCharValue, intValue);

		value = eval(yInt + minusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int minus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minus char : wrong result : ", yIntValue - xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + minusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minus short : wrong result : ", xIntValue - yShortValue, intValue);

		value = eval(yInt + minusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int minus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minus short : wrong result : ", yIntValue - xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + minusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minus int : wrong result : ", xIntValue - yIntValue, intValue);

		value = eval(yInt + minusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int minus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int minus int : wrong result : ", yIntValue - xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntMinusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + minusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int minus long : wrong result : ", xIntValue - yLongValue, longValue);

		value = eval(yInt + minusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int minus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int minus long : wrong result : ", yIntValue - xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testIntMinusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + minusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int minus float : wrong result : ", xIntValue - yFloatValue, floatValue, 0);

		value = eval(yInt + minusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int minus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int minus float : wrong result : ", yIntValue - xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testIntMinusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + minusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int minus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int minus double : wrong result : ", xIntValue - yDoubleValue, doubleValue, 0);

		value = eval(yInt + minusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int minus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int minus double : wrong result : ", yIntValue - xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// int * {byte, char, short, int, long, float, double}

	public void testIntMultiplyByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + multiplyOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiply byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiply byte : wrong result : ", xIntValue * yByteValue, intValue);

		value = eval(yInt + multiplyOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiply byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiply byte : wrong result : ", yIntValue * xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + multiplyOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiply char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiply char : wrong result : ", xIntValue * yCharValue, intValue);

		value = eval(yInt + multiplyOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiply char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiply char : wrong result : ", yIntValue * xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + multiplyOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiply short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiply short : wrong result : ", xIntValue * yShortValue, intValue);

		value = eval(yInt + multiplyOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiply short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiply short : wrong result : ", yIntValue * xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + multiplyOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiply int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiply int : wrong result : ", xIntValue * yIntValue, intValue);

		value = eval(yInt + multiplyOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiply int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int multiply int : wrong result : ", yIntValue * xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + multiplyOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiply long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int multiply long : wrong result : ", xIntValue * yLongValue, longValue);

		value = eval(yInt + multiplyOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiply long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int multiply long : wrong result : ", yIntValue * xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testIntMultiplyFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + multiplyOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiply float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int multiply float : wrong result : ", xIntValue * yFloatValue, floatValue, 0);

		value = eval(yInt + multiplyOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiply float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int multiply float : wrong result : ", yIntValue * xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testIntMultiplyDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + multiplyOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int multiply double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int multiply double : wrong result : ", xIntValue * yDoubleValue, doubleValue, 0);

		value = eval(yInt + multiplyOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int multiply double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int multiply double : wrong result : ", yIntValue * xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// int / {byte, char, short, int, long, float, double}

	public void testIntDivideByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + divideOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divide byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divide byte : wrong result : ", xIntValue / yByteValue, intValue);

		value = eval(yInt + divideOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int divide byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divide byte : wrong result : ", yIntValue / xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + divideOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divide char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divide char : wrong result : ", xIntValue / yCharValue, intValue);

		value = eval(yInt + divideOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int divide char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divide char : wrong result : ", yIntValue / xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + divideOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divide short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divide short : wrong result : ", xIntValue / yShortValue, intValue);

		value = eval(yInt + divideOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int divide short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divide short : wrong result : ", yIntValue / xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + divideOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divide int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divide int : wrong result : ", xIntValue / yIntValue, intValue);

		value = eval(yInt + divideOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int divide int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int divide int : wrong result : ", yIntValue / xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntDivideLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + divideOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divide long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int divide long : wrong result : ", xIntValue / yLongValue, longValue);

		value = eval(yInt + divideOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int divide long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int divide long : wrong result : ", yIntValue / xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testIntDivideFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + divideOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divide float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int divide float : wrong result : ", xIntValue / yFloatValue, floatValue, 0);

		value = eval(yInt + divideOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int divide float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int divide float : wrong result : ", yIntValue / xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testIntDivideDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + divideOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int divide double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int divide double : wrong result : ", xIntValue / yDoubleValue, doubleValue, 0);

		value = eval(yInt + divideOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int divide double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int divide double : wrong result : ", yIntValue / xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// int % {byte, char, short, int, long, float, double}

	public void testIntRemainderByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + remainderOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainder byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainder byte : wrong result : ", xIntValue % yByteValue, intValue);

		value = eval(yInt + remainderOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainder byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainder byte : wrong result : ", yIntValue % xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + remainderOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainder char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainder char : wrong result : ", xIntValue % yCharValue, intValue);

		value = eval(yInt + remainderOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainder char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainder char : wrong result : ", yIntValue % xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + remainderOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainder short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainder short : wrong result : ", xIntValue % yShortValue, intValue);

		value = eval(yInt + remainderOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainder short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainder short : wrong result : ", yIntValue % xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + remainderOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainder int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainder int : wrong result : ", xIntValue % yIntValue, intValue);

		value = eval(yInt + remainderOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainder int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int remainder int : wrong result : ", yIntValue % xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + remainderOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainder long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int remainder long : wrong result : ", xIntValue % yLongValue, longValue);

		value = eval(yInt + remainderOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainder long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int remainder long : wrong result : ", yIntValue % xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testIntRemainderFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + remainderOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainder float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int remainder float : wrong result : ", xIntValue % yFloatValue, floatValue, 0);

		value = eval(yInt + remainderOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainder float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("int remainder float : wrong result : ", yIntValue % xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testIntRemainderDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + remainderOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int remainder double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int remainder double : wrong result : ", xIntValue % yDoubleValue, doubleValue, 0);

		value = eval(yInt + remainderOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int remainder double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("int remainder double : wrong result : ", yIntValue % xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// int > {byte, char, short, int, long, float, double}

	public void testIntGreaterByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greater byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater byte : wrong result : ", xIntValue > yByteValue, booleanValue);

		value = eval(yInt + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater byte : wrong result : ", yIntValue > xByteValue, booleanValue);

		value = eval(xInt + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater byte : wrong result : ", xIntValue > xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greater char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater char : wrong result : ", xIntValue > yCharValue, booleanValue);

		value = eval(yInt + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater char : wrong result : ", yIntValue > xCharValue, booleanValue);

		value = eval(xInt + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater char : wrong result : ", xIntValue > xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greater short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater short : wrong result : ", xIntValue > yShortValue, booleanValue);

		value = eval(yInt + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater short : wrong result : ", yIntValue > xShortValue, booleanValue);

		value = eval(xInt + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater short : wrong result : ", xIntValue > xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greater int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater int : wrong result : ", xIntValue > yIntValue, booleanValue);

		value = eval(yInt + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater int : wrong result : ", yIntValue > xIntValue, booleanValue);

		value = eval(xInt + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater int : wrong result : ", xIntValue > xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greater long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater long : wrong result : ", xIntValue > yLongValue, booleanValue);

		value = eval(yInt + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater long : wrong result : ", yIntValue > xLongValue, booleanValue);

		value = eval(xInt + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater long : wrong result : ", xIntValue > xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greater float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater float : wrong result : ", xIntValue > yFloatValue, booleanValue);

		value = eval(yInt + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater float : wrong result : ", yIntValue > xFloatValue, booleanValue);

		value = eval(xInt + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater float : wrong result : ", xIntValue > xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greater double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater double : wrong result : ", xIntValue > yDoubleValue, booleanValue);

		value = eval(yInt + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater double : wrong result : ", yIntValue > xDoubleValue, booleanValue);

		value = eval(xInt + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greater double : wrong result : ", xIntValue > xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// int >= {byte, char, short, int, long, float, double}

	public void testIntGreaterEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual byte : wrong result : ", xIntValue >= yByteValue, booleanValue);

		value = eval(yInt + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual byte : wrong result : ", yIntValue >= xByteValue, booleanValue);

		value = eval(xInt + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual byte : wrong result : ", xIntValue >= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual char : wrong result : ", xIntValue >= yCharValue, booleanValue);

		value = eval(yInt + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual char : wrong result : ", yIntValue >= xCharValue, booleanValue);

		value = eval(xInt + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual char : wrong result : ", xIntValue >= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual short : wrong result : ", xIntValue >= yShortValue, booleanValue);

		value = eval(yInt + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual short : wrong result : ", yIntValue >= xShortValue, booleanValue);

		value = eval(xInt + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual short : wrong result : ", xIntValue >= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual int : wrong result : ", xIntValue >= yIntValue, booleanValue);

		value = eval(yInt + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual int : wrong result : ", yIntValue >= xIntValue, booleanValue);

		value = eval(xInt + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual int : wrong result : ", xIntValue >= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual long : wrong result : ", xIntValue >= yLongValue, booleanValue);

		value = eval(yInt + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual long : wrong result : ", yIntValue >= xLongValue, booleanValue);

		value = eval(xInt + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual long : wrong result : ", xIntValue >= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual float : wrong result : ", xIntValue >= yFloatValue, booleanValue);

		value = eval(yInt + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual float : wrong result : ", yIntValue >= xFloatValue, booleanValue);

		value = eval(xInt + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual float : wrong result : ", xIntValue >= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntGreaterEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + greaterEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual double : wrong result : ", xIntValue >= yDoubleValue, booleanValue);

		value = eval(yInt + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual double : wrong result : ", yIntValue >= xDoubleValue, booleanValue);

		value = eval(xInt + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int greaterEqual double : wrong result : ", xIntValue >= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// int < {byte, char, short, int, long, float, double}

	public void testIntLessByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int less byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less byte : wrong result : ", xIntValue < yByteValue, booleanValue);

		value = eval(yInt + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less byte : wrong result : ", yIntValue < xByteValue, booleanValue);

		value = eval(xInt + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less byte : wrong result : ", xIntValue < xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int less char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less char : wrong result : ", xIntValue < yCharValue, booleanValue);

		value = eval(yInt + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less char : wrong result : ", yIntValue < xCharValue, booleanValue);

		value = eval(xInt + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less char : wrong result : ", xIntValue < xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int less short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less short : wrong result : ", xIntValue < yShortValue, booleanValue);

		value = eval(yInt + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less short : wrong result : ", yIntValue < xShortValue, booleanValue);

		value = eval(xInt + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less short : wrong result : ", xIntValue < xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int less int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less int : wrong result : ", xIntValue < yIntValue, booleanValue);

		value = eval(yInt + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less int : wrong result : ", yIntValue < xIntValue, booleanValue);

		value = eval(xInt + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less int : wrong result : ", xIntValue < xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int less long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less long : wrong result : ", xIntValue < yLongValue, booleanValue);

		value = eval(yInt + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less long : wrong result : ", yIntValue < xLongValue, booleanValue);

		value = eval(xInt + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less long : wrong result : ", xIntValue < xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int less float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less float : wrong result : ", xIntValue < yFloatValue, booleanValue);

		value = eval(yInt + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less float : wrong result : ", yIntValue < xFloatValue, booleanValue);

		value = eval(xInt + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less float : wrong result : ", xIntValue < xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int less double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less double : wrong result : ", xIntValue < yDoubleValue, booleanValue);

		value = eval(yInt + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less double : wrong result : ", yIntValue < xDoubleValue, booleanValue);

		value = eval(xInt + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int less double : wrong result : ", xIntValue < xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// int <= {byte, char, short, int, long, float, double}

	public void testIntLessEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual byte : wrong result : ", xIntValue <= yByteValue, booleanValue);

		value = eval(yInt + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual byte : wrong result : ", yIntValue <= xByteValue, booleanValue);

		value = eval(xInt + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual byte : wrong result : ", xIntValue <= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual char : wrong result : ", xIntValue <= yCharValue, booleanValue);

		value = eval(yInt + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual char : wrong result : ", yIntValue <= xCharValue, booleanValue);

		value = eval(xInt + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual char : wrong result : ", xIntValue <= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual short : wrong result : ", xIntValue <= yShortValue, booleanValue);

		value = eval(yInt + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual short : wrong result : ", yIntValue <= xShortValue, booleanValue);

		value = eval(xInt + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual short : wrong result : ", xIntValue <= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual int : wrong result : ", xIntValue <= yIntValue, booleanValue);

		value = eval(yInt + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual int : wrong result : ", yIntValue <= xIntValue, booleanValue);

		value = eval(xInt + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual int : wrong result : ", xIntValue <= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual long : wrong result : ", xIntValue <= yLongValue, booleanValue);

		value = eval(yInt + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual long : wrong result : ", yIntValue <= xLongValue, booleanValue);

		value = eval(xInt + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual long : wrong result : ", xIntValue <= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual float : wrong result : ", xIntValue <= yFloatValue, booleanValue);

		value = eval(yInt + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual float : wrong result : ", yIntValue <= xFloatValue, booleanValue);

		value = eval(xInt + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual float : wrong result : ", xIntValue <= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntLessEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + lessEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual double : wrong result : ", xIntValue <= yDoubleValue, booleanValue);

		value = eval(yInt + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual double : wrong result : ", yIntValue <= xDoubleValue, booleanValue);

		value = eval(xInt + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int lessEqual double : wrong result : ", xIntValue <= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// int == {byte, char, short, int, long, float, double}

	public void testIntEqualEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + equalEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual byte : wrong result : ", xIntValue == yByteValue, booleanValue);

		value = eval(yInt + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual byte : wrong result : ", yIntValue == xByteValue, booleanValue);

		value = eval(xInt + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual byte : wrong result : ", xIntValue == xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntEqualEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + equalEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual char : wrong result : ", xIntValue == yCharValue, booleanValue);

		value = eval(yInt + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual char : wrong result : ", yIntValue == xCharValue, booleanValue);

		value = eval(xInt + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual char : wrong result : ", xIntValue == xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntEqualEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + equalEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual short : wrong result : ", xIntValue == yShortValue, booleanValue);

		value = eval(yInt + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual short : wrong result : ", yIntValue == xShortValue, booleanValue);

		value = eval(xInt + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual short : wrong result : ", xIntValue == xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntEqualEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + equalEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual int : wrong result : ", xIntValue == yIntValue, booleanValue);

		value = eval(yInt + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual int : wrong result : ", yIntValue == xIntValue, booleanValue);

		value = eval(xInt + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual int : wrong result : ", true, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntEqualEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + equalEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual long : wrong result : ", xIntValue == yLongValue, booleanValue);

		value = eval(yInt + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual long : wrong result : ", yIntValue == xLongValue, booleanValue);

		value = eval(xInt + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual long : wrong result : ", xIntValue == xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntEqualEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + equalEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual float : wrong result : ", xIntValue == yFloatValue, booleanValue);

		value = eval(yInt + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual float : wrong result : ", yIntValue == xFloatValue, booleanValue);

		value = eval(xInt + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual float : wrong result : ", xIntValue == xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntEqualEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + equalEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual double : wrong result : ", xIntValue == yDoubleValue, booleanValue);

		value = eval(yInt + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual double : wrong result : ", yIntValue == xDoubleValue, booleanValue);

		value = eval(xInt + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int equalEqual double : wrong result : ", xIntValue == xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// int != {byte, char, short, int, long, float, double}

	public void testIntNotEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + notEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int notEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual byte : wrong result : ", xIntValue != yByteValue, booleanValue);

		value = eval(yInt + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual byte : wrong result : ", yIntValue != xByteValue, booleanValue);

		value = eval(xInt + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual byte : wrong result : ", xIntValue != xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntNotEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + notEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int notEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual char : wrong result : ", xIntValue != yCharValue, booleanValue);

		value = eval(yInt + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual char : wrong result : ", yIntValue != xCharValue, booleanValue);

		value = eval(xInt + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual char : wrong result : ", xIntValue != xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntNotEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + notEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int notEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual short : wrong result : ", xIntValue != yShortValue, booleanValue);

		value = eval(yInt + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual short : wrong result : ", yIntValue != xShortValue, booleanValue);

		value = eval(xInt + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual short : wrong result : ", xIntValue != xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntNotEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + notEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int notEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual int : wrong result : ", xIntValue != yIntValue, booleanValue);

		value = eval(yInt + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual int : wrong result : ", yIntValue != xIntValue, booleanValue);

		value = eval(xInt + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual int : wrong result : ", false, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntNotEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + notEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int notEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual long : wrong result : ", xIntValue != yLongValue, booleanValue);

		value = eval(yInt + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual long : wrong result : ", yIntValue != xLongValue, booleanValue);

		value = eval(xInt + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual long : wrong result : ", xIntValue != xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntNotEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + notEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("int notEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual float : wrong result : ", xIntValue != yFloatValue, booleanValue);

		value = eval(yInt + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual float : wrong result : ", yIntValue != xFloatValue, booleanValue);

		value = eval(xInt + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual float : wrong result : ", xIntValue != xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testIntNotEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + notEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("int notEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual double : wrong result : ", xIntValue != yDoubleValue, booleanValue);

		value = eval(yInt + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual double : wrong result : ", yIntValue != xDoubleValue, booleanValue);

		value = eval(xInt + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("int notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("int notEqual double : wrong result : ", xIntValue != xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// int << {byte, char, short, int, long}

	public void testIntLeftShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + leftShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift byte : wrong result : ", xIntValue << yByteValue, intValue);

		value = eval(yInt + leftShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift byte : wrong result : ", yIntValue << xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntLeftShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + leftShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift char : wrong result : ", xIntValue << yCharValue, intValue);

		value = eval(yInt + leftShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift char : wrong result : ", yIntValue << xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntLeftShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + leftShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift short : wrong result : ", xIntValue << yShortValue, intValue);

		value = eval(yInt + leftShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift short : wrong result : ", yIntValue << xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntLeftShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + leftShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift int : wrong result : ", xIntValue << yIntValue, intValue);

		value = eval(yInt + leftShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift int : wrong result : ", yIntValue << xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntLeftShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + leftShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int leftShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift long : wrong result : ", xIntValue << yLongValue, intValue);

		value = eval(yInt + leftShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int leftShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int leftShift long : wrong result : ", yIntValue << xLongValue, intValue);
		} finally {
		end();
		}
	}

	// int >> {byte, char, short, int, long}

	public void testIntRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + rightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift byte : wrong result : ", xIntValue >> yByteValue, intValue);

		value = eval(yInt + rightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift byte : wrong result : ", yIntValue >> xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + rightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift char : wrong result : ", xIntValue >> yCharValue, intValue);

		value = eval(yInt + rightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift char : wrong result : ", yIntValue >> xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + rightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift short : wrong result : ", xIntValue >> yShortValue, intValue);

		value = eval(yInt + rightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift short : wrong result : ", yIntValue >> xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + rightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift int : wrong result : ", xIntValue >> yIntValue, intValue);

		value = eval(yInt + rightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift int : wrong result : ", yIntValue >> xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + rightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int rightShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift long : wrong result : ", xIntValue >> yLongValue, intValue);

		value = eval(yInt + rightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int rightShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int rightShift long : wrong result : ", yIntValue >> xLongValue, intValue);
		} finally {
		end();
		}
	}

	// int >>> {byte, char, short, int, long}

	public void testIntUnsignedRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + unsignedRightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift byte : wrong result : ", xIntValue >>> yByteValue, intValue);

		value = eval(yInt + unsignedRightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift byte : wrong result : ", yIntValue >>> xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntUnsignedRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + unsignedRightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift char : wrong result : ", xIntValue >>> yCharValue, intValue);

		value = eval(yInt + unsignedRightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift char : wrong result : ", yIntValue >>> xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntUnsignedRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + unsignedRightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift short : wrong result : ", xIntValue >>> yShortValue, intValue);

		value = eval(yInt + unsignedRightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift short : wrong result : ", yIntValue >>> xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntUnsignedRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + unsignedRightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift int : wrong result : ", xIntValue >>> yIntValue, intValue);

		value = eval(yInt + unsignedRightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift int : wrong result : ", yIntValue >>> xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntUnsignedRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + unsignedRightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift long : wrong result : ", xIntValue >>> yLongValue, intValue);

		value = eval(yInt + unsignedRightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int unsignedRightShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int unsignedRightShift long : wrong result : ", yIntValue >>> xLongValue, intValue);
		} finally {
		end();
		}
	}

	// int | {byte, char, short, int, long}

	public void testIntOrByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + orOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int or byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int or byte : wrong result : ", xIntValue | yByteValue, intValue);

		value = eval(yInt + orOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int or byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int or byte : wrong result : ", yIntValue | xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntOrChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + orOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int or char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int or char : wrong result : ", xIntValue | yCharValue, intValue);

		value = eval(yInt + orOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int or char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int or char : wrong result : ", yIntValue | xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntOrShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + orOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int or short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int or short : wrong result : ", xIntValue | yShortValue, intValue);

		value = eval(yInt + orOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int or short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int or short : wrong result : ", yIntValue | xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntOrInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + orOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int or int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int or int : wrong result : ", xIntValue | yIntValue, intValue);

		value = eval(yInt + orOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int or int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int or int : wrong result : ", yIntValue | xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntOrLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + orOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int or long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int or long : wrong result : ", xIntValue | yLongValue, longValue);

		value = eval(yInt + orOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int or long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int or long : wrong result : ", yIntValue | xLongValue, longValue);
		} finally {
		end();
		}
	}

	// int & {byte, char, short, int, long}

	public void testIntAndByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + andOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int and byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int and byte : wrong result : ", xIntValue & yByteValue, intValue);

		value = eval(yInt + andOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int and byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int and byte : wrong result : ", yIntValue & xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntAndChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + andOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int and char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int and char : wrong result : ", xIntValue & yCharValue, intValue);

		value = eval(yInt + andOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int and char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int and char : wrong result : ", yIntValue & xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntAndShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + andOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int and short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int and short : wrong result : ", xIntValue & yShortValue, intValue);

		value = eval(yInt + andOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int and short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int and short : wrong result : ", yIntValue & xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntAndInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + andOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int and int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int and int : wrong result : ", xIntValue & yIntValue, intValue);

		value = eval(yInt + andOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int and int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int and int : wrong result : ", yIntValue & xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntAndLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + andOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int and long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int and long : wrong result : ", xIntValue & yLongValue, longValue);

		value = eval(yInt + andOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int and long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int and long : wrong result : ", yIntValue & xLongValue, longValue);
		} finally {
		end();
		}
	}

	// int ^ {byte, char, short, int, long}

	public void testIntXorByte() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + xorOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xor byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xor byte : wrong result : ", xIntValue ^ yByteValue, intValue);

		value = eval(yInt + xorOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("int xor byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xor byte : wrong result : ", yIntValue ^ xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntXorChar() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + xorOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xor char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xor char : wrong result : ", xIntValue ^ yCharValue, intValue);

		value = eval(yInt + xorOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("int xor char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xor char : wrong result : ", yIntValue ^ xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntXorShort() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + xorOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xor short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xor short : wrong result : ", xIntValue ^ yShortValue, intValue);

		value = eval(yInt + xorOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("int xor short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xor short : wrong result : ", yIntValue ^ xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntXorInt() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + xorOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xor int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xor int : wrong result : ", xIntValue ^ yIntValue, intValue);

		value = eval(yInt + xorOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("int xor int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("int xor int : wrong result : ", yIntValue ^ xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testIntXorLong() throws Throwable {
		try {
		init();
		IValue value = eval(xInt + xorOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("int xor long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int xor long : wrong result : ", xIntValue ^ yLongValue, longValue);

		value = eval(yInt + xorOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("int xor long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("int xor long : wrong result : ", yIntValue ^ xLongValue, longValue);
		} finally {
		end();
		}
	}

	// + int

	public void testPlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(plusOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("plus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("plus int : wrong result : ", + xIntValue, intValue);

		value = eval(plusOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("plus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("plus int : wrong result : ", + yIntValue, intValue);
		} finally {
		end();
		}
	}

	// - int

	public void testMinusInt() throws Throwable {
		try {
		init();
		IValue value = eval(minusOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("minus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("minus int : wrong result : ", - xIntValue, intValue);

		value = eval(minusOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("minus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("minus int : wrong result : ", - yIntValue, intValue);
		} finally {
		end();
		}
	}

	// ~ int

	public void testTwiddleInt() throws Throwable {
		try {
		init();
		IValue value = eval(twiddleOp + xInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("twiddle int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("twiddle int : wrong result : ", ~ xIntValue, intValue);

		value = eval(twiddleOp + yInt);
		typeName = value.getReferenceTypeName();
		assertEquals("twiddle int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("twiddle int : wrong result : ", ~ yIntValue, intValue);
		} finally {
		end();
		}
	}


}
