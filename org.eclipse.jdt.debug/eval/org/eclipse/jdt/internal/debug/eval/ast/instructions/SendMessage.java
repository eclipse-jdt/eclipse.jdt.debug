/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.osgi.util.NLS;

/**
 * Sends an message to an instance. The arguments are on the stack in reverse
 * order, followed by the receiver. Pushes the result, if any, onto the stack
 */
public class SendMessage extends CompoundInstruction {

	private final int fArgCount;
	private final String fSelector;
	private final String fSignature;
	private final String fDeclaringType;

	public SendMessage(String selector, String signature, int argCount,
			String declaringType, int start) {
		super(start);
		fArgCount = argCount;
		fSelector = selector;
		fSignature = signature;
		fDeclaringType = declaringType;
	}

	@Override
	public void execute() throws CoreException {
		IJavaValue[] args = new IJavaValue[fArgCount];
		// args are in reverse order
		for (int i = fArgCount - 1; i >= 0; i--) {
			args[i] = popValue();
		}
		Object receiver = pop();
		IJavaValue result = null;

		if (receiver instanceof IJavaVariable) {
			receiver = ((IJavaVariable) receiver).getValue();
		}

		if (receiver instanceof IJavaObject) {
			result = ((IJavaObject) receiver).sendMessage(fSelector,
					fSignature, args, getContext().getThread(), fDeclaringType);
		} else {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							IStatus.OK,
							InstructionsEvaluationMessages.SendMessage_Attempt_to_send_a_message_to_a_non_object_value_1,
							null));
		}
		setLastValue(result);
		if (!fSignature.endsWith(")V")) { //$NON-NLS-1$
			// only push the result if not a void method
			push(result);
		}
	}

	@Override
	public String toString() {
		return NLS.bind(InstructionsEvaluationMessages.SendMessage_send_message__0___1__2,
				fSelector, fSignature);
	}
}
