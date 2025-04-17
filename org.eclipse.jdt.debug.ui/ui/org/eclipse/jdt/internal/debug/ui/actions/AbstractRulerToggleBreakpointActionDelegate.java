/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class AbstractRulerToggleBreakpointActionDelegate extends AbstractRulerActionDelegate implements IActionDelegate2 {

	protected ITextEditor currentEditor;
	private IAction dummyAction;

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		dummyAction = new Action() {
			// empty implementation to make compiler happy
		};
		return dummyAction;
	}

	@Override
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		if (targetEditor instanceof ITextEditor textEditor) {
			currentEditor = textEditor;
		} else {
			currentEditor = null;
		}
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
		if (currentEditor == null) {
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
		IDocument document = getDocument(currentEditor);
		if (document == null) {
			return;
		}
		ToggleBreakpointAdapter toggle = new ToggleBreakpointAdapter();
		try {
			ITextSelection selection = getTextSelection(currentEditor, document, lineOfLastMouseButtonActivity);
			if (toggle.canToggleLineBreakpoints(currentEditor, selection)) {
				boolean success = doWork(currentEditor, selection);
				if (success) {
					toggle.toggleBreakpoints(currentEditor, selection);
				}
			}
		} catch (BadLocationException | CoreException e) {
			DebugUIPlugin.log(e);
		}
	}

	protected static IDocument getDocument(ITextEditor editor) {
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

	protected ITextSelection getTextSelection(ITextEditor editor, IDocument document, int line) throws BadLocationException {
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

	abstract protected boolean doWork(ITextEditor editor, ITextSelection selection);
}
