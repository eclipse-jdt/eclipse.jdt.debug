package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.IHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Displays the result of an evaluation in the Java editor
 */
public class EditorDisplayAction extends DisplayAction {

	public EditorDisplayAction(IWorkbenchPart part, boolean usedInEditor) {
		super(part, usedInEditor);
		WorkbenchHelp.setHelp(this,	new Object[] { IHelpContextIds.DISPLAY_ACTION });	
	}
	
	public EditorDisplayAction() {
		this(null, true);
	}
	
	protected IDataDisplay getDataDisplay() {
		
		IWorkbenchPage page= JDIDebugUIPlugin.getDefault().getActivePage();
		IWorkbenchPart activePart= page.getActivePart();
		IViewPart view= page.findView(DisplayView.ID_DISPLAY_VIEW);
		if (view == null) {
			try {
				view= page.showView(DisplayView.ID_DISPLAY_VIEW);		
			} catch (PartInitException e) {
				MessageDialog.openError(getShell(), DisplayMessages.getString("EditorDisplayAction.Cannot_open_Display_viewer_1"), e.getMessage()); //$NON-NLS-1$
			} finally {
				page.activate(activePart);
			}
		}
		
		if (view != null) {
			page.bringToTop(view);
			Object value= view.getAdapter(IDataDisplay.class);
			if (value instanceof IDataDisplay) {
				return (IDataDisplay) value;
			}	
		}
		
		return null;
	}

}