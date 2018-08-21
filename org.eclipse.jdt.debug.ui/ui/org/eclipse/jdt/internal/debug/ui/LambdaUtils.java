/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Utility class for Lambda Expressions and Stack frames Place holder for all Lambda operation encapsulation.
 */
public class LambdaUtils extends org.eclipse.jdt.internal.debug.core.model.LambdaUtils {


	private static int indexOf(IJavaStackFrame frame, IStackFrame[] stackFrames) {
		int j = 0;
		boolean found = false;
		for (; j < stackFrames.length; j++) {
			if (stackFrames[j] == frame) {
				found = true;
				break;
			}
		}
		if (found) {
			return j;
		}
		return -1;
	}

	/**
	 * Evaluates if the input frame is a lambda frame.
	 *
	 * @param frame
	 *            the frame which needs to be evaluated
	 * @param local
	 *            the local variable to be found
	 * @return <code>null</code> if local variable not found or return the found variable
	 * @since 3.8
	 */
	public static IVariable findLocalVariableFromLambdaScope(IJavaStackFrame frame, ILocalVariable local) throws DebugException, CoreException {
		// This can be local variable defined in the lambda body
		IVariable var = JavaDebugHover.findLocalVariable(frame, local.getElementName());
		if (var != null) {
			return var;
		}

		// ... or this is a local variable captured from enclosing method by the lambda expression.
		IStackFrame[] stackFrames = frame.getThread().getStackFrames();
		int indexOfCurrentFrame = indexOf(frame, stackFrames);
		if (indexOfCurrentFrame < 0) {
			// paranoia, should not happen
			return null;
		}

		// We check frames below current if we can find the one corresponding
		// to the enclosing method and search the variable in this frame.
		int i = 1 + indexOfCurrentFrame;
		IJavaElement parent = local.getParent();
		String enclosingMethodName = parent.getElementName();
		List<String> methodTypeNames = getArgumentTypeNames(parent);
		for (; i < stackFrames.length; i++) {
			IJavaStackFrame currFrame = (IJavaStackFrame) stackFrames[i];
			String methodName = currFrame.getMethodName();
			List<String> typeNames = currFrame.getArgumentTypeNames();

			if (methodName.equals(enclosingMethodName) && typeNames.equals(methodTypeNames)) {
				var = JavaDebugHover.findLocalVariable(currFrame, local.getElementName());
				if (var != null) {
					return var;
				}
				// we can stop searching now
				return null;
			}
		}
		return null;
	}

	private static List<String> getArgumentTypeNames(IJavaElement parent) throws CoreException {
		if (!(parent instanceof IMethod)) {
			return null;
		}
		IMethod method = (IMethod) parent;
		IType type = method.getDeclaringType();
		if (type == null) {
			return null;
		}
		List<String> psig = new ArrayList<>();
		String[] ptypes = method.getParameterTypes();
		for (String ps : ptypes) {
			@SuppressWarnings("restriction")
			String resolvedName = org.eclipse.jdt.internal.corext.util.JavaModelUtil.getResolvedTypeName(ps, type);
			int arrayCount = Signature.getArrayCount(ps);
			for (int i = 0; i < arrayCount; i++) {
				resolvedName += "[]"; //$NON-NLS-1$
			}
			psig.add(resolvedName);
		}
		return psig;
	}
}
