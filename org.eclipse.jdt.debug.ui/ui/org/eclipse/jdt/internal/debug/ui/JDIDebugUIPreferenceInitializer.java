/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class JDIDebugUIPreferenceInitializer extends AbstractPreferenceInitializer {

	public JDIDebugUIPreferenceInitializer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, true);
		store.setDefault(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT, true);

		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES, false);
		
		// JavaStepFilterPreferencePage
		store.setDefault(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, "java.lang.ClassLoader"); //$NON-NLS-1$
		store.setDefault(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, "com.ibm.*,com.sun.*,java.*,javax.*,org.omg.*,sun.*,sunw.*"); //$NON-NLS-1$
				
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_CONSTANTS, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_STATIC_VARIALBES, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_CHAR, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_HEX, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_NULL_ARRAY_ENTRIES, true);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_DETAILS, IJDIPreferencesConstants.DETAIL_PANE);
		
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_SYSTEM_THREADS, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_MONITOR_THREAD_INFO, false);
	}
}
