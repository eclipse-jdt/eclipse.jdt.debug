package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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
	
	private RunSnippetAction fRunSnippetAction;
	private StopAction fStopAction;
	private SelectImportsAction fSelectImportsAction;
	private SnippetOpenOnSelectionAction fOpenOnSelectionAction;
	private SnippetOpenHierarchyOnSelectionAction fOpenOnTypeSelectionAction;
	
	public SnippetEditorActionContributor() {
		super();
		initializeActions();
	}
	
	/**
	 * @see EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		
		super.contributeToToolBar(toolBarManager);
		
		toolBarManager.add(new Separator(IJavaDebugUIConstants.EVALUATION_GROUP));
		toolBarManager.add(fRunSnippetAction);
		toolBarManager.add(fStopAction);
		toolBarManager.add(fSelectImportsAction);
	}
			
	/**
	 *	@see EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {
		
		super.contributeToMenu(menu);
		
		IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fOpenOnSelectionAction);
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fOpenOnTypeSelectionAction);
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
		}
			
		fStopAction.setEditor(fSnippetEditor);		
		fRunSnippetAction.setEditor(fSnippetEditor);
		fSelectImportsAction.setEditor(fSnippetEditor);
		fOpenOnSelectionAction.setContentEditor(fSnippetEditor);
		fOpenOnTypeSelectionAction.setContentEditor(fSnippetEditor);
		updateStatus(fSnippetEditor);			
	}
	 
	protected void initializeActions() {
		 
		fOpenOnSelectionAction= new SnippetOpenOnSelectionAction(fSnippetEditor);
		fOpenOnTypeSelectionAction= new SnippetOpenHierarchyOnSelectionAction(fSnippetEditor);
		fRunSnippetAction= new RunSnippetAction(fSnippetEditor);
		fStopAction= new StopAction(fSnippetEditor);
		fSelectImportsAction= new SelectImportsAction(null);
	}	
	
	protected void updateStatus(JavaSnippetEditor editor) {
		String message= ""; //$NON-NLS-1$
		if (editor != null && editor.isEvaluating()) {
			message= SnippetMessages.getString("SnippetActionContributor.evalMsg");  //$NON-NLS-1$
		} 
		getActionBars().getStatusLineManager().setMessage(message);
	}
}