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
 
/**
 * Pushes a reference type onto the stack.
 */
public class PushType extends SimpleInstruction {
	
	private String fTypeName;
	
	
	public PushType(String signature) {
		fTypeName= signature;
	}
	
	public void execute() throws CoreException {
		push(getType(fTypeName));
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.getString("PushType.push_type__1") + fTypeName; //$NON-NLS-1$
	}

	
	
}

