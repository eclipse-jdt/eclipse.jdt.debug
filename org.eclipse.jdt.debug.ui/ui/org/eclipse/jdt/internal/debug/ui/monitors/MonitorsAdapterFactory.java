/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
    public Object getAdapter(Object adaptableObject, Class adapterType) {
		
    	if (IElementContentProvider.class.equals(adapterType)) {
    		if (adaptableObject instanceof IJavaThread) {
	        	return getThreadPresentation();
	        }
    		if (adaptableObject instanceof IJavaStackFrame) {
    			return fgCPFrame;
    		}
    		if (adaptableObject instanceof JavaOwnedMonitor) {
    			return getOwnedMonitorContentProvider();
    		}
    		if (adaptableObject instanceof JavaWaitingThread) {
    			return getWaitingThreadContentProvider();
    		}
    		if (adaptableObject instanceof JavaContendedMonitor) {
    			return getContendedMonitorContentProvider();
    		}
    		if (adaptableObject instanceof JavaOwningThread) {
    			return getOwningThreadContentProvider();
    		}
    	}
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
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
