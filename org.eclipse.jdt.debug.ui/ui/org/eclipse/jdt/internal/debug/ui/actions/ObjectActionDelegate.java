package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

public abstract class ObjectActionDelegate implements IObjectActionDelegate {
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		update(action, targetPart);
	}
	
	protected abstract boolean isEnabledFor(Object element);
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
	}
	
	protected void update(IAction action, IWorkbenchPart targetPart) {
		ISelectionProvider sp= targetPart.getSite().getSelectionProvider();
		boolean enable= false;
		if (sp != null) {
			ISelection selection= sp.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection)selection;
				enable= ss.size() == 1 && isEnabledFor(ss.getFirstElement());
			}
		}
		action.setEnabled(enable);
	}
	
	protected IStructuredSelection getCurrentSelection() {
		IWorkbenchPage page= JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			ISelection selection= page.getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection)selection;
			}	
		}
		return null;
	}
}
