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
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.ui.progress.IElementCollector;

/**
 * Generates monitor information as well as stack frames
 */
public class DeferredJavaThread extends DeferredMonitorElement {

    /* (non-Javadoc)
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
        IJavaThread thread = (IJavaThread) object;
        if (isDisplayMonitors()) {
			ThreadMonitorManager threadMonitorManager= ThreadMonitorManager.getDefault();
			JavaOwnedMonitor[] ownedMonitors= threadMonitorManager.getOwnedMonitors(thread);
			if (ownedMonitors.length > 0) {
			    collector.add(ownedMonitors, monitor);
			}
			JavaContendedMonitor contendedMonitor= threadMonitorManager.getContendedMonitor(thread);
			if (contendedMonitor != null) {
			    collector.add(contendedMonitor, monitor);
			}
        }
		try {
            collector.add(thread.getStackFrames(), monitor);
        } catch (DebugException e) {
        }
        collector.done();
    }
}
