package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Listens to Java model element changes and uninstalls breakpoints when the breakpoint
 * type's corresponding package fragment root is removed, closed, or removed from the classpath.
 */
class JavaModelListener implements IElementChangedListener {
	/**
	 * @see IElementChangedListener#elementChanged(ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent e) {
		IBreakpoint[] breakpoints= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugPlugin.getUniqueIdentifier());
		IJavaBreakpoint breakpoint= null;
		for (int i= 0, numBreakpoints= breakpoints.length; i < numBreakpoints; i++) {
			if (!(breakpoints[i] instanceof IJavaBreakpoint)) {
				continue;
			}
			breakpoint= (IJavaBreakpoint)breakpoints[i];
			try {
				IType type= BreakpointUtils.getType(breakpoint);
				if (type != null) {
					IJavaElement parent= type.getPackageFragment().getParent();
					check(breakpoint, parent, e.getDelta());
				}
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x);
			}
		}
	}
	
	/**
	 * Recursively check whether the class file has been deleted. 
	 * Returns true if delta processing can be stopped.
	 */
	protected boolean check(IJavaBreakpoint breakpoint, IJavaElement parent, IJavaElementDelta delta) throws CoreException {
		IJavaElement element= delta.getElement();
		if ((delta.getKind() & IJavaElementDelta.REMOVED) != 0 || (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0) { 
			if (element.equals(parent)) {
				DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
				return true;
			}
		} else if (((delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) && element.equals(parent)) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
			return true;
		}

		IJavaElementDelta[] subdeltas= delta.getAffectedChildren();
		for (int i= 0; i < subdeltas.length; i++) {
			if (check(breakpoint, parent, subdeltas[i])) {
				return true;
			}
		}

		return false;
	}
}