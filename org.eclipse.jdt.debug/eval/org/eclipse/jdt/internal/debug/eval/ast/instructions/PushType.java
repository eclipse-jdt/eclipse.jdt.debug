package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
 
/**
 * Pushes a reference type onto the stack.
 */
public class PushType extends SimpleInstruction {
	
	private String fTypeName;
	
	
	public PushType(String signature) {
		fTypeName= signature;
	}
	
	public void execute() throws CoreException {
		push(getType(fTypeName));
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.getString("PushType.push_type__1") + fTypeName; //$NON-NLS-1$
	}

	
	
}

