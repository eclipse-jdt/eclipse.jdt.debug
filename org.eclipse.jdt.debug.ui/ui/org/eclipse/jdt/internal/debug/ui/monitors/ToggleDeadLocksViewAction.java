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


/**
 * Action to switch to the Deadlock centric view for the Monitors view
 */
public class ToggleDeadLocksViewAction extends ToggleViewAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.monitors.ToggleViewAction#getViewId()
	 */
	protected int getViewId() {
		return MonitorsView.VIEW_ID_DEADLOCK;
	}

}
