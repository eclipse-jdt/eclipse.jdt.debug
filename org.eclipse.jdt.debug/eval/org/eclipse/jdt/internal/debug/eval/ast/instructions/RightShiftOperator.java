/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.model.IValue;

/**
 * @version 	1.0
 * @author
 */
public class RightShiftOperator extends BinaryOperator {
	public RightShiftOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected RightShiftOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IValue, IValue)
	 */
	protected boolean getBooleanResult(IValue leftOperand, IValue rightOperand) {
		return false;
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
		// unary type promotion on both operands see 5.6.1 and 15.18
		switch (fRightTypeId) {
			case T_long :
				return ((IPrimitiveValue) leftOperand).getIntValue() >> ((IPrimitiveValue) rightOperand).getLongValue();
			case T_int :
			case T_short :
			case T_byte :
			case T_char :
				return ((IPrimitiveValue) leftOperand).getIntValue() >> ((IPrimitiveValue) rightOperand).getIntValue();
			default :
				return 0;
		}
	}

	/*
	 * @see BinaryOperator#getLongResult(IValue, IValue)
	 */
	protected long getLongResult(IValue leftOperand, IValue rightOperand) {
		// unary type promotion on both operands see 5.6.1 and 15.18
		switch (fRightTypeId) {
			case T_long :
				return ((IPrimitiveValue) leftOperand).getLongValue() >> ((IPrimitiveValue) rightOperand).getLongValue();
			case T_int :
			case T_short :
			case T_byte :
			case T_char :
				return ((IPrimitiveValue) leftOperand).getLongValue() >> ((IPrimitiveValue) rightOperand).getIntValue();
			default :
				return 0;
		}
	}

	/*
	 * @see BinaryOperator#getStringResult(IValue, IValue)
	 */
	protected String getStringResult(IValue leftOperand, IValue rightOperand) {
		return null;
	}

	protected int getInternResultType() {
		// unary type promotion on both operands see 5.6.1 and 15.18
		return getUnaryPromotionType(fLeftTypeId);
	}

	public String toString() {
		return "'>>' operator";
	}

}