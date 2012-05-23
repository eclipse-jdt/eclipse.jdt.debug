/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
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
