/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/**
 * A <code>CompoundInstruction</code> is a container instruction
 * and may have a size greater than one.
 */
public abstract class CompoundInstruction extends Instruction {

	private int fSize;
	
	/**
	 * Constructor for CompoundInstruction.
	 * @param start
	 */
	protected CompoundInstruction(int start) {
		fSize= -start;
	}

	public void setEnd(int end) {
		fSize += end;
	}

	public int getSize() {
		return fSize;
	}
}
