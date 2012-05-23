/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;

/**
 * Object used to display contended monitor in the debug launch view.
 * In this case, the monitor is owned by the owning thread, and waited
 * by the parent thread.
 */
public class JavaContendedMonitor extends PlatformObject implements IDebugElement, ITerminate, ISuspendResume {

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

	/**
	 * Constructor
	 * @param monitor
	 * @param parent
	 */
	public JavaContendedMonitor(JavaMonitor monitor, JavaOwningThread parent) {
		fMonitor= monitor;
		monitor.addElement(this);
		fParent= parent;
	}
	
	/**
	 * returns the monitor that is in contention
	 * @return the monitor that is in contention
	 */
	public JavaMonitor getMonitor() {
		return fMonitor;
	}
	
	/**
	 * Returns the parent <code>JavaOwningThread</code> or the original <code>IThread</code>
	 * @return the parent <code>JavaOwningThread</code> or the original <code>IThread</code>
	 */
	public Object getParent() {
		if (fParent.getParent() == null) {
			return fParent.getThread().getOriginalThread();
		}
		return fParent;
	}
	
	/**
	 * returns the <code>JavaOwningThread</code> that owns this monitor
	 * @return the <code>JavaOwningThread</code> that owns this monitor
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if(adapter == IDebugTarget.class) {
			return getDebugTarget();
		}
		//CONTEXTLAUNCHING
		if(adapter.equals(ILaunchConfiguration.class)) {
			return getLaunch().getLaunchConfiguration();
		}
		return super.getAdapter(adapter);
	}

	/**
	 * returns the parent thread of this monitor
	 * @return the parent <code>IThread</code> that owns this monitor
	 */
	protected IThread getParentThread() {
		Object parent = getParent();
		IThread thread = null;
		if(parent instanceof IThread) {
			thread = (IThread) parent;
		}
		else if(parent instanceof JavaOwningThread) {
			thread = ((JavaOwningThread)parent).getThread().getOriginalThread();
		}
		return thread;
	}
	
	/**
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return getDebugTarget().canTerminate();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		JavaOwningThread owningThread = getOwningThread();
		if(owningThread != null) {
			IThread originalThread = owningThread.getThread().getOriginalThread();
			if (originalThread != null) {
				return originalThread.canResume();
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		JavaOwningThread owningThread = getOwningThread();
		if(owningThread != null) {
			IThread originalThread = owningThread.getThread().getOriginalThread();
			if (originalThread != null) {
				return originalThread.isSuspended();
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		getOwningThread().getThread().getOriginalThread().resume();
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getOwningThread().getThread().getOriginalThread().suspend();
	}
}
