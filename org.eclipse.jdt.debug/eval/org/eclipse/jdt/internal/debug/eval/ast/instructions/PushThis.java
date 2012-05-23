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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext;

/**
 * Pushes the 'this' object onto the stack.
 */
public class PushThis extends SimpleInstruction {

	private int fEnclosingLevel;

	public PushThis(int enclosingLevel) {
		fEnclosingLevel = enclosingLevel;
	}

	@Override
	public void execute() throws CoreException {
		IRuntimeContext context = getContext();
		IJavaObject thisInstance = context.getThis();
		if (thisInstance == null) {
			// static context
			push(context.getReceivingType());
		} else {
			if (fEnclosingLevel != 0) {
				thisInstance = ((JDIObjectValue) thisInstance)
						.getEnclosingObject(fEnclosingLevel);
				if (thisInstance == null) {
					throw new CoreException(
							new Status(
									IStatus.ERROR,
									JDIDebugPlugin.getUniqueIdentifier(),
									IStatus.OK,
									InstructionsEvaluationMessages.PushThis_Unable_to_retrieve_the_correct_enclosing_instance_of__this__2,
									null));
				}
			}
			push(thisInstance);
		}
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.PushThis_push___this__1;
	}
}
