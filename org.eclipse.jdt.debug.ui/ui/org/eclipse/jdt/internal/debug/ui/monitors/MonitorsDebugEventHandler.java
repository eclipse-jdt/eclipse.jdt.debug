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
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * Listen to certain events in the debug view
 */
public class MonitorsDebugEventHandler extends AbstractDebugEventHandler {

	public MonitorsDebugEventHandler(MonitorsView view) {
		super(view);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents(DebugEvent[] events, Object data) {
		DebugEvent event;
		Object source;
		boolean monitorInformationAvailable= true;
		boolean updateNeeded= false;
		final IJavaDebugTarget[] targets = new IJavaDebugTarget[1];
		for (int i = 0; i < events.length; i++) {
			event= events[i];
			source= event.getSource();
			
			//if a thread is suspended in the debug view
			if(event.getKind() == DebugEvent.SUSPEND) {
				if (source instanceof IJavaDebugTarget) {
					IJavaDebugTarget target= (IJavaDebugTarget)source;
					monitorInformationAvailable= target.supportsMonitorInformation();
					if (monitorInformationAvailable) {
						targets[0] = target;
						updateNeeded= true;
					} 
				} else if (source instanceof IJavaThread) {
					IJavaDebugTarget target= (IJavaDebugTarget)((IJavaThread)source).getDebugTarget();
					monitorInformationAvailable= target.supportsMonitorInformation();
					if (monitorInformationAvailable) {
						targets[0] = target;
						updateNeeded= true;
					}
					
				}
			} else if(event.getKind() == DebugEvent.RESUME) { 			
				if (source instanceof IJavaDebugTarget) {
					IJavaDebugTarget target= (IJavaDebugTarget)source;
					monitorInformationAvailable= target.supportsMonitorInformation();
					if (monitorInformationAvailable) {
						targets[0] = target;
						updateNeeded= true;
					}
				} else if (source instanceof IJavaThread) {
					IJavaDebugTarget target= (IJavaDebugTarget)((IJavaThread)source).getDebugTarget();
					monitorInformationAvailable= target.supportsMonitorInformation();
					if (monitorInformationAvailable) {
						targets[0] = target;
						updateNeeded= true;
					}
				}
			} else if(event.getKind() == DebugEvent.TERMINATE && source instanceof IJavaDebugTarget) {
				MonitorManager.getDefault().removeMonitorInformation((IJavaDebugTarget)source);
				((MonitorsView)getView()).refreshCurrentViewer(monitorInformationAvailable, false);
			}
		}
		if (updateNeeded) {
			Job job = new Job(MonitorMessages.getString("MonitorsView.4")) { //$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor) {
					MonitorManager.getDefault().updatePartial(targets[0]);
					Runnable r = new Runnable() {
						public void run() {
							((MonitorsView)getView()).refreshCurrentViewer(true, false);
						}
					};
					getView().asyncExec(r);
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getView().getAdapter(IWorkbenchSiteProgressService.class);
			if (service == null) {
				job.schedule();
			} else {
				service.schedule(job);
			}
		}
	}
	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#refresh()
	 */
	public void refresh() {
		((MonitorsView)getView()).selectionChanged(null, getView().getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW));
	}

}
