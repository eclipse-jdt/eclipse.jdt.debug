/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
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
	@Override
	protected String getPreferenceKey() {
		return IJavaDebugUIConstants.PREF_SHOW_SYSTEM_THREADS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
	    if (!getValue()) {
            
            IJavaThread thread = getJavaThread(element);

	        if (thread != null) {
	            try {
	                // Show only non-system threads and suspended threads.
	                return !thread.isSystemThread() || thread.isSuspended();
	            } catch (DebugException e) {
	            }
	        }
	    }
	    return true;
	}
    
	private IJavaThread getJavaThread(Object element) {
	    IJavaThread thread = null;

	    if (element instanceof IJavaThread)
	        thread = (IJavaThread) element;
	    else if (element instanceof IAdaptable) 
	        thread = (IJavaThread) ((IAdaptable)element).getAdapter(IJavaThread.class);

	    return thread;
	}

    /* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		if (getValue()) {
			// if showing system threads, no need to worry about displaying/hiding
			return;
		}
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			switch (event.getKind()) {
				case DebugEvent.RESUME:
					if (event.getDetail() == DebugEvent.CLIENT_REQUEST) {
						// when a system thread resumes we need to refresh the viewer to re-filter it
						refresh(event.getSource());
					}
					break;
			}
		}
	}
	
	private void refresh(Object source) {
        final IJavaThread thread = getJavaThread(source);
        if (thread != null) {
			try {
				if (thread.isSystemThread()) {
					Runnable r = new Runnable() {
						public void run() {
							getStructuredViewer().refresh();
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
