/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;

public class Pop extends SimpleInstruction {

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		pop();
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return InstructionsEvaluationMessages.getString("Pop.pop_1"); //$NON-NLS-1$
	}

}
