/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * Presents the standard properties dialog to configure
 * the attibutes of a Java Breakpoint.
 */
public class JavaBreakpointPropertiesAction implements IObjectActionDelegate {
	
	private IWorkbenchPart fPart;
	private IJavaBreakpoint fBreakpoint;

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		PropertyDialogAction propertyAction= 
			new PropertyDialogAction(JDIDebugUIPlugin.getActiveWorkbenchShell(), new ISelectionProvider() {
				public void addSelectionChangedListener(ISelectionChangedListener listener) {
				}
				public ISelection getSelection() {
					return new StructuredSelection(getBreakpoint());
				}
				public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				}
				public void setSelection(ISelection selection) {
				}
			});
		propertyAction.run();
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
		}
	}
	
	protected IWorkbenchPart getActivePart() {
		return fPart;
	}

	protected void setActivePart(IWorkbenchPart part) {
		fPart = part;
	}
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	public void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		setActivePart(targetPart);
	}
}
