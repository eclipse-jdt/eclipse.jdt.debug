package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Action to switch to the Deadlock centric view for the Monitors view
 */
public class ToggleDeadLocksViewAction extends ToggleViewAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.monitors.ToggleViewAction#getViewId()
	 */
	protected int getViewId() {
		return fMonitorsView.VIEW_ID_DEADLOCK;
	}

}
