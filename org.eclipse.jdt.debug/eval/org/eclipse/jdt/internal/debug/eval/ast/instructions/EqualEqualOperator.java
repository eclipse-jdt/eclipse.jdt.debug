/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.internal.debug.eval.model.*;
import org.eclipse.jdt.internal.debug.eval.model.EvaluationValue;

public class EqualEqualOperator extends BinaryOperator {
	
	private boolean fIsEquals;

	public EqualEqualOperator(int leftTypeId, int rightTypeId, boolean isEquals, int start) {
		super(T_boolean, leftTypeId, rightTypeId, false, start);
		fIsEquals= isEquals;
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IValue, IValue)
	 */
	protected boolean getBooleanResult(IValue leftOperand, IValue rightOperand) {
		boolean equals= false;
		switch (getInternResultType()) {
			case T_double :
				equals= ((IPrimitiveValue) leftOperand).getDoubleValue() == ((IPrimitiveValue) rightOperand).getDoubleValue();
				break;
			case T_float :
				equals= ((IPrimitiveValue) leftOperand).getFloatValue() == ((IPrimitiveValue) rightOperand).getFloatValue();
				break;
			case T_long :
				equals= ((IPrimitiveValue) leftOperand).getLongValue() == ((IPrimitiveValue) rightOperand).getLongValue();
				break;
			case T_int :
				equals= ((IPrimitiveValue) leftOperand).getIntValue() == ((IPrimitiveValue) rightOperand).getIntValue();
				break;
			case T_boolean :
				equals= ((IPrimitiveValue) leftOperand).getBooleanValue() == ((IPrimitiveValue) rightOperand).getBooleanValue();
				break;
			default :
				if (leftOperand instanceof EvaluationValue && rightOperand instanceof EvaluationValue) {
					equals= ((EvaluationValue) leftOperand).getJavaValue().equals(((EvaluationValue) rightOperand).getJavaValue());
				}
				break;
		}
		return ((fIsEquals) ? equals : !equals);
	}

	/*
	 * @see BinaryOperator#getDoubleResult(IValue, IValue)
	 */
	protected double getDoubleResult(IValue leftOperand, IValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getFloatResult(IValue, IValue)
	 */
	protected float getFloatResult(IValue leftOperand, IValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getIntResult(IValue, IValue)
	 */
	protected int getIntResult(IValue leftOperand, IValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getLongResult(IValue, IValue)
	 */
	protected long getLongResult(IValue leftOperand, IValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getStringResult(IValue, IValue)
	 */
	protected String getStringResult(IValue leftOperand, IValue rightOperand) {
		return null;
	}

	public String toString() {
		return "'==' operator";
	}

}