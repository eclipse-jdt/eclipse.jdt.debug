/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

public class RulerToggleHitBreakpointActionDelegate extends AbstractRulerToggleBreakpointActionDelegate {

	@Override
	protected boolean doWork(ITextEditor editor, ITextSelection selection) {
		IJavaLineBreakpoint jlp = ToggleBreakpointAdapter.findExistingBreakpoint(currentEditor, selection);

		try {
			hitCountDialog();
			if (BreakpointToggleUtils.getHitCount() < 1) {
				return false;
			}
			if (jlp != null) {
				ToggleBreakpointAdapter.deleteBreakpoint(jlp, editor, null);
			}
			BreakpointToggleUtils.setHitpoint(true);
			return true;
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
			return false;
		}
	}

	private void hitCountDialog() {
		String title = ActionMessages.BreakpointHitCountAction_Set_Breakpoint_Hit_Count_2;
		String message = ActionMessages.BreakpointHitCountAction__Enter_the_new_hit_count_for_the_breakpoint__3;
		IInputValidator validator = new IInputValidator() {
			int hitCount = -1;

			@Override
			public String isValid(String value) {
				try {
					hitCount = Integer.parseInt(value.trim());
				} catch (NumberFormatException nfe) {
					hitCount = -1;
				}
				if (hitCount < 1) {
					return ActionMessages.BreakpointHitCountAction_Value_must_be_positive_integer;
				}
				// no error
				return null;
			}
		};

		Shell activeShell = JDIDebugUIPlugin.getActiveWorkbenchShell();
		InputDialog input = new InputDialog(activeShell, title, message, "", validator); //$NON-NLS-1$
		if (input.open() == Window.CANCEL) {
			return;
		}
		String hit = input.getValue();
		if (hit != null && !hit.isEmpty()) {
			BreakpointToggleUtils.setHitCount(Integer.parseInt(hit));
		}
	}
}