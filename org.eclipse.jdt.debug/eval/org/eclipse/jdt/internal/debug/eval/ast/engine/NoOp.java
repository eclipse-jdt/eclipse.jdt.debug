/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IPrimitiveValue;

/**
 * @version 	1.0
 * @author
 */
public class NoOp extends CompoundInstruction {

	public NoOp(int start) {
		super(start);
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
	}
	
	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return "no op";
	}

}
