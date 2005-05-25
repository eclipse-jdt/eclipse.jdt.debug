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

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Object used to display waiting thread in the debug launch view.
 * In this case, the thread owns for the owned monitors, and is waiting
 * for the parent monitor.
 */
public class JavaWaitingThread implements IDebugElement {

	/**
	 * The thread object in the thread and monitor model.
	 */
	private JavaMonitorThread fThread;

	/**
	 * The monitors this thread owns.
	 */
	private JavaOwnedMonitor[] fOwnedMonitors;
	/**
	 * The parent, in the debug view tree.
	 */
	private JavaOwnedMonitor fParent;

	public JavaWaitingThread(JavaMonitorThread thread, JavaOwnedMonitor parent) {
		fThread= thread;
		thread.addElement(this);
		fParent= parent;
	}

	public JavaMonitorThread getThread() {
		return fThread;
	}
	
	public JavaOwnedMonitor getParent() {
		return fParent;
	}

	public JavaOwnedMonitor[] getOwnedMonitors() {
		JavaMonitor[] ownedMonitors= fThread.getOwnedMonitors0();
		JavaOwnedMonitor[] tmp= new JavaOwnedMonitor[ownedMonitors.length];
		if (fOwnedMonitors == null) {
			// the list was empty, creating new objects
			for (int i= 0; i < ownedMonitors.length; i++) {
				tmp[i]= new JavaOwnedMonitor(ownedMonitors[i], this);
			}
		} else {
			// trying to reuse the objects from the previous list
	outer:	for (int i= 0; i < ownedMonitors.length; i++) {
				JavaMonitor ownedMonitor= ownedMonitors[i];
				for (int j= 0; j < fOwnedMonitors.length; j++) {
					if (fOwnedMonitors[j].getMonitor() == ownedMonitor) {
						tmp[i]= fOwnedMonitors[j];
						continue outer;
					}
				}
				tmp[i]= new JavaOwnedMonitor(ownedMonitor, this);
			}
		}
		fOwnedMonitors= tmp;
		return fOwnedMonitors;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return fThread.getModelIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fThread.getDebugTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fThread.getLaunch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fThread.isSuspended();
	}
}
