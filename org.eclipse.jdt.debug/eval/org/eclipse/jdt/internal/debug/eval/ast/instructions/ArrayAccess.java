package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
 
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
		if (index >= array.getLength() || index < 0) {
			throw new CoreException(new Status(Status.ERROR, JDIDebugModel.getPluginIdentifier(), Status.OK, MessageFormat.format(InstructionsEvaluationMessages.getString("ArrayAccess.illegal_index"), new Object[] {new Integer(index)}), null)); //$NON-NLS-1$
		}
		push(array.getVariables()[index]);
	}

	public String toString() {
		return InstructionsEvaluationMessages.getString("ArrayAccess.array_access_1"); //$NON-NLS-1$
	}
}

