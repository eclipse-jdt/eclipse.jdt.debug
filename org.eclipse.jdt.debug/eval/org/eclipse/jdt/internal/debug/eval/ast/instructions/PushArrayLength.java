package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaArray;
 
/**
 * Pops an array object off the stack, and pushes its length.
 */
public class PushArrayLength extends CompoundInstruction {
	
	public PushArrayLength(int start) {
		super(start);
	}
	
	public void execute() throws CoreException {
		IJavaArray receiver= (IJavaArray)popValue();
		
		int length= ((IJavaArray)receiver).getLength();
		pushNewValue(length);
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.getString("PushArrayLength.push_array_length__1"); //$NON-NLS-1$
	}
}

