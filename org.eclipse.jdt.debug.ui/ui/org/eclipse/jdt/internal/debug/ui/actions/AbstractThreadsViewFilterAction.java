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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;

/**
 * An action delegate that toggles the state of its viewer to show/hide certain java threads.
 */
abstract class AbstractThreadsViewFilterAction extends ViewFilterAction implements IDebugEventSetListener {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!getPreferenceValue()) {

			IJavaThread thread = getJavaThread(element);

			if (thread != null) {
				try {
					return selectThread(thread);
				}
				catch (DebugException e) {
				}
			}
		}
		return true;
	}

	private void refresh(Object source) {
		final IJavaThread thread = getJavaThread(source);
		if (thread != null) {
			try {
				if (isCandidateThread(thread)) {
					Runnable r = new Runnable() {
						@Override
						public void run() {
							StructuredViewer viewer = getStructuredViewer();
							if (viewer != null) {
								viewer.refresh();
							}
						}
					};
					JDIDebugUIPlugin.getStandardDisplay().asyncExec(r);
					return;
				}
			}
			catch (DebugException e) {
			}
		}
	}

	protected abstract boolean isCandidateThread(final IJavaThread thread) throws DebugException;

	protected abstract boolean selectThread(IJavaThread thread) throws DebugException;

	private IJavaThread getJavaThread(Object element) {
		IJavaThread thread = null;

		if (element instanceof IJavaThread) {
			thread = (IJavaThread) element;
		} else if (element instanceof IAdaptable) {
			thread = ((IAdaptable) element).getAdapter(IJavaThread.class);
		}

		return thread;
	}

	@Override
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	@Override
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		super.dispose();
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		if (getValue()) {
			// if showing all threads, no need to worry about displaying/hiding
			return;
		}
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			switch (event.getKind()) {
				case DebugEvent.RESUME:
					if (event.getDetail() == DebugEvent.CLIENT_REQUEST) {
						// when a thread resumes we need to refresh the viewer to re-filter it
						refresh(event.getSource());
					}
					break;
			}
		}
	}
}
