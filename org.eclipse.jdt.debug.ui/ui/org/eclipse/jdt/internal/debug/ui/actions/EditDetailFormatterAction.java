/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.DetailFormatter;
import org.eclipse.jdt.internal.debug.ui.DetailFormatterDialog;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDetailFormattersManager;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;

public class EditDetailFormatterAction extends ObjectActionDelegate {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection = getCurrentSelection();
		if(selection != null && selection.size() == 1) {
			Object element = selection.getFirstElement();
			IJavaType type;
			try {
				IJavaValue value;
				if (element instanceof IJavaVariable) {
					value = ((IJavaValue)((IJavaVariable) element).getValue());
				} else if (element instanceof JavaInspectExpression) {
					value = ((IJavaValue)((JavaInspectExpression) element).getValue());
				} else {
					return;
				}
				type= value.getJavaType();
			} catch (DebugException e) {
				return;
			}
			JavaDetailFormattersManager fm = JavaDetailFormattersManager.getDefault();
			DetailFormatter formatter = fm.getAssociatedDetailFormatter(type);
			if(formatter == null & type instanceof IJavaClassType) {
				formatter = fm.getDetailFormatterFromInterface((IJavaClassType) type);
				if(formatter == null) {
					formatter = fm.getDetailFormatterFromSuperclass((IJavaClassType) type);
				}
			}
			if (new DetailFormatterDialog(JDIDebugUIPlugin.getActivePage().getWorkbenchWindow().getShell(), formatter, null, false, true).open() == Window.OK) {
				fm.setAssociatedDetailFormatter(formatter);
			}
		}
	}

}
