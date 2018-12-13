/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.core.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext;

/**
 * Utility class for Lambda Expressions and Stack frames Place holder for all Lambda operation encapsulation.
 */
public class LambdaUtils {

	/**
	 * Inspects the top stack frame of the context; if that frame is a lambda frame, looks for a variable with the specified name in that frame and
	 * two frames below that frame.
	 *
	 * Inside a lambda expression, variable names are mangled by the compiler. Its therefore necessary to check the outer frame when at a lambda
	 * frame, in order to find a variable with its name. The lambda expression itself is called by a synthetic static method, which is the first frame
	 * below the lambda frame. So in total we check 3 stack frames for the variable with the specified name.
	 *
	 * @param context
	 *            The context in which to check.
	 * @param variableName
	 *            The name of the variable.
	 * @return The variable with the specified name if found, {@code null} otherwise. Also returns {@code null} if no thread or top stack frame is
	 *         available. If there are multiple variables with the same name in the lambda context, the most local one is returned.
	 * @throws DebugException
	 *             If accessing the top stack frame or the local variables on stack frames fails, due to failure to communicate with the debug target.
	 */
	public static IVariable findLambdaFrameVariable(IRuntimeContext context, String variableName) throws DebugException {
		IJavaThread thread = context.getThread();
		if (thread != null) {
			IStackFrame topStackFrame = thread.getTopStackFrame();
			List<IVariable> variables = getLambdaFrameVariables(topStackFrame);
			for (IVariable variable : variables) {
				if (variable.getName().equals(variableName)) {
					return variable;
				}
			}
		}
		return null;
	}

	/**
	 * Collects variables visible from a lambda stack frame. I.e. inspects the specified stack frame; if that frame is a lambda frame, collects all
	 * variables in that frame and two frames below that frame.
	 *
	 * Inside a lambda expression, variable names are mangled by the compiler. Its therefore necessary to check the outer frame when at a lambda
	 * frame, in order to find a variable with its name. The lambda expression itself is called by a synthetic static method, which is the first frame
	 * below the lambda frame. So in total we collect variables from 3 stack frames.
	 *
	 * @param frame
	 *            The lambda frame at which to check.
	 * @return The variables visible from the stack frame. An empty list if the specified stack frame is not a lambda frame. The variables are ordered
	 *         top-down, i.e. if shadowing occurs, the more local variable will be first in the resulting list.
	 * @throws DebugException
	 *             If accessing the top stack frame or the local variables on stack frames fails, due to failure to communicate with the debug target.
	 */
	public static List<IVariable> getLambdaFrameVariables(IStackFrame frame) throws DebugException {
		List<IVariable> variables = new ArrayList<>();
		if (LambdaUtils.isLambdaFrame(frame)) {
			IThread thread = frame.getThread();
			IStackFrame[] stackFrames = thread.getStackFrames();
			for (int i = 0; i < Math.min(3, stackFrames.length); ++i) {
				IStackFrame stackFrame = stackFrames[i];
				IVariable[] stackFrameVariables = stackFrame.getVariables();
				variables.addAll(Arrays.asList(stackFrameVariables));
			}
		}
		return Collections.unmodifiableList(variables);
	}

	/**
	 * Evaluates if the input frame is a lambda frame.
	 *
	 * @param frame
	 *            the frame which needs to be evaluated
	 * @return <code>True</code> if the frame is a lambda frame else return <code>False</Code>
	 */
	public static boolean isLambdaFrame(IStackFrame frame) throws DebugException {
		return frame instanceof IJavaStackFrame && isLambdaFrame((IJavaStackFrame) frame);
	}

	/**
	 * Evaluates if the input frame is a lambda frame.
	 *
	 * @param frame
	 *            the frame which needs to be evaluated
	 * @return <code>True</code> if the frame is a lambda frame else return <code>False</Code>
	 * @since 3.8
	 */
	public static boolean isLambdaFrame(IJavaStackFrame frame) throws DebugException {
		return frame.isSynthetic() && frame.getName().startsWith("lambda$"); //$NON-NLS-1$
	}
}
