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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.OpenTypeHierarchyAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * This action opens a Java editor on the element represented by text selection of
 * the connected Java source editor. In addition, if the element is a type, it also 
 * opens shows the element in the type hierarchy viewer.
 */
public class SnippetOpenHierarchyOnSelectionAction extends OpenTypeHierarchyAction {
	
	private JavaSnippetEditor fEditor;
	private String fDialogTitle;
	private String fDialogMessage;
	
	public SnippetOpenHierarchyOnSelectionAction(JavaSnippetEditor editor) {
		super(editor.getSite());
		fEditor= editor;
		setResources();
		setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_TYPE_HIERARCHY);
        
        ActionHandler handler = new ActionHandler(this);
        IHandlerService handlerService = (IHandlerService) editor.getSite().getService(IHandlerService.class);
        handlerService.activateHandler(IJavaEditorActionDefinitionIds.OPEN_TYPE_HIERARCHY, handler);
	}
	
	protected void setResources() {
		setText(SnippetMessages.getString("SnippetOpenHierarchyOnSelectionAction.label")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("SnippetOpenHierarchyOnSelectionAction.tooltip")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("SnippetOpenHierarchyOnSelectionAction.description")); //$NON-NLS-1$
		setDialogTitle(SnippetMessages.getString("SnippetOpenHierarchyOnSelectionDialog.title")); //$NON-NLS-1$
		setDialogMessage(SnippetMessages.getString("SnippetOpenHierarchyOnSelectionDialog.message")); //$NON-NLS-1$
	}
	
	protected void setDialogTitle(String title) {
		fDialogTitle= title;
	}
	
	protected void setDialogMessage(String message) {
		fDialogMessage= message;
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
	
	protected void setEditor(JavaSnippetEditor contentEditor) {
		fEditor= contentEditor;
	}
	
	/**
	 * Filters out source references from the given code resolve results.
	 * A utility method that can be called by subclassers. 
	 */
	protected List<IJavaElement> filterResolveResults(IJavaElement[] codeResolveResults) {
		int nResults= codeResolveResults.length;
		List<IJavaElement> refs= new ArrayList<IJavaElement>(nResults);
		for (int i= 0; i < nResults; i++) {
			if (codeResolveResults[i] instanceof ISourceReference) {
				refs.add(codeResolveResults[i]);
			}
		}
		return refs;
	}
			

	/**
	 * Shows a dialog for resolving an ambigous Java element.
	 * Utility method that can be called by subclassers.
	 */
	protected IJavaElement selectJavaElement(List<IJavaElement> elements, Shell shell, String title, String message) {
		
		int nResults= elements.size();
		
		if (nResults == 0) {
			return null;
		}
		
		if (nResults == 1) {
			return elements.get(0);
		}
		
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
					if (current instanceof IJavaElement) {
						return (IJavaElement) current;
					}
				}
			}
		}		
		return null;
	}
}
