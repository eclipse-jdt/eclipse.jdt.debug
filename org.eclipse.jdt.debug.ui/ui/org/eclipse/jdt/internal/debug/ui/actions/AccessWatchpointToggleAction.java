package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;

public class AccessWatchpointToggleAction extends BreakpointToggleAction {

	/**
	 * @see BreakpointToggleAction#getToggleState(IJavaBreakpoint)
	 */
	protected boolean getToggleState(IJavaBreakpoint watchpoint) throws CoreException {
		return ((IJavaWatchpoint)watchpoint).isAccess();
	}

	/**
	 * @see BreakpointToggleAction#doAction(IJavaBreakpoint)
	 */
	public void doAction(IJavaBreakpoint watchpoint) throws CoreException {
		((IJavaWatchpoint)watchpoint).setAccess(!((IJavaWatchpoint)watchpoint).isAccess());
	}

	/**
	 * @see BreakpointToggleAction#isEnabledFor(Object)
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IJavaWatchpoint;
	}

}

