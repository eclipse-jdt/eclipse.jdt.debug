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

public class BooleanAssignmentOperatorsTests extends Tests {

	public BooleanAssignmentOperatorsTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 40, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// boolean |= boolean

	public void testBooleanOrAssignmentBoolean() throws Throwable {
		try {
		init();
		boolean tmpxVar = xVarBooleanValue;
		tmpxVar |= xBooleanValue;
		IValue value = eval(xVarBoolean + orAssignmentOp + xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean orAssignment boolean : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean orAssignment boolean : wrong result : ", tmpxVar, booleanValue);
		value = eval(xVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpxVar, booleanValue);

		tmpxVar |= yBooleanValue;
		value = eval(xVarBoolean + orAssignmentOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean orAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean orAssignment boolean : wrong result : ", tmpxVar, booleanValue);
		value = eval(xVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpxVar, booleanValue);

		boolean tmpyVar = yVarBooleanValue;
		tmpyVar |= xBooleanValue;
		value = eval(yVarBoolean + orAssignmentOp + xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean orAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean orAssignment boolean : wrong result : ", tmpyVar, booleanValue);
		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpyVar, booleanValue);

		tmpyVar |= yBooleanValue;
		value = eval(yVarBoolean + orAssignmentOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean orAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean orAssignment boolean : wrong result : ", tmpyVar, booleanValue);
		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpyVar, booleanValue);
		} finally {
		end();
		}
	}

	// boolean &= boolean

	public void testBooleanAndAssignmentBoolean() throws Throwable {
		try {
		init();
		boolean tmpxVar = xVarBooleanValue;
		tmpxVar &= xBooleanValue;
		IValue value = eval(xVarBoolean + andAssignmentOp + xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean andAssignment boolean : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean andAssignment boolean : wrong result : ", tmpxVar, booleanValue);
		value = eval(xVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpxVar, booleanValue);

		tmpxVar &= yBooleanValue;
		value = eval(xVarBoolean + andAssignmentOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean andAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean andAssignment boolean : wrong result : ", tmpxVar, booleanValue);
		value = eval(xVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpxVar, booleanValue);

		boolean tmpyVar = yVarBooleanValue;
		tmpyVar &= xBooleanValue;
		value = eval(yVarBoolean + andAssignmentOp + xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean andAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean andAssignment boolean : wrong result : ", tmpyVar, booleanValue);
		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpyVar, booleanValue);

		tmpyVar &= yBooleanValue;
		value = eval(yVarBoolean + andAssignmentOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean andAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean andAssignment boolean : wrong result : ", tmpyVar, booleanValue);
		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpyVar, booleanValue);
		} finally {
		end();
		}
	}

	// boolean ^= boolean

	public void testBooleanXorAssignmentBoolean() throws Throwable {
		try {
		init();
		boolean tmpxVar = xVarBooleanValue;
		tmpxVar ^= xBooleanValue;
		IValue value = eval(xVarBoolean + xorAssignmentOp + xBoolean);
		String typeName = value.getReferenceTypeName();
		assertEquals("boolean xorAssignment boolean : wrong type : ", "boolean", typeName);
		boolean booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean xorAssignment boolean : wrong result : ", tmpxVar, booleanValue);
		value = eval(xVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpxVar, booleanValue);

		tmpxVar ^= yBooleanValue;
		value = eval(xVarBoolean + xorAssignmentOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean xorAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean xorAssignment boolean : wrong result : ", tmpxVar, booleanValue);
		value = eval(xVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpxVar, booleanValue);

		boolean tmpyVar = yVarBooleanValue;
		tmpyVar ^= xBooleanValue;
		value = eval(yVarBoolean + xorAssignmentOp + xBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean xorAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean xorAssignment boolean : wrong result : ", tmpyVar, booleanValue);
		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpyVar, booleanValue);

		tmpyVar ^= yBooleanValue;
		value = eval(yVarBoolean + xorAssignmentOp + yBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean xorAssignment boolean : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean xorAssignment boolean : wrong result : ", tmpyVar, booleanValue);
		value = eval(yVarBoolean);
		typeName = value.getReferenceTypeName();
		assertEquals("boolean local variable value : wrong type : ", "boolean", typeName);
		booleanValue = ((IJavaPrimitiveValue)value).getBooleanValue();
		assertEquals("boolean local variable value : wrong result : ", tmpyVar, booleanValue);
		} finally {
		end();
		}
	}


}
