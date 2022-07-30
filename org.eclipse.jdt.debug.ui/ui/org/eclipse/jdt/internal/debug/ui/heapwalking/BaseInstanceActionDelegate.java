/*******************************************************************************
 * Copyright (c) 2022 Zsombor Gegesy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIFieldVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ObjectActionDelegate;
import org.eclipse.jdt.internal.debug.ui.actions.OpenVariableConcreteTypeAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

abstract class BaseInstanceActionDelegate extends ObjectActionDelegate implements IWorkbenchWindowActionDelegate {

	protected IWorkbenchWindow fWindow;

	@Override
	public void init(IWorkbenchWindow window) {
		this.fWindow = window;
	}

	protected void handleDoubleClick(IAction action, DoubleClickEvent event) {
		var selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
		openInEditor(action, selection);
	}

	private void openInEditor(IAction action, Object selection) {
		if (selection instanceof JDIFieldVariable) {
			var openAction = new OpenVariableConcreteTypeAction();
			openAction.setActivePart(action, getPart());
			try {
				openAction.openElement(action, selection);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		} else if (selection instanceof IJavaVariable) {
			try {
				var openAction = new OpenVariableConcreteTypeAction();
				openAction.setActivePart(action, getPart());
				openAction.openElement(action, selection);
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ObjectActionDelegate#getPart()
	 */
	@Override
	protected IWorkbenchPart getPart() {
		IWorkbenchPart part = super.getPart();
		if (part != null) {
			return part;
		} else if (fWindow != null) {
			return fWindow.getActivePage().getActivePart();
		}
		return null;
	}

}
