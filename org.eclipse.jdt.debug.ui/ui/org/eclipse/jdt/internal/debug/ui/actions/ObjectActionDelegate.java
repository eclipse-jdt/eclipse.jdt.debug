package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public abstract class ObjectActionDelegate implements IObjectActionDelegate {

	protected IStructuredSelection fCurrentSelection;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	
	protected abstract boolean isEnabledFor(Object element);
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fCurrentSelection= (IStructuredSelection)sel;
			update(action);
		}
	}
	
	protected void update(IAction action) {
		action.setEnabled(fCurrentSelection.size() == 1 && isEnabledFor(fCurrentSelection.getFirstElement()));
	}
	
	protected IStructuredSelection getStructuredSelection() {
		return fCurrentSelection;
	}
}
