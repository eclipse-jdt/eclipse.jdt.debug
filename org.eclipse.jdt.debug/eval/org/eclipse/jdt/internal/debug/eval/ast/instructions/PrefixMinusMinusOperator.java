/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.model.IVariable;

public class PrefixMinusMinusOperator extends XfixOperator {
	
	public PrefixMinusMinusOperator(int variableTypeId, int start) {
		super(variableTypeId, start);
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IVariable variable = (IVariable) pop();
		
		switch (fVariableTypeId) {
			case T_byte :
				variable.setValue(newValue((byte)((IPrimitiveValue)variable.getValue()).getByteValue() - 1));
				break;
			case T_short :
				variable.setValue(newValue((short)((IPrimitiveValue)variable.getValue()).getShortValue() - 1));
				break;
			case T_char :
				variable.setValue(newValue((char)((IPrimitiveValue)variable.getValue()).getCharValue() - 1));
				break;
			case T_int :
				variable.setValue(newValue(((IPrimitiveValue)variable.getValue()).getIntValue() - 1));
				break;
			case T_long :
				variable.setValue(newValue(((IPrimitiveValue)variable.getValue()).getLongValue() - 1));
				break;
			case T_float :
				variable.setValue(newValue(((IPrimitiveValue)variable.getValue()).getFloatValue() - 1));
				break;
			case T_double :
				variable.setValue(newValue(((IPrimitiveValue)variable.getValue()).getDoubleValue() - 1));
				break;
		}

		push(variable.getValue());
	}

	public String toString() {
		return "prefix '--' operator";
	}

}
