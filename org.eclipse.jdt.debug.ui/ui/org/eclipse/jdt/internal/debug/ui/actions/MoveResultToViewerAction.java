/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;


/**
 * Moves a display or inspect result to associated view when closing 
 * a debug popup.
 *
 * @since 3.0
 */
public class MoveResultToViewerAction extends Action {

	private Runnable runnable;


	public MoveResultToViewerAction(Runnable runnable) {
		this.runnable = runnable;
	}
	
	
	public void run() {
		Display.getDefault().asyncExec(runnable);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#getActionDefinitionId()
	 */
	public String getActionDefinitionId() {
		return "org.eclipse.debug.ui.commands.defaultDebugPopupClose"; //$NON-NLS-1$
	}
}
