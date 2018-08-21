/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.actions.breakpoints.RetargetToggleBreakpointAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Global retargettable toggle tracepoint action.
 *
 * @since 3.12
 *
 */
public class RetargetToggleTracepointAction extends RetargetToggleBreakpointAction {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.actions.RetargetTracepointAction# performAction(java.lang.Object, org.eclipse.jface.viewers.ISelection,
	 * org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected void performAction(Object target, ISelection selection, IWorkbenchPart part) throws CoreException {
		BreakpointToggleUtils.setUnsetTracepoints(true);
		super.performAction(target, selection, part);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.actions.RetargetBreakpointAction#canPerformAction(java.lang.Object, org.eclipse.jface.viewers.ISelection,
	 * org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected boolean canPerformAction(Object target, ISelection selection, IWorkbenchPart part) {
		return super.canPerformAction(target, selection, part);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#getOperationUnavailableMessage()
	 */
	@Override
	protected String getOperationUnavailableMessage() {
		return ActionMessages.TracepointToggleAction_Unavailable;
	}
}