/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;


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
		fView.refreshCurrentViewer(target.supportsMonitorInformation(), false);
	}
}
