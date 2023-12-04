/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class ObjectActionDelegate implements IObjectActionDelegate, IActionDelegate2 {

	IWorkbenchPart fPart = null;

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fPart = targetPart;
	}

	protected IWorkbenchPart getPart() {
		return fPart;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection sel) {
	}

	/**
	 * Returns the currently selected item(s) from the current workbench page or <code>null</code>
	 * if the current active page could not be resolved.
	 * @return the currently selected item(s) or <code>null</code>
	 */
	protected IStructuredSelection getCurrentSelection() {
		IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
		if (page != null) {
			ISelection selection= page.getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection)selection;
			}
		}
		return null;
	}

	/**
	 * Displays the given error message in the status line.
	 */
	protected void showErrorMessage(String message) {
		if (fPart instanceof IViewPart) {
			IViewSite viewSite = ((IViewPart)fPart).getViewSite();
			IStatusLineManager manager = viewSite.getActionBars().getStatusLineManager();
			manager.setErrorMessage(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	@Override
	public void dispose() {
		fPart = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/**
	 * Returns the workbench window this action is installed in, or <code>null</code>
	 */
	protected IWorkbenchWindow getWorkbenchWindow() {
		if (fPart != null) {
			return fPart.getSite().getWorkbenchWindow();
		}
		return null;
	}
}
