package org.eclipse.jdt.internal.debug.core.monitors;

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
import org.eclipse.jdt.debug.core.IMonitorManager;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Handles all the data for the Threads and Monitors view.
 */

public class MonitorManager implements IMonitorManager {
	
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
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getOwnedMonitors(org.eclipse.jdt.debug.core.IJavaThread)
	 */
	public List getOwnedMonitors(IJavaThread thread) {
		return (List)fThreadToOwnedMonitors.get(thread);
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getContendedMonitor(org.eclipse.jdt.debug.core.IJavaThread)
	 */
	public IJavaObject getContendedMonitor(IJavaThread thread) {
		return (IJavaObject)fThreadToContendedMonitor.get(thread);
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getOwningThread(org.eclipse.jdt.debug.core.IJavaObject)
	 */
	public IJavaThread getOwningThread(IJavaObject monitor) {
		return (IJavaThread)fMonitorToOwningThread.get(monitor);
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getContendingThreads(org.eclipse.jdt.debug.core.IJavaObject)
	 */
	public List getContendingThreads(IJavaObject monitor) {
		Object obj = fMonitorToContendingThreads.get(monitor);
		return (List)obj;
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getThreads()
	 */
	public Set getThreads() {
		Set all= new HashSet();
		all.addAll(fThreadToContendedMonitor.keySet());
		all.addAll(fThreadToOwnedMonitors.keySet());
		return all;
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getMonitors()
	 */
	public Set getMonitors() {
		Set all= new HashSet();
		all.addAll(fMonitorToContendingThreads.keySet());
		all.addAll(fMonitorToOwningThread.keySet());
		return all;
	}	
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#update(org.eclipse.jdt.debug.core.IJavaDebugTarget)
	 */
	public void update(IJavaDebugTarget target){

		try {
			// clear all the tables
			removeMonitorInformation(target);
			
			// construct the list of all the non system threads
			IThread[] threadResult= target.getThreads();
			List threadsList = new ArrayList();
			IJavaThread thread;
			for (int i = 0; i < threadResult.length; i++) {
				thread = (IJavaThread)threadResult[i];
				if(!thread.isSystemThread()){
					threadsList.add(thread);
				}
			}
			IJavaThread[] threads= (IJavaThread[]) threadsList.toArray(new IJavaThread[threadsList.size()]);
			
			//suspend all the non system threads
			suspend(threads);
			
			IJavaObject[] ownedMonitors;
			IJavaObject currentContendedMonitors, monitor;
			//updating data on 
			//owning threads / owned monitors and contending threads / contended monitors
			for (int i = 0; i < threads.length; i++) {
				thread = threads[i];
				ownedMonitors = thread.getOwnedMonitors();
				currentContendedMonitors = thread.getCurrentContendedMonitor();
				// owning threads / owned monitors
				if(thread.hasOwnedMonitors()){
					addThreadWithOwnedMonitors(thread, ownedMonitors);
					
					for(int j=0; j < ownedMonitors.length; j++) {
						monitor = ownedMonitors[j];
						addMonitorWithOwningThread(monitor, thread);
					}
				}
				// contending threads / contended monitors
				if(thread.hasContendedMonitors()){
					addThreadWithContendedMonitor(thread, currentContendedMonitors);
					addMonitorWithContendedThread(currentContendedMonitors, thread);
				}
			}
			
			//updating data on deadlocks
			for (int i = 0; i < threads.length; i++) {
				thread = threads[i];
				
				List l = listToDeadlock(thread, new ArrayList(4));
				// if thread is caught in a deadlock, 
				// l will be the list showing this deadlock
				if(l != null){
					ThreadWrapper tw = new ThreadWrapper(thread, l);
					// adding this deadlock list
					fDeadLockLists.add(tw);
				}
			}
		} catch(DebugException e){
			JDIDebugPlugin.log(e);
		}
	}
		
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#updatePartial(org.eclipse.jdt.debug.core.IJavaDebugTarget)
	 */
	public void updatePartial(IJavaDebugTarget target){

		try {
			removeMonitorInformation(target);
			
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
			
			IJavaObject[] ownedMonitors;
			IJavaObject currentContendedMonitors, monitor;		
			for (int i = 0; i < threads.length; i++) {
				thread = threads[i];
				ownedMonitors = thread.getOwnedMonitors();
				currentContendedMonitors = thread.getCurrentContendedMonitor();
				
				// owning threads / owned monitors
				if(thread.hasOwnedMonitors()){
					addThreadWithOwnedMonitors(thread, ownedMonitors);
					
					for(int j=0; j < ownedMonitors.length; j++){
						monitor = ownedMonitors[j];
						addMonitorWithOwningThread(monitor, thread);
					}
				}
				// contending threads / contended monitors
				if(thread.hasContendedMonitors()){
					addThreadWithContendedMonitor(thread, currentContendedMonitors);
					addMonitorWithContendedThread(currentContendedMonitors, thread);
				}
			}
			
			for (int i = 0; i < threads.length; i++) {
				thread = (IJavaThread)threads[i];
					
				// deadlocks
				List l = listToDeadlock(thread, new ArrayList(4));
				// if thread is in a deadlock
				if(l != null){
					ThreadWrapper tw = new ThreadWrapper(thread, l);
					fDeadLockLists.add(tw);
				}
			}
		} catch(DebugException e){
			JDIDebugPlugin.log(e);
		}
	}

	/**
	 * Suspend all the given threads
	 * @param The list of threads to suspend
	 */
	private void suspend(IJavaThread[] threads){		
		try {
			for (int i = 0; i < threads.length; i++) {
				IJavaThread thread = (IJavaThread)threads[i];
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
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#removeMonitorInformation(org.eclipse.jdt.debug.core.IJavaDebugTarget)
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
		} 
		// if the thread is not waiting for any monitor
		else {
			return null;	
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getNumberOfDeadlocks())
	 */
	public int getNumberOfDeadlocks() {
		return fDeadLockLists.size();
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getDeadlockList(int)
	 */
	public List getDeadlockList(int index) {
		if (index >= fDeadLockLists.size()) {
			return null;
		}
		return ((ThreadWrapper)fDeadLockLists.get(index)).getDeadLockList();
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#getStartThread(int)
	 */
	public IJavaThread getStartThread(int index) {
		if (index >= fDeadLockLists.size()) {
			return null;
		}
		return ((ThreadWrapper)fDeadLockLists.get(index)).getStartThread();
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IMonitorManager#isCaughtInDeadlock(org.eclipse.jdt.debug.core.IJavaThread)
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