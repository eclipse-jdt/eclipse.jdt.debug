/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Steffan Larson - Bug 162368 Include JRockit-internal classes in the default
 *     						step filters
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

public class JDIDebugUIPreferenceInitializer extends AbstractPreferenceInitializer {

	public JDIDebugUIPreferenceInitializer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, true);
		store.setDefault(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT, true);
		store.setDefault(IJDIPreferencesConstants.PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT, true);

		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES, false);
		
		// JavaStepFilterPreferencePage
		store.setDefault(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, "java.lang.ClassLoader"); //$NON-NLS-1$
		store.setDefault(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, "com.ibm.*,com.sun.*,java.*,javax.*,jrockit.*,org.omg.*,sun.*,sunw.*"); //$NON-NLS-1$
		store.setDefault(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS, true);
		
		store.setDefault(IDebugUIConstants.ID_VARIABLE_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_CONSTANTS, false); //$NON-NLS-1$
		store.setDefault(IDebugUIConstants.ID_EXPRESSION_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_CONSTANTS, false); //$NON-NLS-1$
		store.setDefault(IDebugUIConstants.ID_VARIABLE_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_STATIC_VARIABLES, false); //$NON-NLS-1$
		store.setDefault(IDebugUIConstants.ID_EXPRESSION_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_STATIC_VARIABLES, false); //$NON-NLS-1$

		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_CHAR, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_HEX, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_NULL_ARRAY_ENTRIES, true);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_DETAILS, IJDIPreferencesConstants.DETAIL_PANE);
		
		store.setDefault(IJavaDebugUIConstants.PREF_SHOW_SYSTEM_THREADS, false);
		store.setDefault(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, true);
		store.setDefault(IJavaDebugUIConstants.PREF_SHOW_THREAD_GROUPS, false);
		store.setDefault(IJDIPreferencesConstants.PREF_OPEN_INSPECT_POPUP_ON_EXCEPTION, false);
		store.setDefault(IJavaDebugUIConstants.PREF_ALLINSTANCES_MAX_COUNT, 100);
		store.setDefault(IJavaDebugUIConstants.PREF_ALLREFERENCES_MAX_COUNT, 100);
	}
	
	/**
	 * Returns the boolean value from the given property change event.
	 * 
	 * @param event property change event
	 * @return new boolean value from the event
	 */
	public static boolean getBoolean(PropertyChangeEvent event) {
		Object newValue = event.getNewValue();
		if (newValue instanceof String) {
			return ((IPreferenceStore)event.getSource()).getBoolean(event.getProperty());
		}
		return ((Boolean)newValue).booleanValue();
	}
}
