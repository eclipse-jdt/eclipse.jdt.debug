/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

public class UnaryMinusOperator extends UnaryOperator {

	public UnaryMinusOperator(int expressionTypeId, int start) {
		super(expressionTypeId, start);
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaPrimitiveValue value= (IJavaPrimitiveValue)popValue();
		switch (fExpressionTypeId) {
			case T_double:
				pushNewValue(-value.getDoubleValue());
				break;
			case T_float:
				pushNewValue(-value.getFloatValue());
				break;
			case T_long:
				pushNewValue(-value.getLongValue());
				break;
			case T_byte:
			case T_short:
			case T_int:
			case T_char:
				pushNewValue(-value.getIntValue());
				break;
		}
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return EvalMessages.getString("UnaryMinusOperator.unary_minus_operator_1"); //$NON-NLS-1$
	}

}
