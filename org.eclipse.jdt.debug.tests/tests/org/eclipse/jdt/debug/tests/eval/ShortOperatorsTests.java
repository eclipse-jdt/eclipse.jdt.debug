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

public class ShortOperatorsTests extends Tests {

	public ShortOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// short + {byte, char, short, int, long, float, double}

	public void testShortPlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + plusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short plus byte : wrong result : ", xShortValue + yByteValue, intValue);

		value = eval(yShort + plusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short plus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short plus byte : wrong result : ", yShortValue + xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortPlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + plusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short plus char : wrong result : ", xShortValue + yCharValue, intValue);

		value = eval(yShort + plusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short plus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short plus char : wrong result : ", yShortValue + xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortPlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + plusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short plus short : wrong result : ", xShortValue + yShortValue, intValue);

		value = eval(yShort + plusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short plus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short plus short : wrong result : ", yShortValue + xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortPlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short plus int : wrong result : ", xShortValue + yIntValue, intValue);

		value = eval(yShort + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short plus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short plus int : wrong result : ", yShortValue + xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortPlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + plusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short plus long : wrong result : ", xShortValue + yLongValue, longValue);

		value = eval(yShort + plusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short plus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short plus long : wrong result : ", yShortValue + xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testShortPlusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + plusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short plus float : wrong result : ", xShortValue + yFloatValue, floatValue, 0);

		value = eval(yShort + plusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short plus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short plus float : wrong result : ", yShortValue + xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testShortPlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + plusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short plus double : wrong result : ", xShortValue + yDoubleValue, doubleValue, 0);

		value = eval(yShort + plusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short plus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short plus double : wrong result : ", yShortValue + xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testShortPlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("short plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("short plus java.lang.String : wrong result : ", xShortValue + yStringValue, stringValue);

		value = eval(yShort + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("short plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("short plus java.lang.String : wrong result : ", yShortValue + xStringValue, stringValue);
		} finally {
		end();
		}
	}

	// short - {byte, char, short, int, long, float, double}

	public void testShortMinusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + minusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short minus byte : wrong result : ", xShortValue - yByteValue, intValue);

		value = eval(yShort + minusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short minus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short minus byte : wrong result : ", yShortValue - xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortMinusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + minusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short minus char : wrong result : ", xShortValue - yCharValue, intValue);

		value = eval(yShort + minusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short minus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short minus char : wrong result : ", yShortValue - xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortMinusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + minusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short minus short : wrong result : ", xShortValue - yShortValue, intValue);

		value = eval(yShort + minusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short minus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short minus short : wrong result : ", yShortValue - xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortMinusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + minusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short minus int : wrong result : ", xShortValue - yIntValue, intValue);

		value = eval(yShort + minusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short minus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short minus int : wrong result : ", yShortValue - xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortMinusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + minusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short minus long : wrong result : ", xShortValue - yLongValue, longValue);

		value = eval(yShort + minusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short minus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short minus long : wrong result : ", yShortValue - xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testShortMinusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + minusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short minus float : wrong result : ", xShortValue - yFloatValue, floatValue, 0);

		value = eval(yShort + minusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short minus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short minus float : wrong result : ", yShortValue - xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testShortMinusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + minusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short minus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short minus double : wrong result : ", xShortValue - yDoubleValue, doubleValue, 0);

		value = eval(yShort + minusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short minus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short minus double : wrong result : ", yShortValue - xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// short * {byte, char, short, int, long, float, double}

	public void testShortMultiplyByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + multiplyOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiply byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short multiply byte : wrong result : ", xShortValue * yByteValue, intValue);

		value = eval(yShort + multiplyOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiply byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short multiply byte : wrong result : ", yShortValue * xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + multiplyOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiply char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short multiply char : wrong result : ", xShortValue * yCharValue, intValue);

		value = eval(yShort + multiplyOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiply char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short multiply char : wrong result : ", yShortValue * xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + multiplyOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiply short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short multiply short : wrong result : ", xShortValue * yShortValue, intValue);

		value = eval(yShort + multiplyOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiply short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short multiply short : wrong result : ", yShortValue * xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + multiplyOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiply int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short multiply int : wrong result : ", xShortValue * yIntValue, intValue);

		value = eval(yShort + multiplyOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiply int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short multiply int : wrong result : ", yShortValue * xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + multiplyOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiply long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short multiply long : wrong result : ", xShortValue * yLongValue, longValue);

		value = eval(yShort + multiplyOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiply long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short multiply long : wrong result : ", yShortValue * xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testShortMultiplyFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + multiplyOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiply float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short multiply float : wrong result : ", xShortValue * yFloatValue, floatValue, 0);

		value = eval(yShort + multiplyOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiply float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short multiply float : wrong result : ", yShortValue * xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testShortMultiplyDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + multiplyOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short multiply double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short multiply double : wrong result : ", xShortValue * yDoubleValue, doubleValue, 0);

		value = eval(yShort + multiplyOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short multiply double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short multiply double : wrong result : ", yShortValue * xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// short / {byte, char, short, int, long, float, double}

	public void testShortDivideByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + divideOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divide byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short divide byte : wrong result : ", xShortValue / yByteValue, intValue);

		value = eval(yShort + divideOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short divide byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short divide byte : wrong result : ", yShortValue / xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortDivideChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + divideOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divide char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short divide char : wrong result : ", xShortValue / yCharValue, intValue);

		value = eval(yShort + divideOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short divide char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short divide char : wrong result : ", yShortValue / xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortDivideShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + divideOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divide short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short divide short : wrong result : ", xShortValue / yShortValue, intValue);

		value = eval(yShort + divideOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short divide short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short divide short : wrong result : ", yShortValue / xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortDivideInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + divideOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divide int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short divide int : wrong result : ", xShortValue / yIntValue, intValue);

		value = eval(yShort + divideOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short divide int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short divide int : wrong result : ", yShortValue / xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortDivideLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + divideOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divide long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short divide long : wrong result : ", xShortValue / yLongValue, longValue);

		value = eval(yShort + divideOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short divide long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short divide long : wrong result : ", yShortValue / xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testShortDivideFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + divideOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divide float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short divide float : wrong result : ", xShortValue / yFloatValue, floatValue, 0);

		value = eval(yShort + divideOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short divide float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short divide float : wrong result : ", yShortValue / xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testShortDivideDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + divideOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short divide double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short divide double : wrong result : ", xShortValue / yDoubleValue, doubleValue, 0);

		value = eval(yShort + divideOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short divide double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short divide double : wrong result : ", yShortValue / xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// short % {byte, char, short, int, long, float, double}

	public void testShortRemainderByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + remainderOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainder byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short remainder byte : wrong result : ", xShortValue % yByteValue, intValue);

		value = eval(yShort + remainderOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainder byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short remainder byte : wrong result : ", yShortValue % xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + remainderOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainder char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short remainder char : wrong result : ", xShortValue % yCharValue, intValue);

		value = eval(yShort + remainderOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainder char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short remainder char : wrong result : ", yShortValue % xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + remainderOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainder short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short remainder short : wrong result : ", xShortValue % yShortValue, intValue);

		value = eval(yShort + remainderOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainder short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short remainder short : wrong result : ", yShortValue % xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + remainderOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainder int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short remainder int : wrong result : ", xShortValue % yIntValue, intValue);

		value = eval(yShort + remainderOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainder int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short remainder int : wrong result : ", yShortValue % xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + remainderOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainder long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short remainder long : wrong result : ", xShortValue % yLongValue, longValue);

		value = eval(yShort + remainderOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainder long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short remainder long : wrong result : ", yShortValue % xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testShortRemainderFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + remainderOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainder float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short remainder float : wrong result : ", xShortValue % yFloatValue, floatValue, 0);

		value = eval(yShort + remainderOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainder float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("short remainder float : wrong result : ", yShortValue % xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testShortRemainderDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + remainderOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short remainder double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short remainder double : wrong result : ", xShortValue % yDoubleValue, doubleValue, 0);

		value = eval(yShort + remainderOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short remainder double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("short remainder double : wrong result : ", yShortValue % xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// short > {byte, char, short, int, long, float, double}

	public void testShortGreaterByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greater byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater byte : wrong result : ", xShortValue > yByteValue, booleanValue);

		value = eval(yShort + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater byte : wrong result : ", yShortValue > xByteValue, booleanValue);

		value = eval(xShort + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater byte : wrong result : ", xShortValue > xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greater char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater char : wrong result : ", xShortValue > yCharValue, booleanValue);

		value = eval(yShort + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater char : wrong result : ", yShortValue > xCharValue, booleanValue);

		value = eval(xShort + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater char : wrong result : ", xShortValue > xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greater short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater short : wrong result : ", xShortValue > yShortValue, booleanValue);

		value = eval(yShort + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater short : wrong result : ", yShortValue > xShortValue, booleanValue);

		value = eval(xShort + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater short : wrong result : ", xShortValue > xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greater int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater int : wrong result : ", xShortValue > yIntValue, booleanValue);

		value = eval(yShort + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater int : wrong result : ", yShortValue > xIntValue, booleanValue);

		value = eval(xShort + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater int : wrong result : ", xShortValue > xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greater long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater long : wrong result : ", xShortValue > yLongValue, booleanValue);

		value = eval(yShort + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater long : wrong result : ", yShortValue > xLongValue, booleanValue);

		value = eval(xShort + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater long : wrong result : ", xShortValue > xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greater float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater float : wrong result : ", xShortValue > yFloatValue, booleanValue);

		value = eval(yShort + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater float : wrong result : ", yShortValue > xFloatValue, booleanValue);

		value = eval(xShort + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater float : wrong result : ", xShortValue > xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greater double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater double : wrong result : ", xShortValue > yDoubleValue, booleanValue);

		value = eval(yShort + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater double : wrong result : ", yShortValue > xDoubleValue, booleanValue);

		value = eval(xShort + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greater double : wrong result : ", xShortValue > xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// short >= {byte, char, short, int, long, float, double}

	public void testShortGreaterEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual byte : wrong result : ", xShortValue >= yByteValue, booleanValue);

		value = eval(yShort + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual byte : wrong result : ", yShortValue >= xByteValue, booleanValue);

		value = eval(xShort + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual byte : wrong result : ", xShortValue >= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual char : wrong result : ", xShortValue >= yCharValue, booleanValue);

		value = eval(yShort + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual char : wrong result : ", yShortValue >= xCharValue, booleanValue);

		value = eval(xShort + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual char : wrong result : ", xShortValue >= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual short : wrong result : ", xShortValue >= yShortValue, booleanValue);

		value = eval(yShort + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual short : wrong result : ", yShortValue >= xShortValue, booleanValue);

		value = eval(xShort + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual short : wrong result : ", xShortValue >= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual int : wrong result : ", xShortValue >= yIntValue, booleanValue);

		value = eval(yShort + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual int : wrong result : ", yShortValue >= xIntValue, booleanValue);

		value = eval(xShort + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual int : wrong result : ", xShortValue >= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual long : wrong result : ", xShortValue >= yLongValue, booleanValue);

		value = eval(yShort + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual long : wrong result : ", yShortValue >= xLongValue, booleanValue);

		value = eval(xShort + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual long : wrong result : ", xShortValue >= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual float : wrong result : ", xShortValue >= yFloatValue, booleanValue);

		value = eval(yShort + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual float : wrong result : ", yShortValue >= xFloatValue, booleanValue);

		value = eval(xShort + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual float : wrong result : ", xShortValue >= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortGreaterEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + greaterEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual double : wrong result : ", xShortValue >= yDoubleValue, booleanValue);

		value = eval(yShort + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual double : wrong result : ", yShortValue >= xDoubleValue, booleanValue);

		value = eval(xShort + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short greaterEqual double : wrong result : ", xShortValue >= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// short < {byte, char, short, int, long, float, double}

	public void testShortLessByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short less byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less byte : wrong result : ", xShortValue < yByteValue, booleanValue);

		value = eval(yShort + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less byte : wrong result : ", yShortValue < xByteValue, booleanValue);

		value = eval(xShort + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less byte : wrong result : ", xShortValue < xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short less char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less char : wrong result : ", xShortValue < yCharValue, booleanValue);

		value = eval(yShort + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less char : wrong result : ", yShortValue < xCharValue, booleanValue);

		value = eval(xShort + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less char : wrong result : ", xShortValue < xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short less short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less short : wrong result : ", xShortValue < yShortValue, booleanValue);

		value = eval(yShort + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less short : wrong result : ", yShortValue < xShortValue, booleanValue);

		value = eval(xShort + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less short : wrong result : ", xShortValue < xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short less int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less int : wrong result : ", xShortValue < yIntValue, booleanValue);

		value = eval(yShort + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less int : wrong result : ", yShortValue < xIntValue, booleanValue);

		value = eval(xShort + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less int : wrong result : ", xShortValue < xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short less long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less long : wrong result : ", xShortValue < yLongValue, booleanValue);

		value = eval(yShort + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less long : wrong result : ", yShortValue < xLongValue, booleanValue);

		value = eval(xShort + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less long : wrong result : ", xShortValue < xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short less float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less float : wrong result : ", xShortValue < yFloatValue, booleanValue);

		value = eval(yShort + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less float : wrong result : ", yShortValue < xFloatValue, booleanValue);

		value = eval(xShort + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less float : wrong result : ", xShortValue < xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short less double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less double : wrong result : ", xShortValue < yDoubleValue, booleanValue);

		value = eval(yShort + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less double : wrong result : ", yShortValue < xDoubleValue, booleanValue);

		value = eval(xShort + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short less double : wrong result : ", xShortValue < xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// short <= {byte, char, short, int, long, float, double}

	public void testShortLessEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual byte : wrong result : ", xShortValue <= yByteValue, booleanValue);

		value = eval(yShort + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual byte : wrong result : ", yShortValue <= xByteValue, booleanValue);

		value = eval(xShort + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual byte : wrong result : ", xShortValue <= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual char : wrong result : ", xShortValue <= yCharValue, booleanValue);

		value = eval(yShort + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual char : wrong result : ", yShortValue <= xCharValue, booleanValue);

		value = eval(xShort + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual char : wrong result : ", xShortValue <= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual short : wrong result : ", xShortValue <= yShortValue, booleanValue);

		value = eval(yShort + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual short : wrong result : ", yShortValue <= xShortValue, booleanValue);

		value = eval(xShort + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual short : wrong result : ", xShortValue <= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual int : wrong result : ", xShortValue <= yIntValue, booleanValue);

		value = eval(yShort + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual int : wrong result : ", yShortValue <= xIntValue, booleanValue);

		value = eval(xShort + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual int : wrong result : ", xShortValue <= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual long : wrong result : ", xShortValue <= yLongValue, booleanValue);

		value = eval(yShort + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual long : wrong result : ", yShortValue <= xLongValue, booleanValue);

		value = eval(xShort + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual long : wrong result : ", xShortValue <= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual float : wrong result : ", xShortValue <= yFloatValue, booleanValue);

		value = eval(yShort + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual float : wrong result : ", yShortValue <= xFloatValue, booleanValue);

		value = eval(xShort + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual float : wrong result : ", xShortValue <= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortLessEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + lessEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual double : wrong result : ", xShortValue <= yDoubleValue, booleanValue);

		value = eval(yShort + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual double : wrong result : ", yShortValue <= xDoubleValue, booleanValue);

		value = eval(xShort + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short lessEqual double : wrong result : ", xShortValue <= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// short == {byte, char, short, int, long, float, double}

	public void testShortEqualEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + equalEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual byte : wrong result : ", xShortValue == yByteValue, booleanValue);

		value = eval(yShort + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual byte : wrong result : ", yShortValue == xByteValue, booleanValue);

		value = eval(xShort + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual byte : wrong result : ", xShortValue == xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortEqualEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + equalEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual char : wrong result : ", xShortValue == yCharValue, booleanValue);

		value = eval(yShort + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual char : wrong result : ", yShortValue == xCharValue, booleanValue);

		value = eval(xShort + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual char : wrong result : ", xShortValue == xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortEqualEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + equalEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual short : wrong result : ", xShortValue == yShortValue, booleanValue);

		value = eval(yShort + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual short : wrong result : ", yShortValue == xShortValue, booleanValue);

		value = eval(xShort + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual short : wrong result : ", true, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortEqualEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + equalEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual int : wrong result : ", xShortValue == yIntValue, booleanValue);

		value = eval(yShort + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual int : wrong result : ", yShortValue == xIntValue, booleanValue);

		value = eval(xShort + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual int : wrong result : ", xShortValue == xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortEqualEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + equalEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual long : wrong result : ", xShortValue == yLongValue, booleanValue);

		value = eval(yShort + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual long : wrong result : ", yShortValue == xLongValue, booleanValue);

		value = eval(xShort + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual long : wrong result : ", xShortValue == xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortEqualEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + equalEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual float : wrong result : ", xShortValue == yFloatValue, booleanValue);

		value = eval(yShort + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual float : wrong result : ", yShortValue == xFloatValue, booleanValue);

		value = eval(xShort + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual float : wrong result : ", xShortValue == xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortEqualEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + equalEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual double : wrong result : ", xShortValue == yDoubleValue, booleanValue);

		value = eval(yShort + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual double : wrong result : ", yShortValue == xDoubleValue, booleanValue);

		value = eval(xShort + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short equalEqual double : wrong result : ", xShortValue == xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// short != {byte, char, short, int, long, float, double}

	public void testShortNotEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + notEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short notEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual byte : wrong result : ", xShortValue != yByteValue, booleanValue);

		value = eval(yShort + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual byte : wrong result : ", yShortValue != xByteValue, booleanValue);

		value = eval(xShort + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual byte : wrong result : ", xShortValue != xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortNotEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + notEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short notEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual char : wrong result : ", xShortValue != yCharValue, booleanValue);

		value = eval(yShort + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual char : wrong result : ", yShortValue != xCharValue, booleanValue);

		value = eval(xShort + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual char : wrong result : ", xShortValue != xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortNotEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + notEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short notEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual short : wrong result : ", xShortValue != yShortValue, booleanValue);

		value = eval(yShort + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual short : wrong result : ", yShortValue != xShortValue, booleanValue);

		value = eval(xShort + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual short : wrong result : ", false, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortNotEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + notEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short notEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual int : wrong result : ", xShortValue != yIntValue, booleanValue);

		value = eval(yShort + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual int : wrong result : ", yShortValue != xIntValue, booleanValue);

		value = eval(xShort + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual int : wrong result : ", xShortValue != xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortNotEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + notEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short notEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual long : wrong result : ", xShortValue != yLongValue, booleanValue);

		value = eval(yShort + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual long : wrong result : ", yShortValue != xLongValue, booleanValue);

		value = eval(xShort + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual long : wrong result : ", xShortValue != xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortNotEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + notEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("short notEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual float : wrong result : ", xShortValue != yFloatValue, booleanValue);

		value = eval(yShort + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual float : wrong result : ", yShortValue != xFloatValue, booleanValue);

		value = eval(xShort + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual float : wrong result : ", xShortValue != xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testShortNotEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + notEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("short notEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual double : wrong result : ", xShortValue != yDoubleValue, booleanValue);

		value = eval(yShort + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual double : wrong result : ", yShortValue != xDoubleValue, booleanValue);

		value = eval(xShort + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("short notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("short notEqual double : wrong result : ", xShortValue != xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// short << {byte, char, short, int, long}

	public void testShortLeftShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + leftShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift byte : wrong result : ", xShortValue << yByteValue, intValue);

		value = eval(yShort + leftShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift byte : wrong result : ", yShortValue << xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortLeftShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + leftShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift char : wrong result : ", xShortValue << yCharValue, intValue);

		value = eval(yShort + leftShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift char : wrong result : ", yShortValue << xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortLeftShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + leftShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift short : wrong result : ", xShortValue << yShortValue, intValue);

		value = eval(yShort + leftShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift short : wrong result : ", yShortValue << xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortLeftShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + leftShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift int : wrong result : ", xShortValue << yIntValue, intValue);

		value = eval(yShort + leftShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift int : wrong result : ", yShortValue << xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortLeftShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + leftShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short leftShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift long : wrong result : ", xShortValue << yLongValue, intValue);

		value = eval(yShort + leftShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short leftShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short leftShift long : wrong result : ", yShortValue << xLongValue, intValue);
		} finally {
		end();
		}
	}

	// short >> {byte, char, short, int, long}

	public void testShortRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + rightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift byte : wrong result : ", xShortValue >> yByteValue, intValue);

		value = eval(yShort + rightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift byte : wrong result : ", yShortValue >> xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + rightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift char : wrong result : ", xShortValue >> yCharValue, intValue);

		value = eval(yShort + rightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift char : wrong result : ", yShortValue >> xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + rightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift short : wrong result : ", xShortValue >> yShortValue, intValue);

		value = eval(yShort + rightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift short : wrong result : ", yShortValue >> xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + rightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift int : wrong result : ", xShortValue >> yIntValue, intValue);

		value = eval(yShort + rightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift int : wrong result : ", yShortValue >> xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + rightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short rightShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift long : wrong result : ", xShortValue >> yLongValue, intValue);

		value = eval(yShort + rightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short rightShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short rightShift long : wrong result : ", yShortValue >> xLongValue, intValue);
		} finally {
		end();
		}
	}

	// short >>> {byte, char, short, int, long}

	public void testShortUnsignedRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + unsignedRightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift byte : wrong result : ", xShortValue >>> yByteValue, intValue);

		value = eval(yShort + unsignedRightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift byte : wrong result : ", yShortValue >>> xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortUnsignedRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + unsignedRightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift char : wrong result : ", xShortValue >>> yCharValue, intValue);

		value = eval(yShort + unsignedRightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift char : wrong result : ", yShortValue >>> xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortUnsignedRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + unsignedRightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift short : wrong result : ", xShortValue >>> yShortValue, intValue);

		value = eval(yShort + unsignedRightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift short : wrong result : ", yShortValue >>> xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortUnsignedRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + unsignedRightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift int : wrong result : ", xShortValue >>> yIntValue, intValue);

		value = eval(yShort + unsignedRightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift int : wrong result : ", yShortValue >>> xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortUnsignedRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + unsignedRightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift long : wrong result : ", xShortValue >>> yLongValue, intValue);

		value = eval(yShort + unsignedRightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short unsignedRightShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short unsignedRightShift long : wrong result : ", yShortValue >>> xLongValue, intValue);
		} finally {
		end();
		}
	}

	// short | {byte, char, short, int, long}

	public void testShortOrByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + orOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short or byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short or byte : wrong result : ", xShortValue | yByteValue, intValue);

		value = eval(yShort + orOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short or byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short or byte : wrong result : ", yShortValue | xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortOrChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + orOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short or char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short or char : wrong result : ", xShortValue | yCharValue, intValue);

		value = eval(yShort + orOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short or char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short or char : wrong result : ", yShortValue | xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortOrShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + orOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short or short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short or short : wrong result : ", xShortValue | yShortValue, intValue);

		value = eval(yShort + orOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short or short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short or short : wrong result : ", yShortValue | xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortOrInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + orOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short or int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short or int : wrong result : ", xShortValue | yIntValue, intValue);

		value = eval(yShort + orOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short or int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short or int : wrong result : ", yShortValue | xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortOrLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + orOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short or long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short or long : wrong result : ", xShortValue | yLongValue, longValue);

		value = eval(yShort + orOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short or long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short or long : wrong result : ", yShortValue | xLongValue, longValue);
		} finally {
		end();
		}
	}

	// short & {byte, char, short, int, long}

	public void testShortAndByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + andOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short and byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short and byte : wrong result : ", xShortValue & yByteValue, intValue);

		value = eval(yShort + andOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short and byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short and byte : wrong result : ", yShortValue & xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortAndChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + andOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short and char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short and char : wrong result : ", xShortValue & yCharValue, intValue);

		value = eval(yShort + andOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short and char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short and char : wrong result : ", yShortValue & xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortAndShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + andOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short and short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short and short : wrong result : ", xShortValue & yShortValue, intValue);

		value = eval(yShort + andOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short and short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short and short : wrong result : ", yShortValue & xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortAndInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + andOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short and int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short and int : wrong result : ", xShortValue & yIntValue, intValue);

		value = eval(yShort + andOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short and int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short and int : wrong result : ", yShortValue & xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortAndLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + andOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short and long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short and long : wrong result : ", xShortValue & yLongValue, longValue);

		value = eval(yShort + andOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short and long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short and long : wrong result : ", yShortValue & xLongValue, longValue);
		} finally {
		end();
		}
	}

	// short ^ {byte, char, short, int, long}

	public void testShortXorByte() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + xorOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xor byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short xor byte : wrong result : ", xShortValue ^ yByteValue, intValue);

		value = eval(yShort + xorOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("short xor byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short xor byte : wrong result : ", yShortValue ^ xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortXorChar() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + xorOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xor char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short xor char : wrong result : ", xShortValue ^ yCharValue, intValue);

		value = eval(yShort + xorOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("short xor char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short xor char : wrong result : ", yShortValue ^ xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortXorShort() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + xorOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xor short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short xor short : wrong result : ", xShortValue ^ yShortValue, intValue);

		value = eval(yShort + xorOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("short xor short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short xor short : wrong result : ", yShortValue ^ xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortXorInt() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + xorOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xor int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short xor int : wrong result : ", xShortValue ^ yIntValue, intValue);

		value = eval(yShort + xorOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("short xor int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("short xor int : wrong result : ", yShortValue ^ xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testShortXorLong() throws Throwable {
		try {
		init();
		IValue value = eval(xShort + xorOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("short xor long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short xor long : wrong result : ", xShortValue ^ yLongValue, longValue);

		value = eval(yShort + xorOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("short xor long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("short xor long : wrong result : ", yShortValue ^ xLongValue, longValue);
		} finally {
		end();
		}
	}

	// + short

	public void testPlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(plusOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("plus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("plus short : wrong result : ", + xShortValue, intValue);

		value = eval(plusOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("plus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("plus short : wrong result : ", + yShortValue, intValue);
		} finally {
		end();
		}
	}

	// - short

	public void testMinusShort() throws Throwable {
		try {
		init();
		IValue value = eval(minusOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("minus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("minus short : wrong result : ", - xShortValue, intValue);

		value = eval(minusOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("minus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("minus short : wrong result : ", - yShortValue, intValue);
		} finally {
		end();
		}
	}

	// ~ short

	public void testTwiddleShort() throws Throwable {
		try {
		init();
		IValue value = eval(twiddleOp + xShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("twiddle short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("twiddle short : wrong result : ", ~ xShortValue, intValue);

		value = eval(twiddleOp + yShort);
		typeName = value.getReferenceTypeName();
		assertEquals("twiddle short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("twiddle short : wrong result : ", ~ yShortValue, intValue);
		} finally {
		end();
		}
	}


}
