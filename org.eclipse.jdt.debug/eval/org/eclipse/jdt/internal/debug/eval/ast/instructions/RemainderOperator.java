/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;

public class RemainderOperator extends BinaryOperator {
	public RemainderOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected RemainderOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IJavaValue, IJavaValue)
	 */
	protected boolean getBooleanResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return false;
	}

	/*
	 * @see BinaryOperator#getDoubleResult(IJavaValue, IJavaValue)
	 */
	protected double getDoubleResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getDoubleValue() % ((IJavaPrimitiveValue) rightOperand).getDoubleValue();
	}

	/*
	 * @see BinaryOperator#getFloatResult(IJavaValue, IJavaValue)
	 */
	protected float getFloatResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getFloatValue() % ((IJavaPrimitiveValue) rightOperand).getFloatValue();
	}

	/*
	 * @see BinaryOperator#getIntResult(IJavaValue, IJavaValue)
	 */
	protected int getIntResult(IJavaValue leftOperand, IJavaValue rightOperand) throws CoreException {
		int divisor= ((IJavaPrimitiveValue) rightOperand).getIntValue();
		if (divisor == 0) {
			throw new CoreException(new Status(Status.ERROR, JDIDebugModel.getPluginIdentifier(), Status.OK, InstructionsEvaluationMessages.getString("RemainderOperator.Divide_by_zero_1"), null)); //$NON-NLS-1$
		}
		return ((IJavaPrimitiveValue) leftOperand).getIntValue() % divisor;
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand) throws CoreException {
		long divisor= ((IJavaPrimitiveValue) rightOperand).getLongValue();
		if (divisor == 0) {
			throw new CoreException(new Status(Status.ERROR, JDIDebugModel.getPluginIdentifier(), Status.OK, InstructionsEvaluationMessages.getString("RemainderOperator.Divide_by_zero_2"), null)); //$NON-NLS-1$
		}
		return ((IJavaPrimitiveValue) leftOperand).getLongValue() % divisor;
	}

	/*
	 * @see BinaryOperator#getStringResult(IJavaValue, IJavaValue)
	 */
	protected String getStringResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return null;
	}

	public String toString() {
		return InstructionsEvaluationMessages.getString("RemainderOperator._%___operator_3"); //$NON-NLS-1$
	}

}