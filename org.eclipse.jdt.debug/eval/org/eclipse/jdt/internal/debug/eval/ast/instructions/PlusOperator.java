/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;

public class PlusOperator extends BinaryOperator {

	public static final String NULL = "null"; //$NON-NLS-1$
	public static final String TOSTRING_SELECTOR = "toString"; //$NON-NLS-1$
	public static final String TOSTRING_SIGNATURE = "()Ljava/lang/String;"; //$NON-NLS-1$

	public PlusOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected PlusOperator(int resultId, int leftTypeId, int rightTypeId,
			boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
	}

	private String getString(IJavaValue value, int typeId)
			throws DebugException {

		// test if value == null
		if (value instanceof JDINullValue) {
			return NULL;
		}

		if (value instanceof IJavaObject) {
			if (typeId == T_String) {
				return value.getValueString();
			}
			return ((IJavaObject) value).sendMessage(TOSTRING_SELECTOR,
					TOSTRING_SIGNATURE, null, getContext().getThread(), null)
					.getValueString();
		}

		IJavaPrimitiveValue primitiveValue = (IJavaPrimitiveValue) value;
		switch (typeId) {
		case T_boolean:
			return Boolean.toString(primitiveValue.getBooleanValue());
		case T_byte:
			return Integer.toString(primitiveValue.getByteValue());
		case T_char:
			return Character.toString(primitiveValue.getCharValue());
		case T_double:
			return Double.toString(primitiveValue.getDoubleValue());
		case T_float:
			return Float.toString(primitiveValue.getFloatValue());
		case T_int:
			return Integer.toString(primitiveValue.getIntValue());
		case T_long:
			return Long.toString(primitiveValue.getLongValue());
		case T_short:
			return Integer.toString(primitiveValue.getShortValue());
		}
		return NULL;
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected boolean getBooleanResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return false;
	}

	/*
	 * @see BinaryOperator#getDoubleResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected double getDoubleResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getDoubleValue()
				+ ((IJavaPrimitiveValue) rightOperand).getDoubleValue();
	}

	/*
	 * @see BinaryOperator#getFloatResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected float getFloatResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getFloatValue()
				+ ((IJavaPrimitiveValue) rightOperand).getFloatValue();
	}

	/*
	 * @see BinaryOperator#getIntResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected int getIntResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getIntValue()
				+ ((IJavaPrimitiveValue) rightOperand).getIntValue();
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getLongValue()
				+ ((IJavaPrimitiveValue) rightOperand).getLongValue();
	}

	/*
	 * @see BinaryOperator#getStringResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected String getStringResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		return getString(leftOperand, fLeftTypeId)
				+ getString(rightOperand, fRightTypeId);
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.PlusOperator______operator_2;
	}

}
