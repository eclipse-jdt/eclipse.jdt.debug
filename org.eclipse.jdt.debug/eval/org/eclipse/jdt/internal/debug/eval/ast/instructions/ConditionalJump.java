/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *     Jacob Saoumi - Bug 434722 error in ConditionalJump Instruction
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;

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
		IJavaValue conditionValue = popValue();
		IJavaPrimitiveValue condition = null;
		if (conditionValue instanceof IJavaPrimitiveValue) {
			condition = (IJavaPrimitiveValue) conditionValue;
		} else if (conditionValue instanceof IJavaObject) {
			if (((IJavaObject) conditionValue).getJavaType().getName().equals("java.lang.Boolean")) { //$NON-NLS-1$
				condition = (IJavaPrimitiveValue) ((IJavaObject) conditionValue).getField("value", false).getValue(); //$NON-NLS-1$
			}
		}

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
