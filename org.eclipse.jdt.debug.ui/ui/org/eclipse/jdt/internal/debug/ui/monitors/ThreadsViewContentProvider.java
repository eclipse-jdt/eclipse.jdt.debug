package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.monitors.MonitorManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides the tree data for the thread view
 */
public class ThreadsViewContentProvider implements ITreeContentProvider {

	/**
	 * MonitorWrapper for the thread view
	 * We use it to know the state of the monitor we display: owned or contending
	 */	
	public class MonitorWrapper{
		public static final int OWNED_MONITOR = 1;
		public static final int CONTENDED_MONITOR = 2;
		public IJavaObject monitor;
		public int state;
	}

	/**
	 * ThreadWrapper for the monitor view
	 * We use it to know the state of the thread we display: caught in a deadlock or no
	 */	
	public class ThreadWrapper{
		public IJavaThread thread;
		public boolean isCaughtInDeadlock;
	}
		
	protected TreeViewer fViewer= null;

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {
		
		//the parent will be ThreadWrapper
		if (parentElement instanceof ThreadWrapper) {
			IJavaThread thread= (IJavaThread)((ThreadWrapper)parentElement).thread;
			
			//owned monitors
			List ownedMonitors= MonitorManager.getDefault().getOwnedMonitors(thread);
			
			//contended monitor
			IJavaObject contendedMonitor= MonitorManager.getDefault().getContendedMonitor(thread);
			if (ownedMonitors == null && contendedMonitor == null) {
				return null;
			} 
			
			//adding the monitors to the result
			int size= 0;
			if (ownedMonitors != null) {
				size= ownedMonitors.size();
			}
			if (contendedMonitor != null) {
				size= size + 1;
			}
			//transforming the result to MonitorWrapper, setting the type
			Object[] children= new Object[size];
			if (ownedMonitors != null) {
				List wrappedMonitors = new ArrayList();
				for (int i = 0; i < ownedMonitors.size(); i++) {
					MonitorWrapper mw = new MonitorWrapper();
					mw.monitor = (IJavaObject) ownedMonitors.get(i);
					mw.state = MonitorWrapper.OWNED_MONITOR;
					wrappedMonitors.add(mw);
				}
				wrappedMonitors.toArray(children);
			}
			if (contendedMonitor != null) {
				MonitorWrapper mw = new MonitorWrapper();
				mw.monitor = contendedMonitor;
				mw.state = MonitorWrapper.CONTENDED_MONITOR;
				children[children.length - 1]= mw;
			}
			return children;
		}
				
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {	
		
		if (element instanceof IJavaThread) {
			return MonitorManager.getDefault().getOwnedMonitors((IJavaThread)element);
		}
		else if (element instanceof IJavaObject) {
			return MonitorManager.getDefault().getOwningThread((IJavaObject)element);
		}		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
			
		if (element instanceof IJavaThread) {
			IJavaThread thread= (IJavaThread)element;
			List ownedMonitors= MonitorManager.getDefault().getOwnedMonitors(thread);
			IJavaObject contendedMonitor= MonitorManager.getDefault().getContendedMonitor(thread);
			if (ownedMonitors == null && contendedMonitor == null) {
				return false;
			} 
			else{
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		
		//the root elements are ThreadWrapper
		Set allThreads= MonitorManager.getDefault().getThreads();
		Object[] res = allThreads.toArray(new Object[allThreads.size()]);
		for (int i = 0; i < res.length; i++) {
			ThreadWrapper tw = new ThreadWrapper();
			tw.thread = (IJavaThread) res[i];
			if(MonitorManager.getDefault().isCaughtInDeadLock((IJavaThread)res[i])) {
				tw.isCaughtInDeadlock = true;
			} else {
				tw.isCaughtInDeadlock = false;
			}
			res[i] = tw;
		}
		return res;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		fViewer= null;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fViewer= (TreeViewer)viewer;
	}
}