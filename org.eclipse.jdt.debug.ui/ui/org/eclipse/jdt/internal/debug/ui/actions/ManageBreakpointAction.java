package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public abstract class ManageBreakpointAction implements IObjectActionDelegate {

	protected String fRemoveText, fRemoveDescription;
	protected String fAddText, fAddDescription;
	private IAction fAction;
	private IMember fElement;
	private IJavaBreakpoint fBreakpoint;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		if (selection == null) {
			return;
		}
		update(selection);
	}

	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IMember getElement() {
		return fElement;
	}

	protected void setElement(IMember element) {
		fElement = element;
	}
	
	protected abstract IMember getElement(ISelection s);
	
	protected void update(ISelection selection) {
		setElement(getElement(selection));
		update();
	}	
	
	protected abstract IJavaBreakpoint getBreakpoint(IMember element);
	
	protected void update() {
		if(getElement() == null) {
			setBreakpoint(null);
		} else {
			setBreakpoint(getBreakpoint(getElement()));
		}
		boolean doesNotExist= getBreakpoint() == null;
		getAction().setText(doesNotExist ? fAddText : fRemoveText);
		getAction().setDescription(doesNotExist ? fAddDescription : fRemoveDescription);
	}
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
}
