/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jdt.internal.debug.ui.classpath.RuntimeClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * Moves selected entries in a runtime classpath viewer up one position.
 */
public class CopyAction extends RuntimeClasspathAction {
	RuntimeClasspathViewer fClasspathViewer;

	public CopyAction(IClasspathViewer viewer) {
		super(ActionMessages.CopyAction_1, viewer);
		fClasspathViewer = (RuntimeClasspathViewer) viewer;
	}

	@Override
	public void run() {
		String text = fClasspathViewer.getTreeViewer().getSelection().toString();
		Clipboard cp = new Clipboard(null);
		cp.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
		cp.dispose();
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		return getViewer().updateSelection(getActionType(), selection);
	}

	@Override
	protected int getActionType() {
		return COPY;
	}
}
