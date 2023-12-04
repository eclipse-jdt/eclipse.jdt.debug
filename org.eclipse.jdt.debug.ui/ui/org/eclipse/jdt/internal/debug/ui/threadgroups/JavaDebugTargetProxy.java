/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy;
import org.eclipse.debug.internal.ui.viewers.update.StackFrameEventHandler;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaElementContentProvider;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 */
public class JavaDebugTargetProxy extends DebugTargetProxy {

	private JavaThreadEventHandler fThreadEventHandler;

	/**
	 * Whether this proxy is for a scrapbook.
	 */
	private boolean fIsScrapbook = false;

	private IDebugTarget fDebugTarget = null;

	/**
	 * @param target the backing target
	 */
	public JavaDebugTargetProxy(IDebugTarget target) {
		super(target);
		fDebugTarget = target;
		ILaunch launch = target.getLaunch();
		if (launch != null) {
			fIsScrapbook = launch.getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) != null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy#createEventHandlers()
	 */
	@Override
	protected DebugEventHandler[] createEventHandlers() {
		fThreadEventHandler = new JavaThreadEventHandler(this);
		return new DebugEventHandler[] { new DebugTargetEventHandler(this), fThreadEventHandler,
				new StackFrameEventHandler(this, fThreadEventHandler)};
	}

	@Override
	public void installed(Viewer viewer) {
		if (fIsScrapbook) {
			// don't auto expand scrap books
			return;
		}
		final Viewer finalViewer = viewer;
		// Delay the auto-select-expand job to allow for transient suspend states to resolve.
		// See bug 225377
		Job job = new Job("Initialize Java Debug Session") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (!isDisposed()) {
					doInstalled(finalViewer);
				}
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return JavaDebugTargetProxy.this == family;
			}
		};
		job.setSystem(true);
		job.schedule(500);
		fThreadEventHandler.init(viewer);
	}

	/**
	 * @param viewer the viewer
	 */
	private void doInstalled(Viewer viewer) {
        // select any thread that is already suspended after installation
        IDebugTarget target = fDebugTarget;

        if (target != null) {
            ModelDelta delta = getNextSuspendedThreadDelta(null, false);
            if (delta == null) {
                try {
                    ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
                    ILaunch launch = target.getLaunch();
                    int launchIndex = indexOf(manager.getLaunches(), target.getLaunch());
                    int targetIndex = indexOf(target.getLaunch().getChildren(), target);
                    delta = new ModelDelta(manager, IModelDelta.NO_CHANGE);
                    ModelDelta node = delta.addNode(launch, launchIndex, IModelDelta.NO_CHANGE, target.getLaunch().getChildren().length);
                    node = node.addNode(target, targetIndex, IModelDelta.EXPAND | IModelDelta.SELECT, getTargetChildCount(target));
                } catch (DebugException e) {
                    // In case of exception do not fire delta
                    return;
                }
            }
			// Bug 559579: ensure the JavaThreadEventHandler has all suspended threads in case the Debug view is opened post launch
			addSuspendedThreadsToThreadHandler(delta);
            // expand the target if no suspended thread
            fireModelChanged(delta);
        }
	}

	private int getTargetChildCount(IDebugTarget target) throws DebugException{
	    if (target instanceof IJavaDebugTarget) {
	        IJavaDebugTarget javaTarget = (IJavaDebugTarget)target;

            if (JavaElementContentProvider.isDisplayThreadGroups()) {
                if (javaTarget.isDisconnected() || javaTarget.isTerminated()) {
                    return 0;
                }
                return javaTarget.getRootThreadGroups().length;
            }
            return javaTarget.getThreads().length;
	    }
	    return 0;
	}

	@Override
	protected int getStackFrameIndex(IStackFrame stackFrame) {
		int stackFrameIndex = 0;
		if (((IJavaDebugTarget) fDebugTarget).supportsMonitorInformation()) {
			IThread thread = stackFrame.getThread();
			IDebugElement[] ownedMonitors = JavaDebugUtils.getOwnedMonitors(thread);
			stackFrameIndex += ownedMonitors.length;
			IDebugElement contendedMonitor = JavaDebugUtils.getContendedMonitor(thread);
			if (contendedMonitor != null) {
				stackFrameIndex++;
			}
		}
		return stackFrameIndex;
	}

	private void addSuspendedThreadsToThreadHandler(ModelDelta delta) {
		delta.accept(new IModelDeltaVisitor() {
			@Override
			public boolean visit(IModelDelta delta, int depth) {
				Object element = delta.getElement();
				if (element instanceof IJavaThread) {
					IJavaThread thread = (IJavaThread) element;
					boolean suspended = thread.isSuspended();
					if (suspended) {
						fThreadEventHandler.addSuspendedThread(thread);
					}
				}
				return true;
			}
		});
	}
}
