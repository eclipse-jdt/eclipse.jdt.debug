package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class DropToFrameAction extends Action implements IViewActionDelegate {

	protected IStructuredSelection fCurrentSelection;

	public DropToFrameAction() {
		setEnabled(false);
	}

	public boolean isEnabledFor(Object element) {
		IJavaStackFrame frame= getJavaStackFrame(element);
		return frame != null && !frame.isSuspended() && frame.supportsDropToFrame();
	}

	protected IJavaStackFrame getJavaStackFrame(Object object) {
		if (object instanceof IAdaptable) {
			return (IJavaStackFrame) ((IAdaptable)object).getAdapter(IJavaStackFrame.class);
		}
		return null;
	}

	/**
	 * Does the specific action of this action to the process.
	 */
	protected void doAction(Object element) throws DebugException {
		getJavaStackFrame(element).dropToFrame();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Iterator enum= getStructuredSelection().iterator();
		// selectionChanged has already checked for correct selection

		while (enum.hasNext()) {
			Object element= enum.next();
			try {
				doAction(element);
			} catch (DebugException de) {
				String title= ActionMessages.getString("DropToFrameAction.Drop_to_Frame_1"); //$NON-NLS-1$
				String message= ActionMessages.getString("DropToFrameAction.Exceptions_occurred_attempting_to_drop_to_frame._2"); //$NON-NLS-1$
				ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell(), title, message, de.getStatus());
			}
		}
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		run(null);
	}

	protected IStructuredSelection getStructuredSelection() {
		return fCurrentSelection;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fCurrentSelection= (IStructuredSelection)sel;
			Object[] elements= fCurrentSelection.toArray();
			action.setEnabled(elements.length == 1 && isEnabledFor(elements[0]));
		}
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
	}
}
