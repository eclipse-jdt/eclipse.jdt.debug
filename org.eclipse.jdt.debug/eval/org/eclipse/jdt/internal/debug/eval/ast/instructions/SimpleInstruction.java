/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;



/**
 * A simple instruction cannot contain other instructions
 * so its size is always one.
 */
public abstract class SimpleInstruction extends Instruction {

	/**
	 * Constructor for SimpleInstruction.
	 */
	protected SimpleInstruction() {
		super();
	}
	
	public int getSize() {
		return 1;
	}






}
