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

import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.monitors.MonitorManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.sun.jdi.ObjectReference;

/**
 * Provides the tree data for the monitor view
 */
public class MonitorsViewContentProvider implements ITreeContentProvider {
	
	/**
	 * ThreadWrapper for the monitor view
	 * We use it to know the state of the thread we display: owning or waiting
	 */
	public class ThreadWrapper{
		public static final int OWNING_THREAD = 1;
		public static final int CONTENDING_THREAD = 2;
		public JDIThread thread;
		public int state;
	}
	
	TreeViewer fViewer= null;

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {

		//the parents will be monitors
		if (parentElement instanceof ObjectReference) {
			ObjectReference monitor= (ObjectReference)parentElement;
			//owning thread
			JDIThread owningThread = MonitorManager.getDefault().getOwningThread(monitor);
			
			//contending threads
			List contendingThreads = MonitorManager.getDefault().getContendingThreads(monitor);
			if (owningThread == null && contendingThreads == null) {
				return null;
			} 
			
			//adding the threads to the result
			int size= 0;
			if (contendingThreads != null) {
				size= contendingThreads.size();
			}
			if (owningThread != null) {
				size= size + 1;
			}
			//transforming the result to ThreadWrapper, setting the type
			Object[] children= new Object[size];
			if (contendingThreads != null) {
				List wrappedThreads = new ArrayList();
				for (int i = 0; i < contendingThreads.size(); i++) {
					ThreadWrapper tw = new ThreadWrapper();
					tw.thread = (JDIThread) contendingThreads.get(i);
					tw.state = ThreadWrapper.CONTENDING_THREAD;
					wrappedThreads.add(tw);
				}
				wrappedThreads.toArray(children);
				wrappedThreads.toArray(children);
			}
			if (owningThread != null) {
				ThreadWrapper tw = new ThreadWrapper();
				tw.thread = owningThread;
				tw.state = ThreadWrapper.OWNING_THREAD;
				children[children.length - 1] = tw;
			}
			return children;
		}
				
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		
		if (element instanceof JDIThread) {
			return MonitorManager.getDefault().getOwnedMonitors((JDIThread)element);
		}
		else if (element instanceof ObjectReference) {
			return MonitorManager.getDefault().getOwningThread((ObjectReference)element);
		}		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		
		if (element instanceof ObjectReference) {
			ObjectReference monitor= (ObjectReference)element;
			JDIThread owningThread = MonitorManager.getDefault().getOwningThread(monitor);
			List contendingThreads = MonitorManager.getDefault().getContendingThreads(monitor);
			if (owningThread == null && contendingThreads == null) {
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
		Set allMonitors = MonitorManager.getDefault().getMonitors();
		return (Object[]) allMonitors.toArray(new Object[allMonitors.size()]);
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
		fViewer= (TreeViewer)viewer;
	}

}
