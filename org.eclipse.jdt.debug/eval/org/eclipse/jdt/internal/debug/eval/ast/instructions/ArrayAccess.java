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
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

import com.ibm.icu.text.MessageFormat;

/**
 * Resolves an array access - the top of the stack is the position, and the
 * second from top is the array object.
 */
public class ArrayAccess extends ArrayInstruction {

	public ArrayAccess(int start) {
		super(start);
	}

	@Override
	public void execute() throws CoreException {
		int index = ((IJavaPrimitiveValue) popValue()).getIntValue();
		IJavaArray array = popArray();
		if (index >= array.getLength() || index < 0) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							IStatus.OK,
							MessageFormat
									.format(InstructionsEvaluationMessages.ArrayAccess_illegal_index,
											new Object[] { new Integer(index) }),
							null));
		}
		push(array.getVariable(index));
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.ArrayAccess_array_access_1;
	}

	/**
	 * Pops an array object off the top of the stack. Throws an exception if not
	 * an array object or <code>null</code>.
	 * 
	 * @return array object on top of the stack
	 * @throws CoreException
	 *             if not available
	 */
	protected IJavaArray popArray() throws CoreException {
		IJavaValue value = popValue();
		if (value instanceof IJavaArray) {
			return (IJavaArray) value;
		} else if (value.isNull()) {
			// null pointer
			throw new CoreException(new Status(IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK,
					InstructionsEvaluationMessages.ArrayAccess_0, null));
		} else {
			// internal error
			throw new CoreException(new Status(IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK,
					"Internal error: attempt to access non-array object", null)); //$NON-NLS-1$
		}
	}
}
