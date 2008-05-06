/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Moves selected enries in a runtime classpath viewer down one position.
 */
public class MoveDownAction extends RuntimeClasspathAction {

	public MoveDownAction(IClasspathViewer viewer) {
		super(ActionMessages.MoveDownAction_M_ove_Down_1, viewer); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		List targets = getOrderedSelection();
		if (targets.isEmpty()) {
			return;
		}
		List list = getEntriesAsList();
		int bottom = list.size() - 1;
		int index = 0;
		for (int i = targets.size() - 1; i >= 0; i--) {
			Object target = targets.get(i);
			index = list.indexOf(target);
			if (index < bottom) {
				bottom = index + 1;
				Object temp = list.get(bottom);
				list.set(bottom, target);
				list.set(index, temp);
			}
			if (bottom == index){
				bottom--;
			} else {
				bottom = index;
			}
		} 
		setEntries(list);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		return getViewer().updateSelection(getActionType(), selection);	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction#getActionType()
	 */
	protected int getActionType() {
		return MOVE;
	}
}
