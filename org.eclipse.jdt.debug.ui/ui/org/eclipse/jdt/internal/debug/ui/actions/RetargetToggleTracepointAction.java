/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.actions.breakpoints.RetargetToggleBreakpointAction;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
		try {
			ISelection sel = BreakpointToggleUtils.translateToMembers(part, selection);
			if (sel instanceof IStructuredSelection) {
				Object firstElement = ((IStructuredSelection) sel).getFirstElement();
				if (firstElement instanceof IMember) {
					IMember member = (IMember) firstElement;
					int mtype = member.getElementType();
					if (mtype == IJavaElement.FIELD || mtype == IJavaElement.METHOD || mtype == IJavaElement.INITIALIZER) {
						// remove line breakpoint if present first
						if (selection instanceof ITextSelection) {
							ITextSelection ts = (ITextSelection) selection;

							CompilationUnit unit = BreakpointToggleUtils.parseCompilationUnit(BreakpointToggleUtils.getTextEditor(part));
							ValidBreakpointLocationLocator loc = new ValidBreakpointLocationLocator(unit, ts.getStartLine() + 1, true, true);
							unit.accept(loc);
							if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_METHOD) {
								return true;
							} else if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_FIELD) {
								return false;
							} else if (loc.getLocationType() == ValidBreakpointLocationLocator.LOCATION_LINE) {
								return true;
							}
						}
					}

					if (member.getElementType() == IJavaElement.TYPE) {
						return false;
					}
				}
			}
			return super.canPerformAction(target, selection, part);
		}
		catch (CoreException e) {
			return false;
		}
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