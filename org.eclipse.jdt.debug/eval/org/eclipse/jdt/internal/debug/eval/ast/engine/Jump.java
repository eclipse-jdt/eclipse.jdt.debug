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
public class Jump extends SimpleInstruction {
	protected int fOffset;
	
	public void setOffset(int offset) {
		fOffset= offset;
	}

	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		jump(fOffset);
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return "jump";
	}

}
