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
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jdt.debug.core.JDIDebugModel;

public class JDIDebugPluginPreferenceInitializer extends AbstractPreferenceInitializer {

	public JDIDebugPluginPreferenceInitializer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences prefs = JDIDebugPlugin.getDefault().getPluginPreferences();
		prefs.setDefault(JDIDebugModel.PREF_REQUEST_TIMEOUT,
				JDIDebugModel.DEF_REQUEST_TIMEOUT);
		prefs.setDefault(JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS, true);
		prefs.setDefault(
				JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION,
				true);
		prefs.addPropertyChangeListener(JDIDebugPlugin.getDefault());
	}
}
