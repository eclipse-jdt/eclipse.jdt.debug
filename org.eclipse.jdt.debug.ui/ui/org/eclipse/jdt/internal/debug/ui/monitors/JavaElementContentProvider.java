/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.debug.internal.ui.model.elements.ElementContentProvider;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Common element presentation for the debug view.
 * 
 * @since 3.3
 */
public abstract class JavaElementContentProvider extends ElementContentProvider {
	
	private static boolean fgDisplayMonitors;
	private static boolean fgDisplayThreadGroups;
	
	static {
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO)) {
					fgDisplayMonitors= JDIDebugUIPreferenceInitializer.getBoolean(event);
				} else if (event.getProperty().equals(IJavaDebugUIConstants.PREF_SHOW_THREAD_GROUPS)) {
					fgDisplayThreadGroups= JDIDebugUIPreferenceInitializer.getBoolean(event);
				}
			}
		
		});
		fgDisplayMonitors= preferenceStore.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
		fgDisplayThreadGroups = preferenceStore.getBoolean(IJavaDebugUIConstants.PREF_SHOW_THREAD_GROUPS);
	}
	
	public static boolean isDisplayThreadGroups() {
		return fgDisplayThreadGroups;
	}

	public static boolean isDisplayMonitors() {
	    return fgDisplayMonitors;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#supportsContextId(java.lang.String)
	 */
	@Override
	protected boolean supportsContextId(String id) {
		return IDebugUIConstants.ID_DEBUG_VIEW.equals(id);
	}

}
