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
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jface.viewers.IStructuredSelection;

public class EntryToggleAction extends BreakpointToggleAction {

	/**
	 * @see BreakpointToggleAction#getToggleState(IJavaBreakpoint)
	 */
	protected boolean getToggleState(IJavaBreakpoint breakpoint) throws CoreException {
		return ((IJavaMethodBreakpoint)breakpoint).isEntry();
	}

	/**
	 * @see BreakpointToggleAction#doAction(IJavaBreakpoint)
	 */
	public void doAction(IJavaBreakpoint breakpoint) throws CoreException {
		((IJavaMethodBreakpoint)breakpoint).setEntry(!((IJavaMethodBreakpoint)breakpoint).isEntry());
	}

	/**
	 * @see BreakpointToggleAction#isEnabledFor(IStructuredSelection)
	 */
	public boolean isEnabledFor(IStructuredSelection selection) {
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (!(element instanceof IJavaMethodBreakpoint)) {
				return false;
			}
			
		}
		return true;
	}
}

