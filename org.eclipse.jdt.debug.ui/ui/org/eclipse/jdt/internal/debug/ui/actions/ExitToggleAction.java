package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;

public class ExitToggleAction extends BreakpointToggleAction {

	/**
	 * @see BreakpointToggleAction#getToggleState(IJavaBreakpoint)
	 */
	protected boolean getToggleState(IJavaBreakpoint breakpoint) throws CoreException {
		return ((IJavaMethodBreakpoint)breakpoint).isExit();
	}

	/**
	 * @see BreakpointToggleAction#doAction(IJavaBreakpoint)
	 */
	public void doAction(IJavaBreakpoint breakpoint) throws CoreException {
		((IJavaMethodBreakpoint)breakpoint).setExit(!((IJavaMethodBreakpoint)breakpoint).isExit());
	}

	/**
	 * @see BreakpointToggleAction#isEnabledFor(Object)
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IJavaMethodBreakpoint;
	}
}

