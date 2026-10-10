/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.ui.EvaluationContextManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the "Step Out of Code Block" debug action.
 */
public class StepOutOfCodeBlockHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor != null) {
			ISelection selection = editor.getEditorSite().getSelectionProvider().getSelection();
			IJavaStackFrame frame = EvaluationContextManager.getEvaluationContext(editor);
			if (selection instanceof ITextSelection selection2 && frame != null && frame.isSuspended()) {
				try {
					int line = frame.getLineNumber();
					if (line < 1 || !isFrameSourceShownIn(editor, frame)) {
						BreakpointToggleUtils.report("Active editor does not show the source of the selected stack frame", editor); //$NON-NLS-1$
						return null;
					}
					IRunToLineTarget runToLineAction = new RunToLineAdapter(line, true);
					runToLineAction.runToLine(editor, selection2, frame.getThread());
				} catch (CoreException e) {
					DebugPlugin.log(e);
				}
			}
		}
		return null;
	}

	/**
	 * Returns whether the given editor shows the source of the given stack frame. The step-out line number is taken from the frame, so it
	 * must only be applied to the document that the frame is actually executing in.
	 */
	private static boolean isFrameSourceShownIn(IEditorPart editor, IJavaStackFrame frame) {
		ILaunch launch = frame.getLaunch();
		if (launch == null) {
			return false;
		}
		IDebugModelPresentation presentation = DebugUITools.newDebugModelPresentation(JDIDebugModel.getPluginIdentifier());
		try {
			Object sourceElement = JavaDebugUtils.resolveSourceElement(frame, launch);
			if (sourceElement == null) {
				return false;
			}
			IEditorInput frameInput = presentation.getEditorInput(sourceElement);
			return frameInput != null && frameInput.equals(editor.getEditorInput());
		} catch (CoreException e) {
			DebugPlugin.log(e);
			return false;
		} finally {
			presentation.dispose();
		}
	}

	@Override
	public boolean isEnabled() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return false;
		}
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null) {
			return false;
		}
		IJavaStackFrame frame = EvaluationContextManager.getEvaluationContext(window);
		return frame != null && frame.canStepOver();
	}
}
