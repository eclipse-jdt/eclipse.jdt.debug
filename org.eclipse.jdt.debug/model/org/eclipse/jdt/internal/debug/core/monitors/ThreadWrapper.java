package org.eclipse.jdt.internal.debug.core.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * ThreadWrapper used in monitor Manager,
 * wrapping the thread itself and its deadlock list
 */
public class ThreadWrapper {
	
	private IJavaThread thread;
	
	/**
	 * The list holding the way to the deadlock
	 * as described in MonitorManager
	 */
	private List deadLockList;

	/**
	 * Constructor
	 * @param thread The thread
	 * @param deadLockList The deadlock list as described in MonitorManager
	 */
	public ThreadWrapper(IJavaThread thread, List deadLockList){
		this.thread = thread;
		this.deadLockList = new ArrayList(deadLockList);
	}

	/**
	 * Returns the deadLockList.
	 * @return List
	 */
	public List getDeadLockList() {
		return deadLockList;
	}

	/**
	 * Returns the startThread.
	 * @return IJavaThread
	 */
	public IJavaThread getStartThread() {
		return thread;
	}

	/**
	 * Sets the deadLockList.
	 * @param deadLockList The deadLockList to set
	 */
	public void setDeadLockList(List deadLockList) {
		this.deadLockList = deadLockList;
	}

	/**
	 * Sets the startThread.
	 * @param startThread The startThread to set
	 */
	public void setStartThread(IJavaThread startThread) {
		this.thread = startThread;
	}

}
