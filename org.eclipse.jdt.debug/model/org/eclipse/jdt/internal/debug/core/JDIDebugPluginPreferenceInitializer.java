/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;

public class JDIDebugPluginPreferenceInitializer extends
		AbstractPreferenceInitializer {

	public JDIDebugPluginPreferenceInitializer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		node.putInt(JDIDebugModel.PREF_REQUEST_TIMEOUT,	JDIDebugModel.DEF_REQUEST_TIMEOUT);
		node.putBoolean(JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS, true);
		node.putBoolean(JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION, true);
		node.putInt(JDIDebugPlugin.PREF_DEFAULT_BREAKPOINT_SUSPEND_POLICY, IJavaBreakpoint.SUSPEND_THREAD);
		// 0 is the first index, meaning both access and modification
		node.putInt(JDIDebugPlugin.PREF_DEFAULT_WATCHPOINT_SUSPEND_POLICY, 0);
		node.putBoolean(JDIDebugPlugin.PREF_SHOW_REFERENCES_IN_VAR_VIEW, false);
		node.putInt(JDIDebugPlugin.PREF_ALL_REFERENCES_MAX_COUNT, 100);
		node.putInt(JDIDebugPlugin.PREF_ALL_INSTANCES_MAX_COUNT, 100);
	}
}
