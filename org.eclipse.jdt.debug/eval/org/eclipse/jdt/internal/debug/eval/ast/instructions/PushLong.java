package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Pushes a long literal onto the stack.
 */
public class PushLong extends SimpleInstruction {
	
	private long fValue;
	
	public PushLong(long value) {
		fValue = value;
	}
	
	public void execute() {
		pushNewValue(fValue);
	}
	
	public String toString() {
		return "push " + fValue;
	}

}

