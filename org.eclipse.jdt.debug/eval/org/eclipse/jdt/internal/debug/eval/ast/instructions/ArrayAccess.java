package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
 
/**
 * Resolves an array access - the top of the stack is
 * the position, and the second from top is the array
 * object.
 */
public class ArrayAccess extends ArrayInstruction {
	
	public ArrayAccess(int start) {
		super(start);
	}
	
	public void execute() throws CoreException {
		int index = ((IJavaPrimitiveValue)popValue()).getIntValue();
		IJavaArray array = (IJavaArray)popValue();
		push(array.getVariables()[index]);
	}

	public String toString() {
		return "array access";
	}
}

