/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IObject;
import org.eclipse.jdt.debug.eval.ast.model.IPrimitiveValue;

/**
 * @version 	1.0
 * @author
 */
public class UnaryPlusOperator extends UnaryOperator {

	public UnaryPlusOperator(int expressionTypeId, int start) {
		super(expressionTypeId, start);
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IPrimitiveValue value= (IPrimitiveValue)popValue();
		switch (fExpressionTypeId) {
			case T_double:
				pushNewValue(+value.getDoubleValue());
				break;
			case T_float:
				pushNewValue(+value.getFloatValue());
				break;
			case T_long:
				pushNewValue(+value.getLongValue());
				break;
			case T_byte:
			case T_short:
			case T_int:
			case T_char:
				pushNewValue(+value.getIntValue());
				break;
		}
	}

	public String toString() {
		return "unary plus operator";
	}

}
