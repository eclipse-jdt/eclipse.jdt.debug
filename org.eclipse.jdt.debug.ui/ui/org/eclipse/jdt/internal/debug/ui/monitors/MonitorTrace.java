package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Suspend all the threads and updates the data in MonitorManager
 */
public class MonitorTrace implements IViewActionDelegate {
	
	protected IViewPart fView;
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
			
		IJavaDebugTarget target= getDebugTarget();
		if (target == null) {
			return;
		}
		JDIDebugModel.getMonitorManager().update(target);
		IDebugView debugView= (IDebugView)fView.getAdapter(IDebugView.class);
		if (debugView != null) {
			debugView.getViewer().refresh();
			((MonitorsView)debugView).getDeadLocksViewer().refresh();
			((MonitorsView)debugView).getMonitorsViewer().refresh();
		}
		
	}

	/**
	 * Returns the current selection in the debug view or <code>null</code>
	 * if there is no selection.
	 * 
	 * @return IStructuredSelection
	 */
	private IStructuredSelection getDebugViewSelection() {
		if (fView != null) {
			ISelection s =fView.getViewSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
			
			if (s instanceof IStructuredSelection) {
				return (IStructuredSelection)s;
			}
		}
		return null;
	}
	
	protected IJavaDebugTarget getDebugTarget() {
		IStructuredSelection ss= getDebugViewSelection();
		if (ss.isEmpty() || ss.size() > 1) {
			return null;
		}
		Object element= ss.getFirstElement();
		if (element instanceof IDebugElement) {
			return (IJavaDebugTarget)((IDebugElement)element).getDebugTarget();
		}
		
		return null;
		
	}

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		fView= view;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
