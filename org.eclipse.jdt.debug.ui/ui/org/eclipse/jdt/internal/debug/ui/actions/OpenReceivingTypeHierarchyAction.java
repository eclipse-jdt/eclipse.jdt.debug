package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.util.OpenTypeHierarchyUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;


public class OpenReceivingTypeHierarchyAction extends OpenReceivingTypeAction {
	
	protected IWorkbenchPart fTargetPart;
	
	protected void doAction(Object e) throws DebugException {
		IAdaptable element= (IAdaptable) e;
		IDebugElement dbgElement= getDebugElement(element);
		if (dbgElement != null) {
			String typeName= getTypeNameToOpen(dbgElement);
			try {
				IType t= findTypeInWorkspace(typeName);
				if (t != null) {
					OpenTypeHierarchyUtil.open((IJavaElement)t, fTargetPart.getSite().getWorkbenchWindow());
				}
			} catch (CoreException x) {
				JDIDebugUIPlugin.log(x);
			}
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