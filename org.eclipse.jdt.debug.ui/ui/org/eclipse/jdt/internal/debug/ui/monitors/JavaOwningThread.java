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
 * Object used to display owning thread in the debug launch view.
 * In this case, the thread is waiting for the contended monitor,
 * and owns the parent monitor.
 */
public class JavaOwningThread extends PlatformObject implements IDebugElement {
	
	/**
	 * The thread object in the thread and monitor model.
	 */
	private JavaMonitorThread fThread;
	
	/**
	 * The monitor this thread is waiting for.
	 */
	private JavaContendedMonitor fContendedMonitor;
	/**
	 * The parent, in the debug view tree.
	 */
	private JavaContendedMonitor fParent;

	public JavaOwningThread(JavaMonitorThread thread, JavaContendedMonitor parent) {
		fThread= thread;
		thread.addElement(this);
		fParent= parent;
	}

	
	public JavaMonitorThread getThread() {
		return fThread;
	}
	
	public JavaContendedMonitor getParent() {
		return fParent;
	}

	public JavaContendedMonitor getContendedMonitor() {
		JavaMonitor contendedMonitor= fThread.getContendedMonitor0();
		if (contendedMonitor == null) {
			fContendedMonitor= null;
		} else if (fContendedMonitor == null || fContendedMonitor.getMonitor() != contendedMonitor) {
			// create a new object only if the monitor from the model changed
			fContendedMonitor= new JavaContendedMonitor(contendedMonitor, this);
		}
		return fContendedMonitor;
	}
	
	public void update() {
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

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fThread.isSuspended();
	}
}
