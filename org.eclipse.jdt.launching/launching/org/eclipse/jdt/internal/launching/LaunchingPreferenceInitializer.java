/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Initializes preferences for the JDT launching plug-in.
 *
 * @since 3.5
 */
public class LaunchingPreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences dnode = DefaultScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
		if(dnode == null) {
			return;
		}
		dnode.putInt(JavaRuntime.PREF_CONNECT_TIMEOUT, JavaRuntime.DEF_CONNECT_TIMEOUT);
		dnode.put(JavaRuntime.PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE, JavaCore.WARNING);
		dnode.put(JavaRuntime.PREF_COMPILER_COMPLIANCE_DOES_NOT_MATCH_JRE, JavaCore.WARNING);
		dnode.putBoolean(JavaRuntime.PREF_ONLY_INCLUDE_EXPORTED_CLASSPATH_ENTRIES, false);
		try {
			dnode.flush();
		} catch (BackingStoreException e) {
			LaunchingPlugin.log(e);
		}

		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=255381
		// NOTE: only the pref's default value is initialized to avoid deadlock (we don't set the
		// associated JavaCore options, as this can trigger a job to touch the project (see
		// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=260445)
		String launchFilter = "*." + ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION; //$NON-NLS-1$
		dnode = DefaultScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		if(dnode == null) {
			return;
		}
		//check to see if another plug-in has contributed to these defaults as well
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=395366
		String val = dnode.get(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, (String)null);
		if(val != null && !"".equals(val)) { //$NON-NLS-1$
			String[] filters = val.split(","); //$NON-NLS-1$
			StringBuilder buff = new StringBuilder();
			boolean found = false;
			for (int i = 0; i < filters.length; i++) {
				if(launchFilter.equals(val)) {
					found = true;
					break;
				}
				String filter = filters[i].trim();
				buff.append(filter);
				if(i < filters.length-1) {
					buff.append(',');
				}
			}
			if(!found) {
				launchFilter = buff.append(',').append(launchFilter).toString();
				dnode.put(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, launchFilter);
			}
		}
		else {
			dnode.put(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, launchFilter);
		}

		try {
			dnode.flush();
		} catch (BackingStoreException e) {
			LaunchingPlugin.log(e);
		}
	}
}
