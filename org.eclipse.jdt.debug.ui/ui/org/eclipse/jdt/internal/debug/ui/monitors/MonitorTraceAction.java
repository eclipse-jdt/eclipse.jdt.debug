/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * Suspend all non-system threads and updates the data in MonitorManager
 */
public class MonitorTraceAction extends MonitorAction {
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
			
		final IJavaDebugTarget target= getDebugTarget();
		if (target == null) {
			return;
		}
		Job job = new Job(MonitorMessages.getString("MonitorsView.4")) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				MonitorManager.getDefault().update(target);
				Runnable r = new Runnable() {
					public void run() {
						fView.refreshCurrentViewer(target.supportsMonitorInformation(), false);
					}
				};
				fView.asyncExec(r);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) fView.getAdapter(IWorkbenchSiteProgressService.class);
		if (service == null) {
			job.schedule();
		} else {
			service.schedule(job);
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		boolean enable= false;
		if (fAction != null) {
			IJavaDebugTarget target= getDebugTarget();
			if (target != null) {
				enable= target.supportsMonitorInformation();
			}
			fAction.setEnabled(enable);
		}
	}
}
