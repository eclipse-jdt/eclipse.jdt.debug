/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.internal.debug.eval.model.IValue;

public class RemainderOperator extends BinaryOperator {
	public RemainderOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected RemainderOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignmentOperator, int start) {
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
		return ((IPrimitiveValue) leftOperand).getDoubleValue() % ((IPrimitiveValue) rightOperand).getDoubleValue();
	}

	/*
	 * @see BinaryOperator#getFloatResult(IValue, IValue)
	 */
	protected float getFloatResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue) leftOperand).getFloatValue() % ((IPrimitiveValue) rightOperand).getFloatValue();
	}

	/*
	 * @see BinaryOperator#getIntResult(IValue, IValue)
	 */
	protected int getIntResult(IValue leftOperand, IValue rightOperand) throws CoreException {
		int divisor= ((IPrimitiveValue) rightOperand).getIntValue();
		if (divisor == 0) {
			throw new CoreException(new Status(Status.ERROR, "", Status.OK, "Divide by zero", null));
		}
		return ((IPrimitiveValue) leftOperand).getIntValue() % divisor;
	}

	/*
	 * @see BinaryOperator#getLongResult(IValue, IValue)
	 */
	protected long getLongResult(IValue leftOperand, IValue rightOperand) throws CoreException {
		long divisor= ((IPrimitiveValue) rightOperand).getLongValue();
		if (divisor == 0) {
			throw new CoreException(new Status(Status.ERROR, "", Status.OK, "Divide by zero", null));
		}
		return ((IPrimitiveValue) leftOperand).getLongValue() % divisor;
	}

	/*
	 * @see BinaryOperator#getStringResult(IValue, IValue)
	 */
	protected String getStringResult(IValue leftOperand, IValue rightOperand) {
		return null;
	}

	public String toString() {
		return "'%' operator";
	}

}