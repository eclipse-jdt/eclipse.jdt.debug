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
 * A simple instruction cannot contain other instructions so its size is always
 * one.
 */
public abstract class SimpleInstruction extends Instruction {

	/**
	 * Constructor for SimpleInstruction.
	 */
	protected SimpleInstruction() {
		super();
	}

	@Override
	public int getSize() {
		return 1;
	}

}
