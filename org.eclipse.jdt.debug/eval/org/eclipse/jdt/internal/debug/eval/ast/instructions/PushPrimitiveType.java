/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
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
 * Pushes a primitive type onto the stack.
 *
 * @since 3.4
 */
public class PushPrimitiveType extends SimpleInstruction {

	private String fName;

	public PushPrimitiveType(String name) {
		fName = name;
	}

	@Override
	public void execute() throws CoreException {
		push(getPrimitiveType(fName));
	}

	@Override
	public String toString() {
		return "Push Primitive Type: " + fName; //$NON-NLS-1$
	}

}
