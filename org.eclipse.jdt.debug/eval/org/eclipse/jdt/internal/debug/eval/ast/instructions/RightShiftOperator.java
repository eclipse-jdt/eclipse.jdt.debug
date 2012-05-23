/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;

public class RightShiftOperator extends BinaryOperator {
	public RightShiftOperator(int resultId, int leftTypeId, int rightTypeId,
			int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected RightShiftOperator(int resultId, int leftTypeId, int rightTypeId,
			boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
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
		return 0;
	}

	/*
	 * @see BinaryOperator#getFloatResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected float getFloatResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getIntResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected int getIntResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		// unary type promotion on both operands see 5.6.1 and 15.18
		switch (fRightTypeId) {
		case T_long:
			return ((IJavaPrimitiveValue) leftOperand).getIntValue() >> ((IJavaPrimitiveValue) rightOperand)
					.getLongValue();
		case T_int:
		case T_short:
		case T_byte:
		case T_char:
			return ((IJavaPrimitiveValue) leftOperand).getIntValue() >> ((IJavaPrimitiveValue) rightOperand)
					.getIntValue();
		default:
			return 0;
		}
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		// unary type promotion on both operands see 5.6.1 and 15.18
		switch (fRightTypeId) {
		case T_long:
			return ((IJavaPrimitiveValue) leftOperand).getLongValue() >> ((IJavaPrimitiveValue) rightOperand)
					.getLongValue();
		case T_int:
		case T_short:
		case T_byte:
		case T_char:
			return ((IJavaPrimitiveValue) leftOperand).getLongValue() >> ((IJavaPrimitiveValue) rightOperand)
					.getIntValue();
		default:
			return 0;
		}
	}

	/*
	 * @see BinaryOperator#getStringResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected String getStringResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return null;
	}

	@Override
	protected int getInternResultType() {
		// unary type promotion on both operands see 5.6.1 and 15.18
		return getUnaryPromotionType(fLeftTypeId);
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.RightShiftOperator_______operator_1;
	}

}
