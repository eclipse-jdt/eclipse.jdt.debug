package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Pushes a char literal onto the stack.
 */
public class PushChar extends SimpleInstruction {
	
	private char fValue;
	
	public PushChar(char value) {
		fValue = value;
	}
	
	public void execute() {
		pushNewValue(fValue);
	}
	
	public String toString() {
		return EvalMessages.getString("PushChar.push__1") + fValue; //$NON-NLS-1$
	}

}

