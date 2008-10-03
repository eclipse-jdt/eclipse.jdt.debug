/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Initializes preferences for the JDT launching plug-in.
 *
 * @since 3.5
 */
public class LaunchingPreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences preferences = JavaRuntime.getPreferences();
		
		preferences.setDefault(JavaRuntime.PREF_CONNECT_TIMEOUT, JavaRuntime.DEF_CONNECT_TIMEOUT);
		preferences.setDefault(JavaRuntime.PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE, JavaRuntime.VALUE_WARNING);

	}

}
