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

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;

/**
 * Attach source to an archive or variable.
 */
public class AttachSourceAction extends RuntimeClasspathAction {
	
	private IRuntimeClasspathEntry[] fEntries;
	
	/**
	 * Creates an action to open a source attachment dialog.
	 * 
	 * @param viewer the viewer the action is associated with or <code>null</code>
	 * @param style a button or radio button
	 */
	public AttachSourceAction(RuntimeClasspathViewer viewer, int style) {
		super((style == SWT.RADIO) ? ActionMessages.AttachSourceAction_2 : ActionMessages.AttachSourceAction_3, viewer); // 
	}	

	/**
	 * Prompts source attachment.
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */	
	@Override
	public void run() { 
		IClasspathEntry classpathEntry = BuildPathDialogAccess.configureSourceAttachment(getShell(), fEntries[0].getClasspathEntry()); 
		if (classpathEntry != null) {
			for (int i = 0; i < fEntries.length; i++) {
				IRuntimeClasspathEntry entry = fEntries[i];
				entry.setSourceAttachmentPath(classpathEntry.getSourceAttachmentPath());
				entry.setSourceAttachmentRootPath(classpathEntry.getSourceAttachmentRootPath());
				getViewer().refresh(entry);
			}
			getViewer().notifyChanged();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		fEntries = new IRuntimeClasspathEntry[selection.size()];
		Iterator<?> iterator = selection.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Object selected= iterator.next();
			if (selected instanceof IRuntimeClasspathEntry) {
				IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)selected;
				int type = entry.getType();
				switch (type) {
					case IRuntimeClasspathEntry.VARIABLE:
					case IRuntimeClasspathEntry.ARCHIVE:
						fEntries[i] = entry;
						i++;
						break;
					default:
						return false;
				}
			} else {
				return false;
			}
		}
		return selection.size() > 0;
	}
}
