/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.JavaDebugUtils;

/**
 * Java thread presentation adapter.
 * 
 * @since 3.3
 */
public class JavaThreadContentProvider extends JavaElementContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected int getChildCount(Object element, IPresentationContext context, IProgressMonitor monitor) throws CoreException {
		IJavaThread thread = (IJavaThread)element;
		if (!thread.isSuspended()) {
			return 0;
		}
		int childCount = thread.getFrameCount();
		if (isDisplayMonitors()) {
			if (((IJavaDebugTarget) thread.getDebugTarget()).supportsMonitorInformation()) {
				childCount+= thread.getOwnedMonitors().length;
				if (thread.getContendedMonitor() != null) {
					childCount++;
				}
			} else {
				// unavailable notice
				childCount++;
			}
		}
		return childCount;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IProgressMonitor monitor) throws CoreException {
		IJavaThread thread = (IJavaThread)parent;
		if (!thread.isSuspended()) {
			return EMPTY;
		}
		return getElements(getChildren(thread), index, length);
	}
	
	protected Object[] getChildren(IJavaThread thread) {
		try {
			IStackFrame[] frames = thread.getStackFrames();
			if (!isDisplayMonitors()) {
				return frames;
			}

			Object[] children;
			int length = frames.length;
			if (((IJavaDebugTarget) thread.getDebugTarget()).supportsMonitorInformation()) {
				IDebugElement[] ownedMonitors = JavaDebugUtils.getOwnedMonitors(thread);
				IDebugElement contendedMonitor = JavaDebugUtils.getContendedMonitor(thread);

				if (ownedMonitors != null) {
					length += ownedMonitors.length;
				}
				if (contendedMonitor != null) {
					length++;
				}
				children = new Object[length];
				if (ownedMonitors != null && ownedMonitors.length > 0) {
					System.arraycopy(ownedMonitors, 0, children, 0, ownedMonitors.length);
				}
				if (contendedMonitor != null) {
					// Insert the contended monitor after the owned monitors
					children[ownedMonitors.length] = contendedMonitor;
				}
			} else {
				children = new Object[length + 1];
				children[0] = new NoMonitorInformationElement(thread.getDebugTarget());
			}
			int offset = children.length - frames.length;
			System.arraycopy(frames, 0, children, offset, frames.length);
			return children;
		} catch (DebugException e) {
			return EMPTY;
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context, IProgressMonitor monitor) throws CoreException {
		return ((IJavaThread)element).hasStackFrames() ||
			(isDisplayMonitors() && ((IJavaThread)element).hasOwnedMonitors());
	}
	
	

}
