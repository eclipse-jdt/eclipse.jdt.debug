package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Removes selected enries in a runtime classpath viewer.
 */
public class RemoveAction extends RuntimeClasspathAction {

	public RemoveAction(RuntimeClasspathViewer viewer) {
		super(ActionMessages.getString("RemoveAction.&Remove_1"), viewer); //$NON-NLS-1$
	}
	/**
	 * Removes all selected entries.
	 * 
	 * @see IAction#run()
	 */
	public void run() {
		List targets = getOrderedSelection();
		List list = getEntiresAsList();
		list.removeAll(targets);
		setEntries(list);
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return !selection.isEmpty();
	}

}
