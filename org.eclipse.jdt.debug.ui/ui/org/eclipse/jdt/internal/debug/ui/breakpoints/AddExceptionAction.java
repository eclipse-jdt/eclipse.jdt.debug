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
package org.eclipse.jdt.internal.debug.ui.breakpoints;


import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class AddExceptionAction implements IViewActionDelegate, IWorkbenchWindowActionDelegate {

	public void run(IAction action) {		
		Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
		final AddExceptionDialog dialog= new AddExceptionDialog(shell, new ProgressMonitorDialog(shell));
		dialog.setTitle(BreakpointMessages.getString("AddExceptionAction.0")); //$NON-NLS-1$
		dialog.setMessage(BreakpointMessages.getString("AddExceptionAction.1"));		 //$NON-NLS-1$
		if (dialog.open() == Window.OK) {
			Runnable r = new Runnable() {
				public void run() {
					IViewPart part = JDIDebugUIPlugin.getActivePage().findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
					if (part instanceof IDebugView) {
						Viewer viewer = ((IDebugView)part).getViewer();
						if (viewer instanceof StructuredViewer) {
							StructuredViewer sv = (StructuredViewer)viewer;
							sv.setSelection(new StructuredSelection(dialog.getResult()), true);
						}
					}
				}
			};
			JDIDebugUIPlugin.getStandardDisplay().asyncExec(r);
		}
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
}
