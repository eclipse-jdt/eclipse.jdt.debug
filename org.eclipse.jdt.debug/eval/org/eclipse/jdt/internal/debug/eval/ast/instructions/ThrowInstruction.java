/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Represent a throw instruction.
 */
public class ThrowInstruction extends CompoundInstruction {

	/**
	 * @param start
	 */
	public ThrowInstruction(int start) {
		super(start);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaObject exception = (IJavaObject) popValue();
		final IJavaThread javaThread = getContext().getThread();
		javaThread.stop(exception);
		javaThread.queueRunnable(new Runnable() {
			@Override
			public void run() {
				try {
					javaThread.resume();
				} catch (DebugException e) {
					e.printStackTrace();
				}
			}
		});
		stop();
	}

}
