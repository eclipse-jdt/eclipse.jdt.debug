/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;

/**
 * Asks the user to give a label to the variable, and store it in the {@link JDIDebugTarget}. If the user gives an empty string, this will remove the
 * label.
 */
public class SetObjectLabelAction extends ObjectActionDelegate {

	@Override
	public void run(IAction action) {
		IStructuredSelection selection = getCurrentSelection();
		if (selection == null || selection.size() != 1) {
			return;
		}
		Object element = selection.getFirstElement();
		try {
			String name;
			IValue value;
			if (element instanceof IJavaVariable variable) {
				name = variable.getName();
				value = variable.getValue();
			} else if (element instanceof JavaInspectExpression jie) {
				name = jie.getExpressionText();
				value = jie.getValue();
			} else {
				return;
			}
			if (value instanceof final IJavaObject javaValue) {
				if (!javaValue.isNull()) {
					askForLabel(javaValue, name);
				}
			}
		} catch (DebugException e) {
			return;
		}
	}

	private void askForLabel(final IJavaObject javaValue, String variableName) throws DebugException {
		final String currentLabel = javaValue.getLabel();

		InputDialog dialog = new InputDialog(JDIDebugUIPlugin.getShell(), ActionMessages.SetObjectLabel_title, ActionMessages.SetObjectLabel_message, currentLabel != null
				? currentLabel
				: variableName, null);
		if (dialog.open() != Window.OK) {
			return;
		}
		javaValue.setLabel(dialog.getValue());
		refresh();
	}

	protected StructuredViewer getStructuredViewer() {
		IDebugView view = getPart().getAdapter(IDebugView.class);
		if (view != null) {
			Viewer viewer = view.getViewer();
			if (viewer instanceof StructuredViewer) {
				return (StructuredViewer) viewer;
			}
		}
		return null;
	}

	private void refresh() {
		StructuredViewer viewer = getStructuredViewer();
		if (viewer != null) {
			viewer.refresh();
		}
	}

}
