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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

public class RulerToggleTriggerBreakpointActionDelegate extends AbstractRulerToggleBreakpointActionDelegate {

	@Override
	protected boolean doWork(ITextEditor editor, ITextSelection selection) {
		IJavaLineBreakpoint jlp = ToggleBreakpointAdapter.findExistingBreakpoint(currentEditor, selection);
		try {
			if (jlp != null && !jlp.isTriggerPoint()) {
				ToggleBreakpointAdapter.deleteBreakpoint(jlp, editor, null);
			}
			BreakpointToggleUtils.setTriggerpoint(true);
			return true;
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
			return false;
		}
	}
}