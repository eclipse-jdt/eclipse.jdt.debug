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

public class GreaterEqualOperator extends BinaryOperator {
	public GreaterEqualOperator(int leftTypeId, int rightTypeId, int start) {
		super(T_boolean, leftTypeId, rightTypeId, false, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected boolean getBooleanResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		switch (getInternResultType()) {
		case T_double:
			return ((IJavaPrimitiveValue) leftOperand).getDoubleValue() >= ((IJavaPrimitiveValue) rightOperand)
					.getDoubleValue();
		case T_float:
			return ((IJavaPrimitiveValue) leftOperand).getFloatValue() >= ((IJavaPrimitiveValue) rightOperand)
					.getFloatValue();
		case T_long:
			return ((IJavaPrimitiveValue) leftOperand).getLongValue() >= ((IJavaPrimitiveValue) rightOperand)
					.getLongValue();
		case T_int:
			return ((IJavaPrimitiveValue) leftOperand).getIntValue() >= ((IJavaPrimitiveValue) rightOperand)
					.getIntValue();
		default:
			return false;
		}
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
		return 0;
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
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
	public String toString() {
		return InstructionsEvaluationMessages.GreaterEqualOperator_operator_1;
	}

}
