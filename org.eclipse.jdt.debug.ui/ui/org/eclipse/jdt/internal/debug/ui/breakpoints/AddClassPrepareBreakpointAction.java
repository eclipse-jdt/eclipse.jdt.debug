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


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.SelectionDialog;

public class AddClassPrepareBreakpointAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;
	
	public void run(IAction action) {		
		Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
		SelectionDialog dialog = null;
		try {
			dialog = JavaUI.createTypeDialog(shell, workbenchWindow, SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_TYPES, true);
			dialog.setTitle(BreakpointMessages.getString("AddClassPrepareBreakpointAction.0")); //$NON-NLS-1$
			dialog.setMessage(BreakpointMessages.getString("AddClassPrepareBreakpointAction.1")); //$NON-NLS-1$
			if (dialog.open() == Window.OK) {
				final Object[] selection = dialog.getResult();
				for (int i = 0; i < selection.length; i++) {
					IType type = (IType) selection[i];
					IResource resource = BreakpointUtils.getBreakpointResource(type);
					Map map = new HashMap(10);
					BreakpointUtils.addJavaBreakpointAttributes(map, type);
					int kind = IJavaClassPrepareBreakpoint.TYPE_CLASS;
					if (!type.isClass()) {
						kind = IJavaClassPrepareBreakpoint.TYPE_INTERFACE;
					}
					IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
					boolean exists = false;
					for (int j = 0; j < breakpoints.length; j++) {
						IJavaBreakpoint breakpoint = (IJavaBreakpoint) breakpoints[j];
						if (breakpoint instanceof IJavaClassPrepareBreakpoint) {
							if (breakpoint.getTypeName().equals(type.getFullyQualifiedName())) {
								exists = true;
								break;
							}
						}
					}
					if (!exists) {
						JDIDebugModel.createClassPrepareBreakpoint(resource, type.getFullyQualifiedName(), kind, true, map);
					}
				}
				Runnable r = new Runnable() {
					public void run() {
						IViewPart part = JDIDebugUIPlugin.getActivePage().findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
						if (part instanceof IDebugView) {
							Viewer viewer = ((IDebugView)part).getViewer();
							if (viewer instanceof StructuredViewer) {
								StructuredViewer sv = (StructuredViewer)viewer;
								sv.setSelection(new StructuredSelection(selection), true);
							}
						}
					}
				};
				JDIDebugUIPlugin.getStandardDisplay().asyncExec(r);
			}			
		} catch (CoreException e) {
			// TODO:
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		workbenchWindow = null;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		workbenchWindow = window;
	}
}
