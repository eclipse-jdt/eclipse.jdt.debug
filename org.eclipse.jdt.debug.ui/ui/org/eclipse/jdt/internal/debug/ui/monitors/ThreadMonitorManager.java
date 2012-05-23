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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Manager for the thread and monitor model.
 */
public class ThreadMonitorManager implements IDebugEventSetListener, IPropertyChangeListener {
	
	private static ThreadMonitorManager fDefaultManager;
	
	/**
	 * HashMap IJavaThread -> JavaMonitorThread
	 */
	private HashMap<IDebugElement, Object> fJavaMonitorThreads;
	/**
	 * HashMap IJavaObject -> JavaMonitor
	 */
	private HashMap<IDebugElement, Object> fJavaMonitors;
	
	private boolean fIsEnabled;
	
	/**
	 * Returns the default ThreadMonitorManager object.
	 */
	public static ThreadMonitorManager getDefault() {
		if (fDefaultManager == null) {
			fDefaultManager= new ThreadMonitorManager();
		}
		return fDefaultManager;
	}
	
	private ThreadMonitorManager() {
		fJavaMonitorThreads= new HashMap<IDebugElement, Object>();
		fJavaMonitors= new HashMap<IDebugElement, Object>();
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(this);
		fIsEnabled= preferenceStore.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
		if (fIsEnabled) {
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i= 0; i < events.length; i++) {
			DebugEvent debugEvent= events[i];
			Object eventSource= debugEvent.getSource();
			int eventKind= debugEvent.getKind();
			IJavaThread javaThread = null;
			if (eventSource instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable)eventSource;
				javaThread = (IJavaThread) adaptable.getAdapter(IJavaThread.class);
				if (javaThread != null) {
					switch (eventKind) {
						case DebugEvent.SUSPEND:
						case DebugEvent.RESUME:
							// refresh on suspend/resume
							if (debugEvent.getDetail() != DebugEvent.EVALUATION_IMPLICIT) {
								handleSuspendResume();
							}
							break;
						case DebugEvent.TERMINATE:
							// clean the thread map when a thread terminates
							handleThreadTerminate(javaThread);
							break;
					}
				} else {
					IJavaDebugTarget target = (IJavaDebugTarget) adaptable.getAdapter(IJavaDebugTarget.class);
					if (target != null) {
						switch (eventKind) {
							case DebugEvent.SUSPEND:
							case DebugEvent.RESUME:
								// refresh on suspend/resume
								if (debugEvent.getDetail() != DebugEvent.EVALUATION_IMPLICIT) {
									handleSuspendResume();
								}
								break;
							case DebugEvent.TERMINATE:
								// clean the maps when a target terminates
								handleDebugTargetTerminate(target);
								break;
						}
					}
				}
			}
		}
	}
	
	private void handleSuspendResume() {
		JavaMonitorThread[] threads = getJavaMonitorThreads();
		for (int i = 0; i < threads.length; i++) {
			threads[i].setToUpdate();
		}
		DebugPlugin.getDefault().asyncExec(new RefreshAndDetectDeadlock());
	}

	private void handleThreadTerminate(IJavaThread thread) {
		// remove this thread
		synchronized(fJavaMonitorThreads) {
			fJavaMonitorThreads.remove(thread);
		}
	}

	private void handleDebugTargetTerminate(IJavaDebugTarget debugTarget) {
		// remove the threads and monitors for this debug target.
		clean(fJavaMonitors, debugTarget);
		clean(fJavaMonitorThreads, debugTarget);
	}

	private void clean(Map<IDebugElement, Object> map, IJavaDebugTarget debugTarget) {
		IDebugElement debugElements[] = null;
		synchronized(map) {
			debugElements = new IDebugElement[map.size()]; 
			debugElements =	map.keySet().toArray(debugElements);
		}
		for(int i = 0; i < debugElements.length; ++i) {
			if (debugElements[i].getDebugTarget().equals(debugTarget)) {
				synchronized(map) {
					map.remove(debugElements[i]);
				}
			}
		}
	}
	
	/**
	 * Returns the unique JavaMonitorThread object for the given thread.
	 */
	protected JavaMonitorThread getJavaMonitorThread(IJavaThread thread, IThread originalThread) {
		synchronized (fJavaMonitorThreads) {
			JavaMonitorThread javaMonitorThread= (JavaMonitorThread) fJavaMonitorThreads.get(thread);
			if (javaMonitorThread == null) {
				javaMonitorThread= new JavaMonitorThread(thread, originalThread);
				fJavaMonitorThreads.put(thread, javaMonitorThread);
				DebugPlugin.getDefault().asyncExec(new DetectDeadlock());			
			} else if (originalThread != null) {
				javaMonitorThread.setOriginalThread(originalThread);
			}
			return javaMonitorThread;
		}
	}
	
	/**
	 * Returns the unique JavaMonitor object for the given monitor.
	 */
	protected JavaMonitor getJavaMonitor(IJavaObject monitor) {
		synchronized (fJavaMonitors) {
			JavaMonitor javaMonitor= (JavaMonitor) fJavaMonitors.get(monitor);
			if (javaMonitor == null) {
				javaMonitor= new JavaMonitor(monitor);
				fJavaMonitors.put(monitor, javaMonitor);
			}
			return javaMonitor;
		}
	}

	/**
	 * Removes a monitor from the monitor map.
	 */
	protected void removeJavaMonitor(JavaMonitor monitor) {
		synchronized(fJavaMonitors) {
			fJavaMonitors.remove(monitor.getMonitor());
		}
	}
			
	/**
	 * Returns the monitor the given thread is waiting for.
	 */
	public JavaContendedMonitor getContendedMonitor(IThread thread) {
        IJavaThread javaThread = (IJavaThread) thread.getAdapter(IJavaThread.class);
		if (javaThread == null || !fIsEnabled || !((IJavaDebugTarget)javaThread.getDebugTarget()).supportsMonitorInformation()) {
			return null;
		}
		return getJavaMonitorThread(javaThread, thread).getContendedMonitor();
	}
	
	/**
	 * Returns the monitors the given thread owns.
	 */
	public JavaOwnedMonitor[] getOwnedMonitors(IThread thread) {
        IJavaThread javaThread = (IJavaThread) thread.getAdapter(IJavaThread.class);
    	if (javaThread == null || !fIsEnabled || !((IJavaDebugTarget)javaThread.getDebugTarget()).supportsMonitorInformation()) {
    		return new JavaOwnedMonitor[0];
    	}
    	return getJavaMonitorThread(javaThread, thread).getOwnedMonitors();
	}

	/**
	 *  Runnable to be run asynchronously, to refresh the model and 
	 *  look for deadlocks.
	 */
	class RefreshAndDetectDeadlock extends DetectDeadlock {
		@Override
		public void run() {
			JavaMonitorThread[] threads= getJavaMonitorThreads();
			for (int i = 0; i < threads.length; i++) {
				threads[i].refresh();
			}
			super.run();
		}
	}

	class DetectDeadlock implements Runnable {
		public void run() {
			JavaMonitorThread[] threads= getJavaMonitorThreads();
			JavaMonitor[] monitors= getJavaMonitors();
			List<Object> inDeadlock= new ArrayList<Object>();
			for (int i = 0; i < threads.length; i++) {
				JavaMonitorThread thread= threads[i];
				List<JavaMonitorThread> threadStack= new ArrayList<JavaMonitorThread>();
				List<JavaMonitor> monitorStack= new ArrayList<JavaMonitor>();
				while (thread != null) {
					boolean isInDeadlock= false;
					if (inDeadlock.contains(thread) || threadStack.contains(thread)) {
						isInDeadlock= true;
					} else {
						JavaMonitor monitor = thread.getContendedMonitor0();
						if (monitor == null) {
							thread= null;
						} else if (inDeadlock.contains(monitor)) {
							isInDeadlock= true;
						} else {
							threadStack.add(thread);
							monitorStack.add(monitor);
							thread= monitor.getOwningThread0();
						}
					}
					if (isInDeadlock) {
						// is in a deadlock, set the elements of the back trace as 'in a deadlock'
						for (Iterator<JavaMonitorThread> iter = threadStack.iterator(); iter.hasNext();) {
							inDeadlock.add(iter.next());
						}
						for (Iterator<JavaMonitor> iter = monitorStack.iterator(); iter.hasNext();) {
							inDeadlock.add(iter.next());
						}
						thread= null;
					}
				}
			}
			for (int i = 0; i < threads.length; i++) {
				JavaMonitorThread thread= threads[i];
				thread.setInDeadlock(inDeadlock.contains(thread));
			}
			for (int i = 0; i < monitors.length; i++) {
				JavaMonitor monitor= monitors[i];
				monitor.setInDeadlock(inDeadlock.contains(monitor));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO)) {
			fIsEnabled= JDIDebugUIPreferenceInitializer.getBoolean(event);
			if (fIsEnabled) {
				DebugPlugin.getDefault().addDebugEventListener(this);
			} else {
				DebugPlugin.getDefault().removeDebugEventListener(this);
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if SHOW_MONITOR_THREAD_INFO is on and the given thread is
	 * in a deadlock, <code>false</code> otherwise.
	 */
	public boolean isInDeadlock(IThread thread) {
        IJavaThread javaThread = (IJavaThread) thread.getAdapter(IJavaThread.class);
		if (!fIsEnabled || !((IJavaDebugTarget)javaThread.getDebugTarget()).supportsMonitorInformation()) {
			return false;
		}
		return getJavaMonitorThread(javaThread, thread).isInDeadlock();
	}

	private JavaMonitor[] getJavaMonitors() {
		synchronized(fJavaMonitors) {
			JavaMonitor[] monitors = new JavaMonitor[fJavaMonitors.size()];
			return fJavaMonitors.values().toArray(monitors);
		}
	}
	
	private JavaMonitorThread[] getJavaMonitorThreads() {
		synchronized(fJavaMonitorThreads) {
			JavaMonitorThread[] threads = new JavaMonitorThread[fJavaMonitorThreads.size()];
			return fJavaMonitorThreads.values().toArray(threads);
		}
	}
}
