package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.model.IArray;
import org.eclipse.jdt.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.debug.eval.model.IValue;
import org.eclipse.jdt.internal.debug.eval.model.*;
import org.eclipse.jdt.internal.debug.eval.model.EvaluationArrayVariable;
 
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
		int index = ((IPrimitiveValue)popValue()).getIntValue();
		IArray array = (IArray)popValue();
		push(new EvaluationArrayVariable(array, index));
	}

	public String toString() {
		return "array access";
	}
}

