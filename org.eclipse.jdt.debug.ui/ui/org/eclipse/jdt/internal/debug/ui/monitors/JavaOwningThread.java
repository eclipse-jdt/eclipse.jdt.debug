/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ITerminate;

/**
 * Object used to display owning thread in the debug launch view.
 * In this case, the thread is waiting for the contended monitor,
 * and owns the parent monitor.
 */
public class JavaOwningThread extends PlatformObject implements IDebugElement, ITerminate {

	/**
	 * The thread object in the thread and monitor model.
	 */
	private final JavaMonitorThread fThread;

	/**
	 * The monitor this thread is waiting for.
	 */
	private JavaContendedMonitor fContendedMonitor;
	/**
	 * The parent, in the debug view tree.
	 */
	private final JavaContendedMonitor fParent;

	/**
	 * Constructor
	 * @param thread
	 * @param parent
	 */
	public JavaOwningThread(JavaMonitorThread thread, JavaContendedMonitor parent) {
		fThread= thread;
		thread.addElement(this);
		fParent= parent;
	}

	/** Returns the <code>JavaMonitorThread</code> of this owning thread
	 * @return the <code>JavaMonitorThread</code> of this owning thread
	 */
	public JavaMonitorThread getThread() {
		return fThread;
	}

	/**
	 * Returns the parent contended
	 * @return
	 */
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
	@Override
	public String getModelIdentifier() {
		return fThread.getModelIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	@Override
	public IDebugTarget getDebugTarget() {
		return fThread.getDebugTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return fThread.getLaunch();
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fThread.isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if(adapter == IDebugTarget.class) {
			return (T) getDebugTarget();
		}
		//CONTEXTLAUNCHING
		if(adapter.equals(ILaunchConfiguration.class)) {
			return (T) getLaunch().getLaunchConfiguration();
		}
		return super.getAdapter(adapter);
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	@Override
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	@Override
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}

}
