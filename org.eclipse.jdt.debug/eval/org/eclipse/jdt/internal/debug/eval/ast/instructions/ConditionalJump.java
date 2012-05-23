/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

public class ConditionalJump extends Jump {
	private boolean fJumpOnTrue;

	public ConditionalJump(boolean jumpOnTrue) {
		fJumpOnTrue = jumpOnTrue;
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaPrimitiveValue condition = (IJavaPrimitiveValue) popValue();

		if (!(fJumpOnTrue ^ condition.getBooleanValue())) {
			jump(fOffset);
		}
	}

	/*
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return InstructionsEvaluationMessages.ConditionalJump_conditional_jump_1;
	}

}
