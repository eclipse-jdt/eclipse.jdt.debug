/*******************************************************************************
 * Copyright (c) 2022, 2025 Zsombor Gegesy and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;

/**
 * Enable/disable collapsing of the non-relevant stack frames in debug views.
 *
 */
public class CollapseStackFramesAction extends ToggleBooleanPreferenceAction {

	@Override
	protected String getPreferenceKey() {
		return IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES;
	}

	@Override
	protected String getCompositeKey() {
		return getPreferenceKey();
	}

}
