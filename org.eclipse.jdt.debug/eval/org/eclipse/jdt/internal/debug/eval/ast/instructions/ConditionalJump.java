/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IPrimitiveValue;

/**
 * @version 	1.0
 * @author
 */
public class ConditionalJump extends Jump {
	private boolean fJumpOnTrue;
	
	public ConditionalJump(boolean jumpOnTrue) {
		fJumpOnTrue= jumpOnTrue;
	}
	
	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IPrimitiveValue condition= (IPrimitiveValue)popValue();
		
		if (!(fJumpOnTrue ^ condition.getBooleanValue())) {
			jump(fOffset);
		}
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return "conditional jump";
	}

}
