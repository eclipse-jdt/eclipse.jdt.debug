package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Runs a snippet
 */
public class RunAction extends SnippetAction implements IWorkbenchWindowActionDelegate, IPartListener {
	
	private IWorkbenchWindow fWindow;
	private IAction fAction;
	
	public RunAction(JavaSnippetEditor editor) {
		super(editor);
		setText(SnippetMessages.getString("RunAction.label")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("RunAction.tooltip")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("RunAction.description")); //$NON-NLS-1$
		setImageDescriptor(JavaDebugImages.DESC_TOOL_RUNSNIPPET);
		setDisabledImageDescriptor(JavaDebugImages.DESC_TOOL_RUNSNIPPET_DISABLED);
		setHoverImageDescriptor(JavaDebugImages.DESC_TOOL_RUNSNIPPET_HOVER);
	}
	
	public RunAction() {
		super(null);
	}
	
	/**
	 * @see IAction#run()
	 */
	public void run() {
		getEditor().evalSelection(JavaSnippetEditor.RESULT_RUN);
	} 
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		getWindow().getPartService().removePartListener(this);
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		setWindow(window);
		IWorkbenchPage page= window.getActivePage();
		if (page != null) {
			if (page.getActiveEditor() instanceof JavaSnippetEditor) {
				setEditor((JavaSnippetEditor)page.getActiveEditor());
			}
		}
		window.getPartService().addPartListener(this);
		update();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		update();
		
	}
	
	protected IWorkbenchWindow getWindow() {
		return fWindow;
	}
		
	protected void setWindow(IWorkbenchWindow window) {
		fWindow = window;
	}
	
	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}
	
	public void setEnabled(boolean enabled) {
		if (getAction() == null) {
			super.setEnabled(enabled);
		} else {
			getAction().setEnabled(enabled);
		}
	}
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof JavaSnippetEditor) {
			setEditor((JavaSnippetEditor)part);
		}
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part == getEditor()) {
			setEditor(null);
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}

}
