package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;

/**
 * Toggles the caught state of an exception breakpoint
 */
public class ExceptionCaughtToggleAction extends BreakpointToggleAction {

	/**
	 * @see BreakpointToggleAction#getToggleState(IJavaBreakpoint)
	 */
	protected boolean getToggleState(IJavaBreakpoint breakpoint) throws CoreException {
		//will only be called after isEnabledFor so cast is safe
		IJavaExceptionBreakpoint exception= (IJavaExceptionBreakpoint)breakpoint;
		return exception.isCaught();
	}

	/**
	 * @see BreakpointToggleAction#doAction(IJavaBreakpoint)
	 */
	public void doAction(IJavaBreakpoint breakpoint) throws CoreException {
		//will only be called after isEnabledFor so cast is safe
		IJavaExceptionBreakpoint exception= (IJavaExceptionBreakpoint)breakpoint;
		exception.setCaught(!exception.isCaught());
	}
	
	public boolean isEnabledFor(Object element) {
		return element instanceof IJavaExceptionBreakpoint;
	}
}
