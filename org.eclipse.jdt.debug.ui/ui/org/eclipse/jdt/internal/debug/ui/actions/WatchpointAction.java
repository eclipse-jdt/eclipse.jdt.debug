package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.BreakpointsView;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public abstract class WatchpointAction extends Action implements IViewActionDelegate, IBreakpointListener {
	
	private IStructuredSelection fCurrentSelection;
	private IAction fAction;
	
	public WatchpointAction() {
		setEnabled(false);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		run(null);
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart viewPart) {
		if (viewPart instanceof BreakpointsView) {
			((BreakpointsView)viewPart).addBreakpointListenerAction(this);
		}
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		fAction= action;
		IStructuredSelection selection= getStructuredSelection();
		Iterator enum= selection.iterator();
		while (enum.hasNext()) {
			try {
				IJavaWatchpoint breakpoint= (IJavaWatchpoint) enum.next();
				doAction(breakpoint);
			} catch (CoreException e) {
				String title= ActionMessages.getString("WatchPointAction.Watchpoint_configuration_1"); //$NON-NLS-1$
				String message= ActionMessages.getString("WatchPointAction.Exceptions_occurred_attempting_to_modify_watchpoint._2"); //$NON-NLS-1$
				ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell(), title, message, e.getStatus());
			}			
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection.isEmpty()) {
			fCurrentSelection= null;
		}
		else if (selection instanceof IStructuredSelection) {
			fCurrentSelection= (IStructuredSelection)selection;
			boolean enabled= fCurrentSelection.size() == 1 && isEnabledFor(fCurrentSelection.getFirstElement());
			action.setEnabled(enabled);
			if (enabled) {
				IBreakpoint breakpoint= (IBreakpoint)fCurrentSelection.getFirstElement();
				if (breakpoint instanceof IJavaWatchpoint) {
					try {
						action.setChecked(getToggleState((IJavaWatchpoint) breakpoint));
					} catch (CoreException e) {
					}
				}
			}
		}
	}

	/**
	 * Toggle the state of this action
	 */
	public abstract void doAction(IJavaWatchpoint watchpoint) throws CoreException;
	
	/**
	 * Returns whether this action is currently toggled on
	 */
	protected abstract boolean getToggleState(IJavaWatchpoint watchpoint) throws CoreException;
	
	/**
	 * Get the current selection
	 */
	protected IStructuredSelection getStructuredSelection() {
		return fCurrentSelection;
	}
	
	public boolean isEnabledFor(Object element) {
		return element instanceof IJavaWatchpoint;
	}
	
	/** 
	 * @see IBreakpointListener#breakpointAdded(IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
	}

	/** 
	 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/** 
	 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		final Display display= Display.getDefault();
		if (display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			public void run() {
				if (fAction != null && fCurrentSelection != null) {
					selectionChanged(fAction, fCurrentSelection);
				}
			}
		});
	}	

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
}

