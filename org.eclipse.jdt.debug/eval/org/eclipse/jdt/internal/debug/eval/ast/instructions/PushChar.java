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

/**
 * Pushes a char literal onto the stack.
 */
public class PushChar extends SimpleInstruction {

	private char fValue;

	public PushChar(char value) {
		fValue = value;
	}

	@Override
	public void execute() {
		pushNewValue(fValue);
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.PushChar_push__1 + fValue;
	}

}
