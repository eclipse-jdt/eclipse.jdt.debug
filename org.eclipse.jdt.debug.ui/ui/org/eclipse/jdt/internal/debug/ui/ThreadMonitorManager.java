/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Manager for the thread and monitor model.
 */
public class ThreadMonitorManager implements IDebugEventSetListener {
	
	private static ThreadMonitorManager fDefaultManager;
	
	/**
	 * HashMap IJavaThread -> JavaMonitorThread
	 */
	private HashMap fJavaMonitorThreads;
	/**
	 * HashMap IJavaObject -> JavaMonitor
	 */
	private HashMap fJavaMonitors;
	
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
		fJavaMonitorThreads= new HashMap();
		fJavaMonitors= new HashMap();
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i= 0; i < events.length; i++) {
			DebugEvent debugEvent= events[i];
			Object eventSource= debugEvent.getSource();
			int eventKind= debugEvent.getKind();
			if (eventSource instanceof IJavaThread) {
				switch (eventKind) {
					case DebugEvent.SUSPEND:
					case DebugEvent.RESUME:
						// refresh on thread suspend/resume
						handleThreadSuspendResume();
						break;
					case DebugEvent.TERMINATE:
						// clean the thread map when a thread terminates
						handleThreadTerminate((IJavaThread)eventSource);
						break;
				}
			} else if (eventSource instanceof IJavaDebugTarget) {
				if (eventKind == DebugEvent.TERMINATE) {
					// clean the maps when a target terminates
					handleDebugTargetTerminate((IJavaDebugTarget) eventSource);
				}
			}
		}
	}
	
	private void handleThreadSuspendResume() {
		for (Iterator iter= ((HashMap)fJavaMonitorThreads.clone()).values().iterator(); iter.hasNext();) {
			((JavaMonitorThread) iter.next()).setToUpdate();
		}
		DebugPlugin.getDefault().asyncExec(new RefreshAndDetectDeadlock());
	}

	private void handleThreadTerminate(IJavaThread thread) {
		// remove this thread
		fJavaMonitorThreads.remove(thread);
	}

	private void handleDebugTargetTerminate(IJavaDebugTarget debugTarget) {
		// remove the threads and monitors for this debug target.
		clean(fJavaMonitors, debugTarget);
		clean(fJavaMonitorThreads, debugTarget);
	}

	private void clean(Map map, IJavaDebugTarget debugTarget) {
		for (Iterator iter= map.keySet().iterator(); iter.hasNext();) {
			if (((IDebugElement) iter.next()).getDebugTarget().equals(debugTarget)) {
				iter.remove();
			}
		}
	}
	
	/**
	 * Returns the unique JavaMonitorThread object for the given thread.
	 */
	protected JavaMonitorThread getJavaMonitorThread(IJavaThread thread) {
		JavaMonitorThread javaMonitorThread= (JavaMonitorThread) fJavaMonitorThreads.get(thread);
		if (javaMonitorThread == null) {
			javaMonitorThread= new JavaMonitorThread(thread);
			fJavaMonitorThreads.put(thread, javaMonitorThread);
		}
		return javaMonitorThread;
	}
	
	/**
	 * Returns the unique JavaMonitor object for the give monitor.
	 */
	protected JavaMonitor getJavaMonitor(IJavaObject monitor) {
		JavaMonitor javaMonitor= (JavaMonitor) fJavaMonitors.get(monitor);
		if (javaMonitor == null) {
			javaMonitor= new JavaMonitor(monitor);
			fJavaMonitors.put(monitor, javaMonitor);
		}
		return javaMonitor;
	}

	/**
	 * Removes a monitor from the monitor map.
	 */
	protected void removeJavaMonitor(JavaMonitor monitor) {
		fJavaMonitors.remove(monitor.getMonitor());
	}
			
	/**
	 * Returns the monitor the given thread is waiting for.
	 */
	public JavaContendedMonitor getContendedMonitor(IJavaThread thread) {
		return getJavaMonitorThread(thread).getContendedMonitor();
	}
	
	/**
	 * Returns the monitors the given thread owns.
	 */
	public JavaOwnedMonitor[] getOwnedMonitors(IJavaThread thread) {
		return getJavaMonitorThread(thread).getOwnedMonitors();
	}

	/**
	 *  Runnable to be run asynchronously, to refresh the model and 
	 *  look for deadlocks.
	 */
	class RefreshAndDetectDeadlock implements Runnable {
		public void run() {
			Object[] threads= fJavaMonitorThreads.values().toArray();
			for (int i = 0; i < threads.length; i++) {
				((JavaMonitorThread) threads[i]).refresh();
			}
		}
	}

}
