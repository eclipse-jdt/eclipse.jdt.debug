package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Presents a custom properties dialog to configure
 * the attibutes of a Java Breakpoint.
 */
public class JavaBreakpointPropertiesAction implements IObjectActionDelegate {
	
	private IWorkbenchPart fPart;
	private IJavaBreakpoint fBreakpoint;

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Dialog d= new JavaBreakpointPropertiesDialog(getActivePart().getSite().getShell(),getBreakpoint());
		d.open();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection)selection;
			if (ss.isEmpty() || ss.size() > 1) {
				return;
			}
			Object element= ss.getFirstElement();
			if (element instanceof IJavaBreakpoint) {
				setBreakpoint((IJavaBreakpoint)element);
			}
		}
	}
	
	protected IWorkbenchPart getActivePart() {
		return fPart;
	}

	protected void setActivePart(IWorkbenchPart part) {
		fPart = part;
	}
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		setActivePart(targetPart);
	}
}
