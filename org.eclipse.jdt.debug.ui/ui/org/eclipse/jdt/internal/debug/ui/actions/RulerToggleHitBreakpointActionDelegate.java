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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class RulerToggleHitBreakpointActionDelegate extends AbstractRulerActionDelegate implements IActionDelegate2 {

	private IEditorPart currentEditor;
	private IAction dummyAction;

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		dummyAction = new Action() {
		};
		return dummyAction;
	}

	@Override
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		currentEditor = targetEditor;
	}

	@Override
	public void init(IAction action) {
	}

	@Override
	public void dispose() {
		currentEditor = null;
		dummyAction = null;
		super.dispose();
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		if (!(currentEditor instanceof ITextEditor)) {
			return;
		}
		IVerticalRulerInfo rulerInfo = currentEditor.getAdapter(IVerticalRulerInfo.class);
		if (rulerInfo == null) {
			return;
		}
		int lineOfLastMouseButtonActivity = rulerInfo.getLineOfLastMouseButtonActivity();
		if (lineOfLastMouseButtonActivity < 0) {
			return;
		}
		IDocument document = getDocument((ITextEditor) currentEditor);
		if (document == null) {
			return;
		}
		ToggleBreakpointAdapter toggle = new ToggleBreakpointAdapter();
		try {
			ITextSelection selection = getTextSelection(currentEditor, document, lineOfLastMouseButtonActivity);
			if (toggle.canToggleLineBreakpoints(currentEditor, selection)) {
				IJavaLineBreakpoint jlp = ToggleBreakpointAdapter.findExistingBreakpoint((ITextEditor) currentEditor, selection);
				if (jlp == null) {
					hitCountDialog();

					if (BreakpointToggleUtils.getHitCount() < 2) {

						return;

					}
				}
				BreakpointToggleUtils.setHitpoints(true);
				toggle.toggleBreakpoints(currentEditor, selection);
			}
		} catch (BadLocationException | CoreException e) {
			DebugUIPlugin.log(e);
		}
	}

	private static IDocument getDocument(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		if (provider != null) {
			return provider.getDocument(editor.getEditorInput());
		}
		IDocument doc = editor.getAdapter(IDocument.class);
		if (doc != null) {
			return doc;
		}
		return null;
	}

	private static ITextSelection getTextSelection(IEditorPart editor, IDocument document, int line) throws BadLocationException {
		IRegion region = document.getLineInformation(line);
		ITextSelection textSelection = new TextSelection(document, region.getOffset(), 0);
		ISelectionProvider provider = editor.getSite().getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof ITextSelection && ((ITextSelection) selection).getStartLine() <= line
					&& ((ITextSelection) selection).getEndLine() >= line) {
				textSelection = (ITextSelection) selection;
			}
		}
		return textSelection;
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