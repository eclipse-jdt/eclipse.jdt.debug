/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

 
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.ui.javaeditor.BasicCompilationUnitEditorActionContributor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Contributions of the Java Snippet Editor to the Workbench's tool and menu bar.
 */
public class SnippetEditorActionContributor extends BasicCompilationUnitEditorActionContributor {
 	
	protected JavaSnippetEditor fSnippetEditor;
	
	private StopAction fStopAction;
	private SelectImportsAction fSelectImportsAction;
	private SnippetOpenOnSelectionAction fOpenOnSelectionAction;
	private SnippetOpenHierarchyOnSelectionAction fOpenOnTypeSelectionAction;
	
	public SnippetEditorActionContributor() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		
		if (fStopAction == null) {
			toolBarManager.add(new Separator(IJavaDebugUIConstants.EVALUATION_GROUP));
			return;
		}
		toolBarManager.add(fStopAction);
		toolBarManager.add(fSelectImportsAction);
		toolBarManager.update(false);
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	@Override
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
		if (fSnippetEditor.getFile() == null) {
			fSelectImportsAction.setEnabled(false);
		}
	}	
	
	protected void updateStatus(JavaSnippetEditor editor) {
		String message= ""; //$NON-NLS-1$
		if (editor != null && editor.isEvaluating()) {
			message= SnippetMessages.getString("SnippetActionContributor.evalMsg");  //$NON-NLS-1$
		} 
		getActionBars().getStatusLineManager().setMessage(message);
	}
}
