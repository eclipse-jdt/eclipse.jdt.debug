package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

public class ManageWatchpointAction extends ManageBreakpointAction {
	
	public ManageWatchpointAction() {
		super();
		fAddText= ActionMessages.getString("ManagerWatchPointAction.Add_&Watchpoint_1"); //$NON-NLS-1$
		fAddDescription= ActionMessages.getString("ManagerWatchPointAction.Add_a_watchpoint_2"); //$NON-NLS-1$
		
		fRemoveText= ActionMessages.getString("ManagerWatchPointAction.Remove_&Watchpoint_4"); //$NON-NLS-1$
		fRemoveDescription= ActionMessages.getString("ManagerWatchPointAction.Remove_a_field_watchpoint_5"); //$NON-NLS-1$
	}
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart part) {
		setAction(action);
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (getBreakpoint() == null) {
			try {
				IType type = getElement().getDeclaringType();
				int start = -1;
				int end = -1;
				ISourceRange range = getElement().getNameRange();
				if (range != null) {
					start = range.getOffset();
					end = start + range.getLength();
				}
				Map attributes = new HashMap(10);
				BreakpointUtils.addJavaBreakpointAttributes(attributes, getElement());
				setBreakpoint(JDIDebugModel.createWatchpoint(BreakpointUtils.getBreakpointResource(type),type.getFullyQualifiedName(), getElement().getElementName(), -1, start, end, 0, true, attributes));
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManagerWatchPointAction.Problems_adding_watchpoint_7"), x.getMessage()); //$NON-NLS-1$
			}
		} else {
			// remove breakpoint
			try {
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				breakpointManager.removeBreakpoint(getBreakpoint(), true);
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManagerWatchPointAction.Problems_removing_watchpoint_8"), x.getMessage()); //$NON-NLS-1$
			}
		}
		update();
	}
	
	protected IJavaBreakpoint getBreakpoint(IMember selectedField) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaWatchpoint) {
				try {
					if (equalFields(selectedField, (IJavaWatchpoint)breakpoint))
						return (IJavaBreakpoint)breakpoint;
				} catch (CoreException e) {
				}
			}
		}
		return null;
	}

	/**
	 * Compare two fields. The default <code>equals()</code>
	 * method for <code>IField</code> doesn't give the comparison desired.
	 */
	private boolean equalFields(IMember breakpointField, IJavaWatchpoint watchpoint) throws CoreException {
		return (breakpointField.getElementName().equals(watchpoint.getFieldName()) &&
		breakpointField.getDeclaringType().getFullyQualifiedName().equals(watchpoint.getTypeName()));
	}
	
	protected IMember getElement(ISelection s) {
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) s;
			if (ss.size() == 1) {					
				Object o=  ss.getFirstElement();
				if (o instanceof IField)
					return (IField) o;
			}
		}
		return null;
	}
}

