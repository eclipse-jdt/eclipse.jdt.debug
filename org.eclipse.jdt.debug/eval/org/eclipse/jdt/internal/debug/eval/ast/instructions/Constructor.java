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
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * Invokes a constructor. The arguments are on the stack in reverse order,
 * followed by the type. Pushes the result onto the stack
 */
public class Constructor extends CompoundInstruction {

	private int fArgCount;
	private String fSignature;

	public Constructor(String signature, int argCount, int start) {
		super(start);
		fArgCount = argCount;
		fSignature = signature;
	}

	@Override
	public void execute() throws CoreException {
		IJavaValue[] args = new IJavaValue[fArgCount];
		// args are in reverse order
		for (int i = fArgCount - 1; i >= 0; i--) {
			args[i] = popValue();
		}
		IJavaClassType clazz = (IJavaClassType) pop();
		IJavaValue result = clazz.newInstance(fSignature, args, getContext()
				.getThread());
		push(result);
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.Constructor_constructor__1
				+ fSignature;
	}

}
