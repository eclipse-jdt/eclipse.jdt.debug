/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.ui.PlatformUI;

public class AddExceptionAction implements IViewActionDelegate, IWorkbenchWindowActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {		
		Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
		final AddExceptionDialog dialog= new AddExceptionDialog(shell, PlatformUI.getWorkbench().getProgressService());
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
}
