/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.jdt.debug.eval.ast.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.ast.model.IValue;

/**
 * @version 	1.0
 * @author
 */
public class AndOperator extends BinaryOperator {
	public AndOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected AndOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignmentOperateur, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperateur, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IValue, IValue)
	 */
	protected boolean getBooleanResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue) leftOperand).getBooleanValue() & ((IPrimitiveValue) rightOperand).getBooleanValue();
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
		return ((IPrimitiveValue) leftOperand).getIntValue() & ((IPrimitiveValue) rightOperand).getIntValue();
	}

	/*
	 * @see BinaryOperator#getLongResult(IValue, IValue)
	 */
	protected long getLongResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue) leftOperand).getLongValue() & ((IPrimitiveValue) rightOperand).getLongValue();
	}

	/*
	 * @see BinaryOperator#getStringResult(IValue, IValue)
	 */
	protected String getStringResult(IValue leftOperand, IValue rightOperand) {
		return null;
	}

	public String toString() {
		return "'&' operator";
	}

}