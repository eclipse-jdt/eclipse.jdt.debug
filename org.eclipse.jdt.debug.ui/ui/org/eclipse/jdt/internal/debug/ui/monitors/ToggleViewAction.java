package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.texteditor.IUpdate;

public abstract class ToggleViewAction implements IViewActionDelegate, IUpdate {

	protected MonitorsView fMonitorsView;
	protected IAction fAction;
	
	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		if (view instanceof MonitorsView) {
			fMonitorsView= (MonitorsView)view;
			fMonitorsView.add(this);
		}
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fAction= action;
	}
	
	/**
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		if (fMonitorsView.getViewId() != getViewId()) {
			fAction.setChecked(false);
		}
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		fMonitorsView.setViewId(getViewId());
		fMonitorsView.updateObjects();
	}
	
	protected abstract int getViewId();
}
