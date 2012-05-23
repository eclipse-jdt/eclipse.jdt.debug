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

public class Pop extends Instruction {

	private int fSize;

	/**
	 * @param start
	 */
	public Pop(int size) {
		fSize = size;
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() {
		pop();
	}

	/*
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return InstructionsEvaluationMessages.Pop_pop_1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction#getSize
	 * ()
	 */
	@Override
	public int getSize() {
		return fSize;
	}

}
