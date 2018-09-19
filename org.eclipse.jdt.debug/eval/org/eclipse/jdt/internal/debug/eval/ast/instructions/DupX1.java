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
 * Duplicate the top element of the stack and put in it behind the second
 * element of the stack.
 *
 * Element1 Element2 ...
 *
 * ->
 *
 * Element1 Element2 Element3 ...
 *
 */
public class DupX1 extends SimpleInstruction {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction#execute
	 * ()
	 */
	@Override
	public void execute() throws CoreException {
		Object element1 = pop();
		Object element2 = pop();
		push(element1);
		push(element2);
		push(element1);
	}

}
