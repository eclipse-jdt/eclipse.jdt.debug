/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.util.OpenTypeHierarchyUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;

public class OpenDeclaringTypeHierarchyAction extends OpenDeclaringTypeAction {

	protected IWorkbenchPart fTargetPart;
	
	protected void doAction(Object e) throws DebugException {
		Object sourceElement= getSourceElement(e);
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
