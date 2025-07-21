/*******************************************************************************
 * Copyright (c) 2016, 2025 IBM Corporation and others.
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
	private static boolean isTriggerpoint = false;
	private static boolean isHitPoint = false;

	private static int hitCount = 0;


	public static void setUnsetTracepoints(boolean tracePoint) {
		isTracepoint = tracePoint;
	}

	public static boolean isToggleTracepoints() {
		return isTracepoint;
	}

	public static void setTriggerpoints(boolean triggerPoint) {
		isTriggerpoint = triggerPoint;

	}

	public static boolean isTriggerpoints() {
		return isTriggerpoint;

	}

	public static void setUnsetLambdaEntryBreakpoint(boolean lambdaEntryBreakpoint) {
		isLambdaEntryBreakpoint = lambdaEntryBreakpoint;
	}

	public static boolean isToggleLambdaEntryBreakpoint() {
		return isLambdaEntryBreakpoint;
	}

	public static void setHitpoints(boolean hitcount) {
		isHitPoint = hitcount;

		if (!hitcount) {

			setHitCount(0);

		}

	}

	public static boolean isHitpoint() {
		return isHitPoint;

	}

	public static void setHitCount(int hit) {
		hitCount = hit;

	}

	public static int getHitCount() {
		return hitCount;

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

