/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;

public class PlusOperator extends BinaryOperator {

	public static final String NULL= "null"; //$NON-NLS-1$

	public PlusOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}
	
	protected PlusOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
	}
	
	private String getString(IJavaValue value, int typeId) {
		
		// test if value == null
		if (value instanceof JDINullValue) {
			return NULL;
		}
		
		if (value instanceof IJavaObject) {
			try {
				return value.getValueString();
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}		
		} else {
			IJavaPrimitiveValue primitiveValue= (IJavaPrimitiveValue)value;
			switch (typeId) {
				case T_boolean:
					return new Boolean(primitiveValue.getBooleanValue()).toString();
				case T_byte:
					return new Integer(primitiveValue.getByteValue()).toString();
				case T_char:
					return new Character(primitiveValue.getCharValue()).toString();
				case T_double:
					return new Double(primitiveValue.getDoubleValue()).toString();
				case T_float:
					return new Float(primitiveValue.getFloatValue()).toString();
				case T_int:
					return new Integer(primitiveValue.getIntValue()).toString();
				case T_long:
					return new Long(primitiveValue.getLongValue()).toString();
				case T_short:
					return new Integer(primitiveValue.getShortValue()).toString();
			}
		}
		return NULL;
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
		return ((IJavaPrimitiveValue)leftOperand).getDoubleValue() + ((IJavaPrimitiveValue)rightOperand).getDoubleValue();
	}

	/*
	 * @see BinaryOperator#getFloatResult(IJavaValue, IJavaValue)
	 */
	protected float getFloatResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue)leftOperand).getFloatValue() + ((IJavaPrimitiveValue)rightOperand).getFloatValue();
	}

	/*
	 * @see BinaryOperator#getIntResult(IJavaValue, IJavaValue)
	 */
	protected int getIntResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue)leftOperand).getIntValue() + ((IJavaPrimitiveValue)rightOperand).getIntValue();
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return ((IJavaPrimitiveValue)leftOperand).getLongValue() + ((IJavaPrimitiveValue)rightOperand).getLongValue();
	}

	/*
	 * @see BinaryOperator#getStringResult(IJavaValue, IJavaValue)
	 */
	protected String getStringResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return getString(leftOperand, fLeftTypeId) + getString(rightOperand, fRightTypeId);
	}

	public String toString() {
		return EvalMessages.getString("PlusOperator._+___operator_2"); //$NON-NLS-1$
	}

}
