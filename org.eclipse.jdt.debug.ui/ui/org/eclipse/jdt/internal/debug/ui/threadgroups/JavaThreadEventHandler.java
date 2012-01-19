/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPreferenceInitializer;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaElementContentProvider;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.2
 *
 */
public class JavaThreadEventHandler extends ThreadEventHandler implements IPropertyChangeListener, TreeListener {
	
	private boolean fDisplayMonitors;
	private Tree fTree;

	/**
	 * Constructs and event handler for a Java thread.
	 * 
	 * @param proxy
	 */
	public JavaThreadEventHandler(AbstractModelProxy proxy) {
		super(proxy);
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(this);
		fDisplayMonitors= preferenceStore.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
	}
	
	protected void init(Viewer viewer) {
		Control control = viewer.getControl();
		if (control instanceof Tree) {
			fTree = (Tree) control;
			fTree.getDisplay().asyncExec(new Runnable() {
				public void run() {
					fTree.addTreeListener(JavaThreadEventHandler.this);
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#dispose()
	 */
	@Override
	public synchronized void dispose() {
		if (fTree != null) {
			fTree.removeTreeListener(this);
		}
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.removePropertyChangeListener(this);
		super.dispose();
	}

	@Override
	protected ModelDelta addPathToThread(ModelDelta delta, IThread thread) {
		if (JavaElementContentProvider.isDisplayThreadGroups()) {
			ILaunch launch = thread.getLaunch();
			ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
			Object[] launchChildren = launch.getChildren();
			delta = delta.addNode(launch, indexOf(launches, launch), IModelDelta.NO_CHANGE, launchChildren.length);
			IJavaDebugTarget debugTarget = (IJavaDebugTarget) thread.getDebugTarget();
			List<IJavaThreadGroup> groups = new ArrayList<IJavaThreadGroup>();
			try{
				delta = delta.addNode(debugTarget, indexOf(launchChildren, debugTarget), IModelDelta.NO_CHANGE, debugTarget.getRootThreadGroups().length);
				IJavaThread javaThread = (IJavaThread) thread;
				IJavaThreadGroup threadGroup = javaThread.getThreadGroup();
				while (threadGroup != null) {
					groups.add(0, threadGroup);
					threadGroup = threadGroup.getThreadGroup();
				}
				Iterator<IJavaThreadGroup> iterator = groups.iterator();
				while (iterator.hasNext()) {
					IJavaThreadGroup group = iterator.next();
					int index = -1;
					IJavaThreadGroup parent = group.getThreadGroup();
					if (parent != null) {
						index = indexOf(parent.getThreadGroups(), group);
						if (index >= 0) {
							// threads are displayed first
							index += parent.getThreads().length;
						}
					} else {
						index = indexOf(debugTarget.getRootThreadGroups(), group);
					}
					delta = delta.addNode(group, index, IModelDelta.NO_CHANGE, group.getThreadGroups().length + group.getThreads().length);
				}
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
				}
			return delta;
		}
		return super.addPathToThread(delta, thread);
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO)) {
			fDisplayMonitors= JDIDebugUIPreferenceInitializer.getBoolean(event);
		}
	}

	protected boolean isDisplayMonitors() {
	    return fDisplayMonitors;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#indexOf(org.eclipse.debug.core.model.IStackFrame)
	 */
	@Override
	protected int indexOf(IStackFrame frame) {
		if (isDisplayMonitors()) {
			if (((IJavaDebugTarget)frame.getDebugTarget()).supportsMonitorInformation()) {
				IJavaThread thread = (IJavaThread)frame.getThread();
				int index = 0;
				try {
					index = thread.getOwnedMonitors().length;
					if (thread.getContendedMonitor() != null) {
						index++;
					}
				} catch (DebugException e) {
				}
				return index;
			}
			// make room for the 'no monitor info' element
			return 1;
		}
		return super.indexOf(frame);
	}
	
	/**
	 * Returns the number of children the given thread has in the view.
	 * 
	 * @param thread thread
	 * @return number of children
	 */
	@Override
	protected int childCount(IThread thread) {
		try {
			IJavaThread jThread = (IJavaThread) thread;
			int count = jThread.getFrameCount();
			if (isDisplayMonitors()) {
				if (((IJavaDebugTarget)thread.getDebugTarget()).supportsMonitorInformation()) {
					count += jThread.getOwnedMonitors().length;
					if (jThread.getContendedMonitor() != null) {
						count++;
					}
				} else {
					// make room for the 'no monitor info' element
					count++;
				}
			}
			return count;
		} catch (DebugException e) {
		}
		return -1;
	}	
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#indexOf(org.eclipse.debug.core.model.IThread)
	 */
	@Override
	protected int indexOf(IThread thread) {
		if (JavaElementContentProvider.isDisplayThreadGroups()) {
			IJavaThread javaThread = (IJavaThread) thread;
			try {
				return indexOf(javaThread.getThreadGroup().getThreads(), javaThread);
			} catch (CoreException e) {
				return -1;
			}
		}
		return super.indexOf(thread);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#handlesEvent(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	protected boolean handlesEvent(DebugEvent event) {
		if (super.handlesEvent(event)) {
			Object source = event.getSource();
			if (source instanceof IJavaThread) {
				IJavaThread thread = (IJavaThread) source;
				ILaunch launch = thread.getLaunch();
				if (launch != null) {
					if (launch.getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) != null) {
						if (event.getKind() == DebugEvent.SUSPEND) {
							try {
								IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
								if (frame == null || frame.getDeclaringTypeName().startsWith("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain")) { //$NON-NLS-1$
									return false;
								}
							} catch (DebugException e) {
							}
						}
					}
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.TreeListener#treeCollapsed(org.eclipse.swt.events.TreeEvent)
	 */
	public void treeCollapsed(TreeEvent e) {
		// when the user collapses a thread, remove it from the 'next thread to select queue'
		Widget widget = e.item;
		if (widget instanceof TreeItem) {
			TreeItem item = (TreeItem) widget;
			Object data = item.getData();
			if (data instanceof IJavaThread) {
				removeQueuedThread((IJavaThread)data);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.TreeListener#treeExpanded(org.eclipse.swt.events.TreeEvent)
	 */
	public void treeExpanded(TreeEvent e) {
		// when the expands a thread, add it back to the 'next thread to select queue'
		Widget widget = e.item;
		if (widget instanceof TreeItem) {
			TreeItem item = (TreeItem) widget;
			Object data = item.getData();
			if (data instanceof IJavaThread) {
				queueSuspendedThread((IJavaThread)data);
			}
		}
	}	

	/**
	 * Do not update for quiet resume/suspend
	 */
	@Override
	protected void handleOther(DebugEvent event) {
		if (event.getDetail() == JDIThread.SUSPEND_QUIET || event.getDetail() == JDIThread.RESUME_QUIET) {
			return;
		}
		super.handleOther(event);
	}
	
	/**
	 * Returns whether the given thread is missing its required thread group in order
	 * to build a proper delta. See bug 274552. Returns <code>false</code> when not 
	 * displaying thread groups.
	 * 
	 * @param event thread start/death event
	 * @return <code>true</code> if the thread group is missing
	 */
	private boolean isMissingRequiredThreadGroup(DebugEvent event) {
		if (JavaElementContentProvider.isDisplayThreadGroups()) {
			Object source = event.getSource();
			if (source instanceof IJavaThread) {
				// if we can't retrieve a thread group we won't be able to add/remove
				// the thread from the view (we can't get a path to the thread)
				IJavaThread thread = (IJavaThread) source;
				try {
					if (thread.getThreadGroup() == null) {
						return true;
					}
				} catch (DebugException e) {
					return true;
				}
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#handleCreate(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	protected void handleCreate(DebugEvent event) {
		if (isMissingRequiredThreadGroup(event)) {
			// don't bother adding/removing thread missing thread group
			return;
		}
		super.handleCreate(event);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#handleTerminate(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	protected void handleTerminate(DebugEvent event) {
		if (isMissingRequiredThreadGroup(event)) {
			// don't bother adding/removing thread missing thread group
			return;
		}
		super.handleTerminate(event);
	}
}
