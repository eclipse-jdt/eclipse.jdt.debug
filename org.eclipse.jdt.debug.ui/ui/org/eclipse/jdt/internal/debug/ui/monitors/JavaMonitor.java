/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Represent a Java monitor in the threads and monitors model.
 */
public class JavaMonitor {
	
	/**
	 * The underlying object.
	 */
	private IJavaObject fMonitor;
	
	/**
	 * The thread which owns this monitor
	 */
	private JavaMonitorThread fOwningThread;
	/**
	 * The threads waiting for this monitor.
	 */
	private JavaMonitorThread[] fWaitingThreads= new JavaMonitorThread[0];
	/**
	 * Indicate if this monitor is currently part of a deadlock.
	 */
	private boolean fIsInDeadlock;
	/**
	 * Indicate that the information for this monitor need to be update, it
	 * may have changed.
	 */
	private boolean fToUpdate= true;
	
	/**
	 * The List of JavaContendedMonitor and JavaOwnedMonitor associated with this
	 * monitor.
	 */
	private List<PlatformObject> fElements= new ArrayList<PlatformObject>();
	
	public JavaMonitor(IJavaObject monitor) {
		fMonitor= monitor;
	}
	
	public IJavaObject getMonitor() {
		return fMonitor;
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
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return fMonitor.getModelIdentifier();
	}
	
	/**
	 * Returns the thread which owns this monitor, refresh the data
	 * first if need.
	 */
	protected JavaMonitorThread getOwningThread0() {
		if (fToUpdate) {
			update();
		}
		return fOwningThread;
	}
	
	/**
	 * Returns the threads waiting for this monitor, refresh the data
	 * first if need.
	 */
	protected JavaMonitorThread[] getWaitingThreads0() {
		if (fToUpdate) {
			update();
		}
		return fWaitingThreads;
	}
	
	/**
	 * Update the information for this monitor.
	 * @return <code>true</code> if the owning thread or
	 * the waiting threads changed.
	 */
	private boolean update() {
		boolean changed= false;
		boolean toRemove= false;
		ThreadMonitorManager threadMonitorManager= ThreadMonitorManager.getDefault();
		synchronized (this) {
			if (!fToUpdate) {
				return false;
			}
			try {
				if (fMonitor.isAllocated()) {
					// update the owning thread
					IJavaThread owningThread= fMonitor.getOwningThread();
					if (owningThread == null) {
						changed= fOwningThread != null;
						fOwningThread= null;
					} else {
						changed= fOwningThread == null || !owningThread.equals(fOwningThread.getThread());
						fOwningThread= ThreadMonitorManager.getDefault().getJavaMonitorThread(owningThread, null);
					}
					// update the waiting threads
					IJavaThread[] waitingThreads= fMonitor.getWaitingThreads();
					if (waitingThreads == null || waitingThreads.length == 0) {
						// if no waiting threads, not much to do
						changed= fWaitingThreads != null && fWaitingThreads.length != 0;
						fWaitingThreads= new JavaMonitorThread[0];
						toRemove= fOwningThread == null;
					} else {
						JavaMonitorThread[] tmp= new JavaMonitorThread[waitingThreads.length];
						if (changed || fWaitingThreads.length != waitingThreads.length) {
							// if we know it changed, we can just create the new list
							for (int i= 0; i < waitingThreads.length; i++) {
								tmp[i]= threadMonitorManager.getJavaMonitorThread(waitingThreads[i], null);
							}
							changed= true;
						} else {
							// we need to check in the new list contains the same threads as the 
							// previous list
							int sameThread= 0;
							for (int i= 0; i < waitingThreads.length; i++) {
								for (int j= 0; j < fWaitingThreads.length; j++) {
									if (fWaitingThreads[i].getThread().equals(waitingThreads[i])) {
										sameThread++;
										break;
									}
								}
								tmp[i]= threadMonitorManager.getJavaMonitorThread(waitingThreads[i], null);
							}
							changed= sameThread != waitingThreads.length;
						}
						fWaitingThreads= tmp;
					}
				} else {
					toRemove= true;
				}
			} catch (DebugException e) {
				fOwningThread= null;
				fWaitingThreads= new JavaMonitorThread[0];
			} finally {
				fToUpdate= false;
			}
		}
		
		if (toRemove) {
			threadMonitorManager.removeJavaMonitor(this);
		} else if (changed) {
			fireChangeEvent(DebugEvent.CONTENT);
		}
		return changed;
	}
	
	/**
	 * Send a change event for theJavaContendedMonitor and JavaOwnedMonitor
	 * associated with this monitor
	 */
	private void fireChangeEvent(int detail) {
		Object[] elements= fElements.toArray();
		DebugEvent[] changeEvents= new DebugEvent[elements.length];
		for (int i= 0; i < elements.length; i++) {
			changeEvents[i]= new DebugEvent(elements[i], DebugEvent.CHANGE, detail);
		}
		DebugPlugin.getDefault().fireDebugEventSet(changeEvents);
	}

	public synchronized void setToUpdate() {
		if (!fToUpdate) {
			fToUpdate= true;
			if (fOwningThread != null) {
				fOwningThread.setToUpdate();
			}
			if (fWaitingThreads != null) {
				for (int i= 0; i < fWaitingThreads.length; i++) {
					fWaitingThreads[i].setToUpdate();
				}
			}
		}
	}
	
	protected void addElement(JavaOwnedMonitor monitor) {
		fElements.add(monitor);
	}
	
	protected void addElement(JavaContendedMonitor monitor) {
		fElements.add(monitor);
	}
	
	public void refresh() {
		if (fToUpdate && !update()) {
			if (fOwningThread != null) {
				fOwningThread.refresh();
			}
			for (int i= 0; i < fWaitingThreads.length; i++) {
				fWaitingThreads[i].refresh();
			}
		}
	}
	
	/**
	 * Indicate if this monitor is currently part of a deadlock
	 */
	public boolean isInDeadlock() {
		return fIsInDeadlock;
	}
	/**
	 * Set this monitor as being part of a deadlock.
	 */
	public void setInDeadlock(boolean isInDeadlock) {
		boolean oldValue= fIsInDeadlock;
		fIsInDeadlock = isInDeadlock;
		if (oldValue != isInDeadlock) {
			fireChangeEvent(DebugEvent.STATE);
		}
	}
}
