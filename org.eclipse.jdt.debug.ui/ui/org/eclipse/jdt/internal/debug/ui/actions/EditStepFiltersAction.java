/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jdt.internal.debug.ui.JavaStepFilterPreferencePage;
import org.eclipse.jdt.internal.debug.ui.SWTUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Menu action to open the step filtering preferences page, to allow users to edit step filter
 * preferences
 */
public class EditStepFiltersAction extends ActionDelegate {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		SWTUtil.showPreferencePage(JavaStepFilterPreferencePage.PAGE_ID);
	}
	
}
