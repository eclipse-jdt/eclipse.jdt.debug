package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.List;

import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.monitors.MonitorManager;
import org.eclipse.jdt.internal.debug.core.monitors.ThreadWrapper;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides the tree data for the deadlock view
 */
public class DeadLocksViewContentProvider implements ITreeContentProvider {
	
	/**
	 * ThreadWrapper for the deadlock view
	 * Uses the data of ThreadWrapper
	 * We use this to differentiate the same thread in a deadlock list (the first and the last element in the list)
	 * @see ThreadWrapper
	 */
	public class ContentThreadWrapper {
		public IJavaThread fThread;
		public IJavaObject fParent;
		public List fDeadLockList;
		public boolean caughtInADeadLock;
	}

	/**
	 * MonitorWrapper for the deadlock view
	 */	
	public class ContentMonitorWrapper {
		public IJavaObject fMonitor;
		public IJavaThread fParent;
		public List fDeadLockList;
	}
	
	protected TreeViewer fViewer= null;

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {
		
		//by the definition of the deadlock list, there only one child
		Object[] res = new Object[1];
		
		//if the parent is a ContentThreadWrapper, the child will be a ContentMonitorWrapper
		if (parentElement instanceof ContentThreadWrapper) {
			ContentMonitorWrapper cmw = new ContentMonitorWrapper();
			List deadLockList = (((ContentThreadWrapper)parentElement).fDeadLockList);
			
			// searching the element in the list
			for (int i = 0; i < deadLockList.size(); i+=2) {
				// if it is the same elements
				if(((ContentThreadWrapper)parentElement).fThread.equals(deadLockList.get(i))){
					// if they have the same parent or if the parent is null
					if((i < deadLockList.size() - 1) &&
						((i > 0) && ((ContentThreadWrapper)parentElement).fParent.equals(deadLockList.get(i-1)))
						|| ((((ContentThreadWrapper)parentElement).fParent == null) && (i == 0))) {
						//transforms the child in a ContentMonitorWrapper
						cmw.fMonitor = (IJavaObject)deadLockList.get(i+1);
						cmw.fParent = ((ContentThreadWrapper)parentElement).fThread;
						cmw.fDeadLockList = deadLockList;
						res[0] = cmw;
						return res;
					}
				}			
			}
		}
		
		//if the parent is a ContentMonitorWrapper, the child will be a ContentThreadWrapper
		else if (parentElement instanceof ContentMonitorWrapper) {
			ContentThreadWrapper ctw = new ContentThreadWrapper();
			List deadLockList = (((ContentMonitorWrapper)parentElement).fDeadLockList);
			
			// searching the element in the list
			for (int i = 1; i < deadLockList.size(); i+=2) {
				// if it is the same elements
				if(((ContentMonitorWrapper)parentElement).fMonitor.equals(deadLockList.get(i))){
					// if they have the same parent or if the parent is null
					if(((i > 0) && ((ContentMonitorWrapper)parentElement).fParent.equals(deadLockList.get(i-1)))
					|| ((((ContentMonitorWrapper)parentElement).fParent == null) && (i == 0))){
						//transforms the child in a ContentThreadWrapper
						ctw.fThread = (IJavaThread)deadLockList.get(i+1);
						ctw.fParent = ((ContentMonitorWrapper)parentElement).fMonitor;
						ctw.fDeadLockList = deadLockList;
						if(i == deadLockList.size() - 2){
							ctw.caughtInADeadLock = true;
						}
						res[0] = ctw;
						return res;
					}
				}			
			}
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		
		//if the child is a ContentThreadWrapper, the parent will be a ContentMonitorWrapper
		if (element instanceof ContentThreadWrapper) {
			ContentMonitorWrapper cmw = new ContentMonitorWrapper();
			List deadLockList = (((ContentThreadWrapper)element).fDeadLockList);
			
			// searching the element in the list
			for (int i = 0; i < deadLockList.size(); i+=2) {
				// if it is the same elements
				if(((ContentThreadWrapper)element).fThread.equals((IJavaThread)(deadLockList.get(i)))){
					// if they have the same parent or if the parent is null
					if ((i>0)&&((ContentThreadWrapper)element).fParent.equals(deadLockList.get(i-1))) {
						//transforms the parent in a ContentMonitorWrapper
						cmw.fMonitor = (IJavaObject)deadLockList.get(i-1);
						cmw.fParent = (IJavaThread)deadLockList.get(i-2);
						cmw.fDeadLockList = deadLockList;
						return cmw;
					}
					else if((((ContentThreadWrapper)element).fParent == null)&&(i==0)){
						return null;
					}
				}			
			}
		}
	
		//if the child is a ContentMonitorWrapper, the child will be a ContentThreadWrapper
		else if (element instanceof ContentMonitorWrapper) {
			ContentThreadWrapper ctw = new ContentThreadWrapper();
			List deadLockList = (((ContentMonitorWrapper)element).fDeadLockList);
			
			// searching the element in the list
			for (int i = 1; i < deadLockList.size(); i+=2) {
				// if it is the same elements
				if(((ContentMonitorWrapper)element).fMonitor.equals(deadLockList.get(i))){
					// if they have the same parent or if the parent is null
					if((i > 0) && ((ContentMonitorWrapper)element).fParent.equals(deadLockList.get(i-1))) {
						//transforms the parent in a ContentThreadWrapper
						ctw.fThread = (IJavaThread)deadLockList.get(i-1);
						ctw.fParent = (IJavaObject)deadLockList.get(i-2);
						ctw.fDeadLockList = deadLockList;
						return ctw;
					} else if((((ContentMonitorWrapper)element).fParent == null)&&(i==0)){
						return null;	
					}
				}			
			}
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		
		//if the parent is a ContentThreadWrapper, the child will be a ContentMonitorWrapper
		if (element instanceof ContentThreadWrapper) {
			List deadLockList = (((ContentThreadWrapper)element).fDeadLockList);
			
			// searching the element in the list
			for (int i = 0; i < deadLockList.size(); i+=2) {
				// if it is the same elements
				if(((ContentThreadWrapper)element).fThread.equals(deadLockList.get(i))){
					// if they have the same parent or if the parent is null
					if(((i > 0) && ((ContentThreadWrapper)element).fParent.equals(deadLockList.get(i-1)))
					|| ((((ContentThreadWrapper)element).fParent == null) && (i==0))) {
						// if this element has a child
						if(i<(deadLockList.size()-1)){
							return true;
						} 
					}
				}
			}
		}
		
		//if the parent is a ContentMonitorWrapper, the child will be a ContentThreadWrapper
		else if (element instanceof ContentMonitorWrapper) {
			List deadLockList = (((ContentMonitorWrapper)element).fDeadLockList);
			
			// searching the element in the list
			for (int i = 1; i < deadLockList.size(); i+=2) {
				// if it is the same elements
				if(((ContentMonitorWrapper)element).fMonitor.equals(deadLockList.get(i))){
					// if they have the same parent or if the parent is null
					if(((i > 0) && ((ContentMonitorWrapper)element).fParent.equals(deadLockList.get(i-1)))
					|| ((((ContentMonitorWrapper)element).fParent == null) && (i==0))){
						// if this element has a child
						if(i < (deadLockList.size()-1)){
							return true;
						}
					}
				}			
			}
		}
		
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {

		List deadLockLists= MonitorManager.getDefault().getDeadLockLists();
		//the list of roots elements
		Object[] roots= new Object[deadLockLists.size()];
		
		for (int i = 0; i < deadLockLists.size(); i++) {
			//all the root elements are ContentThreadWrapper
			ContentThreadWrapper ctw = new ContentThreadWrapper();
			ctw.fThread = ((ThreadWrapper)(deadLockLists.get(i))).getStartThread();
			ctw.fParent = null;
			ctw.fDeadLockList = ((ThreadWrapper)(deadLockLists.get(i))).getDeadLockList();
			ctw.caughtInADeadLock = true;
			roots[i] = ctw;
		}
		return roots;
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
