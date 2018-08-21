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
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.ui.IActionFilter;

/**
 * Filter which determines if actions relevant to an IJavaThread should
 * be displayed. This filter is provided to the platform by JDIDebugUIAdapterFactory.
 */
public class JavaThreadActionFilter implements IActionFilter {

	@Override
	public boolean testAttribute(Object target, String name, String value) {
		if (target instanceof IJavaThread) {
			if (name.equals("TerminateEvaluationActionFilter") //$NON-NLS-1$
				&& value.equals("supportsTerminateEvaluation")) { //$NON-NLS-1$
				IJavaThread thread = (IJavaThread) target;
				return thread.isPerformingEvaluation();
			} else if (name.equals("ExcludeExceptionLocationFilter") //$NON-NLS-1$
				&& value.equals("suspendedAtException")) { //$NON-NLS-1$
				IJavaThread thread = (IJavaThread) target;
				IBreakpoint[] breakpoints= thread.getBreakpoints();
				for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint= breakpoints[i];
					try {
						if (breakpoint.isRegistered() && breakpoint instanceof IJavaExceptionBreakpoint) {
							return true;
						}
					} catch (CoreException e) {
					}
				}
			}

		}
		return false;
	}
}
