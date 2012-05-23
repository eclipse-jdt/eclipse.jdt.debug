/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * This action opens a Java editor on the element represented by text selection of
 * the code snippet.
 */
public class SnippetOpenOnSelectionAction extends OpenAction {
	
	protected JavaSnippetEditor fEditor;
	private String fDialogTitle;
	private String fDialogMessage;
	
	public SnippetOpenOnSelectionAction(JavaSnippetEditor editor) {
		super(editor.getSite());
		fEditor= editor;
		setResources();
		setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);
        
        
        IHandler handler = new ActionHandler(this);
		IHandlerService service = (IHandlerService) editor.getSite().getService(IHandlerService.class);
        service.activateHandler(IJavaEditorActionDefinitionIds.OPEN_EDITOR, handler);
	}
	
	protected void setResources() {
		setText(SnippetMessages.getString("SnippetOpenOnSelectionAction.label")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("SnippetOpenOnSelectionAction.tooltip")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("SnippetOpenOnSelectionAction.description")); //$NON-NLS-1$
		setDialogTitle(SnippetMessages.getString("SnippetOpenOnSelectionDialog.title")); //$NON-NLS-1$
		setDialogMessage(SnippetMessages.getString("SnippetOpenOnSelectionDialog.message")); //$NON-NLS-1$
	}
	
	protected void setDialogTitle(String title) {
		fDialogTitle= title;
	}
	
	protected void setDialogMessage(String message) {
		fDialogMessage= message;
	}
	
	protected void setEditor(JavaSnippetEditor contentEditor) {
		fEditor= contentEditor;
	}
	
	/**
	 * Shows a dialog for resolving an ambigous java element.
	 * Utility method that can be called by subclassers.
	 */
	protected IJavaElement selectJavaElement(List<IJavaElement> elements, Shell shell, String title, String message) {
		
		int nResults= elements.size();
		
		if (nResults == 0)
			return null;
		
		if (nResults == 1)
			return elements.get(0);
		
		int flags= JavaElementLabelProvider.SHOW_DEFAULT
						| JavaElementLabelProvider.SHOW_QUALIFIED
						| JavaElementLabelProvider.SHOW_ROOT;
						
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new JavaElementLabelProvider(flags));
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setElements(elements.toArray());
		
		if (dialog.open() == Window.OK) {
			Object[] selection= dialog.getResult();
			if (selection != null && selection.length > 0) {
				nResults= selection.length;
				for (int i= 0; i < nResults; i++) {
					Object current= selection[i];
					if (current instanceof IJavaElement)
						return (IJavaElement) current;
				}
			}
		}		
		return null;
	}
	
	
	/**
	 * Filters out source references from the given code resolve results.
	 * A utility method that can be called by subclassers. 
	 */
	protected List<IJavaElement> filterResolveResults(IJavaElement[] codeResolveResults) {
		int nResults= codeResolveResults.length;
		List<IJavaElement> refs= new ArrayList<IJavaElement>(nResults);
		for (int i= 0; i < nResults; i++) {
			if (codeResolveResults[i] instanceof ISourceReference)
				refs.add(codeResolveResults[i]);
		}
		return refs;
	}
			
	@Override
	public void run() {
		if (fEditor == null) {
			return;
		}
		try {
			IJavaElement[] result= fEditor.codeResolve();
			if (result != null && result.length > 0) {
				IJavaElement chosen= selectJavaElement(filterResolveResults(result), getShell(), fDialogTitle, fDialogMessage);
				if (chosen != null) {
					run(new StructuredSelection(chosen));
					return;
				}
			}
		} catch (JavaModelException x) {
			JDIDebugUIPlugin.log(x);
		}
		
		getShell().getDisplay().beep();		
	}
	/**
	 * @see SelectionDispatchAction#selectionChanged(ITextSelection)
	 */
	@Override
	public void selectionChanged(ITextSelection selection) {
		setEnabled(fEditor != null);
	}
}
