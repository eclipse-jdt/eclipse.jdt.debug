package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext;
 
/**
 * Pushes the value of a local, instance, or static
 * variable onto the stack.
 */
public class PushLocalVariable extends SimpleInstruction {
	
	/**
	 * Name of variable to push.
	 */
	private String fName;
	
	public PushLocalVariable(String name) {
		fName = name;
	}
	
	public void execute() throws CoreException {
		IRuntimeContext context= getContext();
		IJavaVariable[] locals = context.getLocals();
		for (int i = 0; i < locals.length; i++) {
			if (locals[i].getName().equals(getName())) {
				push(locals[i]);
				return;
			}
		}

		throw new CoreException(new Status(Status.ERROR, JDIDebugPlugin.getUniqueIdentifier(), Status.OK, MessageFormat.format(InstructionsEvaluationMessages.getString("PushLocalVariable.Cannot_find_the_variable____1"), new String[]{fName}), null)); //$NON-NLS-1$
	}
	
	/**
	 * Returns the name of the variable to push
	 * onto the stack.
	 * 
	 * @return the name of the variable to push
	 * onto the stack
	 */
	protected String getName() {
		return fName;
	}

	public String toString() {
		return MessageFormat.format(InstructionsEvaluationMessages.getString("PushLocalVariable.push___{0}__2"), new String[]{getName()}); //$NON-NLS-1$
	}
}

