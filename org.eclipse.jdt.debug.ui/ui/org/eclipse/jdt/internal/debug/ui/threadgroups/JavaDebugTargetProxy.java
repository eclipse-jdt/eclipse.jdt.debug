/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy;
import org.eclipse.debug.internal.ui.viewers.update.StackFrameEventHandler;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 *
 */
public class JavaDebugTargetProxy extends DebugTargetProxy {

	private JavaThreadEventHandler fThreadEventHandler;
	
	/**
	 * Whether this proxy is for a scrapbook.
	 */
	private boolean fIsScrapbook = false;
	
	/**
	 * @param target
	 */
	public JavaDebugTargetProxy(IDebugTarget target) {
		super(target);
		ILaunch launch = target.getLaunch();
		if (launch != null) {
			fIsScrapbook = launch.getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) != null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy#createEventHandlers()
	 */
	protected DebugEventHandler[] createEventHandlers() {
		fThreadEventHandler = new JavaThreadEventHandler(this);
		return new DebugEventHandler[] { new DebugTargetEventHandler(this), fThreadEventHandler,
				new StackFrameEventHandler(this, fThreadEventHandler)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {
		if (fIsScrapbook) {
			// don't auto expand scrap books
			return;
		}
		final Viewer finalViewer = viewer;
		// Delay the auto-select-expand job to allow for transient suspend states to resolve. 
		// See bug 225377
		Job job = new Job("Initialize Java Debug Session") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (!isDisposed()) {
					JavaDebugTargetProxy.super.installed(finalViewer);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(500);
		fThreadEventHandler.init(viewer);
	}

}
