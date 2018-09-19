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

public class NoOp extends CompoundInstruction {

	public NoOp(int start) {
		super(start);
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() {
	}

	/*
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return InstructionsEvaluationMessages.NoOp_no_op_1;
	}

}
