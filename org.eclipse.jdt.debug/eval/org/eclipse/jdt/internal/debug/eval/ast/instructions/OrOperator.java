/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;


public class OrOperator extends BinaryOperator {
	public OrOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected OrOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IJavaValue, IJavaValue)
	 */
	protected boolean getBooleanResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getBooleanValue() | ((IJavaPrimitiveValue) rightOperand).getBooleanValue();
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
		return ((IJavaPrimitiveValue) leftOperand).getIntValue() | ((IJavaPrimitiveValue) rightOperand).getIntValue();
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getLongValue() | ((IJavaPrimitiveValue) rightOperand).getLongValue();
	}

	/*
	 * @see BinaryOperator#getStringResult(IJavaValue, IJavaValue)
	 */
	protected String getStringResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return null;
	}

	public String toString() {
		return "'|' operator";
	}

}