/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
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
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaElementContentProvider;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @since 3.2
 *
 */
public class JavaThreadEventHandler extends ThreadEventHandler implements IPropertyChangeListener {
	
	private boolean fDisplayMonitors;

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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#dispose()
	 */
	public synchronized void dispose() {
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.removePropertyChangeListener(this);
		super.dispose();
	}

	protected ModelDelta addPathToThread(ModelDelta delta, IThread thread) {
		if (JavaElementContentProvider.isDisplayThreadGroups()) {
			ILaunch launch = thread.getLaunch();
			ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
			Object[] launchChildren = launch.getChildren();
			delta = delta.addNode(launch, indexOf(launches, launch), IModelDelta.NO_CHANGE, launchChildren.length);
			IJavaDebugTarget debugTarget = (IJavaDebugTarget) thread.getDebugTarget();
			List groups = new ArrayList();
			try{
				delta = delta.addNode(debugTarget, indexOf(launchChildren, debugTarget), IModelDelta.NO_CHANGE, debugTarget.getRootThreadGroups().length);
				IJavaThread javaThread = (IJavaThread) thread;
				IJavaThreadGroup threadGroup = javaThread.getThreadGroup();
				while (threadGroup != null) {
					groups.add(0, threadGroup);
					threadGroup = threadGroup.getThreadGroup();
				}
				Iterator iterator = groups.iterator();
				while (iterator.hasNext()) {
					IJavaThreadGroup group = (IJavaThreadGroup) iterator.next();
					int index = -1;
					IJavaThreadGroup parent = group.getThreadGroup();
					if (parent != null) {
						index = indexOf(parent.getThreadGroups(), group);
					} else {
						index = indexOf(debugTarget.getRootThreadGroups(), group);
					}
					delta = delta.addNode(group, index, IModelDelta.NO_CHANGE, group.getThreadGroups().length + group.getThreads().length);
				}
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
				}
			return delta;
		} else {
			return super.addPathToThread(delta, thread);
		}
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO)) {
			fDisplayMonitors= ((Boolean)event.getNewValue()).booleanValue();
		}
	}

	protected boolean isDisplayMonitors() {
	    return fDisplayMonitors;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#indexOf(org.eclipse.debug.core.model.IStackFrame)
	 */
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
			} else {
				// make room for the 'no monitor info' element
				return 1;
			}
		} else {
			return super.indexOf(frame);
		}
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#indexOf(org.eclipse.debug.core.model.IThread)
	 */
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
								if (frame.getDeclaringTypeName().startsWith("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain")) { //$NON-NLS-1$
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

}
