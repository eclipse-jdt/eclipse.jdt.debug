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

/**
 * A <code>CompoundInstruction</code> is a container instruction and may have a
 * size greater than one.
 */
public abstract class CompoundInstruction extends Instruction {

	private int fSize;

	/**
	 * Constructor for CompoundInstruction.
	 */
	protected CompoundInstruction(int start) {
		fSize = -start;
	}

	public void setEnd(int end) {
		fSize += end;
	}

	@Override
	public int getSize() {
		return fSize;
	}
}
