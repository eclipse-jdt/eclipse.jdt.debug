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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
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
	private IJavaBreakpoint fBreakpoint;
	
	private String fAddText, fAddDescription, fAddToolTip;
	private String fRemoveText, fRemoveDescription, fRemoveToolTip;
	
	
	public ManageMethodEntryBreakpointAction() {
		super();
		
		fAddText= ActionMessages.getString("ManageMethodEntryBreakpointAction.&Add_Entry_Breakpoint_1"); //$NON-NLS-1$
		fAddDescription= ActionMessages.getString("ManageMethodEntryBreakpointAction.Add_a_method_entry_breakpoint_2"); //$NON-NLS-1$
		fAddToolTip= ActionMessages.getString("ManageMethodEntryBreakpointAction.Add_Entry_Breakpoint_3"); //$NON-NLS-1$
		
		fRemoveText= ActionMessages.getString("ManageMethodEntryBreakpointAction.Remove_&Entry_Breakpoint_4"); //$NON-NLS-1$
		fRemoveDescription= ActionMessages.getString("ManageMethodEntryBreakpointAction.Remove_a_method_entry_breakpoint_5"); //$NON-NLS-1$
		fRemoveToolTip= ActionMessages.getString("ManageMethodEntryBreakpointAction.Remove_Entry_Breakpoint_6"); //$NON-NLS-1$
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
				IMethod method = getMethod();
				int start = -1;
				int end = -1;
				ISourceRange range = method.getNameRange();
				if (range != null) {
					start = range.getOffset();
					end = start + range.getLength();
				}
				Map attributes = new HashMap(10);
				BreakpointUtils.addJavaBreakpointAttributes(attributes, method);
				String methodName = method.getElementName();
				if (method.isConstructor()) {
					methodName = "<init>";
				}
				setBreakpoint(JDIDebugModel.createMethodBreakpoint(BreakpointUtils.getBreakpointResource(method),method.getDeclaringType().getFullyQualifiedName(), methodName, getMethod().getSignature(), true, false, false, -1, start, end, 0, true, attributes));
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageMethodEntryBreakpointAction.Problems_creating_breakpoint_7"), x.getMessage()); //$NON-NLS-1$
			}
		} else {
			// remove breakpoint
			try {
				IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
				breakpointManager.removeBreakpoint(getBreakpoint(), true);
			} catch (CoreException x) {
				MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ActionMessages.getString("ManageMethodEntryBreakpointAction.Problems_removing_breakpoint_8"), x.getMessage()); //$NON-NLS-1$
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
	
	private IJavaBreakpoint getBreakpoint(IMethod method) {
		IBreakpointManager breakpointManager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i= 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IMember container = null;
				try {
					container= BreakpointUtils.getMember((IJavaMethodBreakpoint) breakpoint);
				} catch (CoreException e) {
					return null;
				}
				if (method.equals(container))
					return (IJavaBreakpoint)breakpoint;
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
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
}


