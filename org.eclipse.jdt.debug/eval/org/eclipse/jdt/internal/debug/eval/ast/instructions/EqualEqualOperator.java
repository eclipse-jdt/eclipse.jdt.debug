/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;

public class EqualEqualOperator extends BinaryOperator {
	
	private boolean fIsEquals;

	public EqualEqualOperator(int leftTypeId, int rightTypeId, boolean isEquals, int start) {
		super(T_boolean, leftTypeId, rightTypeId, false, start);
		fIsEquals= isEquals;
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IJavaValue, IJavaValue)
	 */
	protected boolean getBooleanResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		boolean equals= false;
		switch (getInternResultType()) {
			case T_double :
				equals= ((IJavaPrimitiveValue) leftOperand).getDoubleValue() == ((IJavaPrimitiveValue) rightOperand).getDoubleValue();
				break;
			case T_float :
				equals= ((IJavaPrimitiveValue) leftOperand).getFloatValue() == ((IJavaPrimitiveValue) rightOperand).getFloatValue();
				break;
			case T_long :
				equals= ((IJavaPrimitiveValue) leftOperand).getLongValue() == ((IJavaPrimitiveValue) rightOperand).getLongValue();
				break;
			case T_int :
				if (leftOperand instanceof IJavaObject) {
					// enumerations in switch statement
					equals = leftOperand.equals(rightOperand);
				} else {
					equals= ((IJavaPrimitiveValue) leftOperand).getIntValue() == ((IJavaPrimitiveValue) rightOperand).getIntValue();
				}
				break;
			case T_boolean :
				equals= ((IJavaPrimitiveValue) leftOperand).getBooleanValue() == ((IJavaPrimitiveValue) rightOperand).getBooleanValue();
				break;
			default :
				equals= leftOperand.equals(rightOperand);
				break;
		}
		return ((fIsEquals) ? equals : !equals);
	}

	/*
	 * @see BinaryOperator#getDoubleResult(IJavaValue, IJavaValue)
	 */
	protected double getDoubleResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getFloatResult(IJavaValue, IJavaValue)
	 */
	protected float getFloatResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getIntResult(IJavaValue, IJavaValue)
	 */
	protected int getIntResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getStringResult(IJavaValue, IJavaValue)
	 */
	protected String getStringResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return null;
	}

	public String toString() {
		return InstructionsEvaluationMessages.EqualEqualOperator_operator_1; 
	}

}
