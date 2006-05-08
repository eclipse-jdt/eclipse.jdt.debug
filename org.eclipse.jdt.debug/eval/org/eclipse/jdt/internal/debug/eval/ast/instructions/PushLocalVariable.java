/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

 
import com.ibm.icu.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IVariable;
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
		IVariable internalVariable= getInternalVariable(fName);
		if (internalVariable != null) {
			push(internalVariable);
			return;
		}
		IRuntimeContext context= getContext();
		IJavaVariable[] locals = context.getLocals();
		for (int i = 0; i < locals.length; i++) {
			if (locals[i].getName().equals(getName())) {
				push(locals[i]);
				return;
			}
		}

		throw new CoreException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK, MessageFormat.format(InstructionsEvaluationMessages.PushLocalVariable_Cannot_find_the_variable____1, new String[]{fName}), null)); 
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
		return MessageFormat.format(InstructionsEvaluationMessages.PushLocalVariable_push____0___2, new String[]{getName()}); 
	}
}

