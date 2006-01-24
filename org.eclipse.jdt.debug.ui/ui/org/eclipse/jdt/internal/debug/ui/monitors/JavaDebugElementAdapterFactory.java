/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.IAsynchronousContentAdapter;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Adapter factory that generates workbench adapters for java debug elements to
 * provide thread monitor information in the debug veiw.
 */
public class JavaDebugElementAdapterFactory implements IAdapterFactory {
    
    private static IAsynchronousContentAdapter fgThreadAdapter;
    private static IAsynchronousContentAdapter fgContendedMonitorAdapter;
    private static IAsynchronousContentAdapter fgOwnedMonitorAdapter;
    private static IAsynchronousContentAdapter fgOwningThreadAdapter;
    private static IAsynchronousContentAdapter fgWaitingThreadAdapter;
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof IJavaThread) {
            return getThreadAdapter();
        }
        if (adaptableObject instanceof JavaContendedMonitor) {
            return getContendedMonitorAdapter();
        }
        if (adaptableObject instanceof JavaOwnedMonitor) {
            return getOwnedMonitorAdapater();
        }
        if (adaptableObject instanceof JavaOwningThread) {
            return getOwningThreadAdapter();
        }
        if (adaptableObject instanceof JavaWaitingThread) {
            return getWaitingThreadAdapter();
        }
        return null;
    }

    private Object getWaitingThreadAdapter() {
        if (fgWaitingThreadAdapter == null) {
            fgWaitingThreadAdapter = new AsyncJavaWaitingThreadAdapter();
        }
        return fgWaitingThreadAdapter;
    }

    private Object getOwningThreadAdapter() {
        if (fgOwningThreadAdapter == null) {
            fgOwningThreadAdapter = new AsyncJavaOwningThreadAdapter();
        }
        return fgOwningThreadAdapter;
    }

    private IAsynchronousContentAdapter getOwnedMonitorAdapater() {
        if (fgOwnedMonitorAdapter == null) {
            fgOwnedMonitorAdapter = new AsyncJavaOwnedMonitorAdapter();
        }
        return fgOwnedMonitorAdapter;
    }

    private IAsynchronousContentAdapter getContendedMonitorAdapter() {
        if (fgContendedMonitorAdapter == null) {
            fgContendedMonitorAdapter = new AsyncJavaContendedMonitorAdapter();
        }
        return fgContendedMonitorAdapter;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return new Class[] {IAsynchronousContentAdapter.class};
    }
	
	private IAsynchronousContentAdapter getThreadAdapter() {
	    if (fgThreadAdapter == null) {
	        fgThreadAdapter = new AsyncJavaThreadAdapter();
	    }
	    return fgThreadAdapter;
	}
}
