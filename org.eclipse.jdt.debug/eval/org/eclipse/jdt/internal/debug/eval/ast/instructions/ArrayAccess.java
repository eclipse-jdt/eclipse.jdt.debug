package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayPartition;
 
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
			throw new CoreException(new Status(Status.ERROR, JDIDebugPlugin.getUniqueIdentifier(), Status.OK, MessageFormat.format(InstructionsEvaluationMessages.getString("ArrayAccess.illegal_index"), new Object[] {new Integer(index)}), null)); //$NON-NLS-1$
		}
		IVariable[] variables= array.getVariables();
		// JDIArrayValue#getVariables() may return a array of JDIArrayPartition instead of
		// an array of JDIArrayEntryVariable if the number of element in the array is to big.
		// In this case, we have to through these partition to get the correct variable.
		int firstIndex= 0;
		while (variables[1] instanceof JDIArrayPartition) {
			JDIArrayPartition partition= (JDIArrayPartition)variables[0];
			int offset= partition.getEnd() - firstIndex + 1;
			int partitionIndex= (index - firstIndex) / offset;
			variables= (IVariable[])((JDIArrayPartition)variables[partitionIndex]).getValue().getVariables();
			firstIndex= firstIndex + offset * partitionIndex;
		}
		push(variables[index - firstIndex]);
	}

	public String toString() {
		return InstructionsEvaluationMessages.getString("ArrayAccess.array_access_1"); //$NON-NLS-1$
	}
}

