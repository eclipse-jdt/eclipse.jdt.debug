/*******************************************************************************
 * Copyright (c) 2018, 2022 IBM Corporation and others.
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
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDILambdaVariable;
import org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;

/**
 * Utility class for Lambda Expressions and Stack frames Place holder for all Lambda operation encapsulation.
 */
public class LambdaUtils {

	private static final String LAMBDA_METHOD_PREFIX = "lambda$"; //$NON-NLS-1$
	private static final Pattern LAMBDA_TYPE_PATTERN = Pattern.compile(".*\\$\\$Lambda[\\$,\\.].*"); //$NON-NLS-1$

	/**
	 * Inspects the top stack frame of the context; if that frame is a lambda frame, looks for a variable with the specified name in that frame and
	 * outer frames visible from that frame.
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
		IStackFrame stackFrame = context.getFrame();
		if (stackFrame != null) {
			List<IVariable> variables = getLambdaFrameVariables(stackFrame);
			for (IVariable variable : variables) {
				if (variable.getName().equals(variableName)) {
					return variable;
				}
			}
		}
		return null;
	}

	/**
	 * Collects variables visible from a lambda stack frame.
	 *
	 * If the debugging class generates all debugging info in its classfile (e.g. line number and source file name), we can use these info to find all
	 * enclosing frames of the paused line number and collect their variables. Otherwise, collect variables from that lambda frame and two more frames
	 * below it.
	 *
	 * @param frame
	 *            The lambda frame at which to check.
	 * @return The variables visible from the stack frame. An empty list if the specified stack frame is not a lambda frame. The variables are ordered
	 *         top-down, i.e. if shadowing occurs, the more local variable will be first in the resulting list.
	 * @throws DebugException
	 *             If accessing the top stack frame or the local variables on stack frames fails, due to failure to communicate with the debug target.
	 */
	public static List<IVariable> getLambdaFrameVariables(IStackFrame frame) throws DebugException {
		if (LambdaUtils.isLambdaFrame(frame)) {
			int lineNumber = frame.getLineNumber();
			String sourceName = ((IJavaStackFrame) frame).getSourceName();
			if (lineNumber == -1 || sourceName == null) {
				return collectVariablesFromLambdaFrame(frame);
			}
			return collectVariablesFromEnclosingFrames(frame);
		}
		return Collections.emptyList();
	}

	/**
	 * Collects variables visible from a lambda stack frame and two frames below that frame.
	 *
	 * Inside a lambda expression, variable names are mangled by the compiler. Its therefore necessary to check the outer frame when at a lambda
	 * frame, in order to find a variable with its name. The lambda expression itself is called by a synthetic static method, which is the first frame
	 * below the lambda frame. So in total we collect variables from 3 stack frames.
	 *
	 * @param frame
	 *            The lambda frame at which to check.
	 * @return The variables visible from the stack frame. The variables are ordered top-down, i.e. if shadowing occurs, the more local variable will
	 *         be first in the resulting list.
	 * @throws DebugException
	 *             If accessing the top stack frame or the local variables on stack frames fails, due to failure to communicate with the debug target.
	 */
	private static List<IVariable> collectVariablesFromLambdaFrame(IStackFrame frame) throws DebugException {
		List<IVariable> variables = new ArrayList<>();
		IThread thread = frame.getThread();
		// look for two frames below the frame which is provided instead starting from first frame.
		List<IStackFrame> stackFrames = Stream.of(thread.getStackFrames()).dropWhile(f -> f != frame)
				.limit(3).collect(Collectors.toUnmodifiableList());
		for (IStackFrame stackFrame : stackFrames) {
			IVariable[] stackFrameVariables = stackFrame.getVariables();
			variables.addAll(Arrays.asList(stackFrameVariables));
			for (IVariable frameVariable : stackFrameVariables) {
				if (isLambdaObjectVariable(frameVariable)) {
					variables.addAll(extractVariablesFromLambda(frameVariable));
				}
			}
		}
		return Collections.unmodifiableList(variables);
	}

	/**
	 * Collect variables from all enclosing frames starting from the provided frame.
	 */
	private static List<IVariable> collectVariablesFromEnclosingFrames(IStackFrame frame) throws DebugException {
		List<IVariable> variables = new ArrayList<>();
		IThread thread = frame.getThread();
		List<IStackFrame> stackFrames = Stream.of(thread.getStackFrames()).dropWhile(f -> f != frame)
				.collect(Collectors.toUnmodifiableList());
		int pausedLineNumber = frame.getLineNumber();
		String pausedSourceName = ((IJavaStackFrame) frame).getSourceName();
		String pausedSourcePath = ((IJavaStackFrame) frame).getSourcePath();
		boolean isFocusFrame = true;
		for (IStackFrame stackFrame : stackFrames) {
			JDIStackFrame jdiFrame = (JDIStackFrame) stackFrame;
			if (isFocusFrame) {
				isFocusFrame = false;
			} else {
				if (!Objects.equals(pausedSourceName, jdiFrame.getSourceName())
						|| !Objects.equals(pausedSourcePath, jdiFrame.getSourcePath())) {
					continue;
				}
				List<Location> locations;
				try {
					locations = jdiFrame.getUnderlyingMethod().allLineLocations();
				} catch (AbsentInformationException e) {
					continue;
				}
				if (locations.isEmpty()) {
					continue;
				}
				int methodStartLine = locations.get(0).lineNumber();
				int methodEndLine = locations.get(locations.size() - 1).lineNumber();
				if (methodStartLine > pausedLineNumber || methodEndLine < pausedLineNumber) {
					continue;
				}
			}
			IVariable[] stackFrameVariables = jdiFrame.getVariables();
			variables.addAll(Arrays.asList(stackFrameVariables));
			for (IVariable frameVariable : stackFrameVariables) {
				if (isLambdaObjectVariable(frameVariable)) {
					variables.addAll(extractVariablesFromLambda(frameVariable));
				}
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
		return frame.isSynthetic() && frame.getName().startsWith(LAMBDA_METHOD_PREFIX);
	}

	/**
	 * Returns if the variable represent a variable embedded into Lambda object.
	 *
	 * @param variable
	 *            the variable which needs to be evaluated
	 * @return <code>True</code> if the variable is inside the Lambda object else return <code>False</code>
	 * @since 3.15
	 */
	public static boolean isLambdaField(IVariable variable) throws DebugException {
		return (variable instanceof IJavaFieldVariable) && 
			LAMBDA_TYPE_PATTERN.matcher(((IJavaFieldVariable) variable).getDeclaringType().getName()).matches();
	}

	/**
	 * Returns if the method is a lambda method.
	 *
	 * @param method
	 *            the method for which to check
	 * @return <code>True</code> if the method is a lambda method else return <code>False</code>
	 * @since 3.20
	 */
	public static boolean isLambdaMethod(Method method) {
		return method.name().startsWith(LAMBDA_METHOD_PREFIX);
	}

	private static boolean isLambdaObjectVariable(IVariable variable) {
		return variable instanceof JDILambdaVariable;
	}

	private static List<IVariable> extractVariablesFromLambda(IVariable variable) throws DebugException {
		if (variable.getValue() instanceof IJavaObject) {
			return Arrays.asList(variable.getValue().getVariables());
		}
		return Collections.emptyList();
	}
}
