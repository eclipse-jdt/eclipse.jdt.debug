/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * ThreadWrapper used in by the monitor manager,
 * wrapping the thread itself and its deadlock list
 */
public class ThreadWrapper {
	
	/**
	 * The underlying Java thread
	 */
	private IJavaThread fThread;
	
	/**
	 * The list holding the way to the deadlock
	 * as described in MonitorManager
	 */
	private List fDeadLockList;

	/**
	 * Constructor for the monitor thread wrapper
	 * @param thread The thread
	 * @param deadLockList The deadlock list as described in MonitorManager
	 */
	public ThreadWrapper(IJavaThread thread, List deadLockList){
		fThread = thread;
		fDeadLockList = new ArrayList(deadLockList);
	}

	/**
	 * Returns the dead lock list.
	 * @return List
	 */
	public List getDeadLockList() {
		return fDeadLockList;
	}

	/**
	 * Returns the start thread.
	 * @return IJavaThread
	 */
	public IJavaThread getStartThread() {
		return fThread;
	}
}
