/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;

public class Jump extends SimpleInstruction {
	protected int fOffset;
	
	public void setOffset(int offset) {
		fOffset= offset;
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		jump(fOffset);
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return EvalMessages.getString("Jump.jump_1"); //$NON-NLS-1$
	}

}
