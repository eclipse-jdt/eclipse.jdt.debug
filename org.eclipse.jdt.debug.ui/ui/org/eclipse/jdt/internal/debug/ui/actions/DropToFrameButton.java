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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Local toolbar drop to frame action
 */
public class DropToFrameButton implements IViewActionDelegate, IActionDelegate2 {
	
	private IJavaStackFrame fFrame = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
        Job job = new Job("Drop To Frame") { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                try {
                    fFrame.dropToFrame();
                } catch (DebugException e) {
                    String title= ActionMessages.getString("DropToFrameAction.Drop_to_Frame_1"); //$NON-NLS-1$
                    String message= ActionMessages.getString("DropToFrameAction.Exceptions_occurred_attempting_to_drop_to_frame._2"); //$NON-NLS-1$
                    ExceptionHandler.handle(e, title, message);
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action, ISelection selection) {
		fFrame = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.size() == 1) {
				final Object object = ss.getFirstElement();
				if (object instanceof IAdaptable) {
                    Job stateUpdateJob = new Job("Update Enablement") { //$NON-NLS-1$
                        protected IStatus run(IProgressMonitor monitor) {
                            if (monitor.isCanceled()) {
                                return Status.CANCEL_STATUS;
                            }
                            IJavaStackFrame frame = (IJavaStackFrame) ((IAdaptable)object).getAdapter(IJavaStackFrame.class);
                            if (frame != null && frame.supportsDropToFrame()) {
                                action.setEnabled(true);
                                fFrame = frame;
                                return Status.OK_STATUS;
                            }
                            
                            action.setEnabled(false);
                            return Status.OK_STATUS;                           
                        }
                    };
                    stateUpdateJob.setSystem(true);
                    stateUpdateJob.schedule();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		fFrame = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

}
