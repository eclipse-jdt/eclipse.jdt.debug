package org.eclipse.jdt.debug.tests.eval;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class StringPlusOpTests extends Tests {

	public StringPlusOpTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",5,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// java.lang.String + {byte, char, short, int, long, java.lang.String, null}

	public void testStringPlusByte() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yByte);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus byte : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus byte : wrong result : ", xStringValue + yByteValue, stringValue);

		value = eval(yString + plusOp + xByte);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus byte : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus byte : wrong result : ", yStringValue + xByteValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testStringPlusChar() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yChar);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus char : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus char : wrong result : ", xStringValue + yCharValue, stringValue);

		value = eval(yString + plusOp + xChar);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus char : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus char : wrong result : ", yStringValue + xCharValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testStringPlusShort() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yShort);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus short : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus short : wrong result : ", xStringValue + yShortValue, stringValue);

		value = eval(yString + plusOp + xShort);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus short : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus short : wrong result : ", yStringValue + xShortValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testStringPlusInt() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus int : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus int : wrong result : ", xStringValue + yIntValue, stringValue);

		value = eval(yString + plusOp + xInt);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus int : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus int : wrong result : ", yStringValue + xIntValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testStringPlusLong() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yLong);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus long : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus long : wrong result : ", xStringValue + yLongValue, stringValue);

		value = eval(yString + plusOp + xLong);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus long : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus long : wrong result : ", yStringValue + xLongValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testStringPlusDouble() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yDouble);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus double : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus double : wrong result : ", xStringValue + yDoubleValue, stringValue);

		value = eval(yString + plusOp + xDouble);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus double : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus double : wrong result : ", yStringValue + xDoubleValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testStringPlusBoolean() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus boolean : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus boolean : wrong result : ", xStringValue + yBooleanValue, stringValue);

		value = eval(yString + plusOp + xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus boolean : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus boolean : wrong result : ", yStringValue + xBooleanValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testStringPlusString() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yString);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus java.lang.String : wrong result : ", xStringValue + yStringValue, stringValue);

		value = eval(yString + plusOp + xString);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus java.lang.String : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus java.lang.String : wrong result : ", yStringValue + xStringValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testStringPlusNull() throws Throwable {
		try {
		init();
		IValue value = eval(xString + plusOp + yNull);
		String typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus null : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus null : wrong result : ", xStringValue + yNullValue, stringValue);

		value = eval(yString + plusOp + xNull);
		typeName = value.getReferenceTypeName();
		assertEquals("java.lang.String plus null : wrong type : ", "java.lang.String", typeName);
		stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("java.lang.String plus null : wrong result : ", yStringValue + xNullValue, stringValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}


}
