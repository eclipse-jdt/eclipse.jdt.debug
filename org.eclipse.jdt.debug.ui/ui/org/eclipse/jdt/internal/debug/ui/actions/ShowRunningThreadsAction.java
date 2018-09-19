/*******************************************************************************
 * Copyright (c) 2017 Igor Fedorenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IViewPart;

/**
 * An action delegate that toggles the state of its viewer to show/hide running threads.
 */
public class ShowRunningThreadsAction extends AbstractThreadsViewFilterAction {

	@Override
	public void init(IViewPart view) {
		// compensate for a bug in how ViewFilterAction stores compositeKey value
		// which does not allow default==true and explicit==false
		IPreferenceStore store = getPreferenceStore();
		String preferenceKey = getPreferenceKey();
		if (store.contains(preferenceKey)) {
			String compositeKey = view.getSite().getId() + "." + preferenceKey; //$NON-NLS-1$
			store.setDefault(compositeKey, store.getBoolean(preferenceKey));
		}

		super.init(view);
	}

	@Override
	protected String getPreferenceKey() {
		return IJavaDebugUIConstants.PREF_SHOW_RUNNING_THREADS;
	}

	@Override
	protected boolean selectThread(IJavaThread thread) throws DebugException {
		return thread.isSuspended();
	}

	@Override
	protected boolean isCandidateThread(IJavaThread thread) throws DebugException {
		return true;
	}
}
