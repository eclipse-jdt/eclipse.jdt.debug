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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
		final List removedRoots= new ArrayList();
		getRemovedPackageFragmentRoots(e.getDelta(), removedRoots);
		if (removedRoots.size() == 0) {
			return;
		}
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
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
							if (removedRoots.contains(type.getPackageFragment().getParent())) {
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
							}
						}
					} catch (CoreException x) {
						JDIDebugUIPlugin.log(x);
					}
				}
			}
		};
		fork(wr);
	}
	
	/**
	 * Recursively traverses the given java element delta looking for package
	 * fragment roots which have been removed, closed, or removed from the classpath
	 */
	protected void getRemovedPackageFragmentRoots(IJavaElementDelta delta, List removedRoots) {
		IJavaElement element= delta.getElement();
		if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
			if ((delta.getKind() & IJavaElementDelta.REMOVED) != 0 || (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0 ||
				(delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) {
				removedRoots.add(delta.getElement());
			}
			return; // Stop traversal once a package fragment root is encountered.
		}
		IJavaElementDelta[] subdeltas= delta.getAffectedChildren();
		for (int i= 0, numDeltas= subdeltas.length; i < numDeltas; i++) {
			getRemovedPackageFragmentRoots(subdeltas[i], removedRoots);
		}
	}
	
	protected void fork(final IWorkspaceRunnable wRunnable) {
		Runnable runnable= new Runnable() {
			public void run() {
				try {
					ResourcesPlugin.getWorkspace().run(wRunnable, null);
				} catch (CoreException ce) {
					DebugPlugin.log(ce);
				}
			}
		};
		new Thread(runnable).start();
	}	
}