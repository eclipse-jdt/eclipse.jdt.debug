/*******************************************************************************
 * Copyright (c) 2023 Zsombor Gegesy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.HashSet;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Add the current stack frame to the highlighted frames.
 *
 */
public class HighlightStackFrameAction extends ObjectActionDelegate {

	@Override
	public void run(IAction action) {
		// Make sure there is a current selection
		IStructuredSelection selection = getCurrentSelection();
		if (selection == null) {
			return;
		}

		final var stackFrameCategorizer = JDIDebugUIPlugin.getDefault().getStackFrameCategorizer();
		final var types = new HashSet<String>();
		final var threads = new HashSet<IThread>();
		for (Object selected : selection) {
			if (selected instanceof IJavaStackFrame frame) {
				try {
					types.add(frame.getDeclaringTypeName());
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
				}
				threads.add(frame.getThread());
			}
		}
		stackFrameCategorizer.addTypesToActiveCustomFilters(types);
		for (IThread thread : threads) {
			try {
				for (IStackFrame frame : thread.getStackFrames()) {
					if (frame instanceof IJavaStackFrame javaFrame) {
						javaFrame.resetCategory();
					}
				}
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
			}
		}

	}

}
