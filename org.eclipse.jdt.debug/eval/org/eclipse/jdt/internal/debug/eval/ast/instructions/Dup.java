/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
 * Duplicate the top element of the stack
 *
 * Element ...
 *
 * ->
 *
 * Element Element ...
 *
 */
public class Dup extends SimpleInstruction {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction#execute
	 * ()
	 */
	@Override
	public void execute() throws CoreException {
		Object element = pop();
		push(element);
		push(element);
	}

	@Override
	public String toString() {
		return "Dup"; //$NON-NLS-1$
	}

}
