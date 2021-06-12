/*******************************************************************************
 * Copyright (c) 2021 Zsombor Gegesy.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Helper class to manage converting between active/inactive Filter lists into preference settings.
 *
 */
public class FilterManager {

	final static FilterManager PLATFORM_STACK_FRAMES = new FilterManager(IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST, IJDIPreferencesConstants.PREF_INACTIVE_PLATFORM_FRAME_FILTER_LIST);
	final static FilterManager CUSTOM_STACK_FRAMES = new FilterManager(IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST, IJDIPreferencesConstants.PREF_INACTIVE_CUSTOM_FRAME_FILTER_LIST);
	final static FilterManager STEP_FILTERS = new FilterManager(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST);

	private final String activeListKey;
	private final String inactiveListKey;

	public FilterManager(String activeListKey, String inactiveListKey) {
		this.activeListKey = activeListKey;
		this.inactiveListKey = inactiveListKey;
	}

	public String[] getActiveList(IPreferenceStore store) {
		return JavaDebugOptionsManager.parseList(store.getString(activeListKey));
	}

	public String[] getInactiveList(IPreferenceStore store) {
		return JavaDebugOptionsManager.parseList(store.getString(inactiveListKey));
	}

	public Filter[] getAllStoredFilters(IPreferenceStore store, boolean defaults) {
		Filter[] filters = null;
		String[] activefilters, inactivefilters;
		if (defaults) {
			activefilters = JavaDebugOptionsManager.parseList(store.getDefaultString(activeListKey));
			inactivefilters = JavaDebugOptionsManager.parseList(store.getDefaultString(inactiveListKey));
		} else {
			activefilters = JavaDebugOptionsManager.parseList(store.getString(activeListKey));
			inactivefilters = JavaDebugOptionsManager.parseList(store.getString(inactiveListKey));
		}
		filters = new Filter[activefilters.length + inactivefilters.length];
		for (int i = 0; i < activefilters.length; i++) {
			filters[i] = new Filter(activefilters[i], true);
		}
		for (int i = 0; i < inactivefilters.length; i++) {
			filters[i + activefilters.length] = new Filter(inactivefilters[i], false);
		}
		return filters;
	}

	public void save(IPreferenceStore store, Filter[] filters) {
		ArrayList<String> active = new ArrayList<>();
		ArrayList<String> inactive = new ArrayList<>();
		String name = ""; //$NON-NLS-1$
		for (int i = 0; i < filters.length; i++) {
			name = filters[i].getName();
			if (filters[i].isChecked()) {
				active.add(name);
			} else {
				inactive.add(name);
			}
		}
		String pref = JavaDebugOptionsManager.serializeList(active.toArray(new String[active.size()]));
		store.setValue(activeListKey, pref);
		pref = JavaDebugOptionsManager.serializeList(inactive.toArray(new String[inactive.size()]));
		store.setValue(inactiveListKey, pref);
	}
}
