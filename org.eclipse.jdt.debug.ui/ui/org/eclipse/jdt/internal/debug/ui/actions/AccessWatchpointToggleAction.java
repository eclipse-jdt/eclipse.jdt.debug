package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jface.viewers.IStructuredSelection;

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
	 * @see BreakpointToggleAction#isEnabledFor(IStructuredSelection)
	 */
	public boolean isEnabledFor(IStructuredSelection selection) {
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (!(element instanceof IJavaWatchpoint)) {
				return false;
			}
			
		}
		return true;
	}
}

