/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ObjectActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Action to browse all references to selected object.
 * 
 * @since 3.3
 */
public class AllReferencesActionDelegate extends ObjectActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection currentSelection = getCurrentSelection();
		IJavaVariable var = (IJavaVariable) currentSelection.getFirstElement();
		ReferencesPopupDialog popup;
		try {
			popup = new ReferencesPopupDialog(getWorkbenchWindow().getShell(), (IDebugView) getPart().getAdapter(IDebugView.class), (IJavaObject) var.getValue());
			popup.open();
		} catch (DebugException e) {
			JDIDebugUIPlugin.statusDialog(e.getStatus());
		}
	}
}
