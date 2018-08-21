/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.variables.JavaStackFrameContentProvider;

/**
 * Adapter factory that generates content adapters for java debug elements to
 * provide thread monitor information in the debug view.
 */
public class MonitorsAdapterFactory implements IAdapterFactory {

    private static IElementContentProvider fgCPThread;
    private static IElementContentProvider fgCPFrame = new JavaStackFrameContentProvider();
    private static IElementContentProvider fgCPOwnedMonitor;
    private static IElementContentProvider fgCPWaitingThread;
    private static IElementContentProvider fgCPContendedMonitor;
    private static IElementContentProvider fgCPOwningThread;

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {

    	if (IElementContentProvider.class.equals(adapterType)) {
    		if (adaptableObject instanceof IJavaThread) {
				return (T) getThreadPresentation();
	        }
    		if (adaptableObject instanceof IJavaStackFrame) {
				return (T) fgCPFrame;
    		}
    		if (adaptableObject instanceof JavaOwnedMonitor) {
				return (T) getOwnedMonitorContentProvider();
    		}
    		if (adaptableObject instanceof JavaWaitingThread) {
				return (T) getWaitingThreadContentProvider();
    		}
    		if (adaptableObject instanceof JavaContendedMonitor) {
				return (T) getContendedMonitorContentProvider();
    		}
    		if (adaptableObject instanceof JavaOwningThread) {
				return (T) getOwningThreadContentProvider();
    		}
    	}
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
	@Override
	public Class<?>[] getAdapterList() {
        return new Class[] {IElementContentProvider.class};
    }

	private IElementContentProvider getThreadPresentation() {
		if (fgCPThread == null) {
			fgCPThread = new JavaThreadContentProvider();
		}
		return fgCPThread;
	}

	private IElementContentProvider getOwnedMonitorContentProvider() {
		if (fgCPOwnedMonitor == null) {
			fgCPOwnedMonitor = new OwnedMonitorContentProvider();
		}
		return fgCPOwnedMonitor;
	}

	private IElementContentProvider getWaitingThreadContentProvider() {
		if (fgCPWaitingThread == null) {
			fgCPWaitingThread = new WaitingThreadContentProvider();
		}
		return fgCPWaitingThread;
	}

	private IElementContentProvider getContendedMonitorContentProvider() {
		if (fgCPContendedMonitor == null) {
			fgCPContendedMonitor = new ContendedMonitorContentProvider();
		}
		return fgCPContendedMonitor;
	}

	private IElementContentProvider getOwningThreadContentProvider() {
		if (fgCPOwningThread == null) {
			fgCPOwningThread = new OwningThreadContentProvider();
		}
		return fgCPOwningThread;
	}
}
