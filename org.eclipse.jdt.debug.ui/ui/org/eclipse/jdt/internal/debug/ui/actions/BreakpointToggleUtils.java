/*******************************************************************************
 * Copyright (c) 2016, 2022 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;



import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * Utility class for Java Toggle breakpoints
 */
public class BreakpointToggleUtils {

	private static boolean isTracepoint = false;
	private static boolean isLambdaEntryBreakpoint = false;


	public static void setUnsetTracepoints(boolean tracePoint) {
		isTracepoint = tracePoint;
	}

	public static boolean isToggleTracepoints() {
		return isTracepoint;
	}

	public static void setUnsetLambdaEntryBreakpoint(boolean lambdaEntryBreakpoint) {
		isLambdaEntryBreakpoint = lambdaEntryBreakpoint;
	}

	public static boolean isToggleLambdaEntryBreakpoint() {
		return isLambdaEntryBreakpoint;
	}

	/**
	 * Convenience method for printing messages to the status line
	 *
	 * @param message
	 *            the message to be displayed
	 * @param part
	 *            the currently active workbench part
	 */
	public static void report(final String message, final IWorkbenchPart part) {
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IEditorStatusLine statusLine = part.getAdapter(IEditorStatusLine.class);
				if (statusLine != null) {
					if (message != null) {
						statusLine.setMessage(true, message, null);
					} else {
						statusLine.setMessage(true, null, null);
					}
				}
			}
		});
	}

}
