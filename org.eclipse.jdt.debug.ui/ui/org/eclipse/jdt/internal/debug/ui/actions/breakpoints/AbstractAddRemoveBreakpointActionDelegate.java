package org.eclipse.jdt.internal.debug.ui.actions.breakpoints;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * NOTE: This class is yet experimental. Investigating breakpoint creation
 * and location verification via the use of an AST. This could be used to
 * support breakpoints in external source (i.e. without the knowlegde of
 * Java model elements).
 */
public abstract class AbstractAddRemoveBreakpointActionDelegate extends PartEventAction implements IWorkbenchWindowActionDelegate {
	
	/**
	 * Window this action is active in.	 */
	private IWorkbenchWindow fWorkbenchWindow = null;
	
	/**
	 * The previous active part, or <code>null</code>.	 */
	private IWorkbenchPart fPreviousPart = null;
	
	/**
	 * This delegate's action or <code>null</code>.	 */
	private IAction fAction;

	/**
	 * @see PartEventAction#PartEventAction(java.lang.String)
	 */
	public AbstractAddRemoveBreakpointActionDelegate(String text) {
		super(text);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		if (getWorkbenchWindow() != null) {
			getWorkbenchWindow().getPartService().removePartListener(this);
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWorkbenchWindow = window;
		window.getPartService().addPartListener(this);
	}
	
	/**
	 * Returns the workbench window this action was installed in or <code>null</code>
	 * if none.
	 * 	 * @return the workbench window this action was installed in or <code>null</code>
	 * if none	 */
	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		fAction = action;
		doAction();
	}
	
	/**
	 * Runs the action. Subclasses should override.	 */
	protected abstract void doAction();

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fAction = action;
		fAction.setEnabled(computeEnabled());
	}
	
	/**
	 * Creates an AST based on the contents of the given text editor.
	 * 	 * @param editor	 * @return ast
	 * @exception CoreException if unable to retrieve the contents of the given
	 *  text editor	 */
	protected CompilationUnit createCompilationUnit(ITextEditor editor) throws CoreException {
		IDocumentProvider provider = editor.getDocumentProvider();
		provider.connect(this);
		IDocument document =  provider.getDocument(editor.getEditorInput());
		String content = document.get();
		provider.disconnect(this);
		return AST.parseCompilationUnit(content.toCharArray());
	}
	
	/**
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		fPreviousPart = getActivePart();
		super.partActivated(part);
		checkActivePartChange();
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		fPreviousPart = getActivePart();
		super.partClosed(part);
		checkActivePartChange();
	}
	
	/**
	 * Check if the active part has changed, and if so, update this actions
	 * enablement.
	 */
	protected void checkActivePartChange() {
		if (fPreviousPart != getActivePart()) {
			if (getAction() != null) {
				getAction().setEnabled(computeEnabled());
			}
		}
	}
	
	/**
	 * Returns this delegate's action or <code>null</code>.
	 * 	 * @return IAction or <code>null</code>	 */
	protected IAction getAction() {
		return fAction;
	}
	
	/**
	 * Returns whether this action should currently be enabled.
	 * 	 * @return boolean	 */
	protected boolean computeEnabled() {
		if (getActivePart() instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor)getActivePart();
			String id = editor.getSite().getId();
			if (id.equals(JavaUI.ID_CF_EDITOR) || id.equals(JavaUI.ID_CU_EDITOR)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Displays the given exception in an error dialog.
	 */
	protected void errorDialog(CoreException e) {
		ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), "Error", null, e.getStatus());
	}
	
}
