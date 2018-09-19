/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

/**
 * Pushes the 'null' onto the stack.
 */
public class PushNull extends SimpleInstruction {

	@Override
	public void execute() {
		pushNullValue();
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.PushNull_push___null__1;
	}
}
