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


import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
 
/**
 * Toggles whether a breakpoint suspends a VM or only
 * the event thread.
 */
public class BreakpointSuspendPolicyToggleAction extends BreakpointToggleAction {

	/**
	 * @see BreakpointToggleAction#doAction(IJavaBreakpoint)
	 */
	public void doAction(IJavaBreakpoint breakpoint) throws CoreException {
		if (breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_THREAD) {
			breakpoint.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
		} else {
			breakpoint.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		}
	}

	/**
	 * @see BreakpointToggleAction#getToggleState(IJavaBreakpoint)
	 */
	protected boolean getToggleState(IJavaBreakpoint breakpoint) {
		return false;
	}

	/**
	 * @see BreakpointToggleAction#isEnabledFor(IStructuredSelection)
	 */
	public boolean isEnabledFor(IStructuredSelection selection) {
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (!(element instanceof IJavaBreakpoint)) {
				return false;
			}
			
		}
		return true;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (action.isEnabled()) {
			IJavaBreakpoint bp = (IJavaBreakpoint)((IStructuredSelection)selection).getFirstElement();
			update(action, bp);
		}
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void update(IAction action, IJavaBreakpoint breakpoint) {
		try {
			if (breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_THREAD) {
				action.setText(ActionMessages.BreakpointSuspendPolicy_Suspend__VM_1); //$NON-NLS-1$
			} else {
				action.setText(ActionMessages.BreakpointSuspendPolicy_Suspend__Thread_2); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			 JDIDebugUIPlugin.log(e);
		}
	}	
}
