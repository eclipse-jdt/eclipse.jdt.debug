/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

public class ConditionalJump extends Jump {
	private boolean fJumpOnTrue;
	
	public ConditionalJump(boolean jumpOnTrue) {
		fJumpOnTrue= jumpOnTrue;
	}
	
	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaPrimitiveValue condition= (IJavaPrimitiveValue)popValue();
		
		if (!(fJumpOnTrue ^ condition.getBooleanValue())) {
			jump(fOffset);
		}
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return InstructionsEvaluationMessages.getString("ConditionalJump.conditional_jump_1"); //$NON-NLS-1$
	}

}
