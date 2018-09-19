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

public class BooleanOperatorsTests extends Tests {

	public BooleanOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// boolean | boolean

	public void testBooleanOrBoolean() throws Throwable {
		try {
		init();
		IValue value = eval(xBoolean + orOp + xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean or boolean : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean or boolean : wrong result : ", xBooleanValue | xBooleanValue, booleanValue);

		value = eval(xBoolean + orOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean or boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean or boolean : wrong result : ", xBooleanValue | yBooleanValue, booleanValue);

		value = eval(yBoolean + orOp + xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean or boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean or boolean : wrong result : ", yBooleanValue | xBooleanValue, booleanValue);

		value = eval(yBoolean + orOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean or boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean or boolean : wrong result : ", yBooleanValue | yBooleanValue, booleanValue);
		} finally {
		end();
		}
	}

	// boolean & boolean

	public void testBooleanAndBoolean() throws Throwable {
		try {
		init();
		IValue value = eval(xBoolean + andOp + xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean and boolean : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean and boolean : wrong result : ", xBooleanValue & xBooleanValue, booleanValue);

		value = eval(xBoolean + andOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean and boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean and boolean : wrong result : ", xBooleanValue & yBooleanValue, booleanValue);

		value = eval(yBoolean + andOp + xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean and boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean and boolean : wrong result : ", yBooleanValue & xBooleanValue, booleanValue);

		value = eval(yBoolean + andOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean and boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean and boolean : wrong result : ", yBooleanValue & yBooleanValue, booleanValue);
		} finally {
		end();
		}
	}

	// boolean ^ boolean

	public void testBooleanXorBoolean() throws Throwable {
		try {
		init();
		IValue value = eval(xBoolean + xorOp + xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean xor boolean : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean xor boolean : wrong result : ", xBooleanValue ^ xBooleanValue, booleanValue);

		value = eval(xBoolean + xorOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean xor boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean xor boolean : wrong result : ", xBooleanValue ^ yBooleanValue, booleanValue);

		value = eval(yBoolean + xorOp + xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean xor boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean xor boolean : wrong result : ", yBooleanValue ^ xBooleanValue, booleanValue);

		value = eval(yBoolean + xorOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean xor boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean xor boolean : wrong result : ", yBooleanValue ^ yBooleanValue, booleanValue);
		} finally {
		end();
		}
	}

	// ! boolean

	public void testNotBoolean() throws Throwable {
		try {
		init();
		IValue value = eval(notOp + xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("not boolean : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("not boolean : wrong result : ", ! xBooleanValue, booleanValue);

		value = eval(notOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("not boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("not boolean : wrong result : ", ! yBooleanValue, booleanValue);
		} finally {
		end();
		}
	}


}
