/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
		fJumpOnTrue= jumpOnTrue;
	}
	
	/*
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		IJavaPrimitiveValue condition= (IJavaPrimitiveValue)popValue();
		
		if (!(fJumpOnTrue ^ condition.getBooleanValue())) {
			jump(fOffset);
		}
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return InstructionsEvaluationMessages.getString("ConditionalJump.conditional_jump_1"); //$NON-NLS-1$
	}

}
