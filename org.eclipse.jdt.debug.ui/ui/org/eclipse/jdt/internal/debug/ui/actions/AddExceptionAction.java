
package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class AddExceptionAction implements IViewActionDelegate {

	public void run(IAction action) {		
		Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
		AddExceptionDialog dialog= new AddExceptionDialog(shell);
		if (dialog.open() == dialog.OK) {
			IType type= dialog.getType();
			boolean checked= dialog.getExceptionType() == AddExceptionDialog.CHECKED_EXCEPTION;
			boolean caught= dialog.isCaughtSelected();
			boolean uncaught= dialog.isUncaughtSelected();
			try {
				Map attributes = new HashMap(10);
				BreakpointUtils.addJavaBreakpointAttributes(attributes, type);
				JDIDebugModel.createExceptionBreakpoint(BreakpointUtils.getBreakpointResource(type), type.getFullyQualifiedName(), caught, uncaught, checked, true, attributes);
			} catch (CoreException exc) {
				ExceptionHandler.handle(exc, ActionMessages.getString("AddExceptionAction.error.title"), ActionMessages.getString("AddExceptionAction.error.message")); //$NON-NLS-2$ //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}