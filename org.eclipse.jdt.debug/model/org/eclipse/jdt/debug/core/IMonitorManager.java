package org.eclipse.jdt.debug.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.List;
import java.util.Set;

/**
 * 
 * The expression manager manages monitor information
 * for Java threads in the JDI Debug model. This manager can
 * be used for deadlock detection.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 2.1
 */
public interface IMonitorManager {
	
	/**
	 * Returns the monitor contended by the given thread, or <code>null</code>
	 * 
	 * @param thread The thread from to determine the contended monitor
	 * @return The monitor contended by the given thread
	 */
	public IJavaObject getContendedMonitor(IJavaThread thread);

	/**
	 * Returns the list of threads awaiting the given monitor, or <code>null</code>
	 * 
	 * @param monitor The monitor from to determine the contending threads
	 * @return List a list of the threads in contention for the monitor
	 */
	public List getContendingThreads(IJavaObject monitor);

	/**
	 * Returns the dead lock lists.
	 * 
	 * @return List a list of all of the listings of current deadlocks
	 */
	public List getDeadLockLists();

	/**
	 * Returns all the monitors owned or contended, or <code>null</code>
	 * 
	 * @return The set of all the monitors (owned or in contention)
	 */
	public Set getMonitors();

	/**
	 * Returns the list of monitors owned by the given thread, or null
	 * 
	 * @param thread The thread from which we want the owned monitors
	 * @return The list of monitors owned by the given thread
	 */
	public List getOwnedMonitors(IJavaThread thread);
	
	/**
	 * Returns the thread owning the given monitor, or null
	 * 
	 * @param monitor The monitor from which we want the owning thread
	 * @return The thread owning the given monitor
	 */
	public IJavaThread getOwningThread(IJavaObject monitor);
	
	/**
	 * Returns all the threads owning or waiting, or <code>null</code>
	 * 
	 * @return The set of all the threads (owning or waiting)
	 */
	public Set getThreads();
	
	/**
	 * Returns whether the given thread is caught in a deadlock
	 * 
	 * @param thread The thread we want the info on
	 * @return <code>true<code> if the thread is in a deadlock, <code>false<code> otherwise.
	 */
	public boolean isCaughtInDeadLock(IJavaThread thread);
	
	/**
	 * Clears all the cached monitor information for the specified target.
	 * 
	 * @param target The target to remove the cached information for
	 */
	public void removeMonitorInformation(IJavaDebugTarget target);
	
	/**
	 * Updates the data on threads, monitors and deadlocks
	 * for the specified debug target.
	 * 
	 * @param target The debug target
	 */
	public void update(IJavaDebugTarget target);
	
	/**
	 * Updates the data on threads, monitors and deadlocks
	 * for the suspended threads contained within the specified
	 * debug target.
	 * 
	 * @param target The debug target
	 * @see update(IJavaDebugTarget target)
	 */
	public void updatePartial(IJavaDebugTarget target);
}
