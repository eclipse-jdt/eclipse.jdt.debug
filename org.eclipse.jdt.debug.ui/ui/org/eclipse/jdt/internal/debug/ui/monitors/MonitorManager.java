package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Handles all the data for the Threads and Monitors view.
 */

public class MonitorManager {
	
	/**
	 * Table containing the references to the monitors owned by the threads
	 */
	private Map fThreadToOwnedMonitors;

	/**
	 * Table containing the references to the monitor contended by the threads
	 */
	private Map fThreadToContendedMonitor;

	/**
	 * Table containing the references to the thread owning the monitors
	 */
	private Map fMonitorToOwningThread;
	
	/**
	 * Table containing the references to the threads contending the monitors
	 */
	private Map fMonitorToContendingThreads;
	
	private static MonitorManager fgDefault= null;
	
	/**
	 * List containing the lists of the different deadlocks
	 */
	private List fDeadLockLists;

	/**
	 * Constructor
	 */
	private MonitorManager() {
		//@see getDefault()
		fThreadToOwnedMonitors= new Hashtable(4);
		fThreadToContendedMonitor= new Hashtable(4);
		fMonitorToOwningThread= new Hashtable();
		fMonitorToContendingThreads= new Hashtable();
		fDeadLockLists = new ArrayList();
	}

	public static MonitorManager getDefault() {
		if (fgDefault == null) {
			fgDefault= new MonitorManager();
		}
		return fgDefault;
	}
	
	/**
	 * Adds the the monitors owned by the thread
	 * If the list is <code>null</code>, remove the thread from the mappings
	 * @param thread The thread
	 * @param monitors The monitors owned by the thread
	 */
	protected void addThreadWithOwnedMonitors(IJavaThread thread, IJavaObject[] monitors) {
		if (monitors == null) {
			fThreadToOwnedMonitors.remove(thread);
		} else {
			fThreadToOwnedMonitors.put(thread, monitors);
		}
	}
	
	/**
	 * Adds the monitor contended by the thread
	 * If the list is <code>null</code>, remove the thread from the mappings
	 * @param thread The thread
	 * @param monitor The monitor contended by the thread
	 */
	protected void addThreadWithContendedMonitor(IJavaThread thread, IJavaObject monitor) {
		if (monitor == null) {
			fThreadToContendedMonitor.remove(thread);
		} else {
			fThreadToContendedMonitor.put(thread, monitor);
		}
	}

	/**
	 * Adds the thread owning the monitor
	 * If the list is <code>null</code>, remove the monitor from the mappings
	 * @param monitor The monitor
	 * @param thread The thread owning the monitor
	 */
	protected void addMonitorWithOwningThread(IJavaObject monitor, IJavaThread thread) {
		if (monitor == null) {
			fMonitorToOwningThread.remove(monitor);
		} else {
			fMonitorToOwningThread.put(monitor, thread);
		}
	}
	
	/**
	 * Adds a thread waiting for the monitor
	 * If the list is <code>null</code>, remove the monitors from the mappings
	 * @param monitor The monitor
	 * @param thread The thread waiting for the monitor
	 */
	protected void addMonitorWithContendedThread(IJavaObject monitor, IJavaThread thread) {
		if (monitor == null) {
			fMonitorToContendingThreads.remove(monitor);
		} else {
			List threads= (List)fMonitorToContendingThreads.get(monitor);
			if (threads == null) {
				threads= new ArrayList();
				fMonitorToContendingThreads.put(monitor, threads);
			}
			threads.add(thread);
		}
	}
		
	/**
	 * Returns the monitors owned by the given thread, or <code>null</code>
	 * if the thread does not own any monitors.
	 * 
	 * @param thread The thread owning the monitors
	 * @return The monitors owned by the given thread
	 */
	public IJavaObject[] getOwnedMonitors(IJavaThread thread) {
		return (IJavaObject[])fThreadToOwnedMonitors.get(thread);
	}

	/**
	 * Returns the monitor contended by the given thread, or <code>null</code>
	 * 
	 * @param thread The thread from to determine the contended monitor
	 * @return The monitor contended by the given thread
	 */
	public IJavaObject getContendedMonitor(IJavaThread thread) {
		return (IJavaObject)fThreadToContendedMonitor.get(thread);
	}
	
	/**
	 * Returns the thread owning the given monitor, or <code>null</code>
	 * if no thread owns the specified monitor.
	 * 
	 * @param monitor The monitor from to determine the owning thread
	 * @return The thread owning the given monitor
	 */
	public IJavaThread getOwningThread(IJavaObject monitor) {
		return (IJavaThread)fMonitorToOwningThread.get(monitor);
	}
	
	/**
	 * Returns the list of threads awaiting the given monitor, or <code>null</code>
	 * 
	 * @param monitor The monitor from to determine the contending threads
	 * @return List a list of the threads in contention for the monitor
	 */
	public List getContendingThreads(IJavaObject monitor) {
		Object obj = fMonitorToContendingThreads.get(monitor);
		return (List)obj;
	}
	
	/**
	 * Returns all the threads owning or waiting on a monitor
	 * 
	 * @return All the threads (owning or waiting on a monitor)
	 */
	public IJavaThread[] getThreads() {
		Set all= new HashSet();
		all.addAll(fThreadToContendedMonitor.keySet());
		all.addAll(fThreadToOwnedMonitors.keySet());
		return (IJavaThread[])all.toArray(new IJavaThread[all.size()]);
	}
	
	/**
	 * Returns all the monitors owned or in contention.
	 * 
	 * @return All the monitors (owned or in contention)
	 */
	public IJavaObject[] getMonitors() {
		Set all= new HashSet();
		all.addAll(fMonitorToContendingThreads.keySet());
		all.addAll(fMonitorToOwningThread.keySet());
		return (IJavaObject[])all.toArray(new IJavaObject[all.size()]);
	}	
	
	/**
	 * Updates the data on threads, monitors and deadlocks
	 * for the specified debug target.
	 * 
	 * @param target The debug target
	 */
	public void update(IJavaDebugTarget target){

		update(target, true);
	}
		
	/**
	 * Updates the data on threads, monitors and deadlocks
	 * for the suspended threads contained within the specified
	 * debug target.
	 * 
	 * @param target The debug target
	 * @see update(IJavaDebugTarget target)
	 */
	public void updatePartial(IJavaDebugTarget target){

		update(target, false);
	}
	
	/**
	 * Updates the data on threads, monitors and deadlocks
	 * for the suspended threads contained within the specified
	 * debug target. If <code>suspendThreads</code>, 
	 * 
	 * @param target The debug target
	 * @param whether to suspend the threads
	 */
	private void update(IJavaDebugTarget target, boolean suspendThreads){

		try {
			// clear all the tables
			removeMonitorInformation(target);
			
			// construct the list of all the non system threads
			IThread[] threadResult= target.getThreads();
			List threadsList = new ArrayList(threadResult.length);
			IJavaThread thread;
			for (int i = 0; i < threadResult.length; i++) {
				thread = (IJavaThread)threadResult[i];
				if(!thread.isSystemThread()){
					threadsList.add(thread);
				}
			}
			IJavaThread[] threads= (IJavaThread[]) threadsList.toArray(new IJavaThread[threadsList.size()]);
			
			if (suspendThreads) {
				//suspend all the non system threads
				suspend(threads);
			}
			
			//updating data on owning threads / owned monitors
			// and contending threads / contended monitors
			for (int i = 0; i < threads.length; i++) {
				thread = threads[i];
				updateMonitors(thread);
			}
			//all of the monitor information is needed before
			//the deadlock information can be calculated
			for (int i = 0; i < threads.length; i++) {
				thread = threads[i];
				updateDeadlock(thread);
			}
		} catch(DebugException e){
			JDIDebugPlugin.log(e);
		}
	}

	protected void updateDeadlock(IJavaThread thread) throws DebugException {
		//updating data on deadlocks
		List l = listToDeadlock(thread, new ArrayList(4));
		// if thread is caught in a deadlock, 
		// l will be the list showing this deadlock
		if(l != null){
			ThreadWrapper tw = new ThreadWrapper(thread, l);
			// adding this deadlock list
			fDeadLockLists.add(tw);
		}
	}
	
	protected void updateMonitors(IJavaThread thread) throws DebugException {
		IJavaObject[] ownedMonitors;
		IJavaObject currentContendedMonitor;
		IJavaObject monitor;
		ownedMonitors = thread.getOwnedMonitors();
		currentContendedMonitor = thread.getContendedMonitor();
		// owning threads / owned monitors
		if(thread.hasOwnedMonitors()){
			addThreadWithOwnedMonitors(thread, ownedMonitors);
			
			for(int j=0; j < ownedMonitors.length; j++) {
				monitor = ownedMonitors[j];
				addMonitorWithOwningThread(monitor, thread);
			}
		}
		// contending threads / contended monitors
		if(currentContendedMonitor != null){
			addThreadWithContendedMonitor(thread, currentContendedMonitor);
			addMonitorWithContendedThread(currentContendedMonitor, thread);
		}
	}

	/**
	 * Suspend all the given threads
	 * @param The list of threads to suspend
	 */
	private void suspend(IJavaThread[] threads){		
		try {
			for (int i = 0; i < threads.length; i++) {
				IJavaThread thread = threads[i];
				if (!thread.isSuspended()) {
					thread.suspend();
					while (!thread.isSuspended()) {
						Thread.sleep(100);
					}
				}
			}
		}
		catch (DebugException e) {
			JDIDebugPlugin.log(e);
		}
		catch (InterruptedException e){
			JDIDebugPlugin.log(e);
		}
	}
	
	/**
	 * Clears all the cached monitor information for the specified target.
	 * 
	 * @param target The target to remove the cached information for
	 */
	public void removeMonitorInformation(IJavaDebugTarget target) {
		fThreadToOwnedMonitors.clear();
		fThreadToContendedMonitor.clear();
		fMonitorToOwningThread.clear();
		fMonitorToContendingThreads.clear();
		fDeadLockLists.clear();
	}
	
	/**
	 * If the thread is in a deadlock, returns the list to the deadlock
	 * This list has the following structure:
	 * <ul>
	 * 	<li>First element: Thread in the deadlock</li>
	 * 	<li>Second element: Monitor contended by the first element</li>
	 * 	<li>Third element: Thread owning the second element</li>
	 * 	<li>Fourth element: Monitor contended by the third element</li>
	 * 	<li>...</li>
	 * 	<li>Last element: Same element as the first one, proving that it is in a deadlock</li>
	 * </ul>
	 * 
	 * @param thread The thread we want to get the list of
	 * @param threadTree The list that records the element already used (call with an empty list)
	 * @return The deadlock list
	 */
	private List listToDeadlock(IJavaThread thread, List usedThreadsList){
		
		List res = new ArrayList();
		IJavaObject contendedMonitor = (IJavaObject)fThreadToContendedMonitor.get(thread);
						
		//if the thread is waiting for one monitor
		if(contendedMonitor!=null){
			
			IJavaThread owningThread = (IJavaThread)fMonitorToOwningThread.get(contendedMonitor);
			// check if owningThread has already been used, and therefore is already in the given list
			// if owningThread has already been used, returns the end of the list
			if(usedThreadsList.contains(owningThread)){
				res.add(thread);
				res.add(contendedMonitor);
				res.add(owningThread);
				return res;
			}
			// if owningThread has not already been used
			else{
				List newUsedThreadsList= new ArrayList(usedThreadsList);
				
				//adding current thread to the new used list
				newUsedThreadsList.add(thread);
				
				if(owningThread==null){
					return null;
				}
				// recursive call, one level lower in the deadlock list
				List newRes = listToDeadlock(owningThread, newUsedThreadsList);
					
				if(newRes!=null){
					res.add(thread);
					res.add(contendedMonitor);
					res.addAll(newRes);
					return res;
				}
			}
		} else {
			// if the thread is not waiting for any monitor
			return null;	
		}
		return null;
	}
	
	/**
	 * Returns the number of determined deadlocks
	 * 
	 * @return List a list of all of the listings of current deadlocks
	 */
	public int getNumberOfDeadlocks() {
		return fDeadLockLists.size();
	}
	
	/**
	 * Returns the deadlock list at the specified index or <code>null</code>
	 * if the index is greater than the number of detected deadlocks.
	 * 
	 * @return List a list of all of the listings of current deadlocks
	 * @see getNumberOfDeadlocks();
	 */
	public List getDeadlockList(int index) {
		if (index >= fDeadLockLists.size()) {
			return null;
		}
		return ((ThreadWrapper)fDeadLockLists.get(index)).getDeadLockList();
	}
	
	/**
	 * Returns the thread that is at the root of the deadlock at the specified
	 * index or <code>null</code> if the index is greater than the number of 
	 * detected deadlocks.
	 * 
	 * @return IJavaThread the thread at the root of the deadlock
	 * @see getNumberOfDeadlocks();
	 */
	public IJavaThread getStartThread(int index) {
		if (index >= fDeadLockLists.size()) {
			return null;
		}
		return ((ThreadWrapper)fDeadLockLists.get(index)).getStartThread();
	}
	
	/**
	 * Returns whether the given thread is caught in a deadlock
	 * 
	 * @param thread The thread to check if in deadlock
	 * @return <code>true<code> if the thread is in a deadlock, <code>false<code> otherwise.
	 */
	public boolean isCaughtInDeadlock(IJavaThread thread){
		for (int i = 0; i < fDeadLockLists.size(); i++) {
			if(((ThreadWrapper)fDeadLockLists.get(i)).getStartThread().equals(thread)){
				return true;				
			}	
		}
		return false;
	}
}