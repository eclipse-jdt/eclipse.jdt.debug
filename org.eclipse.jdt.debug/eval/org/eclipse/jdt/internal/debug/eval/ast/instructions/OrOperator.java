/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.model.IValue;

public class OrOperator extends BinaryOperator {
	public OrOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected OrOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IValue, IValue)
	 */
	protected boolean getBooleanResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue) leftOperand).getBooleanValue() | ((IPrimitiveValue) rightOperand).getBooleanValue();
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
		return ((IPrimitiveValue) leftOperand).getIntValue() | ((IPrimitiveValue) rightOperand).getIntValue();
	}

	/*
	 * @see BinaryOperator#getLongResult(IValue, IValue)
	 */
	protected long getLongResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue) leftOperand).getLongValue() | ((IPrimitiveValue) rightOperand).getLongValue();
	}

	/*
	 * @see BinaryOperator#getStringResult(IValue, IValue)
	 */
	protected String getStringResult(IValue leftOperand, IValue rightOperand) {
		return null;
	}

	public String toString() {
		return "'|' operator";
	}

}