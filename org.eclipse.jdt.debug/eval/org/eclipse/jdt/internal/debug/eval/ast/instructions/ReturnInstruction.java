package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

public class ReturnInstruction extends CompoundInstruction {

	/**
	 * Constructor for ReturnInstruction.
	 * @param start
	 */
	public ReturnInstruction(int start) {
		super(start);
	}

	/**
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		stop();
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.getString("ReturnInstruction.return"); //$NON-NLS-1$
	}

}
