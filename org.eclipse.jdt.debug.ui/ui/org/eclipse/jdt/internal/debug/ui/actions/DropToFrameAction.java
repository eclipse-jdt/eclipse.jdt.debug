package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

public class DropToFrameAction extends ObjectActionDelegate {

	protected boolean isEnabledFor(Object element) {
		IJavaStackFrame frame= (IJavaStackFrame)element;
		return frame != null && frame.isSuspended() && frame.supportsDropToFrame();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		if (selection == null) {
			return;
		}
		Iterator enum= selection.iterator();
		
		while (enum.hasNext()) {
			IJavaStackFrame frame= (IJavaStackFrame)enum.next();
			try {
				frame.dropToFrame();
			} catch (DebugException de) {
				String title= ActionMessages.getString("DropToFrameAction.Drop_to_Frame_1"); //$NON-NLS-1$
				String message= ActionMessages.getString("DropToFrameAction.Exceptions_occurred_attempting_to_drop_to_frame._2"); //$NON-NLS-1$
				ExceptionHandler.handle(de, title, message);
			}
		}
	}
}
