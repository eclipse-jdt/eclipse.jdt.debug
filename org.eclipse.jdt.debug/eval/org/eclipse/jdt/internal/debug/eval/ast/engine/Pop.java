/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;

/**
 * @version 	1.0
 * @author
 */
public class Pop extends SimpleInstruction {

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		pop();
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return "pop";
	}

}
