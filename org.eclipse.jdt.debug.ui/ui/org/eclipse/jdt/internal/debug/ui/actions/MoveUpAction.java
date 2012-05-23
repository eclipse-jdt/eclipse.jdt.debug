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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Moves selected entries in a runtime classpath viewer up one position.
 */
public class MoveUpAction extends RuntimeClasspathAction {

	public MoveUpAction(IClasspathViewer viewer) {
		super(ActionMessages.MoveUpAction_Move_U_p_1, viewer); 
	}

	/* (non-Javadoc)
	 * Moves all selected entries up one position (if possible).
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		List<IRuntimeClasspathEntry> targets = getOrderedSelection();
		if (targets.isEmpty()) {
			return;
		}
		int top = 0;
		int index = 0;
		List<IRuntimeClasspathEntry> list = getEntriesAsList();
		Iterator<IRuntimeClasspathEntry> entries = targets.iterator();
		while (entries.hasNext()) {
			IRuntimeClasspathEntry target = entries.next();
			index = list.indexOf(target);
			if (index > top) {
				top = index - 1;
				IRuntimeClasspathEntry temp = list.get(top);
				list.set(top, target);
				list.set(index, temp);
			}
			if (index == top){
				top++;
			} else {
				top = index;
			}
		} 
		setEntries(list);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		return getViewer().updateSelection(getActionType(), selection);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction#getActionType()
	 */
	@Override
	protected int getActionType() {
		return MOVE;
	}
}
