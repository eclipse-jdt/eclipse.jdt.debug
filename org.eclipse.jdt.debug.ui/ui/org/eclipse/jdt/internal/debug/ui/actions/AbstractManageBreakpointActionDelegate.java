package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public abstract class AbstractManageBreakpointActionDelegate extends ManageBreakpointActionDelegate implements IObjectActionDelegate {

	protected String fRemoveText;
	protected String fRemoveDescription;
	protected String fAddText;
	protected String fAddDescription;
	private IMember fMember;
	private IJavaBreakpoint fBreakpoint;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	protected IMember getMember() {
		return fMember;
	}

	protected void setMember(IMember element) {
		fMember = element;
	}
	
	protected abstract IMember getMember(ISelection s);
	
	protected void update(ISelection selection) {
		IMember member= null;
		if (selection instanceof ITextSelection) {
			member= ActionDelegateHelper.getDefault().getCurrentMember(selection);
		} else {
			member= getMember(selection);
		}
		setMember(member);
		update();
	}	
	
	protected abstract IJavaBreakpoint getBreakpoint(IMember element);
	
	protected void update() {
		if (enableForMember(getMember())) {
			setBreakpoint(getBreakpoint(getMember()));
			boolean doesNotExist= getBreakpoint() == null;
			getAction().setText(doesNotExist ? fAddText : fRemoveText);
			getAction().setDescription(doesNotExist ? fAddDescription : fRemoveDescription);
			getAction().setEnabled(true);
		} else {
			setBreakpoint(null);
			getAction().setEnabled(false);
		}
	}
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	
	protected abstract boolean enableForMember(IMember member);
}
