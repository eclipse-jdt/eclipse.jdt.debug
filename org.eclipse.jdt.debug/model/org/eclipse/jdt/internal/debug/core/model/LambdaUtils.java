/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;


import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Utility class for Lambda Expressions and Stack frames Place holder for all Lambda operation encapsulation.
 */
public class LambdaUtils {

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
