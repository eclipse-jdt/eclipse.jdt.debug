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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
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
					OpenTypeHierarchyUtil.open(t, fTargetPart.getSite().getWorkbenchWindow());
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
