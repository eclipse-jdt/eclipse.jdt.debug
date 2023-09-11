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

import org.eclipse.core.runtime.CoreException;

/**
 * Pushes a reference type onto the stack.
 */
public class PushType extends SimpleInstruction {

	private final String fTypeName;

	public PushType(String signature) {
		fTypeName = signature;
	}

	@Override
	public void execute() throws CoreException {
		push(getType(fTypeName));
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.PushType_push_type__1 + fTypeName;
	}

}
