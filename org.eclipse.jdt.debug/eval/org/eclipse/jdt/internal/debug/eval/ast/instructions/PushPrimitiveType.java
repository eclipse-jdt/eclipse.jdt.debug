/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
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
