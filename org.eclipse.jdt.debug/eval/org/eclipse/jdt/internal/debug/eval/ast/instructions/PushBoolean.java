package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Pushes a boolean literal onto the stack.
 */
public class PushBoolean extends SimpleInstruction {
	private boolean fValue;
	
	public PushBoolean(boolean value) {
		fValue= value;
	}
	
	public void execute() {
		pushNewValue(fValue);
	}

	public String toString() {
		return EvalMessages.getString("PushBoolean.push__1") + fValue; //$NON-NLS-1$
	}
}

