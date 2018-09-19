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

public class LongOperatorsTests extends Tests {

	public LongOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// long + {byte, char, short, int, long, float, double}

	public void testLongPlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + plusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plus byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus byte : wrong result : ", xLongValue + yByteValue, longValue);

		value = eval(yLong + plusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long plus byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus byte : wrong result : ", yLongValue + xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + plusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plus char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus char : wrong result : ", xLongValue + yCharValue, longValue);

		value = eval(yLong + plusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long plus char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus char : wrong result : ", yLongValue + xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + plusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plus short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus short : wrong result : ", xLongValue + yShortValue, longValue);

		value = eval(yLong + plusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long plus short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus short : wrong result : ", yLongValue + xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plus int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus int : wrong result : ", xLongValue + yIntValue, longValue);

		value = eval(yLong + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long plus int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus int : wrong result : ", yLongValue + xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + plusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus long : wrong result : ", xLongValue + yLongValue, longValue);

		value = eval(yLong + plusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long plus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long plus long : wrong result : ", yLongValue + xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongPlusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + plusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long plus float : wrong result : ", xLongValue + yFloatValue, floatValue, 0);

		value = eval(yLong + plusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long plus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long plus float : wrong result : ", yLongValue + xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testLongPlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + plusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long plus double : wrong result : ", xLongValue + yDoubleValue, doubleValue, 0);

		value = eval(yLong + plusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long plus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long plus double : wrong result : ", yLongValue + xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testLongPlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("long plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("long plus java.lang.String : wrong result : ", xLongValue + yStringValue, stringValue);

		value = eval(yLong + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("long plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("long plus java.lang.String : wrong result : ", yLongValue + xStringValue, stringValue);
		} finally {
		end();
		}
	}

	// long - {byte, char, short, int, long, float, double}

	public void testLongMinusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + minusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minus byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus byte : wrong result : ", xLongValue - yByteValue, longValue);

		value = eval(yLong + minusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long minus byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus byte : wrong result : ", yLongValue - xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + minusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minus char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus char : wrong result : ", xLongValue - yCharValue, longValue);

		value = eval(yLong + minusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long minus char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus char : wrong result : ", yLongValue - xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + minusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minus short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus short : wrong result : ", xLongValue - yShortValue, longValue);

		value = eval(yLong + minusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long minus short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus short : wrong result : ", yLongValue - xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + minusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minus int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus int : wrong result : ", xLongValue - yIntValue, longValue);

		value = eval(yLong + minusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long minus int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus int : wrong result : ", yLongValue - xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + minusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus long : wrong result : ", xLongValue - yLongValue, longValue);

		value = eval(yLong + minusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long minus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long minus long : wrong result : ", yLongValue - xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMinusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + minusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long minus float : wrong result : ", xLongValue - yFloatValue, floatValue, 0);

		value = eval(yLong + minusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long minus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long minus float : wrong result : ", yLongValue - xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testLongMinusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + minusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long minus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long minus double : wrong result : ", xLongValue - yDoubleValue, doubleValue, 0);

		value = eval(yLong + minusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long minus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long minus double : wrong result : ", yLongValue - xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// long * {byte, char, short, int, long, float, double}

	public void testLongMultiplyByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + multiplyOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiply byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply byte : wrong result : ", xLongValue * yByteValue, longValue);

		value = eval(yLong + multiplyOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiply byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply byte : wrong result : ", yLongValue * xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + multiplyOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiply char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply char : wrong result : ", xLongValue * yCharValue, longValue);

		value = eval(yLong + multiplyOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiply char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply char : wrong result : ", yLongValue * xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + multiplyOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiply short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply short : wrong result : ", xLongValue * yShortValue, longValue);

		value = eval(yLong + multiplyOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiply short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply short : wrong result : ", yLongValue * xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + multiplyOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiply int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply int : wrong result : ", xLongValue * yIntValue, longValue);

		value = eval(yLong + multiplyOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiply int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply int : wrong result : ", yLongValue * xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + multiplyOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiply long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply long : wrong result : ", xLongValue * yLongValue, longValue);

		value = eval(yLong + multiplyOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiply long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long multiply long : wrong result : ", yLongValue * xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongMultiplyFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + multiplyOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiply float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long multiply float : wrong result : ", xLongValue * yFloatValue, floatValue, 0);

		value = eval(yLong + multiplyOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiply float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long multiply float : wrong result : ", yLongValue * xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testLongMultiplyDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + multiplyOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long multiply double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long multiply double : wrong result : ", xLongValue * yDoubleValue, doubleValue, 0);

		value = eval(yLong + multiplyOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long multiply double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long multiply double : wrong result : ", yLongValue * xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// long / {byte, char, short, int, long, float, double}

	public void testLongDivideByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + divideOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divide byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide byte : wrong result : ", xLongValue / yByteValue, longValue);

		value = eval(yLong + divideOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long divide byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide byte : wrong result : ", yLongValue / xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + divideOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divide char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide char : wrong result : ", xLongValue / yCharValue, longValue);

		value = eval(yLong + divideOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long divide char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide char : wrong result : ", yLongValue / xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + divideOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divide short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide short : wrong result : ", xLongValue / yShortValue, longValue);

		value = eval(yLong + divideOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long divide short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide short : wrong result : ", yLongValue / xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + divideOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divide int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide int : wrong result : ", xLongValue / yIntValue, longValue);

		value = eval(yLong + divideOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long divide int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide int : wrong result : ", yLongValue / xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + divideOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divide long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide long : wrong result : ", xLongValue / yLongValue, longValue);

		value = eval(yLong + divideOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long divide long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long divide long : wrong result : ", yLongValue / xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongDivideFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + divideOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divide float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long divide float : wrong result : ", xLongValue / yFloatValue, floatValue, 0);

		value = eval(yLong + divideOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long divide float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long divide float : wrong result : ", yLongValue / xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testLongDivideDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + divideOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long divide double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long divide double : wrong result : ", xLongValue / yDoubleValue, doubleValue, 0);

		value = eval(yLong + divideOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long divide double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long divide double : wrong result : ", yLongValue / xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// long % {byte, char, short, int, long, float, double}

	public void testLongRemainderByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + remainderOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainder byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder byte : wrong result : ", xLongValue % yByteValue, longValue);

		value = eval(yLong + remainderOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainder byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder byte : wrong result : ", yLongValue % xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + remainderOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainder char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder char : wrong result : ", xLongValue % yCharValue, longValue);

		value = eval(yLong + remainderOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainder char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder char : wrong result : ", yLongValue % xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + remainderOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainder short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder short : wrong result : ", xLongValue % yShortValue, longValue);

		value = eval(yLong + remainderOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainder short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder short : wrong result : ", yLongValue % xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + remainderOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainder int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder int : wrong result : ", xLongValue % yIntValue, longValue);

		value = eval(yLong + remainderOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainder int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder int : wrong result : ", yLongValue % xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + remainderOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainder long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder long : wrong result : ", xLongValue % yLongValue, longValue);

		value = eval(yLong + remainderOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainder long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long remainder long : wrong result : ", yLongValue % xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRemainderFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + remainderOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainder float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long remainder float : wrong result : ", xLongValue % yFloatValue, floatValue, 0);

		value = eval(yLong + remainderOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainder float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("long remainder float : wrong result : ", yLongValue % xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testLongRemainderDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + remainderOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long remainder double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long remainder double : wrong result : ", xLongValue % yDoubleValue, doubleValue, 0);

		value = eval(yLong + remainderOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long remainder double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("long remainder double : wrong result : ", yLongValue % xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// long > {byte, char, short, int, long, float, double}

	public void testLongGreaterByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greater byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater byte : wrong result : ", xLongValue > yByteValue, booleanValue);

		value = eval(yLong + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater byte : wrong result : ", yLongValue > xByteValue, booleanValue);

		value = eval(xLong + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater byte : wrong result : ", xLongValue > xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greater char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater char : wrong result : ", xLongValue > yCharValue, booleanValue);

		value = eval(yLong + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater char : wrong result : ", yLongValue > xCharValue, booleanValue);

		value = eval(xLong + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater char : wrong result : ", xLongValue > xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greater short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater short : wrong result : ", xLongValue > yShortValue, booleanValue);

		value = eval(yLong + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater short : wrong result : ", yLongValue > xShortValue, booleanValue);

		value = eval(xLong + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater short : wrong result : ", xLongValue > xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greater int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater int : wrong result : ", xLongValue > yIntValue, booleanValue);

		value = eval(yLong + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater int : wrong result : ", yLongValue > xIntValue, booleanValue);

		value = eval(xLong + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater int : wrong result : ", xLongValue > xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greater long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater long : wrong result : ", xLongValue > yLongValue, booleanValue);

		value = eval(yLong + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater long : wrong result : ", yLongValue > xLongValue, booleanValue);

		value = eval(xLong + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater long : wrong result : ", xLongValue > xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greater float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater float : wrong result : ", xLongValue > yFloatValue, booleanValue);

		value = eval(yLong + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater float : wrong result : ", yLongValue > xFloatValue, booleanValue);

		value = eval(xLong + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater float : wrong result : ", xLongValue > xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greater double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater double : wrong result : ", xLongValue > yDoubleValue, booleanValue);

		value = eval(yLong + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater double : wrong result : ", yLongValue > xDoubleValue, booleanValue);

		value = eval(xLong + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greater double : wrong result : ", xLongValue > xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// long >= {byte, char, short, int, long, float, double}

	public void testLongGreaterEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual byte : wrong result : ", xLongValue >= yByteValue, booleanValue);

		value = eval(yLong + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual byte : wrong result : ", yLongValue >= xByteValue, booleanValue);

		value = eval(xLong + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual byte : wrong result : ", xLongValue >= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual char : wrong result : ", xLongValue >= yCharValue, booleanValue);

		value = eval(yLong + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual char : wrong result : ", yLongValue >= xCharValue, booleanValue);

		value = eval(xLong + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual char : wrong result : ", xLongValue >= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual short : wrong result : ", xLongValue >= yShortValue, booleanValue);

		value = eval(yLong + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual short : wrong result : ", yLongValue >= xShortValue, booleanValue);

		value = eval(xLong + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual short : wrong result : ", xLongValue >= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual int : wrong result : ", xLongValue >= yIntValue, booleanValue);

		value = eval(yLong + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual int : wrong result : ", yLongValue >= xIntValue, booleanValue);

		value = eval(xLong + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual int : wrong result : ", xLongValue >= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual long : wrong result : ", xLongValue >= yLongValue, booleanValue);

		value = eval(yLong + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual long : wrong result : ", yLongValue >= xLongValue, booleanValue);

		value = eval(xLong + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual long : wrong result : ", xLongValue >= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual float : wrong result : ", xLongValue >= yFloatValue, booleanValue);

		value = eval(yLong + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual float : wrong result : ", yLongValue >= xFloatValue, booleanValue);

		value = eval(xLong + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual float : wrong result : ", xLongValue >= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongGreaterEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + greaterEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual double : wrong result : ", xLongValue >= yDoubleValue, booleanValue);

		value = eval(yLong + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual double : wrong result : ", yLongValue >= xDoubleValue, booleanValue);

		value = eval(xLong + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long greaterEqual double : wrong result : ", xLongValue >= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// long < {byte, char, short, int, long, float, double}

	public void testLongLessByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long less byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less byte : wrong result : ", xLongValue < yByteValue, booleanValue);

		value = eval(yLong + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less byte : wrong result : ", yLongValue < xByteValue, booleanValue);

		value = eval(xLong + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less byte : wrong result : ", xLongValue < xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long less char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less char : wrong result : ", xLongValue < yCharValue, booleanValue);

		value = eval(yLong + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less char : wrong result : ", yLongValue < xCharValue, booleanValue);

		value = eval(xLong + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less char : wrong result : ", xLongValue < xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long less short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less short : wrong result : ", xLongValue < yShortValue, booleanValue);

		value = eval(yLong + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less short : wrong result : ", yLongValue < xShortValue, booleanValue);

		value = eval(xLong + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less short : wrong result : ", xLongValue < xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long less int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less int : wrong result : ", xLongValue < yIntValue, booleanValue);

		value = eval(yLong + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less int : wrong result : ", yLongValue < xIntValue, booleanValue);

		value = eval(xLong + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less int : wrong result : ", xLongValue < xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long less long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less long : wrong result : ", xLongValue < yLongValue, booleanValue);

		value = eval(yLong + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less long : wrong result : ", yLongValue < xLongValue, booleanValue);

		value = eval(xLong + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less long : wrong result : ", xLongValue < xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long less float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less float : wrong result : ", xLongValue < yFloatValue, booleanValue);

		value = eval(yLong + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less float : wrong result : ", yLongValue < xFloatValue, booleanValue);

		value = eval(xLong + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less float : wrong result : ", xLongValue < xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long less double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less double : wrong result : ", xLongValue < yDoubleValue, booleanValue);

		value = eval(yLong + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less double : wrong result : ", yLongValue < xDoubleValue, booleanValue);

		value = eval(xLong + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long less double : wrong result : ", xLongValue < xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// long <= {byte, char, short, int, long, float, double}

	public void testLongLessEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual byte : wrong result : ", xLongValue <= yByteValue, booleanValue);

		value = eval(yLong + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual byte : wrong result : ", yLongValue <= xByteValue, booleanValue);

		value = eval(xLong + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual byte : wrong result : ", xLongValue <= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual char : wrong result : ", xLongValue <= yCharValue, booleanValue);

		value = eval(yLong + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual char : wrong result : ", yLongValue <= xCharValue, booleanValue);

		value = eval(xLong + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual char : wrong result : ", xLongValue <= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual short : wrong result : ", xLongValue <= yShortValue, booleanValue);

		value = eval(yLong + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual short : wrong result : ", yLongValue <= xShortValue, booleanValue);

		value = eval(xLong + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual short : wrong result : ", xLongValue <= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual int : wrong result : ", xLongValue <= yIntValue, booleanValue);

		value = eval(yLong + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual int : wrong result : ", yLongValue <= xIntValue, booleanValue);

		value = eval(xLong + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual int : wrong result : ", xLongValue <= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual long : wrong result : ", xLongValue <= yLongValue, booleanValue);

		value = eval(yLong + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual long : wrong result : ", yLongValue <= xLongValue, booleanValue);

		value = eval(xLong + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual long : wrong result : ", xLongValue <= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual float : wrong result : ", xLongValue <= yFloatValue, booleanValue);

		value = eval(yLong + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual float : wrong result : ", yLongValue <= xFloatValue, booleanValue);

		value = eval(xLong + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual float : wrong result : ", xLongValue <= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongLessEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + lessEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual double : wrong result : ", xLongValue <= yDoubleValue, booleanValue);

		value = eval(yLong + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual double : wrong result : ", yLongValue <= xDoubleValue, booleanValue);

		value = eval(xLong + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long lessEqual double : wrong result : ", xLongValue <= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// long == {byte, char, short, int, long, float, double}

	public void testLongEqualEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + equalEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual byte : wrong result : ", xLongValue == yByteValue, booleanValue);

		value = eval(yLong + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual byte : wrong result : ", yLongValue == xByteValue, booleanValue);

		value = eval(xLong + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual byte : wrong result : ", xLongValue == xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongEqualEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + equalEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual char : wrong result : ", xLongValue == yCharValue, booleanValue);

		value = eval(yLong + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual char : wrong result : ", yLongValue == xCharValue, booleanValue);

		value = eval(xLong + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual char : wrong result : ", xLongValue == xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongEqualEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + equalEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual short : wrong result : ", xLongValue == yShortValue, booleanValue);

		value = eval(yLong + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual short : wrong result : ", yLongValue == xShortValue, booleanValue);

		value = eval(xLong + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual short : wrong result : ", xLongValue == xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongEqualEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + equalEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual int : wrong result : ", xLongValue == yIntValue, booleanValue);

		value = eval(yLong + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual int : wrong result : ", yLongValue == xIntValue, booleanValue);

		value = eval(xLong + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual int : wrong result : ", xLongValue == xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongEqualEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + equalEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual long : wrong result : ", xLongValue == yLongValue, booleanValue);

		value = eval(yLong + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual long : wrong result : ", yLongValue == xLongValue, booleanValue);

		value = eval(xLong + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual long : wrong result : ", true, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongEqualEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + equalEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual float : wrong result : ", xLongValue == yFloatValue, booleanValue);

		value = eval(yLong + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual float : wrong result : ", yLongValue == xFloatValue, booleanValue);

		value = eval(xLong + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual float : wrong result : ", xLongValue == xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongEqualEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + equalEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual double : wrong result : ", xLongValue == yDoubleValue, booleanValue);

		value = eval(yLong + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual double : wrong result : ", yLongValue == xDoubleValue, booleanValue);

		value = eval(xLong + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long equalEqual double : wrong result : ", xLongValue == xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// long != {byte, char, short, int, long, float, double}

	public void testLongNotEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + notEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long notEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual byte : wrong result : ", xLongValue != yByteValue, booleanValue);

		value = eval(yLong + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual byte : wrong result : ", yLongValue != xByteValue, booleanValue);

		value = eval(xLong + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual byte : wrong result : ", xLongValue != xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongNotEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + notEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long notEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual char : wrong result : ", xLongValue != yCharValue, booleanValue);

		value = eval(yLong + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual char : wrong result : ", yLongValue != xCharValue, booleanValue);

		value = eval(xLong + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual char : wrong result : ", xLongValue != xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongNotEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + notEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long notEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual short : wrong result : ", xLongValue != yShortValue, booleanValue);

		value = eval(yLong + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual short : wrong result : ", yLongValue != xShortValue, booleanValue);

		value = eval(xLong + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual short : wrong result : ", xLongValue != xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongNotEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + notEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long notEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual int : wrong result : ", xLongValue != yIntValue, booleanValue);

		value = eval(yLong + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual int : wrong result : ", yLongValue != xIntValue, booleanValue);

		value = eval(xLong + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual int : wrong result : ", xLongValue != xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongNotEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + notEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long notEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual long : wrong result : ", xLongValue != yLongValue, booleanValue);

		value = eval(yLong + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual long : wrong result : ", yLongValue != xLongValue, booleanValue);

		value = eval(xLong + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual long : wrong result : ", false, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongNotEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + notEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("long notEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual float : wrong result : ", xLongValue != yFloatValue, booleanValue);

		value = eval(yLong + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual float : wrong result : ", yLongValue != xFloatValue, booleanValue);

		value = eval(xLong + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual float : wrong result : ", xLongValue != xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testLongNotEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + notEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("long notEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual double : wrong result : ", xLongValue != yDoubleValue, booleanValue);

		value = eval(yLong + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual double : wrong result : ", yLongValue != xDoubleValue, booleanValue);

		value = eval(xLong + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("long notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("long notEqual double : wrong result : ", xLongValue != xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// long << {byte, char, short, int, long}

	public void testLongLeftShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + leftShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShift byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift byte : wrong result : ", xLongValue << yByteValue, longValue);

		value = eval(yLong + leftShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShift byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift byte : wrong result : ", yLongValue << xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongLeftShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + leftShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShift char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift char : wrong result : ", xLongValue << yCharValue, longValue);

		value = eval(yLong + leftShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShift char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift char : wrong result : ", yLongValue << xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongLeftShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + leftShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShift short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift short : wrong result : ", xLongValue << yShortValue, longValue);

		value = eval(yLong + leftShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShift short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift short : wrong result : ", yLongValue << xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongLeftShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + leftShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShift int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift int : wrong result : ", xLongValue << yIntValue, longValue);

		value = eval(yLong + leftShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShift int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift int : wrong result : ", yLongValue << xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongLeftShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + leftShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long leftShift long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift long : wrong result : ", xLongValue << yLongValue, longValue);

		value = eval(yLong + leftShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long leftShift long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long leftShift long : wrong result : ", yLongValue << xLongValue, longValue);
		} finally {
		end();
		}
	}

	// long >> {byte, char, short, int, long}

	public void testLongRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + rightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShift byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift byte : wrong result : ", xLongValue >> yByteValue, longValue);

		value = eval(yLong + rightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShift byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift byte : wrong result : ", yLongValue >> xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + rightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShift char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift char : wrong result : ", xLongValue >> yCharValue, longValue);

		value = eval(yLong + rightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShift char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift char : wrong result : ", yLongValue >> xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + rightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShift short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift short : wrong result : ", xLongValue >> yShortValue, longValue);

		value = eval(yLong + rightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShift short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift short : wrong result : ", yLongValue >> xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + rightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShift int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift int : wrong result : ", xLongValue >> yIntValue, longValue);

		value = eval(yLong + rightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShift int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift int : wrong result : ", yLongValue >> xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + rightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long rightShift long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift long : wrong result : ", xLongValue >> yLongValue, longValue);

		value = eval(yLong + rightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long rightShift long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long rightShift long : wrong result : ", yLongValue >> xLongValue, longValue);
		} finally {
		end();
		}
	}

	// long >>> {byte, char, short, int, long}

	public void testLongUnsignedRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + unsignedRightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift byte : wrong result : ", xLongValue >>> yByteValue, longValue);

		value = eval(yLong + unsignedRightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift byte : wrong result : ", yLongValue >>> xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongUnsignedRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + unsignedRightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift char : wrong result : ", xLongValue >>> yCharValue, longValue);

		value = eval(yLong + unsignedRightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift char : wrong result : ", yLongValue >>> xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongUnsignedRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + unsignedRightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift short : wrong result : ", xLongValue >>> yShortValue, longValue);

		value = eval(yLong + unsignedRightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift short : wrong result : ", yLongValue >>> xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongUnsignedRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + unsignedRightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift int : wrong result : ", xLongValue >>> yIntValue, longValue);

		value = eval(yLong + unsignedRightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift int : wrong result : ", yLongValue >>> xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongUnsignedRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + unsignedRightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift long : wrong result : ", xLongValue >>> yLongValue, longValue);

		value = eval(yLong + unsignedRightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long unsignedRightShift long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long unsignedRightShift long : wrong result : ", yLongValue >>> xLongValue, longValue);
		} finally {
		end();
		}
	}

	// long | {byte, char, short, int, long}

	public void testLongOrByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + orOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long or byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or byte : wrong result : ", xLongValue | yByteValue, longValue);

		value = eval(yLong + orOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long or byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or byte : wrong result : ", yLongValue | xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongOrChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + orOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long or char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or char : wrong result : ", xLongValue | yCharValue, longValue);

		value = eval(yLong + orOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long or char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or char : wrong result : ", yLongValue | xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongOrShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + orOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long or short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or short : wrong result : ", xLongValue | yShortValue, longValue);

		value = eval(yLong + orOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long or short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or short : wrong result : ", yLongValue | xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongOrInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + orOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long or int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or int : wrong result : ", xLongValue | yIntValue, longValue);

		value = eval(yLong + orOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long or int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or int : wrong result : ", yLongValue | xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongOrLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + orOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long or long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or long : wrong result : ", xLongValue | yLongValue, longValue);

		value = eval(yLong + orOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long or long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long or long : wrong result : ", yLongValue | xLongValue, longValue);
		} finally {
		end();
		}
	}

	// long & {byte, char, short, int, long}

	public void testLongAndByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + andOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long and byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and byte : wrong result : ", xLongValue & yByteValue, longValue);

		value = eval(yLong + andOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long and byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and byte : wrong result : ", yLongValue & xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongAndChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + andOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long and char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and char : wrong result : ", xLongValue & yCharValue, longValue);

		value = eval(yLong + andOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long and char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and char : wrong result : ", yLongValue & xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongAndShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + andOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long and short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and short : wrong result : ", xLongValue & yShortValue, longValue);

		value = eval(yLong + andOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long and short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and short : wrong result : ", yLongValue & xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongAndInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + andOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long and int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and int : wrong result : ", xLongValue & yIntValue, longValue);

		value = eval(yLong + andOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long and int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and int : wrong result : ", yLongValue & xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongAndLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + andOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long and long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and long : wrong result : ", xLongValue & yLongValue, longValue);

		value = eval(yLong + andOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long and long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long and long : wrong result : ", yLongValue & xLongValue, longValue);
		} finally {
		end();
		}
	}

	// long ^ {byte, char, short, int, long}

	public void testLongXorByte() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + xorOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xor byte : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor byte : wrong result : ", xLongValue ^ yByteValue, longValue);

		value = eval(yLong + xorOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("long xor byte : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor byte : wrong result : ", yLongValue ^ xByteValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongXorChar() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + xorOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xor char : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor char : wrong result : ", xLongValue ^ yCharValue, longValue);

		value = eval(yLong + xorOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("long xor char : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor char : wrong result : ", yLongValue ^ xCharValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongXorShort() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + xorOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xor short : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor short : wrong result : ", xLongValue ^ yShortValue, longValue);

		value = eval(yLong + xorOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("long xor short : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor short : wrong result : ", yLongValue ^ xShortValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongXorInt() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + xorOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xor int : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor int : wrong result : ", xLongValue ^ yIntValue, longValue);

		value = eval(yLong + xorOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("long xor int : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor int : wrong result : ", yLongValue ^ xIntValue, longValue);
		} finally {
		end();
		}
	}

	public void testLongXorLong() throws Throwable {
		try {
		init();
		IValue value = eval(xLong + xorOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("long xor long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor long : wrong result : ", xLongValue ^ yLongValue, longValue);

		value = eval(yLong + xorOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("long xor long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("long xor long : wrong result : ", yLongValue ^ xLongValue, longValue);
		} finally {
		end();
		}
	}

	// + long

	public void testPlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(plusOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("plus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("plus long : wrong result : ", + xLongValue, longValue);

		value = eval(plusOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("plus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("plus long : wrong result : ", + yLongValue, longValue);
		} finally {
		end();
		}
	}

	// - long

	public void testMinusLong() throws Throwable {
		try {
		init();
		IValue value = eval(minusOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("minus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("minus long : wrong result : ", - xLongValue, longValue);

		value = eval(minusOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("minus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("minus long : wrong result : ", - yLongValue, longValue);
		} finally {
		end();
		}
	}

	// ~ long

	public void testTwiddleLong() throws Throwable {
		try {
		init();
		IValue value = eval(twiddleOp + xLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("twiddle long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("twiddle long : wrong result : ", ~ xLongValue, longValue);

		value = eval(twiddleOp + yLong);
		typeName = value.getReferenceTypeName();
		assertEquals("twiddle long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("twiddle long : wrong result : ", ~ yLongValue, longValue);
		} finally {
		end();
		}
	}


}
