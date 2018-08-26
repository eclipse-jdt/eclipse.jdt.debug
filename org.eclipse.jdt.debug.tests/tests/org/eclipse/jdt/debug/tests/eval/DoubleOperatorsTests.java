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

public class DoubleOperatorsTests extends Tests {

	public DoubleOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// double + {byte, char, short, int, long, float, double}

	public void testDoublePlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + plusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plus byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus byte : wrong result : ", xDoubleValue + yByteValue, doubleValue, 0);

		value = eval(yDouble + plusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double plus byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus byte : wrong result : ", yDoubleValue + xByteValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + plusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plus char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus char : wrong result : ", xDoubleValue + yCharValue, doubleValue, 0);

		value = eval(yDouble + plusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double plus char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus char : wrong result : ", yDoubleValue + xCharValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + plusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plus short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus short : wrong result : ", xDoubleValue + yShortValue, doubleValue, 0);

		value = eval(yDouble + plusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double plus short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus short : wrong result : ", yDoubleValue + xShortValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plus int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus int : wrong result : ", xDoubleValue + yIntValue, doubleValue, 0);

		value = eval(yDouble + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double plus int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus int : wrong result : ", yDoubleValue + xIntValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + plusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plus long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus long : wrong result : ", xDoubleValue + yLongValue, doubleValue, 0);

		value = eval(yDouble + plusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double plus long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus long : wrong result : ", yDoubleValue + xLongValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + plusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plus float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus float : wrong result : ", xDoubleValue + yFloatValue, doubleValue, 0);

		value = eval(yDouble + plusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double plus float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus float : wrong result : ", yDoubleValue + xFloatValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + plusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus double : wrong result : ", xDoubleValue + yDoubleValue, doubleValue, 0);

		value = eval(yDouble + plusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double plus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double plus double : wrong result : ", yDoubleValue + xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoublePlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("double plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("double plus java.lang.String : wrong result : ", xDoubleValue + yStringValue, stringValue);

		value = eval(yDouble + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("double plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("double plus java.lang.String : wrong result : ", yDoubleValue + xStringValue, stringValue);
		} finally {
		end();
		}
	}

	// double - {byte, char, short, int, long, float, double}

	public void testDoubleMinusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + minusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minus byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus byte : wrong result : ", xDoubleValue - yByteValue, doubleValue, 0);

		value = eval(yDouble + minusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double minus byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus byte : wrong result : ", yDoubleValue - xByteValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + minusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minus char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus char : wrong result : ", xDoubleValue - yCharValue, doubleValue, 0);

		value = eval(yDouble + minusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double minus char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus char : wrong result : ", yDoubleValue - xCharValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + minusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minus short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus short : wrong result : ", xDoubleValue - yShortValue, doubleValue, 0);

		value = eval(yDouble + minusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double minus short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus short : wrong result : ", yDoubleValue - xShortValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + minusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minus int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus int : wrong result : ", xDoubleValue - yIntValue, doubleValue, 0);

		value = eval(yDouble + minusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double minus int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus int : wrong result : ", yDoubleValue - xIntValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + minusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minus long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus long : wrong result : ", xDoubleValue - yLongValue, doubleValue, 0);

		value = eval(yDouble + minusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double minus long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus long : wrong result : ", yDoubleValue - xLongValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + minusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minus float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus float : wrong result : ", xDoubleValue - yFloatValue, doubleValue, 0);

		value = eval(yDouble + minusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double minus float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus float : wrong result : ", yDoubleValue - xFloatValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMinusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + minusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double minus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus double : wrong result : ", xDoubleValue - yDoubleValue, doubleValue, 0);

		value = eval(yDouble + minusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double minus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double minus double : wrong result : ", yDoubleValue - xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// double * {byte, char, short, int, long, float, double}

	public void testDoubleMultiplyByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + multiplyOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiply byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply byte : wrong result : ", xDoubleValue * yByteValue, doubleValue, 0);

		value = eval(yDouble + multiplyOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiply byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply byte : wrong result : ", yDoubleValue * xByteValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + multiplyOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiply char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply char : wrong result : ", xDoubleValue * yCharValue, doubleValue, 0);

		value = eval(yDouble + multiplyOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiply char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply char : wrong result : ", yDoubleValue * xCharValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + multiplyOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiply short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply short : wrong result : ", xDoubleValue * yShortValue, doubleValue, 0);

		value = eval(yDouble + multiplyOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiply short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply short : wrong result : ", yDoubleValue * xShortValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + multiplyOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiply int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply int : wrong result : ", xDoubleValue * yIntValue, doubleValue, 0);

		value = eval(yDouble + multiplyOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiply int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply int : wrong result : ", yDoubleValue * xIntValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + multiplyOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiply long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply long : wrong result : ", xDoubleValue * yLongValue, doubleValue, 0);

		value = eval(yDouble + multiplyOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiply long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply long : wrong result : ", yDoubleValue * xLongValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + multiplyOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiply float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply float : wrong result : ", xDoubleValue * yFloatValue, doubleValue, 0);

		value = eval(yDouble + multiplyOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiply float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply float : wrong result : ", yDoubleValue * xFloatValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleMultiplyDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + multiplyOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double multiply double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply double : wrong result : ", xDoubleValue * yDoubleValue, doubleValue, 0);

		value = eval(yDouble + multiplyOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double multiply double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double multiply double : wrong result : ", yDoubleValue * xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// double / {byte, char, short, int, long, float, double}

	public void testDoubleDivideByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + divideOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divide byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide byte : wrong result : ", xDoubleValue / yByteValue, doubleValue, 0);

		value = eval(yDouble + divideOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double divide byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide byte : wrong result : ", yDoubleValue / xByteValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + divideOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divide char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide char : wrong result : ", xDoubleValue / yCharValue, doubleValue, 0);

		value = eval(yDouble + divideOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double divide char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide char : wrong result : ", yDoubleValue / xCharValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + divideOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divide short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide short : wrong result : ", xDoubleValue / yShortValue, doubleValue, 0);

		value = eval(yDouble + divideOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double divide short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide short : wrong result : ", yDoubleValue / xShortValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + divideOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divide int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide int : wrong result : ", xDoubleValue / yIntValue, doubleValue, 0);

		value = eval(yDouble + divideOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double divide int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide int : wrong result : ", yDoubleValue / xIntValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + divideOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divide long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide long : wrong result : ", xDoubleValue / yLongValue, doubleValue, 0);

		value = eval(yDouble + divideOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double divide long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide long : wrong result : ", yDoubleValue / xLongValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + divideOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divide float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide float : wrong result : ", xDoubleValue / yFloatValue, doubleValue, 0);

		value = eval(yDouble + divideOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double divide float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide float : wrong result : ", yDoubleValue / xFloatValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleDivideDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + divideOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double divide double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide double : wrong result : ", xDoubleValue / yDoubleValue, doubleValue, 0);

		value = eval(yDouble + divideOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double divide double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double divide double : wrong result : ", yDoubleValue / xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// double % {byte, char, short, int, long, float, double}

	public void testDoubleRemainderByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + remainderOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainder byte : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder byte : wrong result : ", xDoubleValue % yByteValue, doubleValue, 0);

		value = eval(yDouble + remainderOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainder byte : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder byte : wrong result : ", yDoubleValue % xByteValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + remainderOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainder char : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder char : wrong result : ", xDoubleValue % yCharValue, doubleValue, 0);

		value = eval(yDouble + remainderOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainder char : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder char : wrong result : ", yDoubleValue % xCharValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + remainderOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainder short : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder short : wrong result : ", xDoubleValue % yShortValue, doubleValue, 0);

		value = eval(yDouble + remainderOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainder short : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder short : wrong result : ", yDoubleValue % xShortValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + remainderOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainder int : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder int : wrong result : ", xDoubleValue % yIntValue, doubleValue, 0);

		value = eval(yDouble + remainderOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainder int : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder int : wrong result : ", yDoubleValue % xIntValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + remainderOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainder long : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder long : wrong result : ", xDoubleValue % yLongValue, doubleValue, 0);

		value = eval(yDouble + remainderOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainder long : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder long : wrong result : ", yDoubleValue % xLongValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + remainderOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainder float : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder float : wrong result : ", xDoubleValue % yFloatValue, doubleValue, 0);

		value = eval(yDouble + remainderOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainder float : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder float : wrong result : ", yDoubleValue % xFloatValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testDoubleRemainderDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + remainderOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double remainder double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder double : wrong result : ", xDoubleValue % yDoubleValue, doubleValue, 0);

		value = eval(yDouble + remainderOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double remainder double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("double remainder double : wrong result : ", yDoubleValue % xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// double > {byte, char, short, int, long, float, double}

	public void testDoubleGreaterByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greater byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater byte : wrong result : ", xDoubleValue > yByteValue, booleanValue);

		value = eval(yDouble + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater byte : wrong result : ", yDoubleValue > xByteValue, booleanValue);

		value = eval(xDouble + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater byte : wrong result : ", xDoubleValue > xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greater char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater char : wrong result : ", xDoubleValue > yCharValue, booleanValue);

		value = eval(yDouble + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater char : wrong result : ", yDoubleValue > xCharValue, booleanValue);

		value = eval(xDouble + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater char : wrong result : ", xDoubleValue > xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greater short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater short : wrong result : ", xDoubleValue > yShortValue, booleanValue);

		value = eval(yDouble + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater short : wrong result : ", yDoubleValue > xShortValue, booleanValue);

		value = eval(xDouble + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater short : wrong result : ", xDoubleValue > xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greater int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater int : wrong result : ", xDoubleValue > yIntValue, booleanValue);

		value = eval(yDouble + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater int : wrong result : ", yDoubleValue > xIntValue, booleanValue);

		value = eval(xDouble + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater int : wrong result : ", xDoubleValue > xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greater long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater long : wrong result : ", xDoubleValue > yLongValue, booleanValue);

		value = eval(yDouble + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater long : wrong result : ", yDoubleValue > xLongValue, booleanValue);

		value = eval(xDouble + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater long : wrong result : ", xDoubleValue > xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greater float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater float : wrong result : ", xDoubleValue > yFloatValue, booleanValue);

		value = eval(yDouble + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater float : wrong result : ", yDoubleValue > xFloatValue, booleanValue);

		value = eval(xDouble + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater float : wrong result : ", xDoubleValue > xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greater double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater double : wrong result : ", xDoubleValue > yDoubleValue, booleanValue);

		value = eval(yDouble + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater double : wrong result : ", yDoubleValue > xDoubleValue, booleanValue);

		value = eval(xDouble + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greater double : wrong result : ", xDoubleValue > xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// double >= {byte, char, short, int, long, float, double}

	public void testDoubleGreaterEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual byte : wrong result : ", xDoubleValue >= yByteValue, booleanValue);

		value = eval(yDouble + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual byte : wrong result : ", yDoubleValue >= xByteValue, booleanValue);

		value = eval(xDouble + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual byte : wrong result : ", xDoubleValue >= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual char : wrong result : ", xDoubleValue >= yCharValue, booleanValue);

		value = eval(yDouble + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual char : wrong result : ", yDoubleValue >= xCharValue, booleanValue);

		value = eval(xDouble + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual char : wrong result : ", xDoubleValue >= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual short : wrong result : ", xDoubleValue >= yShortValue, booleanValue);

		value = eval(yDouble + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual short : wrong result : ", yDoubleValue >= xShortValue, booleanValue);

		value = eval(xDouble + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual short : wrong result : ", xDoubleValue >= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual int : wrong result : ", xDoubleValue >= yIntValue, booleanValue);

		value = eval(yDouble + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual int : wrong result : ", yDoubleValue >= xIntValue, booleanValue);

		value = eval(xDouble + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual int : wrong result : ", xDoubleValue >= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual long : wrong result : ", xDoubleValue >= yLongValue, booleanValue);

		value = eval(yDouble + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual long : wrong result : ", yDoubleValue >= xLongValue, booleanValue);

		value = eval(xDouble + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual long : wrong result : ", xDoubleValue >= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual float : wrong result : ", xDoubleValue >= yFloatValue, booleanValue);

		value = eval(yDouble + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual float : wrong result : ", yDoubleValue >= xFloatValue, booleanValue);

		value = eval(xDouble + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual float : wrong result : ", xDoubleValue >= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleGreaterEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + greaterEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual double : wrong result : ", xDoubleValue >= yDoubleValue, booleanValue);

		value = eval(yDouble + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual double : wrong result : ", yDoubleValue >= xDoubleValue, booleanValue);

		value = eval(xDouble + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double greaterEqual double : wrong result : ", xDoubleValue >= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// double < {byte, char, short, int, long, float, double}

	public void testDoubleLessByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double less byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less byte : wrong result : ", xDoubleValue < yByteValue, booleanValue);

		value = eval(yDouble + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less byte : wrong result : ", yDoubleValue < xByteValue, booleanValue);

		value = eval(xDouble + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less byte : wrong result : ", xDoubleValue < xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double less char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less char : wrong result : ", xDoubleValue < yCharValue, booleanValue);

		value = eval(yDouble + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less char : wrong result : ", yDoubleValue < xCharValue, booleanValue);

		value = eval(xDouble + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less char : wrong result : ", xDoubleValue < xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double less short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less short : wrong result : ", xDoubleValue < yShortValue, booleanValue);

		value = eval(yDouble + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less short : wrong result : ", yDoubleValue < xShortValue, booleanValue);

		value = eval(xDouble + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less short : wrong result : ", xDoubleValue < xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double less int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less int : wrong result : ", xDoubleValue < yIntValue, booleanValue);

		value = eval(yDouble + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less int : wrong result : ", yDoubleValue < xIntValue, booleanValue);

		value = eval(xDouble + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less int : wrong result : ", xDoubleValue < xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double less long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less long : wrong result : ", xDoubleValue < yLongValue, booleanValue);

		value = eval(yDouble + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less long : wrong result : ", yDoubleValue < xLongValue, booleanValue);

		value = eval(xDouble + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less long : wrong result : ", xDoubleValue < xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double less float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less float : wrong result : ", xDoubleValue < yFloatValue, booleanValue);

		value = eval(yDouble + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less float : wrong result : ", yDoubleValue < xFloatValue, booleanValue);

		value = eval(xDouble + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less float : wrong result : ", xDoubleValue < xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double less double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less double : wrong result : ", xDoubleValue < yDoubleValue, booleanValue);

		value = eval(yDouble + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less double : wrong result : ", yDoubleValue < xDoubleValue, booleanValue);

		value = eval(xDouble + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double less double : wrong result : ", xDoubleValue < xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// double <= {byte, char, short, int, long, float, double}

	public void testDoubleLessEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual byte : wrong result : ", xDoubleValue <= yByteValue, booleanValue);

		value = eval(yDouble + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual byte : wrong result : ", yDoubleValue <= xByteValue, booleanValue);

		value = eval(xDouble + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual byte : wrong result : ", xDoubleValue <= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual char : wrong result : ", xDoubleValue <= yCharValue, booleanValue);

		value = eval(yDouble + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual char : wrong result : ", yDoubleValue <= xCharValue, booleanValue);

		value = eval(xDouble + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual char : wrong result : ", xDoubleValue <= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual short : wrong result : ", xDoubleValue <= yShortValue, booleanValue);

		value = eval(yDouble + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual short : wrong result : ", yDoubleValue <= xShortValue, booleanValue);

		value = eval(xDouble + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual short : wrong result : ", xDoubleValue <= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual int : wrong result : ", xDoubleValue <= yIntValue, booleanValue);

		value = eval(yDouble + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual int : wrong result : ", yDoubleValue <= xIntValue, booleanValue);

		value = eval(xDouble + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual int : wrong result : ", xDoubleValue <= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual long : wrong result : ", xDoubleValue <= yLongValue, booleanValue);

		value = eval(yDouble + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual long : wrong result : ", yDoubleValue <= xLongValue, booleanValue);

		value = eval(xDouble + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual long : wrong result : ", xDoubleValue <= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual float : wrong result : ", xDoubleValue <= yFloatValue, booleanValue);

		value = eval(yDouble + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual float : wrong result : ", yDoubleValue <= xFloatValue, booleanValue);

		value = eval(xDouble + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual float : wrong result : ", xDoubleValue <= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleLessEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + lessEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual double : wrong result : ", xDoubleValue <= yDoubleValue, booleanValue);

		value = eval(yDouble + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual double : wrong result : ", yDoubleValue <= xDoubleValue, booleanValue);

		value = eval(xDouble + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double lessEqual double : wrong result : ", xDoubleValue <= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// double == {byte, char, short, int, long, float, double}

	public void testDoubleEqualEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + equalEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual byte : wrong result : ", xDoubleValue == yByteValue, booleanValue);

		value = eval(yDouble + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual byte : wrong result : ", yDoubleValue == xByteValue, booleanValue);

		value = eval(xDouble + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual byte : wrong result : ", xDoubleValue == xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleEqualEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + equalEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual char : wrong result : ", xDoubleValue == yCharValue, booleanValue);

		value = eval(yDouble + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual char : wrong result : ", yDoubleValue == xCharValue, booleanValue);

		value = eval(xDouble + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual char : wrong result : ", xDoubleValue == xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleEqualEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + equalEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual short : wrong result : ", xDoubleValue == yShortValue, booleanValue);

		value = eval(yDouble + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual short : wrong result : ", yDoubleValue == xShortValue, booleanValue);

		value = eval(xDouble + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual short : wrong result : ", xDoubleValue == xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleEqualEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + equalEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual int : wrong result : ", xDoubleValue == yIntValue, booleanValue);

		value = eval(yDouble + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual int : wrong result : ", yDoubleValue == xIntValue, booleanValue);

		value = eval(xDouble + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual int : wrong result : ", xDoubleValue == xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleEqualEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + equalEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual long : wrong result : ", xDoubleValue == yLongValue, booleanValue);

		value = eval(yDouble + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual long : wrong result : ", yDoubleValue == xLongValue, booleanValue);

		value = eval(xDouble + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual long : wrong result : ", xDoubleValue == xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleEqualEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + equalEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual float : wrong result : ", xDoubleValue == yFloatValue, booleanValue);

		value = eval(yDouble + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual float : wrong result : ", yDoubleValue == xFloatValue, booleanValue);

		value = eval(xDouble + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual float : wrong result : ", xDoubleValue == xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleEqualEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + equalEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual double : wrong result : ", xDoubleValue == yDoubleValue, booleanValue);

		value = eval(yDouble + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual double : wrong result : ", yDoubleValue == xDoubleValue, booleanValue);

		value = eval(xDouble + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double equalEqual double : wrong result : ", true, booleanValue);
		} finally {
		end();
		}
	}

	// double != {byte, char, short, int, long, float, double}

	public void testDoubleNotEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + notEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("double notEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual byte : wrong result : ", xDoubleValue != yByteValue, booleanValue);

		value = eval(yDouble + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual byte : wrong result : ", yDoubleValue != xByteValue, booleanValue);

		value = eval(xDouble + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual byte : wrong result : ", xDoubleValue != xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleNotEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + notEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("double notEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual char : wrong result : ", xDoubleValue != yCharValue, booleanValue);

		value = eval(yDouble + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual char : wrong result : ", yDoubleValue != xCharValue, booleanValue);

		value = eval(xDouble + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual char : wrong result : ", xDoubleValue != xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleNotEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + notEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("double notEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual short : wrong result : ", xDoubleValue != yShortValue, booleanValue);

		value = eval(yDouble + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual short : wrong result : ", yDoubleValue != xShortValue, booleanValue);

		value = eval(xDouble + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual short : wrong result : ", xDoubleValue != xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleNotEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + notEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("double notEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual int : wrong result : ", xDoubleValue != yIntValue, booleanValue);

		value = eval(yDouble + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual int : wrong result : ", yDoubleValue != xIntValue, booleanValue);

		value = eval(xDouble + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual int : wrong result : ", xDoubleValue != xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleNotEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + notEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("double notEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual long : wrong result : ", xDoubleValue != yLongValue, booleanValue);

		value = eval(yDouble + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual long : wrong result : ", yDoubleValue != xLongValue, booleanValue);

		value = eval(xDouble + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual long : wrong result : ", xDoubleValue != xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleNotEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + notEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("double notEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual float : wrong result : ", xDoubleValue != yFloatValue, booleanValue);

		value = eval(yDouble + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual float : wrong result : ", yDoubleValue != xFloatValue, booleanValue);

		value = eval(xDouble + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual float : wrong result : ", xDoubleValue != xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testDoubleNotEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xDouble + notEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("double notEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual double : wrong result : ", xDoubleValue != yDoubleValue, booleanValue);

		value = eval(yDouble + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual double : wrong result : ", yDoubleValue != xDoubleValue, booleanValue);

		value = eval(xDouble + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("double notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("double notEqual double : wrong result : ", false, booleanValue);
		} finally {
		end();
		}
	}

	// + double

	public void testPlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(plusOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("plus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("plus double : wrong result : ", + xDoubleValue, doubleValue, 0);

		value = eval(plusOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("plus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("plus double : wrong result : ", + yDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// - double

	public void testMinusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(minusOp + xDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("minus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("minus double : wrong result : ", - xDoubleValue, doubleValue, 0);

		value = eval(minusOp + yDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("minus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("minus double : wrong result : ", - yDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}


}
