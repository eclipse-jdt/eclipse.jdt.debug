/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.JavaDebugUtils;
import org.eclipse.ui.progress.IElementCollector;

/**
 * Generates monitor information as well as stack frames
 */
public class DeferredJavaThread extends DeferredMonitorElement {

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parent) {
        IJavaThread thread = (IJavaThread) parent;
        try {
            IStackFrame[] frames = thread.getStackFrames();
            if (!isDisplayMonitors()) {
                return frames;
            }
            IDebugElement[] ownedMonitors = JavaDebugUtils.getOwnedMonitors(thread);
            IDebugElement contendedMonitor = JavaDebugUtils.getContendedMonitor(thread);
            Object[] children;
            int length = frames.length;
            if (((IJavaDebugTarget) thread.getDebugTarget()).supportsMonitorInformation()) {
                if (ownedMonitors != null) {
                    length+=ownedMonitors.length;
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
                children= new Object[length + 1];
                children[0]= new NoMonitorInformationElement(thread.getDebugTarget());
            }
            int offset= children.length - frames.length;
            System.arraycopy(frames, 0, children, offset, frames.length);
            return children;
        } catch (DebugException e) {
            return EMPTY;
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
        IJavaThread thread = (IJavaThread) object;
        if (isDisplayMonitors()) {
        	if (((IJavaDebugTarget) thread.getDebugTarget()).supportsMonitorInformation()) {
				IDebugElement[] ownedMonitors= JavaDebugUtils.getOwnedMonitors(thread);
				if (ownedMonitors.length > 0) {
				    collector.add(ownedMonitors, monitor);
				}
				IDebugElement contendedMonitor= JavaDebugUtils.getContendedMonitor(thread);
				if (contendedMonitor != null) {
				    collector.add(contendedMonitor, monitor);
				}
        	} else {
        		collector.add(new NoMonitorInformationElement(thread.getDebugTarget()), monitor);
        	}
        }
		try {
            collector.add(thread.getStackFrames(), monitor);
        } catch (DebugException e) {
        }
        collector.done();
    }
}
