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
package org.eclipse.jdt.internal.debug.ui.display;


import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Clears the display.
 */
public class ClearDisplayAction extends Action {
	
	private IWorkbenchPart fWorkbenchPart;

	public ClearDisplayAction(IWorkbenchPart workbenchPart) {
		fWorkbenchPart= workbenchPart;
		
		setText(DisplayMessages.getString("ClearDisplay.label")); //$NON-NLS-1$
		setToolTipText(DisplayMessages.getString("ClearDisplay.tooltip")); //$NON-NLS-1$
		setDescription(DisplayMessages.getString("ClearDisplay.description")); //$NON-NLS-1$
		
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
