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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;

import com.sun.jdi.IncompatibleThreadStateException;

/**
 * Represent a Java thread in the threads and monitors model.
 */
public class JavaMonitorThread extends PlatformObject {
	
	/**
	 * The underlying thread.
	 */
	private IJavaThread fThread;

	private IThread fOriginalThread;
	
	/**
	 * The monitor this thread is waiting for.
	 */
	private JavaMonitor fContendedMonitor;
	/**
	 * The monitors owned by this thread.
	 */
	private JavaMonitor[] fOwnedMonitors= new JavaMonitor[0];
	/**
	 * Indicate if this thread is currently part of a deadlock.
	 */
	private boolean fIsInDeadlock;
	/**
	 * Indicate that the information for this thread need to be update, it
	 * may have changed.
	 */
	private boolean fToUpdate= true;
	
	/**
	 * List of JavaOwningThread and JavaWaitingThread associated with this thread.
	 */
	private List<IDebugElement> fElements= new ArrayList<IDebugElement>();

	/**
	 * JavaWaitingThread object used to return the JavaOwnedMonitor for this
	 * thread.
	 */
	private JavaWaitingThread fBaseWaitingThread;
	/**
	 * JavaOwningThread object uset to return the JavaWaitingMonitor for this
	 * thread.
	 */
	private JavaOwningThread fBaseOwningThread;

	public JavaMonitorThread(IJavaThread underlyingThread, IThread originalThread) {
		fThread= underlyingThread;
		fOriginalThread= originalThread;
	}
	
	public IJavaThread getThread() {
		return fThread;
	}
	
	public IThread getOriginalThread() {
		return fOriginalThread;
	}
	
	protected void setOriginalThread(IThread originalThread) {
		fOriginalThread= originalThread;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return fThread.getModelIdentifier();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fThread.getDebugTarget();
	}

	/**
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
	
	/**
	 * Returns the contended monitor to be used as a child
	 * of the underlying thread in the debug launch view.
	 */
	public JavaContendedMonitor getContendedMonitor() {
		if (fBaseOwningThread == null) {
			fBaseOwningThread= new JavaOwningThread(this, null);
		}
		return fBaseOwningThread.getContendedMonitor();
	}

	/**
	 * Returns the owned monitors to be used as children
	 * of the underlying thread in the debug launch view.
	 */
	public JavaOwnedMonitor[] getOwnedMonitors() {
		if (fBaseWaitingThread == null) {
			fBaseWaitingThread= new JavaWaitingThread(this, null);
		}
		return fBaseWaitingThread.getOwnedMonitors();
	}
	
	/**
	 * Returns the monitor this thread is waiting for.
	 */
	protected JavaMonitor getContendedMonitor0() {
		if (fToUpdate) {
			update();
		}
		return fContendedMonitor;
	}
	
	/**
	 * Returns the monitors owned by this thread.
	 */
	protected JavaMonitor[] getOwnedMonitors0() {
		if (fToUpdate) {
			update();
		}
		return fOwnedMonitors;
	}
	
	/**
	 * Update the information for this thread.
	 * @return <code>true</code> if the contended monitor or
	 * the owned monitors changed.
	 */
	private boolean update() {
		boolean changed= false;
		synchronized(this) {
			if (!fToUpdate) {
				return false;
			}
			try {
				// update the contended monitor
				IJavaObject contendedMonitor= fThread.getContendedMonitor();
				if (contendedMonitor == null) {
					changed= fContendedMonitor != null;
					fContendedMonitor= null;
				} else {
					changed= fContendedMonitor == null || !contendedMonitor.equals(fContendedMonitor.getMonitor());
					fContendedMonitor= ThreadMonitorManager.getDefault().getJavaMonitor(contendedMonitor);
				}
				// update the owned monitors
				IJavaObject[] ownedMonitors= fThread.getOwnedMonitors();
				if (ownedMonitors == null || ownedMonitors.length == 0) {
					// no owned monitor, not much to do
					changed= fOwnedMonitors != null && fOwnedMonitors.length != 0;
					fOwnedMonitors= new JavaMonitor[0];
				} else {
					JavaMonitor[] tmp= new JavaMonitor[ownedMonitors.length];
					ThreadMonitorManager threadMonitorManager= ThreadMonitorManager.getDefault();
					if (changed || fOwnedMonitors.length != ownedMonitors.length) {
						// if we know it changed, we can just create the new list.
						for (int i= 0; i < ownedMonitors.length; i++) {
							tmp[i]= threadMonitorManager.getJavaMonitor(ownedMonitors[i]);
						}
						changed= true;
					} else {
						// we need to check in the new list contains the same monitors as the 
						// previous list
						int sameMonitor= 0;
						for (int i= 0; i < ownedMonitors.length; i++) {
							for (int j= 0; j < fOwnedMonitors.length; j++) {
								if (ownedMonitors[i].equals(fOwnedMonitors[i].getMonitor())) {
									sameMonitor++;
									break;
								}
							}
							tmp[i]= threadMonitorManager.getJavaMonitor(ownedMonitors[i]);
						}
						changed= sameMonitor != ownedMonitors.length;
					}
					fOwnedMonitors= tmp;
				}
			} catch (DebugException e) {
			    Throwable cause= e.getStatus().getException();
			    if (!(cause instanceof IncompatibleThreadStateException)) {
			        // IncompatibleThreadStateException are expected from Sun VMs
			        // if the thread is not suspended.
			        // For all other exceptions, null the values.
					fContendedMonitor= null;
					changed= fOwnedMonitors != null && fOwnedMonitors.length != 0;
					fOwnedMonitors= new JavaMonitor[0];
			    }
			} finally {
				fToUpdate= false;
			}
		}
		if (changed) {
			fireChangeEvent(DebugEvent.CONTENT);
		}
		return changed;
	}
	
	/**
	 * send a change event for theJavaWaitingThread and JavaOwningThread
	 * associated with this thread
	 */
	private void fireChangeEvent(int detail) {
		Object[] elements= fElements.toArray();
		List<Object> changedElement= new ArrayList<Object>();
		if (fOriginalThread != null) {
			changedElement.add(fOriginalThread);
		}
		for (int i= 0; i < elements.length; i++) {
			Object element= elements[i];
			// the two 'base' elements are not part of the hierarchy, they are 
			// used to get the children of the Thread.
			if (element != fBaseOwningThread && element != fBaseWaitingThread) {
			    changedElement.add(element);
			}
		}
		DebugEvent[] changeEvents= new DebugEvent[changedElement.size()];
		int i= 0;
		for (Iterator<Object> iter= changedElement.iterator(); iter.hasNext();) {
		    changeEvents[i++]= new DebugEvent(iter.next(), DebugEvent.CHANGE, detail);
        }
		DebugPlugin.getDefault().fireDebugEventSet(changeEvents);
	}

	public synchronized void setToUpdate() {
		if (!fToUpdate) {
			fToUpdate= true;
			if (fContendedMonitor != null) {
				fContendedMonitor.setToUpdate();
			}
			if (fOwnedMonitors != null) {
				for (int i= 0; i < fOwnedMonitors.length; i++) {
					fOwnedMonitors[i].setToUpdate();
				}
			}
		}
	}
	
	protected void addElement(JavaOwningThread thread) {
		fElements.add(thread);
	}
	
	protected void addElement(JavaWaitingThread thread) {
		fElements.add(thread);
	}
	
	public void refresh() {
		if (fToUpdate && !update()) {
			if (fContendedMonitor != null) {
				fContendedMonitor.refresh();
			}
			for (int i= 0; i < fOwnedMonitors.length; i++) {
				fOwnedMonitors[i].refresh();
			}
		}
	}
	
	/**
	 * Indicate if this thread is currently part of a deadlock
	 */
	public boolean isInDeadlock() {
		return fIsInDeadlock;
	}
	/**
	 * Set this thread as being part of a deadlock.
	 */
	public void setInDeadlock(boolean isInDeadlock) {
		boolean oldValue= fIsInDeadlock;
		fIsInDeadlock = isInDeadlock;
		if (oldValue != isInDeadlock) {
			fireChangeEvent(DebugEvent.STATE);
		}
	}
}
