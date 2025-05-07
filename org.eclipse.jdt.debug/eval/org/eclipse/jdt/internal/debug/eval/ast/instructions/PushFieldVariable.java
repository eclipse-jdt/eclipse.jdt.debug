/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext;
import org.eclipse.osgi.util.NLS;

/**
 * Pops an object off the stack, and pushes the value of one of its fields onto
 * the stack.
 */
public class PushFieldVariable extends CompoundInstruction {

	private String fDeclaringTypeSignature;

	private final String fName;

	private int fSuperClassLevel;

	public PushFieldVariable(String name, int superClassLevel, int start) {
		super(start);
		fName = name;
		fSuperClassLevel = superClassLevel;
	}

	public PushFieldVariable(String name, String declaringTypeSignature,
			int start) {
		super(start);
		fName = name;
		fDeclaringTypeSignature = declaringTypeSignature;
	}

	@Override
	public void execute() throws CoreException {
		Object value = popValue();
		if (value instanceof JDINullValue) {
			throw new CoreException(new Status(IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK,
					InstructionsEvaluationMessages.PushFieldVariable_0, null));
		}
		IJavaObject receiver = (IJavaObject) value;

		IJavaVariable field = null;

		if (fDeclaringTypeSignature == null) {
			field = ((JDIObjectValue) receiver).getField(fName,
					fSuperClassLevel);
		} else {
			field = receiver.getField(fName, fDeclaringTypeSignature);
		}

		if (field == null) {
			// For anonymous classes, getting variables from outer class
			IRuntimeContext context = getContext();
			final IJavaObject innerThis = context.getThis();
			if (null != innerThis) {
				int i = fDeclaringTypeSignature.indexOf('$');
				if (i > 0) {
					IJavaFieldVariable f = innerThis.getField(fName, false);
					if (null != f) {
						push(f);
						return;
					}
				}
			}

			throw new CoreException(
					new Status(
							IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							IStatus.OK,
							NLS.bind(InstructionsEvaluationMessages.PushFieldVariable_Cannot_find_the_field__0__for_the_object__1__1,
									fName, receiver.toString()),
							null)); //
		}
		push(field);
	}

	@Override
	public String toString() {
		return NLS.bind(InstructionsEvaluationMessages.PushFieldVariable_push_field__0__2,
				fName);
	}
}
