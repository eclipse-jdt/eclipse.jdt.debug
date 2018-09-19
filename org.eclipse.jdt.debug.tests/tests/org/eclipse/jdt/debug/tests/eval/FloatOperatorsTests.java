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

public class FloatOperatorsTests extends Tests {

	public FloatOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// float + {byte, char, short, int, long, float, double}

	public void testFloatPlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + plusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float plus byte : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus byte : wrong result : ", xFloatValue + yByteValue, floatValue, 0);

		value = eval(yFloat + plusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float plus byte : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus byte : wrong result : ", yFloatValue + xByteValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatPlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + plusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float plus char : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus char : wrong result : ", xFloatValue + yCharValue, floatValue, 0);

		value = eval(yFloat + plusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float plus char : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus char : wrong result : ", yFloatValue + xCharValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatPlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + plusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float plus short : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus short : wrong result : ", xFloatValue + yShortValue, floatValue, 0);

		value = eval(yFloat + plusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float plus short : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus short : wrong result : ", yFloatValue + xShortValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatPlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float plus int : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus int : wrong result : ", xFloatValue + yIntValue, floatValue, 0);

		value = eval(yFloat + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float plus int : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus int : wrong result : ", yFloatValue + xIntValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatPlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + plusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float plus long : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus long : wrong result : ", xFloatValue + yLongValue, floatValue, 0);

		value = eval(yFloat + plusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float plus long : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus long : wrong result : ", yFloatValue + xLongValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatPlusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + plusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float plus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus float : wrong result : ", xFloatValue + yFloatValue, floatValue, 0);

		value = eval(yFloat + plusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float plus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float plus float : wrong result : ", yFloatValue + xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatPlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + plusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float plus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float plus double : wrong result : ", xFloatValue + yDoubleValue, doubleValue, 0);

		value = eval(yFloat + plusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float plus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float plus double : wrong result : ", yFloatValue + xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatPlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("float plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("float plus java.lang.String : wrong result : ", xFloatValue + yStringValue, stringValue);

		value = eval(yFloat + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("float plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("float plus java.lang.String : wrong result : ", yFloatValue + xStringValue, stringValue);
		} finally {
		end();
		}
	}

	// float - {byte, char, short, int, long, float, double}

	public void testFloatMinusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + minusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float minus byte : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus byte : wrong result : ", xFloatValue - yByteValue, floatValue, 0);

		value = eval(yFloat + minusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float minus byte : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus byte : wrong result : ", yFloatValue - xByteValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMinusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + minusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float minus char : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus char : wrong result : ", xFloatValue - yCharValue, floatValue, 0);

		value = eval(yFloat + minusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float minus char : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus char : wrong result : ", yFloatValue - xCharValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMinusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + minusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float minus short : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus short : wrong result : ", xFloatValue - yShortValue, floatValue, 0);

		value = eval(yFloat + minusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float minus short : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus short : wrong result : ", yFloatValue - xShortValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMinusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + minusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float minus int : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus int : wrong result : ", xFloatValue - yIntValue, floatValue, 0);

		value = eval(yFloat + minusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float minus int : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus int : wrong result : ", yFloatValue - xIntValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMinusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + minusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float minus long : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus long : wrong result : ", xFloatValue - yLongValue, floatValue, 0);

		value = eval(yFloat + minusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float minus long : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus long : wrong result : ", yFloatValue - xLongValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMinusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + minusOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float minus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus float : wrong result : ", xFloatValue - yFloatValue, floatValue, 0);

		value = eval(yFloat + minusOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float minus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float minus float : wrong result : ", yFloatValue - xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMinusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + minusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float minus double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float minus double : wrong result : ", xFloatValue - yDoubleValue, doubleValue, 0);

		value = eval(yFloat + minusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float minus double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float minus double : wrong result : ", yFloatValue - xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// float * {byte, char, short, int, long, float, double}

	public void testFloatMultiplyByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + multiplyOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float multiply byte : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply byte : wrong result : ", xFloatValue * yByteValue, floatValue, 0);

		value = eval(yFloat + multiplyOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float multiply byte : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply byte : wrong result : ", yFloatValue * xByteValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMultiplyChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + multiplyOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float multiply char : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply char : wrong result : ", xFloatValue * yCharValue, floatValue, 0);

		value = eval(yFloat + multiplyOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float multiply char : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply char : wrong result : ", yFloatValue * xCharValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMultiplyShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + multiplyOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float multiply short : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply short : wrong result : ", xFloatValue * yShortValue, floatValue, 0);

		value = eval(yFloat + multiplyOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float multiply short : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply short : wrong result : ", yFloatValue * xShortValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMultiplyInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + multiplyOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float multiply int : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply int : wrong result : ", xFloatValue * yIntValue, floatValue, 0);

		value = eval(yFloat + multiplyOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float multiply int : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply int : wrong result : ", yFloatValue * xIntValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMultiplyLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + multiplyOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float multiply long : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply long : wrong result : ", xFloatValue * yLongValue, floatValue, 0);

		value = eval(yFloat + multiplyOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float multiply long : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply long : wrong result : ", yFloatValue * xLongValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMultiplyFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + multiplyOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float multiply float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply float : wrong result : ", xFloatValue * yFloatValue, floatValue, 0);

		value = eval(yFloat + multiplyOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float multiply float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float multiply float : wrong result : ", yFloatValue * xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatMultiplyDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + multiplyOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float multiply double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float multiply double : wrong result : ", xFloatValue * yDoubleValue, doubleValue, 0);

		value = eval(yFloat + multiplyOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float multiply double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float multiply double : wrong result : ", yFloatValue * xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// float / {byte, char, short, int, long, float, double}

	public void testFloatDivideByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + divideOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float divide byte : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide byte : wrong result : ", xFloatValue / yByteValue, floatValue, 0);

		value = eval(yFloat + divideOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float divide byte : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide byte : wrong result : ", yFloatValue / xByteValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatDivideChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + divideOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float divide char : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide char : wrong result : ", xFloatValue / yCharValue, floatValue, 0);

		value = eval(yFloat + divideOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float divide char : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide char : wrong result : ", yFloatValue / xCharValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatDivideShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + divideOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float divide short : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide short : wrong result : ", xFloatValue / yShortValue, floatValue, 0);

		value = eval(yFloat + divideOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float divide short : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide short : wrong result : ", yFloatValue / xShortValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatDivideInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + divideOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float divide int : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide int : wrong result : ", xFloatValue / yIntValue, floatValue, 0);

		value = eval(yFloat + divideOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float divide int : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide int : wrong result : ", yFloatValue / xIntValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatDivideLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + divideOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float divide long : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide long : wrong result : ", xFloatValue / yLongValue, floatValue, 0);

		value = eval(yFloat + divideOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float divide long : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide long : wrong result : ", yFloatValue / xLongValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatDivideFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + divideOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float divide float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide float : wrong result : ", xFloatValue / yFloatValue, floatValue, 0);

		value = eval(yFloat + divideOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float divide float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float divide float : wrong result : ", yFloatValue / xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatDivideDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + divideOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float divide double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float divide double : wrong result : ", xFloatValue / yDoubleValue, doubleValue, 0);

		value = eval(yFloat + divideOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float divide double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float divide double : wrong result : ", yFloatValue / xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// float % {byte, char, short, int, long, float, double}

	public void testFloatRemainderByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + remainderOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float remainder byte : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder byte : wrong result : ", xFloatValue % yByteValue, floatValue, 0);

		value = eval(yFloat + remainderOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float remainder byte : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder byte : wrong result : ", yFloatValue % xByteValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatRemainderChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + remainderOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float remainder char : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder char : wrong result : ", xFloatValue % yCharValue, floatValue, 0);

		value = eval(yFloat + remainderOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float remainder char : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder char : wrong result : ", yFloatValue % xCharValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatRemainderShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + remainderOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float remainder short : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder short : wrong result : ", xFloatValue % yShortValue, floatValue, 0);

		value = eval(yFloat + remainderOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float remainder short : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder short : wrong result : ", yFloatValue % xShortValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatRemainderInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + remainderOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float remainder int : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder int : wrong result : ", xFloatValue % yIntValue, floatValue, 0);

		value = eval(yFloat + remainderOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float remainder int : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder int : wrong result : ", yFloatValue % xIntValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatRemainderLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + remainderOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float remainder long : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder long : wrong result : ", xFloatValue % yLongValue, floatValue, 0);

		value = eval(yFloat + remainderOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float remainder long : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder long : wrong result : ", yFloatValue % xLongValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatRemainderFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + remainderOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float remainder float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder float : wrong result : ", xFloatValue % yFloatValue, floatValue, 0);

		value = eval(yFloat + remainderOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float remainder float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("float remainder float : wrong result : ", yFloatValue % xFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	public void testFloatRemainderDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + remainderOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float remainder double : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float remainder double : wrong result : ", xFloatValue % yDoubleValue, doubleValue, 0);

		value = eval(yFloat + remainderOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float remainder double : wrong type : ", "double", typeName);
		doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("float remainder double : wrong result : ", yFloatValue % xDoubleValue, doubleValue, 0);
		} finally {
		end();
		}
	}

	// float > {byte, char, short, int, long, float, double}

	public void testFloatGreaterByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greater byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater byte : wrong result : ", xFloatValue > yByteValue, booleanValue);

		value = eval(yFloat + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater byte : wrong result : ", yFloatValue > xByteValue, booleanValue);

		value = eval(xFloat + greaterOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater byte : wrong result : ", xFloatValue > xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greater char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater char : wrong result : ", xFloatValue > yCharValue, booleanValue);

		value = eval(yFloat + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater char : wrong result : ", yFloatValue > xCharValue, booleanValue);

		value = eval(xFloat + greaterOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater char : wrong result : ", xFloatValue > xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greater short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater short : wrong result : ", xFloatValue > yShortValue, booleanValue);

		value = eval(yFloat + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater short : wrong result : ", yFloatValue > xShortValue, booleanValue);

		value = eval(xFloat + greaterOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater short : wrong result : ", xFloatValue > xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greater int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater int : wrong result : ", xFloatValue > yIntValue, booleanValue);

		value = eval(yFloat + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater int : wrong result : ", yFloatValue > xIntValue, booleanValue);

		value = eval(xFloat + greaterOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater int : wrong result : ", xFloatValue > xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greater long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater long : wrong result : ", xFloatValue > yLongValue, booleanValue);

		value = eval(yFloat + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater long : wrong result : ", yFloatValue > xLongValue, booleanValue);

		value = eval(xFloat + greaterOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater long : wrong result : ", xFloatValue > xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greater float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater float : wrong result : ", xFloatValue > yFloatValue, booleanValue);

		value = eval(yFloat + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater float : wrong result : ", yFloatValue > xFloatValue, booleanValue);

		value = eval(xFloat + greaterOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater float : wrong result : ", xFloatValue > xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greater double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater double : wrong result : ", xFloatValue > yDoubleValue, booleanValue);

		value = eval(yFloat + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater double : wrong result : ", yFloatValue > xDoubleValue, booleanValue);

		value = eval(xFloat + greaterOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float greater double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greater double : wrong result : ", xFloatValue > xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// float >= {byte, char, short, int, long, float, double}

	public void testFloatGreaterEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual byte : wrong result : ", xFloatValue >= yByteValue, booleanValue);

		value = eval(yFloat + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual byte : wrong result : ", yFloatValue >= xByteValue, booleanValue);

		value = eval(xFloat + greaterEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual byte : wrong result : ", xFloatValue >= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual char : wrong result : ", xFloatValue >= yCharValue, booleanValue);

		value = eval(yFloat + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual char : wrong result : ", yFloatValue >= xCharValue, booleanValue);

		value = eval(xFloat + greaterEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual char : wrong result : ", xFloatValue >= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual short : wrong result : ", xFloatValue >= yShortValue, booleanValue);

		value = eval(yFloat + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual short : wrong result : ", yFloatValue >= xShortValue, booleanValue);

		value = eval(xFloat + greaterEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual short : wrong result : ", xFloatValue >= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual int : wrong result : ", xFloatValue >= yIntValue, booleanValue);

		value = eval(yFloat + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual int : wrong result : ", yFloatValue >= xIntValue, booleanValue);

		value = eval(xFloat + greaterEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual int : wrong result : ", xFloatValue >= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual long : wrong result : ", xFloatValue >= yLongValue, booleanValue);

		value = eval(yFloat + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual long : wrong result : ", yFloatValue >= xLongValue, booleanValue);

		value = eval(xFloat + greaterEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual long : wrong result : ", xFloatValue >= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual float : wrong result : ", xFloatValue >= yFloatValue, booleanValue);

		value = eval(yFloat + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual float : wrong result : ", yFloatValue >= xFloatValue, booleanValue);

		value = eval(xFloat + greaterEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual float : wrong result : ", xFloatValue >= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatGreaterEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + greaterEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual double : wrong result : ", xFloatValue >= yDoubleValue, booleanValue);

		value = eval(yFloat + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual double : wrong result : ", yFloatValue >= xDoubleValue, booleanValue);

		value = eval(xFloat + greaterEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float greaterEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float greaterEqual double : wrong result : ", xFloatValue >= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// float < {byte, char, short, int, long, float, double}

	public void testFloatLessByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float less byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less byte : wrong result : ", xFloatValue < yByteValue, booleanValue);

		value = eval(yFloat + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less byte : wrong result : ", yFloatValue < xByteValue, booleanValue);

		value = eval(xFloat + lessOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float less byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less byte : wrong result : ", xFloatValue < xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float less char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less char : wrong result : ", xFloatValue < yCharValue, booleanValue);

		value = eval(yFloat + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less char : wrong result : ", yFloatValue < xCharValue, booleanValue);

		value = eval(xFloat + lessOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float less char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less char : wrong result : ", xFloatValue < xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float less short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less short : wrong result : ", xFloatValue < yShortValue, booleanValue);

		value = eval(yFloat + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less short : wrong result : ", yFloatValue < xShortValue, booleanValue);

		value = eval(xFloat + lessOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float less short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less short : wrong result : ", xFloatValue < xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float less int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less int : wrong result : ", xFloatValue < yIntValue, booleanValue);

		value = eval(yFloat + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less int : wrong result : ", yFloatValue < xIntValue, booleanValue);

		value = eval(xFloat + lessOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float less int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less int : wrong result : ", xFloatValue < xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float less long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less long : wrong result : ", xFloatValue < yLongValue, booleanValue);

		value = eval(yFloat + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less long : wrong result : ", yFloatValue < xLongValue, booleanValue);

		value = eval(xFloat + lessOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float less long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less long : wrong result : ", xFloatValue < xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float less float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less float : wrong result : ", xFloatValue < yFloatValue, booleanValue);

		value = eval(yFloat + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less float : wrong result : ", yFloatValue < xFloatValue, booleanValue);

		value = eval(xFloat + lessOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float less float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less float : wrong result : ", xFloatValue < xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float less double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less double : wrong result : ", xFloatValue < yDoubleValue, booleanValue);

		value = eval(yFloat + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less double : wrong result : ", yFloatValue < xDoubleValue, booleanValue);

		value = eval(xFloat + lessOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float less double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float less double : wrong result : ", xFloatValue < xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// float <= {byte, char, short, int, long, float, double}

	public void testFloatLessEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual byte : wrong result : ", xFloatValue <= yByteValue, booleanValue);

		value = eval(yFloat + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual byte : wrong result : ", yFloatValue <= xByteValue, booleanValue);

		value = eval(xFloat + lessEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual byte : wrong result : ", xFloatValue <= xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual char : wrong result : ", xFloatValue <= yCharValue, booleanValue);

		value = eval(yFloat + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual char : wrong result : ", yFloatValue <= xCharValue, booleanValue);

		value = eval(xFloat + lessEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual char : wrong result : ", xFloatValue <= xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual short : wrong result : ", xFloatValue <= yShortValue, booleanValue);

		value = eval(yFloat + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual short : wrong result : ", yFloatValue <= xShortValue, booleanValue);

		value = eval(xFloat + lessEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual short : wrong result : ", xFloatValue <= xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual int : wrong result : ", xFloatValue <= yIntValue, booleanValue);

		value = eval(yFloat + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual int : wrong result : ", yFloatValue <= xIntValue, booleanValue);

		value = eval(xFloat + lessEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual int : wrong result : ", xFloatValue <= xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual long : wrong result : ", xFloatValue <= yLongValue, booleanValue);

		value = eval(yFloat + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual long : wrong result : ", yFloatValue <= xLongValue, booleanValue);

		value = eval(xFloat + lessEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual long : wrong result : ", xFloatValue <= xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual float : wrong result : ", xFloatValue <= yFloatValue, booleanValue);

		value = eval(yFloat + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual float : wrong result : ", yFloatValue <= xFloatValue, booleanValue);

		value = eval(xFloat + lessEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual float : wrong result : ", xFloatValue <= xFloatValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatLessEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + lessEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual double : wrong result : ", xFloatValue <= yDoubleValue, booleanValue);

		value = eval(yFloat + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual double : wrong result : ", yFloatValue <= xDoubleValue, booleanValue);

		value = eval(xFloat + lessEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float lessEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float lessEqual double : wrong result : ", xFloatValue <= xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// float == {byte, char, short, int, long, float, double}

	public void testFloatEqualEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + equalEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual byte : wrong result : ", xFloatValue == yByteValue, booleanValue);

		value = eval(yFloat + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual byte : wrong result : ", yFloatValue == xByteValue, booleanValue);

		value = eval(xFloat + equalEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual byte : wrong result : ", xFloatValue == xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatEqualEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + equalEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual char : wrong result : ", xFloatValue == yCharValue, booleanValue);

		value = eval(yFloat + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual char : wrong result : ", yFloatValue == xCharValue, booleanValue);

		value = eval(xFloat + equalEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual char : wrong result : ", xFloatValue == xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatEqualEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + equalEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual short : wrong result : ", xFloatValue == yShortValue, booleanValue);

		value = eval(yFloat + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual short : wrong result : ", yFloatValue == xShortValue, booleanValue);

		value = eval(xFloat + equalEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual short : wrong result : ", xFloatValue == xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatEqualEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + equalEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual int : wrong result : ", xFloatValue == yIntValue, booleanValue);

		value = eval(yFloat + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual int : wrong result : ", yFloatValue == xIntValue, booleanValue);

		value = eval(xFloat + equalEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual int : wrong result : ", xFloatValue == xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatEqualEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + equalEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual long : wrong result : ", xFloatValue == yLongValue, booleanValue);

		value = eval(yFloat + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual long : wrong result : ", yFloatValue == xLongValue, booleanValue);

		value = eval(xFloat + equalEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual long : wrong result : ", xFloatValue == xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatEqualEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + equalEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual float : wrong result : ", xFloatValue == yFloatValue, booleanValue);

		value = eval(yFloat + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual float : wrong result : ", yFloatValue == xFloatValue, booleanValue);

		value = eval(xFloat + equalEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual float : wrong result : ", true, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatEqualEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + equalEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual double : wrong result : ", xFloatValue == yDoubleValue, booleanValue);

		value = eval(yFloat + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual double : wrong result : ", yFloatValue == xDoubleValue, booleanValue);

		value = eval(xFloat + equalEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float equalEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float equalEqual double : wrong result : ", xFloatValue == xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// float != {byte, char, short, int, long, float, double}

	public void testFloatNotEqualByte() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + notEqualOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("float notEqual byte : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual byte : wrong result : ", xFloatValue != yByteValue, booleanValue);

		value = eval(yFloat + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual byte : wrong result : ", yFloatValue != xByteValue, booleanValue);

		value = eval(xFloat + notEqualOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual byte : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual byte : wrong result : ", xFloatValue != xByteValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatNotEqualChar() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + notEqualOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("float notEqual char : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual char : wrong result : ", xFloatValue != yCharValue, booleanValue);

		value = eval(yFloat + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual char : wrong result : ", yFloatValue != xCharValue, booleanValue);

		value = eval(xFloat + notEqualOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual char : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual char : wrong result : ", xFloatValue != xCharValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatNotEqualShort() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + notEqualOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("float notEqual short : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual short : wrong result : ", xFloatValue != yShortValue, booleanValue);

		value = eval(yFloat + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual short : wrong result : ", yFloatValue != xShortValue, booleanValue);

		value = eval(xFloat + notEqualOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual short : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual short : wrong result : ", xFloatValue != xShortValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatNotEqualInt() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + notEqualOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("float notEqual int : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual int : wrong result : ", xFloatValue != yIntValue, booleanValue);

		value = eval(yFloat + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual int : wrong result : ", yFloatValue != xIntValue, booleanValue);

		value = eval(xFloat + notEqualOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual int : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual int : wrong result : ", xFloatValue != xIntValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatNotEqualLong() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + notEqualOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("float notEqual long : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual long : wrong result : ", xFloatValue != yLongValue, booleanValue);

		value = eval(yFloat + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual long : wrong result : ", yFloatValue != xLongValue, booleanValue);

		value = eval(xFloat + notEqualOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual long : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual long : wrong result : ", xFloatValue != xLongValue, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatNotEqualFloat() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + notEqualOp + yFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("float notEqual float : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual float : wrong result : ", xFloatValue != yFloatValue, booleanValue);

		value = eval(yFloat + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual float : wrong result : ", yFloatValue != xFloatValue, booleanValue);

		value = eval(xFloat + notEqualOp + xFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual float : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual float : wrong result : ", false, booleanValue);
		} finally {
		end();
		}
	}

	public void testFloatNotEqualDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xFloat + notEqualOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("float notEqual double : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual double : wrong result : ", xFloatValue != yDoubleValue, booleanValue);

		value = eval(yFloat + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual double : wrong result : ", yFloatValue != xDoubleValue, booleanValue);

		value = eval(xFloat + notEqualOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("float notEqual double : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("float notEqual double : wrong result : ", xFloatValue != xDoubleValue, booleanValue);
		} finally {
		end();
		}
	}

	// + float

	public void testPlusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(plusOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("plus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("plus float : wrong result : ", + xFloatValue, floatValue, 0);

		value = eval(plusOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("plus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("plus float : wrong result : ", + yFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}

	// - float

	public void testMinusFloat() throws Throwable {
		try {
		init();
		IValue value = eval(minusOp + xFloat);
		String typeName = value.getReferenceTypeName();
		assertEquals("minus float : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("minus float : wrong result : ", - xFloatValue, floatValue, 0);

		value = eval(minusOp + yFloat);
		typeName = value.getReferenceTypeName();
		assertEquals("minus float : wrong type : ", "float", typeName);
		floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("minus float : wrong result : ", - yFloatValue, floatValue, 0);
		} finally {
		end();
		}
	}


}
