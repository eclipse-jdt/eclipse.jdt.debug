/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ExitToggleAction extends BreakpointToggleAction {

	/**
	 * @see BreakpointToggleAction#getToggleState(IJavaBreakpoint)
	 */
	@Override
	protected boolean getToggleState(IJavaBreakpoint breakpoint) throws CoreException {
		return ((IJavaMethodBreakpoint)breakpoint).isExit();
	}

	/**
	 * @see BreakpointToggleAction#doAction(IJavaBreakpoint)
	 */
	@Override
	public void doAction(IJavaBreakpoint breakpoint) throws CoreException {
		((IJavaMethodBreakpoint)breakpoint).setExit(!((IJavaMethodBreakpoint)breakpoint).isExit());
	}

	/**
	 * @see BreakpointToggleAction#isEnabledFor(IStructuredSelection)
	 */
	@Override
	public boolean isEnabledFor(IStructuredSelection selection) {
		Iterator<?> iter= selection.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (!(element instanceof IJavaMethodBreakpoint)) {
				return false;
			}
			
		}
		return true;
	}
}

