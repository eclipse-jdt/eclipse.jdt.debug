/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.DefaultDebugViewContentProvider;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Java debugger content provider for the debug view. Provides monitor
 * information.
 * 
 * @since 3.1
 */
public class JavaDebugContentProvider extends DefaultDebugViewContentProvider implements IPropertyChangeListener {
	
	private boolean fDisplayMonitors= false;
	
	public JavaDebugContentProvider() {
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(this);
		fDisplayMonitors= preferenceStore.getBoolean(IJDIPreferencesConstants.PREF_SHOW_MONITOR_THREAD_INFO);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		try {
			if (fDisplayMonitors && parent instanceof IJavaThread) {
				IJavaThread thread= (IJavaThread)parent;
				ThreadMonitorManager threadMonitorManager= ThreadMonitorManager.getDefault();
				JavaOwnedMonitor[] ownedMonitors= threadMonitorManager.getOwnedMonitors(thread);
				JavaContendedMonitor contendedMonitor= threadMonitorManager.getContendedMonitor(thread);
				IStackFrame[] stackFrames= thread.getStackFrames();
				Object[] children= new Object[ownedMonitors.length + (contendedMonitor == null ? 0 : 1) + stackFrames.length];
				int k= 0;
				for (int i= 0; i < ownedMonitors.length; i++) {
					children[k++]= ownedMonitors[i];
				}
				if (contendedMonitor != null) {
					children[k++]= contendedMonitor;
				}
				for (int i= 0; i < stackFrames.length; i++) {
					children[k++]= stackFrames[i];
				}
				return children;
			}
			if (parent instanceof  JavaOwnedMonitor) {
				return ((JavaOwnedMonitor)parent).getWaitingThreads();
			}
			if (parent instanceof JavaContendedMonitor) {
				JavaOwningThread owningThread= ((JavaContendedMonitor)parent).getOwningThread();
				if (owningThread != null) {
					return new Object[] {owningThread};
				}
			}
			if (parent instanceof JavaWaitingThread) {
				return ((JavaWaitingThread)parent).getOwnedMonitors();
			}
			if (parent instanceof JavaOwningThread) {
				JavaContendedMonitor contendedMonitor= ((JavaOwningThread)parent).getContendedMonitor();
				if (contendedMonitor != null) {
					return new Object[] {contendedMonitor};
				}
			}
		} catch (DebugException e) {
		}
		return super.getChildren(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.DefaultDebugViewContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof JavaOwnedMonitor) {
			return ((JavaOwnedMonitor)element).getWaitingThreads().length > 0;
		}
		if (element instanceof JavaContendedMonitor) {
			return ((JavaContendedMonitor)element).getOwningThread() != null;
		}
		if (element instanceof JavaOwningThread) {
			return ((JavaOwningThread)element).getContendedMonitor() != null;
		}
		if (element instanceof JavaWaitingThread) {
			return ((JavaWaitingThread)element).getOwnedMonitors().length > 0;
		}
		return super.hasChildren(element);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.DefaultDebugViewContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof JavaOwnedMonitor) {
			JavaWaitingThread parent= ((JavaOwnedMonitor)element).getParent();
			if (parent.getParent() == null) {
				return parent.getThread();
			}
			return parent;
		}
		if (element instanceof JavaContendedMonitor) {
			JavaOwningThread parent= ((JavaContendedMonitor) element).getParent();
			if (parent.getParent() == null) {
				return parent.getThread();
			}
			return parent;
		}
		if (element instanceof JavaOwningThread) {
			return ((JavaOwningThread)element).getParent();
		}
		if (element instanceof JavaWaitingThread) {
			return ((JavaWaitingThread)element).getParent();
		}
		return super.getParent(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IJDIPreferencesConstants.PREF_SHOW_MONITOR_THREAD_INFO)) {
			fDisplayMonitors= ((Boolean)event.getNewValue()).booleanValue();
		}
	}

}
