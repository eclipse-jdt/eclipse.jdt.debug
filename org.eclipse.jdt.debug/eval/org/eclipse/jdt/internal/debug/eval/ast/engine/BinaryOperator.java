/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.ast.model.IValue;
import org.eclipse.jdt.debug.eval.ast.model.IVariable;

/**
 * @version 	1.0
 * @author
 */
public abstract class BinaryOperator extends CompoundInstruction {
	protected int fResultTypeId;
	protected int fLeftTypeId;
	protected int fRightTypeId;
	protected boolean fIsAssignmentOperator;

	protected BinaryOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignementOperator, int start) {
		super(start);
		fResultTypeId= resultId;
		fLeftTypeId= leftTypeId;
		fRightTypeId= rightTypeId;
		fIsAssignmentOperator= isAssignementOperator;
	}
	
	/*
	 * @see Instruction#execute()
	 */
	final public void execute() throws CoreException {
		if (fIsAssignmentOperator) {
			executeAssignment();
		} else {
			executeBinary();
		}
	}
	
	private void executeAssignment() throws CoreException {
		IValue value = (IValue) popValue();
		IVariable variable = (IVariable) pop();
		IValue variableValue = variable.getValue();
		
		switch (fResultTypeId) {
			case T_byte:
				variableValue= getByteValueResult(variableValue, value);
				break;
			case T_short:
				variableValue= getShortValueResult(variableValue, value);
				break;
			case T_char:
				variableValue= getCharValueResult(variableValue, value);
				break;
			case T_int:
				variableValue= getIntValueResult(variableValue, value);
				break;
			case T_long:
				variableValue= getLongValueResult(variableValue, value);
				break;
			case T_float:
				variableValue= getFloatValueResult(variableValue, value);
				break;
			case T_double:
				variableValue= getDoubleValueResult(variableValue, value);
				break;
			case T_boolean:
				variableValue= getBooleanValueResult(variableValue, value);
				break;
			case T_String:
				variableValue= getStringValueResult(variableValue, value);
				break;
		}
		variable.setValue(variableValue);
		push(variableValue);
	}
	
	private void executeBinary() throws CoreException {
		IValue right= (IValue)popValue();
		IValue left= (IValue)popValue();

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
	
	private IValue getByteValueResult(IValue leftOperand, IValue rightOperand) throws CoreException {
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
	
	private IValue getShortValueResult(IValue leftOperand, IValue rightOperand) throws CoreException {
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
	
	private IValue getCharValueResult(IValue leftOperand, IValue rightOperand) throws CoreException {
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
	
	private IValue getIntValueResult(IValue leftOperand, IValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
			case T_double:
				return newValue((int) getDoubleResult(leftOperand, rightOperand));
			case T_float:
				return newValue((int) getFloatResult(leftOperand, rightOperand));
			case T_long:
				return newValue((int) getLongResult(leftOperand, rightOperand));
			case T_int:
				return newValue((int) getIntResult(leftOperand, rightOperand));
			default:
				return null;
		}
	}
	
	private IValue getLongValueResult(IValue leftOperand, IValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
			case T_double:
				return newValue((long) getDoubleResult(leftOperand, rightOperand));
			case T_float:
				return newValue((long) getFloatResult(leftOperand, rightOperand));
			case T_long:
				return newValue((long) getLongResult(leftOperand, rightOperand));
			case T_int:
				return newValue((long) getIntResult(leftOperand, rightOperand));
			default:
				return null;
		}
	}
	
	private IValue getFloatValueResult(IValue leftOperand, IValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
			case T_double:
				return newValue((float) getDoubleResult(leftOperand, rightOperand));
			case T_float:
				return newValue((float) getFloatResult(leftOperand, rightOperand));
			case T_long:
				return newValue((float) getLongResult(leftOperand, rightOperand));
			case T_int:
				return newValue((float) getIntResult(leftOperand, rightOperand));
			default:
				return null;
		}
	}
	
	private IValue getDoubleValueResult(IValue leftOperand, IValue rightOperand) throws CoreException {
		switch (getInternResultType()) {
			case T_double:
				return newValue((double) getDoubleResult(leftOperand, rightOperand));
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
	
	private IValue getBooleanValueResult(IValue leftOperand, IValue rightOperand) {
		return newValue(getBooleanResult(leftOperand, rightOperand));
	}
	
	private IValue getStringValueResult(IValue leftOperand, IValue rightOperand) {
		return newValue(getStringResult(leftOperand, rightOperand));
	}
	
	protected abstract int getIntResult(IValue leftOperand, IValue rightOperand) throws CoreException;
	
	protected abstract long getLongResult(IValue leftOperand, IValue rightOperand) throws CoreException;

	protected abstract float getFloatResult(IValue leftOperand, IValue rightOperand);

	protected abstract double getDoubleResult(IValue leftOperand, IValue rightOperand);

	protected abstract boolean getBooleanResult(IValue leftOperand, IValue rightOperand);

	protected abstract String getStringResult(IValue leftOperand, IValue rightOperand);

	protected int getInternResultType() {
		return getBinaryPromotionType(fLeftTypeId, fRightTypeId);
	}

}
