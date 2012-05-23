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

public class PushArrayType extends ArrayInstruction {

	private String fTypeSignature;

	private int fDimension;

	public PushArrayType(String typeSignature, int dimension, int start) {
		super(start);
		fTypeSignature = typeSignature;
		fDimension = dimension;
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		push(getArrayType(fTypeSignature.replace('/', '.'), fDimension));
	}

}
