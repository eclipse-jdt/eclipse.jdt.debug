package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
 
/**
 * Toggles whether a breakpoint suspends a VM or only
 * the event thread.
 */
public class BreakpointThreadToggleAction extends BreakpointToggleAction {

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
	protected boolean getToggleState(IJavaBreakpoint breakpoint)
		throws CoreException {
		return false;
	}

	/**
	 * @see BreakpointToggleAction#isEnabledFor(Object)
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IJavaBreakpoint;
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
				action.setText(ActionMessages.getString("BreakpointSuspendPolicy.Suspend_&VM_1")); //$NON-NLS-1$
			} else {
				action.setText(ActionMessages.getString("BreakpointSuspendPolicy.Suspend_&Thread_2")); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			 
		}
	}	

}
