/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Create a watch item from a selected variable
 */
public class WatchAction extends InspectAction {
		
	@Override
	public void run() {
		Object selectedObject= getSelectedObject();
		if (selectedObject instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection)selectedObject;
			Iterator<IJavaVariable> elements = selection.iterator();
			while (elements.hasNext()) {
				try {
					createWatchExpression(elements.next().getName());
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
					return;
				}
			}
			showExpressionView();
		} else if (selectedObject instanceof String) {
			createWatchExpression((String) selectedObject);
			showExpressionView();
		}
	}

	private void createWatchExpression(String snippet) {
		IWatchExpression expression= DebugPlugin.getDefault().getExpressionManager().newWatchExpression(snippet);
		DebugPlugin.getDefault().getExpressionManager().addExpression(expression);
		IAdaptable object = DebugUITools.getDebugContext();
		IDebugElement context= null;
		if (object instanceof IDebugElement) {
			context= (IDebugElement) object;
		} else if (object instanceof ILaunch) {
			context= ((ILaunch) object).getDebugTarget();
		}
		expression.setExpressionContext(context);
	}

}
