/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IObject;
import org.eclipse.jdt.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.model.IValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.eval.model.*;
import org.eclipse.jdt.internal.debug.eval.model.EvaluationObject;
import org.eclipse.jdt.internal.debug.eval.model.EvaluationValue;

public class PlusOperator extends BinaryOperator {

	public static final String NULL= "null";

	public PlusOperator(int resultId, int leftTypeId, int rightTypeId, int start) {
		this(resultId, leftTypeId, rightTypeId, false, start);
	}
	
	protected PlusOperator(int resultId, int leftTypeId, int rightTypeId, boolean isAssignmentOperator, int start) {
		super(resultId, leftTypeId, rightTypeId, isAssignmentOperator, start);
	}
	
	private String getString(IValue value, int typeId) {
		
		// test if value == null
		EvaluationValue eValue = (EvaluationValue)value;
		if (eValue.getJavaValue() instanceof JDINullValue) {
			return NULL;
		}
		
		if (value instanceof IObject) {
			EvaluationObject object= (EvaluationObject)value;
			JDIObjectValue javaValue= (JDIObjectValue)object.getJavaObject();

			try {
				return javaValue.getValueString();
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}		
		} else {
			IPrimitiveValue primitiveValue= (IPrimitiveValue)value;
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
	 * @see BinaryOperator#getBooleanResult(IValue, IValue)
	 */
	protected boolean getBooleanResult(IValue leftOperand, IValue rightOperand) {
		return false;
	}

	/*
	 * @see BinaryOperator#getDoubleResult(IValue, IValue)
	 */
	protected double getDoubleResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue)leftOperand).getDoubleValue() + ((IPrimitiveValue)rightOperand).getDoubleValue();
	}

	/*
	 * @see BinaryOperator#getFloatResult(IValue, IValue)
	 */
	protected float getFloatResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue)leftOperand).getFloatValue() + ((IPrimitiveValue)rightOperand).getFloatValue();
	}

	/*
	 * @see BinaryOperator#getIntResult(IValue, IValue)
	 */
	protected int getIntResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue)leftOperand).getIntValue() + ((IPrimitiveValue)rightOperand).getIntValue();
	}

	/*
	 * @see BinaryOperator#getLongResult(IValue, IValue)
	 */
	protected long getLongResult(IValue leftOperand, IValue rightOperand) {
		return ((IPrimitiveValue)leftOperand).getLongValue() + ((IPrimitiveValue)rightOperand).getLongValue();
	}

	/*
	 * @see BinaryOperator#getStringResult(IValue, IValue)
	 */
	protected String getStringResult(IValue leftOperand, IValue rightOperand) {
		return getString(leftOperand, fLeftTypeId) + getString(rightOperand, fRightTypeId);
	}

	public String toString() {
		return "'+' operator";
	}

}
