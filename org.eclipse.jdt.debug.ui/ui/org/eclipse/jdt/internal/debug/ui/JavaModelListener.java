/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Listens to Java model element changes and uninstalls breakpoints when:<ul>
 *  <li>the breakpoint type's corresponding package fragment root is removed, closed, or removed from the classpath.</li>
 *  <li>the breakpoints is a method breakpoint and the method has been removed.</li>
 * </ul>
 */
class JavaModelListener implements IElementChangedListener {
	/**
	 * @see IElementChangedListener#elementChanged(ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent e) {
		if (e.getType() != ElementChangedEvent.POST_CHANGE) {
			return;
		}
		List removedElements= new ArrayList();
		getRemovedElements(e.getDelta(), removedElements);
		if (removedElements.isEmpty()) {
			return;
		}
				
		final List breakpointsToRemove= new ArrayList();
		IBreakpoint[] breakpoints= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugPlugin.getUniqueIdentifier());
		IJavaBreakpoint breakpoint= null;
		for (int i= 0, numBreakpoints= breakpoints.length; i < numBreakpoints; i++) {
			breakpoint= (IJavaBreakpoint)breakpoints[i];
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint methodBreakpoint= (IJavaMethodBreakpoint)breakpoint;
				IMethod method= null;
				try {
					method = BreakpointUtils.getMethod(methodBreakpoint);
				} catch (CoreException x) {
					JDIDebugUIPlugin.log(x);
				}
				if (method != null && containedInCollection(method, removedElements)) {
					breakpointsToRemove.add(breakpoint);
				}
				continue;
			}
			
			try {
				IType type= BreakpointUtils.getType(breakpoint);
				if (type != null && containedInCollection(type, removedElements)) {
					breakpointsToRemove.add(breakpoint);
				}
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x);
			}
		}
		if (!breakpointsToRemove.isEmpty()) {
			IWorkspaceRunnable wr = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					Iterator itr= breakpointsToRemove.iterator();
					while (itr.hasNext()) {
						IBreakpoint breakpointToRemove = (IBreakpoint) itr.next();
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpointToRemove, true);	
					}	
				}
			};
			
			fork(wr);
		} 
	}
	
	/**
	 * Recursively traverses the given Java element delta looking for Java elements
	 * which have been removed, closed, or removed from the classpath
	 */
	protected void getRemovedElements(IJavaElementDelta delta, List removedElements) {
		if ((delta.getKind() & IJavaElementDelta.REMOVED) != 0 || (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0 ||
			(delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) {
			removedElements.add(delta.getElement());
		}
			
		IJavaElementDelta[] subdeltas= delta.getAffectedChildren();
		for (int i= 0, numDeltas= subdeltas.length; i < numDeltas; i++) {
			getRemovedElements(subdeltas[i], removedElements);
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
	
	protected boolean containedInCollection(IJavaElement element, List removedElements) {
		if (removedElements.contains(element)) {
			return true;
		}
		IJavaElement parent= element.getParent();
		while (parent != null) {
			if(removedElements.contains(parent)) {
				return true;
			}
			parent= parent.getParent();
		}
		return false;
	}
}
