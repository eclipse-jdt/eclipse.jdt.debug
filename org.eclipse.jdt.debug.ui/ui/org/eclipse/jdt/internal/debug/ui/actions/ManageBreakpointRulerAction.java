/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ManageBreakpointRulerAction extends Action {	
	
	private IVerticalRulerInfo fRuler;
	private ITextEditor fTextEditor;
	private ToggleBreakpointAdapter fBreakpointAdapter;

	public ManageBreakpointRulerAction(IVerticalRulerInfo ruler, ITextEditor editor) {
		super(ActionMessages.getString("ManageBreakpointRulerAction.label")); //$NON-NLS-1$
		fRuler= ruler;
		fTextEditor= editor;
		fBreakpointAdapter = new ToggleBreakpointAdapter();
	}
	
	/**
	 * Disposes this action
	 */
	public void dispose() {
		fTextEditor = null;
		fRuler = null;
	}
		
	/**
	 * Returns this action's vertical ruler info.
	 *
	 * @return this action's vertical ruler
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRuler;
	}
	
	/**
	 * Returns this action's editor.
	 *
	 * @return this action's editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}
	
	/**
	 * Returns the <code>IDocument</code> of the editor's input.
	 *
	 * @return the document of the editor's input
	 */
	protected IDocument getDocument() {
		IDocumentProvider provider= fTextEditor.getDocumentProvider();
		return provider.getDocument(fTextEditor.getEditorInput());
	}
	
	/**
	 * @see Action#run()
	 */
	public void run() {
		try {
			IDocument document= getDocument();
			int lineNumber= getVerticalRulerInfo().getLineOfLastMouseButtonActivity();
			IRegion line= document.getLineInformation(lineNumber);
			ITextSelection selection = new TextSelection(document, line.getOffset(), line.getLength());
			fBreakpointAdapter.toggleLineBreakpoints(fTextEditor, selection);
		} catch (BadLocationException e) {
			JDIDebugUIPlugin.errorDialog(ActionMessages.getString("ManageBreakpointRulerAction.error.adding.message1"), e); //$NON-NLS-1$
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(ActionMessages.getString("ManageBreakpointRulerAction.error.adding.message1"), e); //$NON-NLS-1$
		}
	}		
}
