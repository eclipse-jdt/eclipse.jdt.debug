/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IType;

/**
 * Handles code like "new Object().class"
 * 
 * @version 	1.0
 * @author
 */
public class PushClassLiteralValue extends CompoundInstruction {
	public PushClassLiteralValue(int start) {
		super(start);
	}
	
	/**
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IType type = (IType)pop();
		push(getClassObject(type));
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return "push class literal value";
	}

}
