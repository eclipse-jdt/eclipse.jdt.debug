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
 * Object used to display contended monitor in the debug launch view.
 * In this case, the monitor is owned by the owning thread, and waited
 * by the parent thread.
 */
public class JavaContendedMonitor extends PlatformObject implements IDebugElement {

	/**
	 * The monitor object in the threads and monitors model.
	 */
	private JavaMonitor fMonitor;
	/**
	 * The thread which owns this monitor.
	 */
	private JavaOwningThread fOwningThread;
	/**
	 * The parent, in the debug view tree.
	 */
	private JavaOwningThread fParent;

	public JavaContendedMonitor(JavaMonitor monitor, JavaOwningThread parent) {
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
	
	public JavaOwningThread getOwningThread() {
		JavaMonitorThread owningThread= fMonitor.getOwningThread0();
		if (owningThread == null) {
			fOwningThread= null;
		} else if (fOwningThread == null || fOwningThread.getThread() != owningThread) {
			// create a new object only if thread from the model changed
			fOwningThread= new JavaOwningThread(owningThread, this);
		}
		return fOwningThread;
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
