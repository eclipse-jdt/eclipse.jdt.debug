/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewPart;

/**
 * An action delegate that toggles the state of its viewer to
 * show/hide System Threads.
 */
public class ShowSystemThreadsAction extends ViewFilterAction implements IDebugEventSetListener {

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
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		if (getValue()) {
			// if showing system threads, no need to worry about displaying/hinding
			return;
		}
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			switch (event.getKind()) {
				case DebugEvent.SUSPEND:
					if (event.getDetail() == DebugEvent.BREAKPOINT) {
						refresh(event.getSource(), true);
					}
					break;
				case DebugEvent.RESUME:
					if (event.getDetail() == DebugEvent.CLIENT_REQUEST) {
						refresh(event.getSource(), false);
					}
					break;
			}
		}
	}
	
	private void refresh(Object source, final boolean select) {
		if (source instanceof IJavaThread) {
			final IJavaThread thread = (IJavaThread)source;
			try {
				if (thread.isSystemThread()) {
					Runnable r = new Runnable() {
						public void run() {
							getStructuredViewer().refresh();
							if (select) {
								Object tos;
								try {
									tos = thread.getTopStackFrame();
									getStructuredViewer().setSelection(new StructuredSelection(tos));
								} catch (DebugException e) {
								}								
							}
						}
					};
					JDIDebugUIPlugin.getStandardDisplay().asyncExec(r);
					return;
				}
			} catch (DebugException e) {
			}
		}		
	}
}
