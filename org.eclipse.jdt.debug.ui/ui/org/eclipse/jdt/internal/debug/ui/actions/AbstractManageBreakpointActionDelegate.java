package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;


public abstract class AbstractManageBreakpointActionDelegate extends ManageBreakpointActionDelegate implements IObjectActionDelegate {

	private IMember fMember;
	private IJavaBreakpoint fBreakpoint;
	private IWorkbenchPart fTargetPart;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		setTargetPart(targetPart);
	}

	protected IMember getMember() {
		return fMember;
	}

	protected void setMember(IMember element) {
		fMember = element;
	}
	
	protected abstract IMember getMember(ISelection s);
	
	protected void updateForRun() {
		IMember member= null;
		IWorkbenchPage page= getPage();
		if (page != null) {
			ISelection selection= page.getSelection();
			if (selection instanceof ITextSelection) {
				member= getMember();
				if (member != null) {
					try {
						ISourceRange sourceRange= member.getSourceRange();
						ITextSelection textSelection= (ITextSelection)selection;
						if (textSelection.getOffset() >= sourceRange.getOffset() && textSelection.getOffset() <= (sourceRange.getOffset() + sourceRange.getLength() - 1)) {
							update();
							return;
						}
					} catch(JavaModelException e) {
						JDIDebugUIPlugin.log(e);
					}	
				}
				member= ActionDelegateHelper.getDefault().getCurrentMember(selection);
				
			} else {
				member= getMember(selection);
				try {
					IJavaProject project= member.getJavaProject();
					if (member != null && (!member.exists() || (project == null || !project.isOnClasspath(member)))) {
						member= null;
					}
				} catch (JavaModelException e) {
					JDIDebugUIPlugin.log(e);
					member= null;
				}
			}
		}
		
		setMember(member);
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
	
	protected void update() {
		if (enableForMember(getMember())) {
			setBreakpoint(getBreakpoint(getMember()));
		} else {
			setBreakpoint(null);
		}
	}
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	
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