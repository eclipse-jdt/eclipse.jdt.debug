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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

public class RemainderOperator extends BinaryOperator {
	public RemainderOperator(int resultId, int leftTypeId, int rightTypeId,
			int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}

	protected RemainderOperator(int resultId, int leftTypeId, int rightTypeId,
			boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected boolean getBooleanResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return false;
	}

	/*
	 * @see BinaryOperator#getDoubleResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected double getDoubleResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getDoubleValue()
				% ((IJavaPrimitiveValue) rightOperand).getDoubleValue();
	}

	/*
	 * @see BinaryOperator#getFloatResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected float getFloatResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue) leftOperand).getFloatValue()
				% ((IJavaPrimitiveValue) rightOperand).getFloatValue();
	}

	/*
	 * @see BinaryOperator#getIntResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected int getIntResult(IJavaValue leftOperand, IJavaValue rightOperand)
			throws CoreException {
		int divisor = ((IJavaPrimitiveValue) rightOperand).getIntValue();
		if (divisor == 0) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							IStatus.OK,
							InstructionsEvaluationMessages.RemainderOperator_Divide_by_zero_1,
							null));
		}
		return ((IJavaPrimitiveValue) leftOperand).getIntValue() % divisor;
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	@Override
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand)
			throws CoreException {
		long divisor = ((IJavaPrimitiveValue) rightOperand).getLongValue();
		if (divisor == 0) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							IStatus.OK,
							InstructionsEvaluationMessages.RemainderOperator_Divide_by_zero_2,
							null));
		}
		return ((IJavaPrimitiveValue) leftOperand).getLongValue() % divisor;
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
		return InstructionsEvaluationMessages.RemainderOperator______operator_3;
	}

}
