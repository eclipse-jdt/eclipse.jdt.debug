package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.IHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Adds a method entry breakpoint on a single selected element of type IMethod 
 */
public class ManageMethodEntryBreakpointAction extends Action implements IObjectActionDelegate {
	
	private IMethod fMethod;
	private IBreakpoint fBreakpoint;
	
	private String fAddText, fAddDescription, fAddToolTip;
	private String fRemoveText, fRemoveDescription, fRemoveToolTip;
	
	
	public ManageMethodEntryBreakpointAction() {
		super();
		
		fAddText= "&Add Entry Breakpoint";
		fAddDescription= "Add a method entry breakpoint";
		fAddToolTip= "Add Entry Breakpoint";
		
		fRemoveText= "Remove &Entry Breakpoint";
		fRemoveDescription= "Remove a method entry breakpoint";
		fRemoveToolTip= "Remove Entry Breakpoint";
		setText(fAddText);
		setDescription(fAddDescription);
		setToolTipText(fAddToolTip);
		WorkbenchHelp.setHelp(this, new Object[] { IHelpContextIds.MANAGE_METHODBREAKPOINT_ACTION });
	}
	
	/**
	 * Perform the action
	 */
	public void run() {
		if (getBreakpoint() == null) {
			// add breakpoint
			try {
				setBreakpoint(JDIDebugModel.createMethodEntryBreakpoint(getMethod(), 0));
			} catch (DebugException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), "Problems creating breakpoint", x.getMessage());
			}
		} else {
			// remove breakpoint
			try {
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				breakpointManager.removeBreakpoint(getBreakpoint(), true);
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), "Problems removing breakpoint", x.getMessage());
			}
		}
		update();
	}
	
	protected void update(ISelection selection) {
		setMethod(getMethod(selection));
		update();
	}
	
	protected void update() {
		IMethod method= getMethod();
		if (method != null && method.isBinary()) { // only add to class files
			setEnabled(true);
			setBreakpoint(getBreakpoint(method));
			boolean doesNotExist= getBreakpoint() == null;
			setText(doesNotExist ? fAddText : fRemoveText);
			setDescription(doesNotExist ? fAddDescription : fRemoveDescription);
			setToolTipText(doesNotExist ? fAddToolTip : fRemoveToolTip);
		} else {
			setEnabled(false);
		}
	}
	
	private IBreakpoint getBreakpoint(IMethod method) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaMethodEntryBreakpoint) {
				IMethod container = null;
				try {
					container= ((IJavaMethodEntryBreakpoint) breakpoint).getMethod();
				} catch (CoreException e) {
					return null;
				}
				if (method.equals(container))
					return breakpoint;
			}
		}
		return null;
	}
	
	private IMethod getMethod(ISelection s) {
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) s;
			if (ss.size() == 1) {					
				Object o=  ss.getFirstElement();
				if (o instanceof IMethod)
					return (IMethod) o;
			}
		}
		return null;
	}
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart part) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run();
		updateAction(action);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		update(selection);
		updateAction(action);
	}
	
	protected void updateAction(IAction action) {
		action.setEnabled(isEnabled());
		action.setToolTipText(getToolTipText());
		action.setText(getText());
		action.setDescription(getDescription());
	}
	protected IMethod getMethod() {
		return fMethod;
	}

	protected void setMethod(IMethod method) {
		fMethod = method;
	}
	
	protected IBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
}


