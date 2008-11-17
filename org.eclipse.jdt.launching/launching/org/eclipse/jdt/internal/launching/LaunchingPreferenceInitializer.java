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

import java.util.Hashtable;

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
	public void initializeDefaultPreferences() {
		DefaultScope scope = new DefaultScope();
		IEclipsePreferences dnode = scope.getNode(LaunchingPlugin.ID_PLUGIN);
		if(dnode == null) {
			return;
		}
		dnode.putInt(JavaRuntime.PREF_CONNECT_TIMEOUT, JavaRuntime.DEF_CONNECT_TIMEOUT);
		dnode.put(JavaRuntime.PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE, JavaCore.WARNING);
		
		try {
			dnode.flush();
		} catch (BackingStoreException e) {
			LaunchingPlugin.log(e);
		}
		
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255381
		String launchFilter = "*." + ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION; //$NON-NLS-1$
		Hashtable optionsMap = JavaCore.getOptions();
		String filters= (String)optionsMap.get(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);
		boolean added = false;
		if (filters == null || filters.length() == 0) {
			filters = launchFilter;
			added = true;
		} else if (filters.indexOf(launchFilter) == -1) {
			added = true;
			filters = filters + ',' + launchFilter;
		}
		if(added) {
			optionsMap.put(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, filters);
			JavaCore.setOptions(optionsMap);
		}
		
		dnode = scope.getNode(JavaCore.PLUGIN_ID);
		if(dnode == null) {
			return;
		}
		dnode.put(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, filters);
		
		try {
			dnode.flush();
		} catch (BackingStoreException e) {
			LaunchingPlugin.log(e);
		}
	}
}
