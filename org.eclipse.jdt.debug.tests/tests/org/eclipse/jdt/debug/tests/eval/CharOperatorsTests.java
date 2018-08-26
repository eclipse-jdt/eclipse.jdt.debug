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

public class CharOperatorsTests extends Tests {

	public CharOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// char + {byte, char, short, int, long, float, double}

	public void testCharPlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + plusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char plus byte : wrong result : ", xCharValue + yByteValue, intValue);

		value = eval(yChar + plusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char plus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char plus byte : wrong result : ", yCharValue + xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharPlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + plusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char plus char : wrong result : ", xCharValue + yCharValue, intValue);

		value = eval(yChar + plusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char plus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char plus char : wrong result : ", yCharValue + xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharPlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + plusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char plus short : wrong result : ", xCharValue + yShortValue, intValue);

		value = eval(yChar + plusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char plus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char plus short : wrong result : ", yCharValue + xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharPlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char plus int : wrong result : ", xCharValue + yIntValue, intValue);

		value = eval(yChar + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char plus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char plus int : wrong result : ", yCharValue + xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharPlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + plusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char plus long : wrong result : ", xCharValue + yLongValue, longValue);

		value = eval(yChar + plusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char plus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char plus long : wrong result : ", yCharValue + xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testCharPlusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + plusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char plus float : wrong result : ", xCharValue + yFloatValue, floatValue, 0);

		value = eval(yChar + plusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char plus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char plus float : wrong result : ", yCharValue + xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testCharPlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + plusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char plus double : wrong result : ", xCharValue + yDoubleValue, doubleValue, 0);

		value = eval(yChar + plusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char plus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char plus double : wrong result : ", yCharValue + xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testCharPlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("char plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("char plus java.lang.String : wrong result : ", xCharValue + yStringValue, stringValue);

		value = eval(yChar + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("char plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("char plus java.lang.String : wrong result : ", yCharValue + xStringValue, stringValue);
		} finally {
		end();
		}
	}

	// char - {byte, char, short, int, long, float, double}

	public void testCharMinusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + minusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minus byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char minus byte : wrong result : ", xCharValue - yByteValue, intValue);

		value = eval(yChar + minusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char minus byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char minus byte : wrong result : ", yCharValue - xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharMinusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + minusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char minus char : wrong result : ", xCharValue - yCharValue, intValue);

		value = eval(yChar + minusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char minus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char minus char : wrong result : ", yCharValue - xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharMinusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + minusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minus short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char minus short : wrong result : ", xCharValue - yShortValue, intValue);

		value = eval(yChar + minusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char minus short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char minus short : wrong result : ", yCharValue - xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharMinusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + minusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minus int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char minus int : wrong result : ", xCharValue - yIntValue, intValue);

		value = eval(yChar + minusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char minus int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char minus int : wrong result : ", yCharValue - xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharMinusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + minusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minus long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char minus long : wrong result : ", xCharValue - yLongValue, longValue);

		value = eval(yChar + minusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char minus long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char minus long : wrong result : ", yCharValue - xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testCharMinusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + minusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char minus float : wrong result : ", xCharValue - yFloatValue, floatValue, 0);

		value = eval(yChar + minusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char minus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char minus float : wrong result : ", yCharValue - xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testCharMinusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + minusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char minus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char minus double : wrong result : ", xCharValue - yDoubleValue, doubleValue, 0);

		value = eval(yChar + minusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char minus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char minus double : wrong result : ", yCharValue - xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// char * {byte, char, short, int, long, float, double}

	public void testCharMultiplyByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + multiplyOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiply byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char multiply byte : wrong result : ", xCharValue * yByteValue, intValue);

		value = eval(yChar + multiplyOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiply byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char multiply byte : wrong result : ", yCharValue * xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + multiplyOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiply char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char multiply char : wrong result : ", xCharValue * yCharValue, intValue);

		value = eval(yChar + multiplyOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiply char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char multiply char : wrong result : ", yCharValue * xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + multiplyOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiply short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char multiply short : wrong result : ", xCharValue * yShortValue, intValue);

		value = eval(yChar + multiplyOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiply short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char multiply short : wrong result : ", yCharValue * xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + multiplyOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiply int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char multiply int : wrong result : ", xCharValue * yIntValue, intValue);

		value = eval(yChar + multiplyOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiply int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char multiply int : wrong result : ", yCharValue * xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + multiplyOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiply long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char multiply long : wrong result : ", xCharValue * yLongValue, longValue);

		value = eval(yChar + multiplyOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiply long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char multiply long : wrong result : ", yCharValue * xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testCharMultiplyFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + multiplyOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiply float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char multiply float : wrong result : ", xCharValue * yFloatValue, floatValue, 0);

		value = eval(yChar + multiplyOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiply float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char multiply float : wrong result : ", yCharValue * xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testCharMultiplyDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + multiplyOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char multiply double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char multiply double : wrong result : ", xCharValue * yDoubleValue, doubleValue, 0);

		value = eval(yChar + multiplyOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char multiply double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char multiply double : wrong result : ", yCharValue * xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// char / {byte, char, short, int, long, float, double}

	public void testCharDivideByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + divideOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divide byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char divide byte : wrong result : ", xCharValue / yByteValue, intValue);

		value = eval(yChar + divideOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char divide byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char divide byte : wrong result : ", yCharValue / xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharDivideChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + divideOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divide char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char divide char : wrong result : ", xCharValue / yCharValue, intValue);

		value = eval(yChar + divideOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char divide char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char divide char : wrong result : ", yCharValue / xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharDivideShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + divideOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divide short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char divide short : wrong result : ", xCharValue / yShortValue, intValue);

		value = eval(yChar + divideOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char divide short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char divide short : wrong result : ", yCharValue / xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharDivideInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + divideOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divide int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char divide int : wrong result : ", xCharValue / yIntValue, intValue);

		value = eval(yChar + divideOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char divide int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char divide int : wrong result : ", yCharValue / xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharDivideLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + divideOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divide long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char divide long : wrong result : ", xCharValue / yLongValue, longValue);

		value = eval(yChar + divideOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char divide long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char divide long : wrong result : ", yCharValue / xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testCharDivideFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + divideOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divide float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char divide float : wrong result : ", xCharValue / yFloatValue, floatValue, 0);

		value = eval(yChar + divideOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char divide float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char divide float : wrong result : ", yCharValue / xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testCharDivideDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + divideOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char divide double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char divide double : wrong result : ", xCharValue / yDoubleValue, doubleValue, 0);

		value = eval(yChar + divideOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char divide double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char divide double : wrong result : ", yCharValue / xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// char % {byte, char, short, int, long, float, double}

	public void testCharRemainderByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + remainderOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainder byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char remainder byte : wrong result : ", xCharValue % yByteValue, intValue);

		value = eval(yChar + remainderOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainder byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char remainder byte : wrong result : ", yCharValue % xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + remainderOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainder char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char remainder char : wrong result : ", xCharValue % yCharValue, intValue);

		value = eval(yChar + remainderOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainder char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char remainder char : wrong result : ", yCharValue % xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + remainderOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainder short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char remainder short : wrong result : ", xCharValue % yShortValue, intValue);

		value = eval(yChar + remainderOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainder short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char remainder short : wrong result : ", yCharValue % xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + remainderOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainder int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char remainder int : wrong result : ", xCharValue % yIntValue, intValue);

		value = eval(yChar + remainderOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainder int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char remainder int : wrong result : ", yCharValue % xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + remainderOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainder long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char remainder long : wrong result : ", xCharValue % yLongValue, longValue);

		value = eval(yChar + remainderOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainder long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char remainder long : wrong result : ", yCharValue % xLongValue, longValue);
		} finally {
		end();
		}
	}

	public void testCharRemainderFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + remainderOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainder float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char remainder float : wrong result : ", xCharValue % yFloatValue, floatValue, 0);

		value = eval(yChar + remainderOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainder float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("char remainder float : wrong result : ", yCharValue % xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testCharRemainderDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + remainderOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char remainder double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char remainder double : wrong result : ", xCharValue % yDoubleValue, doubleValue, 0);

		value = eval(yChar + remainderOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char remainder double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("char remainder double : wrong result : ", yCharValue % xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// char > {byte, char, short, int, long, float, double}

	public void testCharGreaterByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greater byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater byte : wrong result : ", xCharValue > yByteValue, booleanValue);

		value = eval(yChar + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater byte : wrong result : ", yCharValue > xByteValue, booleanValue);

		value = eval(xChar + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater byte : wrong result : ", xCharValue > xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greater char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater char : wrong result : ", xCharValue > yCharValue, booleanValue);

		value = eval(yChar + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater char : wrong result : ", yCharValue > xCharValue, booleanValue);

		value = eval(xChar + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater char : wrong result : ", xCharValue > xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greater short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater short : wrong result : ", xCharValue > yShortValue, booleanValue);

		value = eval(yChar + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater short : wrong result : ", yCharValue > xShortValue, booleanValue);

		value = eval(xChar + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater short : wrong result : ", xCharValue > xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greater int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater int : wrong result : ", xCharValue > yIntValue, booleanValue);

		value = eval(yChar + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater int : wrong result : ", yCharValue > xIntValue, booleanValue);

		value = eval(xChar + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater int : wrong result : ", xCharValue > xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greater long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater long : wrong result : ", xCharValue > yLongValue, booleanValue);

		value = eval(yChar + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater long : wrong result : ", yCharValue > xLongValue, booleanValue);

		value = eval(xChar + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater long : wrong result : ", xCharValue > xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greater float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater float : wrong result : ", xCharValue > yFloatValue, booleanValue);

		value = eval(yChar + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater float : wrong result : ", yCharValue > xFloatValue, booleanValue);

		value = eval(xChar + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater float : wrong result : ", xCharValue > xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greater double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater double : wrong result : ", xCharValue > yDoubleValue, booleanValue);

		value = eval(yChar + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater double : wrong result : ", yCharValue > xDoubleValue, booleanValue);

		value = eval(xChar + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greater double : wrong result : ", xCharValue > xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// char >= {byte, char, short, int, long, float, double}

	public void testCharGreaterEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual byte : wrong result : ", xCharValue >= yByteValue, booleanValue);

		value = eval(yChar + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual byte : wrong result : ", yCharValue >= xByteValue, booleanValue);

		value = eval(xChar + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual byte : wrong result : ", xCharValue >= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual char : wrong result : ", xCharValue >= yCharValue, booleanValue);

		value = eval(yChar + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual char : wrong result : ", yCharValue >= xCharValue, booleanValue);

		value = eval(xChar + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual char : wrong result : ", xCharValue >= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual short : wrong result : ", xCharValue >= yShortValue, booleanValue);

		value = eval(yChar + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual short : wrong result : ", yCharValue >= xShortValue, booleanValue);

		value = eval(xChar + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual short : wrong result : ", xCharValue >= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual int : wrong result : ", xCharValue >= yIntValue, booleanValue);

		value = eval(yChar + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual int : wrong result : ", yCharValue >= xIntValue, booleanValue);

		value = eval(xChar + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual int : wrong result : ", xCharValue >= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual long : wrong result : ", xCharValue >= yLongValue, booleanValue);

		value = eval(yChar + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual long : wrong result : ", yCharValue >= xLongValue, booleanValue);

		value = eval(xChar + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual long : wrong result : ", xCharValue >= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual float : wrong result : ", xCharValue >= yFloatValue, booleanValue);

		value = eval(yChar + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual float : wrong result : ", yCharValue >= xFloatValue, booleanValue);

		value = eval(xChar + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual float : wrong result : ", xCharValue >= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharGreaterEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + greaterEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual double : wrong result : ", xCharValue >= yDoubleValue, booleanValue);

		value = eval(yChar + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual double : wrong result : ", yCharValue >= xDoubleValue, booleanValue);

		value = eval(xChar + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char greaterEqual double : wrong result : ", xCharValue >= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// char < {byte, char, short, int, long, float, double}

	public void testCharLessByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char less byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less byte : wrong result : ", xCharValue < yByteValue, booleanValue);

		value = eval(yChar + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less byte : wrong result : ", yCharValue < xByteValue, booleanValue);

		value = eval(xChar + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less byte : wrong result : ", xCharValue < xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char less char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less char : wrong result : ", xCharValue < yCharValue, booleanValue);

		value = eval(yChar + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less char : wrong result : ", yCharValue < xCharValue, booleanValue);

		value = eval(xChar + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less char : wrong result : ", xCharValue < xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char less short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less short : wrong result : ", xCharValue < yShortValue, booleanValue);

		value = eval(yChar + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less short : wrong result : ", yCharValue < xShortValue, booleanValue);

		value = eval(xChar + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less short : wrong result : ", xCharValue < xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char less int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less int : wrong result : ", xCharValue < yIntValue, booleanValue);

		value = eval(yChar + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less int : wrong result : ", yCharValue < xIntValue, booleanValue);

		value = eval(xChar + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less int : wrong result : ", xCharValue < xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char less long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less long : wrong result : ", xCharValue < yLongValue, booleanValue);

		value = eval(yChar + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less long : wrong result : ", yCharValue < xLongValue, booleanValue);

		value = eval(xChar + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less long : wrong result : ", xCharValue < xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char less float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less float : wrong result : ", xCharValue < yFloatValue, booleanValue);

		value = eval(yChar + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less float : wrong result : ", yCharValue < xFloatValue, booleanValue);

		value = eval(xChar + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less float : wrong result : ", xCharValue < xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char less double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less double : wrong result : ", xCharValue < yDoubleValue, booleanValue);

		value = eval(yChar + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less double : wrong result : ", yCharValue < xDoubleValue, booleanValue);

		value = eval(xChar + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char less double : wrong result : ", xCharValue < xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// char <= {byte, char, short, int, long, float, double}

	public void testCharLessEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual byte : wrong result : ", xCharValue <= yByteValue, booleanValue);

		value = eval(yChar + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual byte : wrong result : ", yCharValue <= xByteValue, booleanValue);

		value = eval(xChar + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual byte : wrong result : ", xCharValue <= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual char : wrong result : ", xCharValue <= yCharValue, booleanValue);

		value = eval(yChar + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual char : wrong result : ", yCharValue <= xCharValue, booleanValue);

		value = eval(xChar + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual char : wrong result : ", xCharValue <= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual short : wrong result : ", xCharValue <= yShortValue, booleanValue);

		value = eval(yChar + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual short : wrong result : ", yCharValue <= xShortValue, booleanValue);

		value = eval(xChar + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual short : wrong result : ", xCharValue <= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual int : wrong result : ", xCharValue <= yIntValue, booleanValue);

		value = eval(yChar + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual int : wrong result : ", yCharValue <= xIntValue, booleanValue);

		value = eval(xChar + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual int : wrong result : ", xCharValue <= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual long : wrong result : ", xCharValue <= yLongValue, booleanValue);

		value = eval(yChar + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual long : wrong result : ", yCharValue <= xLongValue, booleanValue);

		value = eval(xChar + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual long : wrong result : ", xCharValue <= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual float : wrong result : ", xCharValue <= yFloatValue, booleanValue);

		value = eval(yChar + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual float : wrong result : ", yCharValue <= xFloatValue, booleanValue);

		value = eval(xChar + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual float : wrong result : ", xCharValue <= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharLessEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + lessEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual double : wrong result : ", xCharValue <= yDoubleValue, booleanValue);

		value = eval(yChar + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual double : wrong result : ", yCharValue <= xDoubleValue, booleanValue);

		value = eval(xChar + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char lessEqual double : wrong result : ", xCharValue <= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// char == {byte, char, short, int, long, float, double}

	public void testCharEqualEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + equalEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual byte : wrong result : ", xCharValue == yByteValue, booleanValue);

		value = eval(yChar + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual byte : wrong result : ", yCharValue == xByteValue, booleanValue);

		value = eval(xChar + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual byte : wrong result : ", xCharValue == xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharEqualEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + equalEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual char : wrong result : ", xCharValue == yCharValue, booleanValue);

		value = eval(yChar + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual char : wrong result : ", yCharValue == xCharValue, booleanValue);

		value = eval(xChar + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual char : wrong result : ", true, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharEqualEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + equalEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual short : wrong result : ", xCharValue == yShortValue, booleanValue);

		value = eval(yChar + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual short : wrong result : ", yCharValue == xShortValue, booleanValue);

		value = eval(xChar + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual short : wrong result : ", xCharValue == xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharEqualEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + equalEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual int : wrong result : ", xCharValue == yIntValue, booleanValue);

		value = eval(yChar + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual int : wrong result : ", yCharValue == xIntValue, booleanValue);

		value = eval(xChar + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual int : wrong result : ", xCharValue == xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharEqualEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + equalEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual long : wrong result : ", xCharValue == yLongValue, booleanValue);

		value = eval(yChar + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual long : wrong result : ", yCharValue == xLongValue, booleanValue);

		value = eval(xChar + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual long : wrong result : ", xCharValue == xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharEqualEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + equalEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual float : wrong result : ", xCharValue == yFloatValue, booleanValue);

		value = eval(yChar + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual float : wrong result : ", yCharValue == xFloatValue, booleanValue);

		value = eval(xChar + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual float : wrong result : ", xCharValue == xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharEqualEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + equalEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual double : wrong result : ", xCharValue == yDoubleValue, booleanValue);

		value = eval(yChar + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual double : wrong result : ", yCharValue == xDoubleValue, booleanValue);

		value = eval(xChar + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char equalEqual double : wrong result : ", xCharValue == xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// char != {byte, char, short, int, long, float, double}

	public void testCharNotEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + notEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char notEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual byte : wrong result : ", xCharValue != yByteValue, booleanValue);

		value = eval(yChar + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual byte : wrong result : ", yCharValue != xByteValue, booleanValue);

		value = eval(xChar + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual byte : wrong result : ", xCharValue != xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharNotEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + notEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char notEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual char : wrong result : ", xCharValue != yCharValue, booleanValue);

		value = eval(yChar + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual char : wrong result : ", yCharValue != xCharValue, booleanValue);

		value = eval(xChar + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual char : wrong result : ", false, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharNotEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + notEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char notEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual short : wrong result : ", xCharValue != yShortValue, booleanValue);

		value = eval(yChar + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual short : wrong result : ", yCharValue != xShortValue, booleanValue);

		value = eval(xChar + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual short : wrong result : ", xCharValue != xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharNotEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + notEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char notEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual int : wrong result : ", xCharValue != yIntValue, booleanValue);

		value = eval(yChar + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual int : wrong result : ", yCharValue != xIntValue, booleanValue);

		value = eval(xChar + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual int : wrong result : ", xCharValue != xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharNotEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + notEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char notEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual long : wrong result : ", xCharValue != yLongValue, booleanValue);

		value = eval(yChar + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual long : wrong result : ", yCharValue != xLongValue, booleanValue);

		value = eval(xChar + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual long : wrong result : ", xCharValue != xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharNotEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + notEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("char notEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual float : wrong result : ", xCharValue != yFloatValue, booleanValue);

		value = eval(yChar + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual float : wrong result : ", yCharValue != xFloatValue, booleanValue);

		value = eval(xChar + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual float : wrong result : ", xCharValue != xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testCharNotEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + notEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("char notEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual double : wrong result : ", xCharValue != yDoubleValue, booleanValue);

		value = eval(yChar + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual double : wrong result : ", yCharValue != xDoubleValue, booleanValue);

		value = eval(xChar + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("char notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("char notEqual double : wrong result : ", xCharValue != xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// char << {byte, char, short, int, long}

	public void testCharLeftShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + leftShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift byte : wrong result : ", xCharValue << yByteValue, intValue);

		value = eval(yChar + leftShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift byte : wrong result : ", yCharValue << xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharLeftShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + leftShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift char : wrong result : ", xCharValue << yCharValue, intValue);

		value = eval(yChar + leftShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift char : wrong result : ", yCharValue << xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharLeftShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + leftShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift short : wrong result : ", xCharValue << yShortValue, intValue);

		value = eval(yChar + leftShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift short : wrong result : ", yCharValue << xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharLeftShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + leftShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift int : wrong result : ", xCharValue << yIntValue, intValue);

		value = eval(yChar + leftShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift int : wrong result : ", yCharValue << xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharLeftShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + leftShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char leftShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift long : wrong result : ", xCharValue << yLongValue, intValue);

		value = eval(yChar + leftShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char leftShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char leftShift long : wrong result : ", yCharValue << xLongValue, intValue);
		} finally {
		end();
		}
	}

	// char >> {byte, char, short, int, long}

	public void testCharRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + rightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift byte : wrong result : ", xCharValue >> yByteValue, intValue);

		value = eval(yChar + rightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift byte : wrong result : ", yCharValue >> xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + rightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift char : wrong result : ", xCharValue >> yCharValue, intValue);

		value = eval(yChar + rightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift char : wrong result : ", yCharValue >> xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + rightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift short : wrong result : ", xCharValue >> yShortValue, intValue);

		value = eval(yChar + rightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift short : wrong result : ", yCharValue >> xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + rightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift int : wrong result : ", xCharValue >> yIntValue, intValue);

		value = eval(yChar + rightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift int : wrong result : ", yCharValue >> xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + rightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char rightShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift long : wrong result : ", xCharValue >> yLongValue, intValue);

		value = eval(yChar + rightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char rightShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char rightShift long : wrong result : ", yCharValue >> xLongValue, intValue);
		} finally {
		end();
		}
	}

	// char >>> {byte, char, short, int, long}

	public void testCharUnsignedRightShiftByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + unsignedRightShiftOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift byte : wrong result : ", xCharValue >>> yByteValue, intValue);

		value = eval(yChar + unsignedRightShiftOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift byte : wrong result : ", yCharValue >>> xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharUnsignedRightShiftChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + unsignedRightShiftOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift char : wrong result : ", xCharValue >>> yCharValue, intValue);

		value = eval(yChar + unsignedRightShiftOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift char : wrong result : ", yCharValue >>> xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharUnsignedRightShiftShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + unsignedRightShiftOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift short : wrong result : ", xCharValue >>> yShortValue, intValue);

		value = eval(yChar + unsignedRightShiftOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift short : wrong result : ", yCharValue >>> xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharUnsignedRightShiftInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + unsignedRightShiftOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift int : wrong result : ", xCharValue >>> yIntValue, intValue);

		value = eval(yChar + unsignedRightShiftOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift int : wrong result : ", yCharValue >>> xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharUnsignedRightShiftLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + unsignedRightShiftOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift long : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift long : wrong result : ", xCharValue >>> yLongValue, intValue);

		value = eval(yChar + unsignedRightShiftOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char unsignedRightShift long : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char unsignedRightShift long : wrong result : ", yCharValue >>> xLongValue, intValue);
		} finally {
		end();
		}
	}

	// char | {byte, char, short, int, long}

	public void testCharOrByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + orOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char or byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char or byte : wrong result : ", xCharValue | yByteValue, intValue);

		value = eval(yChar + orOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char or byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char or byte : wrong result : ", yCharValue | xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharOrChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + orOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char or char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char or char : wrong result : ", xCharValue | yCharValue, intValue);

		value = eval(yChar + orOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char or char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char or char : wrong result : ", yCharValue | xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharOrShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + orOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char or short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char or short : wrong result : ", xCharValue | yShortValue, intValue);

		value = eval(yChar + orOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char or short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char or short : wrong result : ", yCharValue | xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharOrInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + orOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char or int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char or int : wrong result : ", xCharValue | yIntValue, intValue);

		value = eval(yChar + orOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char or int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char or int : wrong result : ", yCharValue | xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharOrLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + orOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char or long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char or long : wrong result : ", xCharValue | yLongValue, longValue);

		value = eval(yChar + orOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char or long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char or long : wrong result : ", yCharValue | xLongValue, longValue);
		} finally {
		end();
		}
	}

	// char & {byte, char, short, int, long}

	public void testCharAndByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + andOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char and byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char and byte : wrong result : ", xCharValue & yByteValue, intValue);

		value = eval(yChar + andOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char and byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char and byte : wrong result : ", yCharValue & xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharAndChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + andOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char and char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char and char : wrong result : ", xCharValue & yCharValue, intValue);

		value = eval(yChar + andOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char and char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char and char : wrong result : ", yCharValue & xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharAndShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + andOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char and short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char and short : wrong result : ", xCharValue & yShortValue, intValue);

		value = eval(yChar + andOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char and short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char and short : wrong result : ", yCharValue & xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharAndInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + andOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char and int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char and int : wrong result : ", xCharValue & yIntValue, intValue);

		value = eval(yChar + andOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char and int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char and int : wrong result : ", yCharValue & xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharAndLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + andOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char and long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char and long : wrong result : ", xCharValue & yLongValue, longValue);

		value = eval(yChar + andOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char and long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char and long : wrong result : ", yCharValue & xLongValue, longValue);
		} finally {
		end();
		}
	}

	// char ^ {byte, char, short, int, long}

	public void testCharXorByte() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + xorOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xor byte : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char xor byte : wrong result : ", xCharValue ^ yByteValue, intValue);

		value = eval(yChar + xorOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("char xor byte : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char xor byte : wrong result : ", yCharValue ^ xByteValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharXorChar() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + xorOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xor char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char xor char : wrong result : ", xCharValue ^ yCharValue, intValue);

		value = eval(yChar + xorOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("char xor char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char xor char : wrong result : ", yCharValue ^ xCharValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharXorShort() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + xorOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xor short : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char xor short : wrong result : ", xCharValue ^ yShortValue, intValue);

		value = eval(yChar + xorOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("char xor short : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char xor short : wrong result : ", yCharValue ^ xShortValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharXorInt() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + xorOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xor int : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char xor int : wrong result : ", xCharValue ^ yIntValue, intValue);

		value = eval(yChar + xorOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("char xor int : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("char xor int : wrong result : ", yCharValue ^ xIntValue, intValue);
		} finally {
		end();
		}
	}

	public void testCharXorLong() throws Throwable {
		try {
		init();
		IValue value = eval(xChar + xorOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("char xor long : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char xor long : wrong result : ", xCharValue ^ yLongValue, longValue);

		value = eval(yChar + xorOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("char xor long : wrong type : ", "long", typeName);
		longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("char xor long : wrong result : ", yCharValue ^ xLongValue, longValue);
		} finally {
		end();
		}
	}

	// + char

	public void testPlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(plusOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("plus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("plus char : wrong result : ", + xCharValue, intValue);

		value = eval(plusOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("plus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("plus char : wrong result : ", + yCharValue, intValue);
		} finally {
		end();
		}
	}

	// - char

	public void testMinusChar() throws Throwable {
		try {
		init();
		IValue value = eval(minusOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("minus char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("minus char : wrong result : ", - xCharValue, intValue);

		value = eval(minusOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("minus char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("minus char : wrong result : ", - yCharValue, intValue);
		} finally {
		end();
		}
	}

	// ~ char

	public void testTwiddleChar() throws Throwable {
		try {
		init();
		IValue value = eval(twiddleOp + xChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("twiddle char : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("twiddle char : wrong result : ", ~ xCharValue, intValue);

		value = eval(twiddleOp + yChar);
		typeName = value.getReferenceTypeName();
		assertEquals("twiddle char : wrong type : ", "int", typeName);
		intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("twiddle char : wrong result : ", ~ yCharValue, intValue);
		} finally {
		end();
		}
	}


}
