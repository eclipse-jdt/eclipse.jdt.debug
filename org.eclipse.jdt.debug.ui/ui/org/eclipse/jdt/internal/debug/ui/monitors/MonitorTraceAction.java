package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jface.action.IAction;

/**
 * Suspend all non-system threads and updates the data in MonitorManager
 */
public class MonitorTraceAction extends MonitorAction {
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
			
		IJavaDebugTarget target= getDebugTarget();
		if (target == null) {
			return;
		}
		MonitorManager.getDefault().update(target);
		IDebugView debugView= (IDebugView)fView.getAdapter(IDebugView.class);
		if (debugView != null) {
			debugView.getViewer().refresh();
			((MonitorsView)debugView).getDeadLocksViewer().refresh();
			((MonitorsView)debugView).getMonitorsViewer().refresh();
		}
		
	}

}
