package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
