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


import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides a general toggle action for breakpoints to reuse
 */
public abstract class BreakpointToggleAction implements IObjectActionDelegate, IBreakpointsListener, IPartListener {

	private IStructuredSelection fSelection;
	private IAction fAction;
	private IWorkbenchPart fPart;

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		IStructuredSelection selection= getStructuredSelection();
		Iterator<IJavaBreakpoint> itr= selection.iterator();
		while (itr.hasNext()) {
			try {
				IJavaBreakpoint breakpoint= itr.next();
				doAction(breakpoint);
			} catch (CoreException e) {
				String title= ActionMessages.BreakpointAction_Breakpoint_configuration_1;
				String message= ActionMessages.BreakpointAction_Exceptions_occurred_attempting_to_modify_breakpoint__2;
				ExceptionHandler.handle(e, title, message);
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		if (selection.isEmpty()) {
			setStructuredSelection(null);
			return;
		}
		if (selection instanceof IStructuredSelection) {
			setStructuredSelection((IStructuredSelection)selection);
			boolean enabled = isEnabledFor(getStructuredSelection());
			action.setEnabled(enabled);
			if (enabled && isToggleAction()) {
				IBreakpoint breakpoint = (IBreakpoint)getStructuredSelection().getFirstElement();
				if (breakpoint instanceof IJavaBreakpoint) {
					try {
						action.setChecked(getToggleState((IJavaBreakpoint) breakpoint));
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				}
			}
		}
	}

	/**
	 * Returns if the action is a checkable action. i.e. if we should bother updating checked state
	 * @return if the action is a checkable action
	 *
	 * @since 3.3
	 */
	protected boolean isToggleAction() {
		return true;
	}

	/**
	 * Toggle the state of this action
	 */
	public abstract void doAction(IJavaBreakpoint breakpoint) throws CoreException;

	/**
	 * Returns whether this action is currently toggled on
	 */
	protected abstract boolean getToggleState(IJavaBreakpoint breakpoint) throws CoreException;

	/**
	 * Get the current selection
	 */
	protected IStructuredSelection getStructuredSelection() {
		return fSelection;
	}

	/**
	 * Allows the current structured selection to be set
	 * @param selection the new selection
	 */
	protected void setStructuredSelection(IStructuredSelection selection) {
		fSelection= selection;
	}

	/**
	 * Returns if the underlying action should be enabled for the given selection
	 * @return if the underlying action should be enabled for the given selection
	 */
	public abstract boolean isEnabledFor(IStructuredSelection selection);

	/**
	 * Get the breakpoint manager for the debug plugin
	 */
	protected IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}

	/**
	 * Get the breakpoint associated with the given marker
	 */
	protected IBreakpoint getBreakpoint(IMarker marker) {
		return getBreakpointManager().getBreakpoint(marker);
	}

	/**
	 * Returns the underlying <code>IAction</code> for this delegate
	 * @return the underlying <code>IAction</code> for this delegate
	 */
	protected IAction getAction() {
		return fAction;
	}

	/**
	 * Allows the underlying <code>IAction</code> for this delegate to be set
	 * @param action the new action to set for this delegate
	 */
	protected void setAction(IAction action) {
		fAction = action;
	}

	/**
	 * @see IBreakpointsListener#breakpointsAdded(IBreakpoint[])
	 */
	@Override
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
	}

	/**
	 * @see IBreakpointsListener#breakpointsChanged(IBreakpoint[], IMarkerDelta[])
	 */
	@Override
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (getAction() != null) {
			IStructuredSelection selection= getStructuredSelection();
			if (selection != null) {
				IBreakpoint selectedBreakpoint= (IBreakpoint)selection.getFirstElement();
				for (IBreakpoint breakpoint : breakpoints) {
					if (selectedBreakpoint.equals(breakpoint)) {
						selectionChanged(getAction(), selection);
						return;
					}
				}
			}
		}
	}

	/**
	 * @see IBreakpointsListener#breakpointsRemoved(IBreakpoint[], IMarkerDelta[])
	 */
	@Override
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
	}

	/**
	 * Returns the <code>IWorkbenchPart</code> this delegate is associated with
	 * @return the <code>IWorkbenchPart</code> this delegate is associated with
	 */
	protected IWorkbenchPart getPart() {
		return fPart;
	}

	/**
	 * Allows the <code>IWorkbenchPart</code> to be set for this delegate
	 * @param part the new part to set
	 */
	protected void setPart(IWorkbenchPart part) {
		fPart = part;
	}

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part == getPart()) {
			getBreakpointManager().removeBreakpointListener(this);
			part.getSite().getPage().removePartListener(this);
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		IWorkbenchPart oldPart= getPart();
		if (oldPart != null) {
			getPart().getSite().getPage().removePartListener(this);
		}

		getBreakpointManager().addBreakpointListener(this);
		setPart(targetPart);
		targetPart.getSite().getPage().addPartListener(this);
	}
}

