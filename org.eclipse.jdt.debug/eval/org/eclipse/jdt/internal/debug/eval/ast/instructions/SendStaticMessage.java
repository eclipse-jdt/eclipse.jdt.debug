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
 *     Jesper Steen Møller <jesper@selskabet.org> - Bug 430839
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.osgi.util.NLS;

/**
 * Sends a message. The arguments are on the stack in reverse order, followed by
 * the receiver. Pushes the result, if any, onto the stack
 */
public class SendStaticMessage extends CompoundInstruction {

	private final int fArgCount;
	private final String fSelector;
	private final String fSignature;
	private final String fTypeName;

	public SendStaticMessage(String typeName, String selector,
			String signature, int argCount, int start) {
		super(start);
		fArgCount = argCount;
		fSelector = selector;
		fSignature = signature;
		fTypeName = typeName;
	}

	@Override
	public void execute() throws CoreException {
		IJavaValue[] args = new IJavaValue[fArgCount];
		// args are in reverse order
		for (int i = fArgCount - 1; i >= 0; i--) {
			args[i] = popValue();
		}

		IJavaType receiver = getType(fTypeName);
		IJavaValue result;
		if (receiver instanceof IJavaClassType) {
			result = ((IJavaClassType) receiver).sendMessage(fSelector,
					fSignature, args, getContext().getThread());
		} else if (receiver instanceof IJavaInterfaceType) {
			result = ((IJavaInterfaceType) receiver).sendMessage(fSelector,
					fSignature, args, getContext().getThread());
		} else {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							JDIDebugPlugin.getUniqueIdentifier(),
							IStatus.OK,
							InstructionsEvaluationMessages.SendStaticMessage_Cannot_send_a_static_message_to_a_non_class_type_object_1,
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
		return NLS.bind(InstructionsEvaluationMessages.SendStaticMessage_send_static_message__0___1__2,
				fSelector, fSignature);
	}
}
