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


import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;


public abstract class AbstractManageBreakpointActionDelegate extends ManageBreakpointActionDelegate implements IObjectActionDelegate {

	private IMember[] fMembers;
	private IJavaBreakpoint fBreakpoint;
	private IWorkbenchPart fTargetPart;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		setTargetPart(targetPart);
	}

	protected IMember[] getMembers() {
		return fMembers;
	}

	protected void setMembers(IMember[] elements) {
		fMembers = elements;
	}
	
	protected abstract IMember[] getMembers(ISelection s);
	
	protected void updateForRun() {
		IMember[] members;
		IWorkbenchPage page= getPage();
		if (page != null) {
			ISelection selection= page.getSelection();
			if (selection instanceof ITextSelection) {
				members= new IMember[] {ActionDelegateHelper.getDefault().getCurrentMember(selection)};
			} else {
				members= getMembers(selection);
			}
		} else {
			members= new IMember[0];
		}
		
		setMembers(members);
		update();
	}
	
	protected IWorkbenchPage getPage() {
		if (getWorkbenchWindow() != null) {
			return getWorkbenchWindow().getActivePage();
		} else if (getTargetPart() != null) {
			return getTargetPart().getSite().getPage();
		}
		return null;
	}

	protected abstract IJavaBreakpoint getBreakpoint(IMember element);
	
	protected abstract boolean enableForMember(IMember member);
	
	protected IWorkbenchPart getTargetPart() {
		return fTargetPart;
	}

	protected void setTargetPart(IWorkbenchPart targetPart) {
		fTargetPart = targetPart;
		setEnabledState(null);
	}
	
	protected void update(ISelection selection) {
		setEnabledState(null);
	}
}
