/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jface.action.IAction;

public class DisableCondtionalBreakpoints extends AbstractDisableAllActionDelegate implements IBreakpointsListener {
	public DisableCondtionalBreakpoints() {
		super();
	}

	@Override
	protected boolean isEnabled() {
		for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
			if (breakpoint instanceof JavaLineBreakpoint javaBreakpoint) {
				try {
					if (javaBreakpoint.isConditionEnabled()) {
						return true;
					}
				} catch (CoreException e) {
					DebugUIPlugin.log(e);
				}
			}
		}
		return false;
	}

	@Override
	public void run(IAction action) {

		new Job(ActionMessages.DisableConditionalBreakpoints_1) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
						if (breakpoint instanceof JavaLineBreakpoint javaBp) {
							if (javaBp.isConditionEnabled()) {
								javaBp.setConditionEnabled(false);
							}
						}
					}
					refreshAllBreakpoints();
				} catch (Exception e) {
					DebugUIPlugin.log(e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		}.schedule();

	}

	private void refreshAllBreakpoints() {
		IWorkspaceRunnable runnable = monitor -> {
			for (IBreakpoint breakpoint : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
				try {
					if (breakpoint instanceof JavaLineBreakpoint javaLB) {
						javaLB.getMarker().setAttribute(IBreakpoint.ENABLED, breakpoint.isEnabled());
					}
				} catch (CoreException e) {
					DebugPlugin.log(e);
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			DebugPlugin.log(e);
		}
	}

	@Override
	protected void initialize() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}

	@Override
	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		update();
	}

	@Override
	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		if (getAction() != null) {
			update();
		}
	}

	@Override
	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		update();
	}

	@Override
	public void dispose() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		super.dispose();
	}

}
