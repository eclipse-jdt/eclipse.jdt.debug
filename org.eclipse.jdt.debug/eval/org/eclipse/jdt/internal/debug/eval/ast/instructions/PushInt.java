package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Pushes an int literal onto the stack.
 */
public class PushInt extends SimpleInstruction {
	
	private int fValue;
	
	public PushInt(int value) {
		fValue = value;
	}
	
	public void execute() {
		pushNewValue(fValue);
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.getString("PushInt.push__1") + fValue; //$NON-NLS-1$
	}

}

