package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext;
 
/**
 * Pushes the 'this' object onto the stack.
 */
public class PushThis extends SimpleInstruction {
	
	private int fEnclosingLevel;
	
	public PushThis(int enclosingLevel) {
		fEnclosingLevel= enclosingLevel;
	}
	
	public void execute() throws CoreException {
		IRuntimeContext context= getContext();
		IJavaObject thisInstance = context.getThis();
		if (thisInstance == null) {
			// static context
			push(context.getReceivingType());
		} else {
			if (fEnclosingLevel != 0) {
				thisInstance= ((JDIObjectValue)thisInstance).getEnclosingObject(fEnclosingLevel);
				if (thisInstance == null) {
					throw new CoreException(new Status(Status.ERROR, JDIDebugModel.getPluginIdentifier(), Status.OK, InstructionsEvaluationMessages.getString("PushThis.Unable_to_retrieve_the_correct_enclosing_instance_of__this__2"), null)); //$NON-NLS-1$
				}
			}
			push(thisInstance);
		}
	}

	public String toString() {
		return InstructionsEvaluationMessages.getString("PushThis.push___this__1"); //$NON-NLS-1$
	}
}

