/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

 
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Presents the standard properties dialog to configure
 * the attributes of a Java Breakpoint.
 */
public class JavaBreakpointPropertiesAction implements IObjectActionDelegate {
	
	private IJavaBreakpoint fBreakpoint;

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//hack to prevent https://bugs.eclipse.org/bugs/show_bug.cgi?id=269878
		//where conditions randomly seem to have errors while using an IBM VM in testing mode
		if(fBreakpoint != null && !ErrorDialog.AUTOMATED_MODE) {
		    PreferencesUtil.createPropertyDialogOn(
		    		JDIDebugUIPlugin.getActiveWorkbenchShell(), 
		    		fBreakpoint, 
		    		null, 
		    		null, 
		    		null).open();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection)selection;
			if (ss.isEmpty() || ss.size() > 1) {
				return;
			}
			Object element= ss.getFirstElement();
			if (element instanceof IJavaBreakpoint) {
				setBreakpoint((IJavaBreakpoint)element);
			}
			else {
				setBreakpoint(null);
			}
		}
	}

	/**
	 * Allows the underlying breakpoint for the properties page to be set
	 * @param breakpoint
	 */
	public void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
}
