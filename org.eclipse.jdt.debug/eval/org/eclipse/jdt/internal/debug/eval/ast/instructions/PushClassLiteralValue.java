/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaType;

/**
 * Handles code like "new Object().class"
 */
public class PushClassLiteralValue extends CompoundInstruction {
	public PushClassLiteralValue(int start) {
		super(start);
	}
	
	/**
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaType type = (IJavaType)pop();
		push(getClassObject(type));
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return "push class literal value";
	}

}
