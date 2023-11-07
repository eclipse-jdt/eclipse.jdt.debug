/*******************************************************************************
 * Copyright (c) 20227 IBM Corporation and others.
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
 * Global retargettable toggle Line Entry action.
 *
 * @since 3.12
 */
public class RetargetToggleLambdaEntryBreakpointAction extends RetargetToggleBreakpointAction {

	@Override
	protected void performAction(Object target, ISelection selection, IWorkbenchPart part) throws CoreException {
		BreakpointToggleUtils.setUnsetLambdaEntryBreakpoint(true);
		super.performAction(target, selection, part);
	}

	@Override
	protected boolean canPerformAction(Object target, ISelection selection, IWorkbenchPart part) {
		return super.canPerformAction(target, selection, part);
	}

	@Override
	protected String getOperationUnavailableMessage() {
		return ActionMessages.LambdaEntryBreakpointToggleAction_Unavailable;
	}
}