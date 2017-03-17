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
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

public abstract class BinaryOperator extends CompoundInstruction {
	protected int fResultTypeId;
	protected int fLeftTypeId;
	protected int fRightTypeId;
	protected boolean fIsAssignmentOperator;

	protected BinaryOperator(int resultId, int leftTypeId, int rightTypeId,
			boolean isAssignementOperator, int start) {
		super(start);
		fResultTypeId = resultId;
		fLeftTypeId = leftTypeId;
		fRightTypeId = rightTypeId;
		fIsAssignmentOperator = isAssignementOperator;
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	final public void execute() throws CoreException {
		if (fIsAssignmentOperator) {
			executeAssignment();
		} else {
			executeBinary();
		}
	}

	private void executeAssignment() throws CoreException {
		IJavaValue value = popValue();
		IJavaVariable variable = (IJavaVariable) pop();
		IJavaValue variableValue = (IJavaValue) variable.getValue();

		switch (fResultTypeId) {
		case T_byte:
			variableValue = getByteValueResult(variableValue, value);
			break;
		case T_short:
			variableValue = getShortValueResult(variableValue, value);
			break;
		case T_char:
			variableValue = getCharValueResult(variableValue, value);
			break;
		case T_int:
			variableValue = getIntValueResult(variableValue, value);
			break;
		case T_long:
			variableValue = getLongValueResult(variableValue, value);
			break;
		case T_float:
			variableValue = getFloatValueResult(variableValue, value);
			break;
		case T_double:
			variableValue = getDoubleValueResult(variableValue, value);
			break;
		case T_boolean:
			variableValue = getBooleanValueResult(variableValue, value);
			break;
		case T_String:
			variableValue = getStringValueResult(variableValue, value);
			break;
		}
		variable.setValue(variableValue);
		push(variableValue);
	}

	private void executeBinary() throws CoreException {
		IJavaValue right = popValue();
		IJavaValue left = popValue();

		switch (fResultTypeId) {
		case T_String:
			pushNewValue(getStringResult(left, right));
			break;
		case T_double:
			pushNewValue(getDoubleResult(left, right));
			break;
		case T_float:
			pushNewValue(getFloatResult(left, right));
			break;
		case T_long:
			pushNewValue(getLongResult(left, right));
			break;
		case T_int:
			pushNewValue(getIntResult(left, right));
			break;
		case T_boolean:
			pushNewValue(getBooleanResult(left, right));
			break;
		}
	}

	private IJavaValue getByteValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
		case T_double:
			return newValue((byte) getDoubleResult(leftOperand, rightOperand));
		case T_float:
			return newValue((byte) getFloatResult(leftOperand, rightOperand));
		case T_long:
			return newValue((byte) getLongResult(leftOperand, rightOperand));
		case T_int:
			return newValue((byte) getIntResult(leftOperand, rightOperand));
		default:
			return null;
		}
	}

	private IJavaValue getShortValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
		case T_double:
			return newValue((short) getDoubleResult(leftOperand, rightOperand));
		case T_float:
			return newValue((short) getFloatResult(leftOperand, rightOperand));
		case T_long:
			return newValue((short) getLongResult(leftOperand, rightOperand));
		case T_int:
			return newValue((short) getIntResult(leftOperand, rightOperand));
		default:
			return null;
		}
	}

	private IJavaValue getCharValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
		case T_double:
			return newValue((char) getDoubleResult(leftOperand, rightOperand));
		case T_float:
			return newValue((char) getFloatResult(leftOperand, rightOperand));
		case T_long:
			return newValue((char) getLongResult(leftOperand, rightOperand));
		case T_int:
			return newValue((char) getIntResult(leftOperand, rightOperand));
		default:
			return null;
		}
	}

	private IJavaValue getIntValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
		case T_double:
			return newValue((int) getDoubleResult(leftOperand, rightOperand));
		case T_float:
			return newValue((int) getFloatResult(leftOperand, rightOperand));
		case T_long:
			return newValue((int) getLongResult(leftOperand, rightOperand));
		case T_int:
			return newValue(getIntResult(leftOperand, rightOperand));
		default:
			return null;
		}
	}

	private IJavaValue getLongValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
		case T_double:
			return newValue((long) getDoubleResult(leftOperand, rightOperand));
		case T_float:
			return newValue((long) getFloatResult(leftOperand, rightOperand));
		case T_long:
			return newValue(getLongResult(leftOperand, rightOperand));
		case T_int:
			return newValue((long) getIntResult(leftOperand, rightOperand));
		default:
			return null;
		}
	}

	private IJavaValue getFloatValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
		case T_double:
			return newValue((float) getDoubleResult(leftOperand, rightOperand));
		case T_float:
			return newValue(getFloatResult(leftOperand, rightOperand));
		case T_long:
			return newValue((float) getLongResult(leftOperand, rightOperand));
		case T_int:
			return newValue((float) getIntResult(leftOperand, rightOperand));
		default:
			return null;
		}
	}

	private IJavaValue getDoubleValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
		case T_double:
			return newValue(getDoubleResult(leftOperand, rightOperand));
		case T_float:
			return newValue((double) getFloatResult(leftOperand, rightOperand));
		case T_long:
			return newValue((double) getLongResult(leftOperand, rightOperand));
		case T_int:
			return newValue((double) getIntResult(leftOperand, rightOperand));
		default:
			return null;
		}
	}

	private IJavaValue getBooleanValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) {
		return newValue(getBooleanResult(leftOperand, rightOperand));
	}

	private IJavaValue getStringValueResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException {
		return newValue(getStringResult(leftOperand, rightOperand));
	}

	protected abstract int getIntResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException;

	protected abstract long getLongResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException;

	protected abstract float getFloatResult(IJavaValue leftOperand,
			IJavaValue rightOperand);

	protected abstract double getDoubleResult(IJavaValue leftOperand,
			IJavaValue rightOperand);

	protected abstract boolean getBooleanResult(IJavaValue leftOperand,
			IJavaValue rightOperand);

	protected abstract String getStringResult(IJavaValue leftOperand,
			IJavaValue rightOperand) throws CoreException;

	protected int getInternResultType() {
		return getBinaryPromotionType(fLeftTypeId, fRightTypeId);
	}

}
