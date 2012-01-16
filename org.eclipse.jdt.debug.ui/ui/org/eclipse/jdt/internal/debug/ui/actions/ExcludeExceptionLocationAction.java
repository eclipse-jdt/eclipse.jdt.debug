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
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

public class ExcludeExceptionLocationAction extends ObjectActionDelegate {

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		if (selection == null) {
			return;
		}
		Iterator<IJavaThread> itr= selection.iterator();
		
		while (itr.hasNext()) {
			IJavaThread thread= itr.next();
			try {
				IBreakpoint[] breakpoints= thread.getBreakpoints();
				IJavaStackFrame frame= (IJavaStackFrame)thread.getTopStackFrame();
				String newFilter= frame.getDeclaringTypeName();
				int index = newFilter.indexOf(Signature.C_GENERIC_START);
				if (index > 0) {
					newFilter = newFilter.substring(0, index);
				}
				for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint = breakpoints[i];
					if (breakpoint instanceof IJavaExceptionBreakpoint) {
						IJavaExceptionBreakpoint exBreakpoint= (IJavaExceptionBreakpoint)breakpoint;
						String[] current= exBreakpoint.getExclusionFilters();
						String[] newFilters= new String[current.length+1];
						System.arraycopy(current, 0, newFilters, 0, current.length);
						newFilters[current.length]= newFilter;
						exBreakpoint.setExclusionFilters(newFilters);
						action.setEnabled(false);
					}
				}
			} catch (CoreException de) {
				JDIDebugUIPlugin.log(de);
			}
		}
	}
}
