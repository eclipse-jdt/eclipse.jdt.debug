/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.util.OpenTypeHierarchyUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;

public class OpenVariableConcreteTypeHierarchyAction extends OpenVariableConcreteTypeAction {
	
	private IWorkbenchPart fTargetPart;
	
	protected void openInEditor(Object sourceElement) {
		if (sourceElement instanceof IJavaElement) {
			OpenTypeHierarchyUtil.open((IJavaElement)sourceElement, fTargetPart.getSite().getWorkbenchWindow());
		} else {
			typeHierarchyError();
		}
	}	
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		super.setActivePart(action, targetPart);
		fTargetPart= targetPart;
	}
}
