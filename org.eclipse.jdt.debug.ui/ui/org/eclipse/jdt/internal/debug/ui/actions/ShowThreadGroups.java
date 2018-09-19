/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;

/**
 * Toggle to display the thread and monitor information in the debug view.
 */
public class ShowThreadGroups extends ToggleBooleanPreferenceAction {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ViewFilterAction#getPreferenceKey()
	 */
	@Override
	protected String getPreferenceKey() {
		return IJavaDebugUIConstants.PREF_SHOW_THREAD_GROUPS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ViewFilterAction#getCompositeKey()
	 */
	@Override
	protected String getCompositeKey() {
		return getPreferenceKey();
	}
}
