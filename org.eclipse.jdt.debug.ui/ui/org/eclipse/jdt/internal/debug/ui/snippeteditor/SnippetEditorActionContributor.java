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
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

 
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.ui.javaeditor.BasicEditorActionContributor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Contributions of the Java Snippet Editor to the Workbench's tool and menu bar.
 */
public class SnippetEditorActionContributor extends BasicEditorActionContributor {
 	
	protected JavaSnippetEditor fSnippetEditor;
	
	private StopAction fStopAction;
	private SelectImportsAction fSelectImportsAction;
	private SnippetOpenOnSelectionAction fOpenOnSelectionAction;
	private SnippetOpenHierarchyOnSelectionAction fOpenOnTypeSelectionAction;
	
	public SnippetEditorActionContributor() {
		super();
	}
	
	/**
	 * @see EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		
		if (fStopAction == null) {
			toolBarManager.add(new Separator(IJavaDebugUIConstants.EVALUATION_GROUP));
			return;
		}
		toolBarManager.add(fStopAction);
		toolBarManager.add(fSelectImportsAction);
		toolBarManager.update(false);
	}
			
	/**
	 *	@see EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {
		if (fOpenOnSelectionAction == null) {
			return;
		}
		super.contributeToMenu(menu);
		
		IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fOpenOnSelectionAction);
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fOpenOnTypeSelectionAction);
			navigateMenu.setVisible(true);
		}
	}
	
	/**
	 *	@see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		
		super.setActiveEditor(part);
		fSnippetEditor= null;
		if (part instanceof JavaSnippetEditor) {
			fSnippetEditor= (JavaSnippetEditor) part;
			if (fOpenOnSelectionAction == null) {
				initializeActions();
				contributeToMenu(getActionBars().getMenuManager());
				contributeToToolBar(getActionBars().getToolBarManager());
			}
		}

		if (fOpenOnSelectionAction != null) {
			fStopAction.setEditor(fSnippetEditor);		
			fSelectImportsAction.setEditor(fSnippetEditor);
			fOpenOnSelectionAction.setEditor(fSnippetEditor);
			fOpenOnTypeSelectionAction.setEditor(fSnippetEditor);
		}
			
		updateStatus(fSnippetEditor);			
	}
	 
	protected void initializeActions() {
		 
		fOpenOnSelectionAction= new SnippetOpenOnSelectionAction(fSnippetEditor);
		fOpenOnTypeSelectionAction= new SnippetOpenHierarchyOnSelectionAction(fSnippetEditor);
		fStopAction= new StopAction(fSnippetEditor);
		fSelectImportsAction= new SelectImportsAction(fSnippetEditor);
	}	
	
	protected void updateStatus(JavaSnippetEditor editor) {
		String message= ""; //$NON-NLS-1$
		if (editor != null && editor.isEvaluating()) {
			message= SnippetMessages.getString("SnippetActionContributor.evalMsg");  //$NON-NLS-1$
		} 
		getActionBars().getStatusLineManager().setMessage(message);
	}
}
