package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaObject;
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
		IJavaObject rec = context.getThis();
		if (rec == null) {
			// static context
			push(context.getReceivingType());
		} else {
			if (fEnclosingLevel != 0) {
				rec= ((JDIObjectValue)rec).getEnclosingObject(fEnclosingLevel);
			}
			push(rec);
		}
	}

	public String toString() {
		return EvalMessages.getString("PushThis.push___this__1"); //$NON-NLS-1$
	}
}

