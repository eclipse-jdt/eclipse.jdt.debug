package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Pushes a float literal onto the stack.
 */
public class PushFloat extends SimpleInstruction {
	
	private float fValue;
	
	public PushFloat(float value) {
		fValue = value;
	}
	
	public void execute() {
		pushNewValue(fValue);
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.getString("PushFloat.push__1") + fValue; //$NON-NLS-1$
	}

}

