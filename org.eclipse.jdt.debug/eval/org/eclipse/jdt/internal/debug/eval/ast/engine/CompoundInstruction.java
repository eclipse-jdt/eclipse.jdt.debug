/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

/**
 * A <code>CompoundInstruction</code> is a container instruction
 * and may have a size greater than one.
 * 
 * @version 	1.0
 * @author
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
