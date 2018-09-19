/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;

/**
 * An action delegate that toggles the state of its viewer to
 * show/hide System Threads.
 */
public class ShowSystemThreadsAction extends AbstractThreadsViewFilterAction {

	@Override
	protected String getPreferenceKey() {
		return IJavaDebugUIConstants.PREF_SHOW_SYSTEM_THREADS;
	}

	@Override
	protected boolean selectThread(IJavaThread thread) throws DebugException {
		// Show only non-system threads and suspended threads.
		return !thread.isSystemThread() || thread.isSuspended();
	}

	@Override
	protected boolean isCandidateThread(IJavaThread thread) throws DebugException {
		return thread.isSystemThread();
	}
}
