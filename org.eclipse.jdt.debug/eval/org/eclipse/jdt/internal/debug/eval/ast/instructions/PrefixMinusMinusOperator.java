/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

public class PrefixMinusMinusOperator extends XfixOperator {
	
	public PrefixMinusMinusOperator(int variableTypeId, int start) {
		super(variableTypeId, start);
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaVariable variable = (IJavaVariable) pop();
		
		switch (fVariableTypeId) {
			case T_byte :
				variable.setValue(newValue((byte)((IJavaPrimitiveValue)variable.getValue()).getByteValue() - 1));
				break;
			case T_short :
				variable.setValue(newValue((short)((IJavaPrimitiveValue)variable.getValue()).getShortValue() - 1));
				break;
			case T_char :
				variable.setValue(newValue((char)((IJavaPrimitiveValue)variable.getValue()).getCharValue() - 1));
				break;
			case T_int :
				variable.setValue(newValue(((IJavaPrimitiveValue)variable.getValue()).getIntValue() - 1));
				break;
			case T_long :
				variable.setValue(newValue(((IJavaPrimitiveValue)variable.getValue()).getLongValue() - 1));
				break;
			case T_float :
				variable.setValue(newValue(((IJavaPrimitiveValue)variable.getValue()).getFloatValue() - 1));
				break;
			case T_double :
				variable.setValue(newValue(((IJavaPrimitiveValue)variable.getValue()).getDoubleValue() - 1));
				break;
		}

		push(variable.getValue());
	}

	public String toString() {
		return EvalMessages.getString("PrefixMinusMinusOperator.prefix___--___operator_1"); //$NON-NLS-1$
	}

}
