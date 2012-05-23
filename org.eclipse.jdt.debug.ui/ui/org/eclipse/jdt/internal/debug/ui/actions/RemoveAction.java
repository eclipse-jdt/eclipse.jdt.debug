/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.List;

import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Removes selected entries in a runtime classpath viewer.
 */
public class RemoveAction extends RuntimeClasspathAction {

	public RemoveAction(IClasspathViewer viewer) {
		super(ActionMessages.RemoveAction__Remove_1, viewer); 
	}
	/**
	 * Removes all selected entries.
	 * 
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		List<IRuntimeClasspathEntry> targets = getOrderedSelection();
		List<IRuntimeClasspathEntry> list = getEntriesAsList();
		list.removeAll(targets);
		setEntries(list);
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		return getViewer().updateSelection(getActionType(), selection);
	}
	
	@Override
	protected int getActionType() {
		return REMOVE;
	}
}
