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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
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

public class ManageWatchpointAction extends Action implements IObjectActionDelegate {

	private IField fField;
	
	private IBreakpoint fBreakpoint;
	
	private String fAddText, fAddDescription, fAddToolTip;
	private String fRemoveText, fRemoveDescription, fRemoveToolTip;
	
	public ManageWatchpointAction() {
		super();
		fAddText= ActionMessages.getString("ManagerWatchPointAction.Add_&Watchpoint_1"); //$NON-NLS-1$
		fAddDescription= ActionMessages.getString("ManagerWatchPointAction.Add_a_watchpoint_2"); //$NON-NLS-1$
		fAddToolTip= ActionMessages.getString("ManagerWatchPointAction.Add_Watchpoint_3"); //$NON-NLS-1$
		
		fRemoveText= ActionMessages.getString("ManagerWatchPointAction.Remove_&Watchpoint_4"); //$NON-NLS-1$
		fRemoveDescription= ActionMessages.getString("ManagerWatchPointAction.Remove_a_field_watchpoint_5"); //$NON-NLS-1$
		fRemoveToolTip= ActionMessages.getString("ManagerWatchPointAction.Remove_Watchpoint_6"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.MANAGE_WATCHPOINT_ACTION });
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		if (getBreakpoint() == null) {
			try {
				setBreakpoint(JDIDebugModel.createWatchpoint(getField(), 0));
			} catch (DebugException x) {
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
	
	/**
	 * Returns whether this action can be added for the given selection
	 */
	public boolean canActionBeAdded(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IStructuredSelection structSel= (IStructuredSelection)selection;
			Object obj= structSel.getFirstElement();
			if (structSel.size() == 1 && obj instanceof IField) {
				fField = (IField) obj;
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				IBreakpoint[] breakpoints= breakpointManager.getBreakpoints();
				for (int i=0; i<breakpoints.length; i++) {
					IBreakpoint breakpoint= breakpoints[i];
					if (breakpoint instanceof IJavaWatchpoint) {
						IField breakpointField= null;
						try {
							breakpointField = ((IJavaWatchpoint) breakpoint).getField();
						} catch (CoreException e) {
							return false;
						}
						if (breakpointField != null && equalFields(breakpointField, fField)) {
							return false;
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Compare two fields for <code>canActionBeAdded()</code>. The default <code>equals()</code>
	 * method for <code>IField</code> doesn't give the comparison desired.
	 */
	private boolean equalFields(IField breakpointField, IField field) {
		return (breakpointField.getElementName().equals(field.getElementName()) &&
		breakpointField.getDeclaringType().getElementName().equals(field.getDeclaringType().getElementName()));
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
	
	private IBreakpoint getBreakpoint(IField selectedField) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaWatchpoint) {
				IField field = null;
				try {
					field= ((IJavaWatchpoint) breakpoint).getField();
				} catch (CoreException e) {
					return null;
				}
				if (equalFields(selectedField, field))
					return breakpoint;
			}
		}
		return null;
	}

	protected void update(ISelection selection) {
		setField(getField(selection));
		update();
	}	
	
	protected void update() {
		IField field= getField();
		if (field != null) {
			setEnabled(true);
			setBreakpoint(getBreakpoint(field));
			boolean doesNotExist= getBreakpoint() == null;
			setText(doesNotExist ? fAddText : fRemoveText);
			setDescription(doesNotExist ? fAddDescription : fRemoveDescription);
			setToolTipText(doesNotExist ? fAddToolTip : fRemoveToolTip);
		} else {
			setEnabled(false);
		}
	}
	
	protected IField getField() {
		return fField;
	}

	protected void setField(IField field) {
		fField = field;
	}
	
	protected void updateAction(IAction action) {
		action.setEnabled(isEnabled());
		action.setToolTipText(getToolTipText());
		action.setText(getText());
		action.setDescription(getDescription());
	}
	
	protected IBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	
	private IField getField(ISelection s) {
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

