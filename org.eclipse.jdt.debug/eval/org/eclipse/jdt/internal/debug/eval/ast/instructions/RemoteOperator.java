/*******************************************************************************
 * Copyright (c) 2020, Jesper Steen Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.eval.RemoteEvaluator;

/**
 * Invokes a method on a object in a class injected into the debug target. The arguments are on the stack in reverse order, followed by the type.
 * Pushes the result onto the stack
 */
public class RemoteOperator extends CompoundInstruction {

	private final RemoteEvaluator fEvaluator;
	private final String fSignature;

	public RemoteOperator(String body, int start, RemoteEvaluator evaluator) {
		super(start);
		fSignature = body;
		fEvaluator = evaluator;
	}

	@Override
	public void execute() throws CoreException {
		int variableCount = fEvaluator.getVariableCount();
		IJavaValue[] args = new IJavaValue[variableCount];
		// args are in reverse order
		for (int i = variableCount - 1; i >= 0; i--) {
			args[i] = popValue();
		}
		IJavaValue result = fEvaluator.evaluate(this.getContext().getThread(), args);
		push(result);
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.Run_Remote_1
				+ fSignature;
	}

}
