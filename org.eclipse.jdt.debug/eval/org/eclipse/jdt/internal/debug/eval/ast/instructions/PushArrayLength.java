/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaArray;
 
/**
 * Pops an array object off the stack, and pushes its length.
 */
public class PushArrayLength extends ArrayAccess {
	
	public PushArrayLength(int start) {
		super(start);
	}
	
	public void execute() throws CoreException {
		IJavaArray receiver= popArray();
		int length= receiver.getLength();
		pushNewValue(length);
	}
	
	public String toString() {
		return InstructionsEvaluationMessages.PushArrayLength_push_array_length__1; 
	}
}

