/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IObject;
import org.eclipse.jdt.debug.eval.ast.model.IPrimitiveValue;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * @version 	1.0
 * @author
 */
public class TwiddleOperator extends UnaryOperator implements TypeIds {

	public TwiddleOperator(int expressionTypeId, int start) {
		super(expressionTypeId, start);
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IPrimitiveValue value= (IPrimitiveValue)popValue();
		switch (fExpressionTypeId) {
			case T_long:
				pushNewValue(~value.getLongValue());
				break;
			case T_byte:
			case T_short:
			case T_int:
			case T_char:
				pushNewValue(~value.getIntValue());
				break;
		}
	}

	public String toString() {
		return "'~' operator";
	}

}
