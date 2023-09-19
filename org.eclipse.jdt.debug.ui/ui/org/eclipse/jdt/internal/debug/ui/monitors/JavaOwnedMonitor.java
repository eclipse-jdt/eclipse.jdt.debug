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
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;

/**
 * Object used to display owned monitor in the debug launch view.
 * In this case, the monitor is waited by the waiting threads, and owned
 * by the parent thread.
 */
public class JavaOwnedMonitor extends PlatformObject implements IDebugElement, ITerminate, ISuspendResume {

	/**
	 * The monitor object in the thread and monitor model.
	 */
	private final JavaMonitor fMonitor;

	/**
	 * The threads waiting for this monitor.
	 */
	private JavaWaitingThread[] fWaitingThreads;
	/**
	 * The parent, in the debug view tree.
	 */
	private final JavaWaitingThread fParent;

	/**
	 * Constructor
	 * @param monitor
	 * @param parent
	 */
	public JavaOwnedMonitor(JavaMonitor monitor, JavaWaitingThread parent) {
		fMonitor= monitor;
		monitor.addElement(this);
		fParent= parent;
	}

	/**
	 * Returns the monitor
	 * @return the monitor
	 */
	public JavaMonitor getMonitor() {
		return fMonitor;
	}

	/**
	 * Returns the original <code>IThread</code> or the parent thread
	 * @return the original <code>IThread</code> of the parent thread
	 */
	public Object getParent() {
		if (fParent.getParent() == null) {
			return fParent.getThread().getOriginalThread();
		}
		return fParent;
	}

	/**
	 * Returns an array of all of the threads waiting on this monitor
	 * @return the array of <code>JavaWaitingThread</code>s waiting on this monitor
	 */
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
		fWaitingThreads = tmp;
		return fWaitingThreads;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	@Override
	public String getModelIdentifier() {
		return fMonitor.getModelIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	@Override
	public IDebugTarget getDebugTarget() {
		return fMonitor.getDebugTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return fMonitor.getLaunch();
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
	 * returns the parent thread of this monitor
	 * @return the parent <code>IThread</code> that owns this monitor
	 */
	protected IThread getParentThread() {
		Object parent = getParent();
		IThread thread = null;
		if(parent instanceof IThread) {
			thread = (IThread) parent;
		}
		else if(parent instanceof JavaWaitingThread) {
			thread = ((JavaWaitingThread)parent).getThread().getOriginalThread();
		}
		return thread;
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

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	@Override
	public boolean canResume() {
		IThread thread = getParentThread();
		if(thread != null) {
			return thread.canResume();
		}
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	@Override
	public boolean canSuspend() {
		IThread thread = getParentThread();
		if(thread != null) {
			return thread.canSuspend();
		}
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	@Override
	public boolean isSuspended() {
		IThread thread = getParentThread();
		if(thread != null) {
			return thread.isSuspended();
		}
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	@Override
	public void resume() throws DebugException {
		IThread thread = getParentThread();
		if(thread != null) {
			thread.resume();
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	@Override
	public void suspend() throws DebugException {
		IThread thread = getParentThread();
		if(thread != null) {
			thread.suspend();
		}
	}
}
