/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.internal.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.internal.debug.eval.model.IValue;

public class GreaterEqualOperator extends BinaryOperator {
	public GreaterEqualOperator(int leftTypeId, int rightTypeId, int start) {
		super(T_boolean, leftTypeId, rightTypeId, false, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IValue, IValue)
	 */
	protected boolean getBooleanResult(IValue leftOperand, IValue rightOperand) {
		switch (getInternResultType()) {
			case T_double :
				return ((IPrimitiveValue) leftOperand).getDoubleValue() >= ((IPrimitiveValue) rightOperand).getDoubleValue();
			case T_float :
				return ((IPrimitiveValue) leftOperand).getFloatValue() >= ((IPrimitiveValue) rightOperand).getFloatValue();
			case T_long :
				return ((IPrimitiveValue) leftOperand).getLongValue() >= ((IPrimitiveValue) rightOperand).getLongValue();
			case T_int :
				return ((IPrimitiveValue) leftOperand).getIntValue() >= ((IPrimitiveValue) rightOperand).getIntValue();
			default :
				return false;
		}
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
		return "'>=' operator";
	}

}