/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

public class NotOperator extends UnaryOperator {

	public NotOperator(int expressionTypeId, int start) {
		super(expressionTypeId, start);
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaPrimitiveValue value= (IJavaPrimitiveValue)popValue();
		pushNewValue(!value.getBooleanValue());
	}

	public String toString() {
		return "'!' operator";
	}

}
