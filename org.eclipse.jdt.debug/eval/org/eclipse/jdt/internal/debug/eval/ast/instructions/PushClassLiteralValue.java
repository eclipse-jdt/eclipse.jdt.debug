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
import org.eclipse.jdt.debug.core.IJavaType;

/**
 * Handles code like "new Object().class"
 */
public class PushClassLiteralValue extends CompoundInstruction {
	public PushClassLiteralValue(int start) {
		super(start);
	}

	/**
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaType type = (IJavaType) pop();
		push(getClassObject(type));
	}

	/*
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return InstructionsEvaluationMessages.PushClassLiteralValue_push_class_literal_value_1;
	}

}
