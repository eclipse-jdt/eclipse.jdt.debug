package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


 
/**
 * Pushes the 'null' onto the stack.
 */
public class PushNull extends SimpleInstruction {
	
	public void execute() {
		pushNullValue();
	}

	public String toString() {
		return InstructionsEvaluationMessages.getString("PushNull.push___null__1"); //$NON-NLS-1$
	}
}

