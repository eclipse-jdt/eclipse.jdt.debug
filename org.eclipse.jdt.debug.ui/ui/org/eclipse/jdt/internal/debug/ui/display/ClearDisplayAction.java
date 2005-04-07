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
package org.eclipse.jdt.internal.debug.ui.display;


import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * Clears the display.
 */
public class ClearDisplayAction extends Action {
	
	private IWorkbenchPart fWorkbenchPart;

	public ClearDisplayAction(IWorkbenchPart workbenchPart) {
		fWorkbenchPart= workbenchPart;
		
		setText(DisplayMessages.ClearDisplay_label); //$NON-NLS-1$
		setToolTipText(DisplayMessages.ClearDisplay_tooltip); //$NON-NLS-1$
		setDescription(DisplayMessages.ClearDisplay_description); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaDebugHelpContextIds.CLEAR_DISPLAY_VIEW_ACTION);
		
		JavaDebugImages.setLocalImageDescriptors(this, "clear_co.gif"); //$NON-NLS-1$
	}

	/**
	 * @see Action#run
	 */
	public void run() {
		Object value= fWorkbenchPart.getAdapter(IDataDisplay.class);
		if (value instanceof IDataDisplay) {
			IDataDisplay dataDisplay= (IDataDisplay) value;
			dataDisplay.clear();
		}
	}
}
