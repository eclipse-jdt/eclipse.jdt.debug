/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jface.viewers.Viewer;

/**
 * An action delegate that toggles the state of its viewer to
 * show/hide System Threads.
 */
public class ShowSystemThreadsAction extends ViewFilterAction {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ThreadFilterAction#getPreferenceKey()
	 */
	protected String getPreferenceKey() {
		return IJDIPreferencesConstants.PREF_SHOW_SYSTEM_THREADS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!getValue() && (element instanceof IJavaThread)) {
			try {
				IJavaThread thread = (IJavaThread) element;
				// Show only non-system threads and suspended threads.
				return !thread.isSystemThread() || thread.isSuspended();
			} catch (DebugException e) {
			}
		}
		return true;
	}
}
