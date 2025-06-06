/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris West (Faux) - Bug 45507
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.LambdaUtils;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext;
import org.eclipse.osgi.util.NLS;

/**
 * Pushes the value of a local, instance, or static variable onto the stack.
 */
public class PushLocalVariable extends SimpleInstruction {

	/**
	 * Name of variable to push.
	 */
	private final String fName;

	public PushLocalVariable(String name) {
		fName = name;
	}

	@Override
	public void execute() throws CoreException {
		IVariable internalVariable = getInternalVariable(fName);
		if (internalVariable != null) {
			push(internalVariable);
			return;
		}
		IRuntimeContext context = getContext();
		IJavaVariable[] locals = context.getLocals();
		for (IJavaVariable local : locals) {
			if (local.getName().equals(getName())) {
				push(local);
				return;
			}
		}
		// For anonymous classes, getting variables from outer class
		final IJavaObject innerThis = context.getThis();
		if (null != innerThis) {
			IJavaFieldVariable f = innerThis.getField(ASTEvaluationEngine.ANONYMOUS_VAR_PREFIX + getName(), false);
			if (null != f) {
				push(f);
				return;
			}
		}
		IVariable variable = LambdaUtils.findLambdaFrameVariable(context, getName());
		if (variable != null) {
			push(variable);
			return;
		}

		// Try search for lambda object variable
		variable = LambdaUtils.findLambdaFrameVariable(context, ASTEvaluationEngine.ANONYMOUS_VAR_PREFIX + getName());
		if (variable != null) {
			push(variable);
			return;
		}

		throw new CoreException(
				new Status(
						IStatus.ERROR,
						JDIDebugPlugin.getUniqueIdentifier(),
						IStatus.OK,
						NLS.bind(InstructionsEvaluationMessages.PushLocalVariable_Cannot_find_the_variable____1,
								fName), null));
	}

	/**
	 * Returns the name of the variable to push onto the stack.
	 *
	 * @return the name of the variable to push onto the stack
	 */
	protected String getName() {
		return fName;
	}

	@Override
	public String toString() {
		return NLS.bind(
				InstructionsEvaluationMessages.PushLocalVariable_push____0___2,
				getName());
	}
}
