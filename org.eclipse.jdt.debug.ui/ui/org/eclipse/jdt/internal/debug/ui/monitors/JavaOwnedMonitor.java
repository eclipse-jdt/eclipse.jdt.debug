/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Object used to display owned monitor in the debug launch view.
 * In this case, the monitor is waited by the waiting threads, and owned
 * by the parent thread.
 */
public class JavaOwnedMonitor extends PlatformObject implements IDebugElement {
	
	/**
	 * The monitor object in the thread and monitor model.
	 */
	private JavaMonitor fMonitor;
	
	/**
	 * The threads waiting for this monitor.
	 */
	private JavaWaitingThread[] fWaitingThreads;
	/**
	 * The parent, in the debug view tree.
	 */
	private JavaWaitingThread fParent;

	public JavaOwnedMonitor(JavaMonitor monitor, JavaWaitingThread parent) {
		fMonitor= monitor;
		monitor.addElement(this);
		fParent= parent;
	}
	
	public JavaMonitor getMonitor() {
		return fMonitor;
	}
	
	public Object getParent() {
		if (fParent.getParent() == null) {
			return fParent.getThread().getOriginalThread();
		}
		return fParent;
	}
	
	public JavaWaitingThread[] getWaitingThreads() {
		JavaMonitorThread[] waitingThreads= fMonitor.getWaitingThreads0();
		JavaWaitingThread[] tmp= new JavaWaitingThread[waitingThreads.length];
		if (fWaitingThreads == null) {
			// the list was empty, creating new objects
			for (int i= 0; i < waitingThreads.length; i++) {
				tmp[i]= new JavaWaitingThread(waitingThreads[i], this);
			}
		} else {
			// trying to reuse the objects from the previous list
	outer:	for (int i= 0; i < waitingThreads.length; i++) {
				JavaMonitorThread waitingThread= waitingThreads[i];
				for (int j= 0; j < fWaitingThreads.length; j++) {
					if (fWaitingThreads[j].getThread() == waitingThread) {
						tmp[i]= fWaitingThreads[j];
						continue outer;
					}
				}
				tmp[i]= new JavaWaitingThread(waitingThread, this);
			}
		}
		fWaitingThreads= tmp;
		return fWaitingThreads;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return fMonitor.getModelIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fMonitor.getDebugTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fMonitor.getLaunch();
	}
}
