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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEditor;
import org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Moves selected entries in a runtime classpath viewer up one position.
 */
public class EditClasspathEntryAction extends RuntimeClasspathAction {
	
	private ILaunchConfiguration fConfiguration;

	public EditClasspathEntryAction(IClasspathViewer viewer, ILaunchConfiguration configuration) {
		super(ActionMessages.EditClasspathEntryAction_0, viewer);
		fConfiguration = configuration;
	}
	/**
	 * Moves all selected entries up one position (if possible).
	 * 
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		List<IRuntimeClasspathEntry> targets = getOrderedSelection();
		if (targets.size() != 1) {
			return;
		}
		IRuntimeClasspathEntry entry = targets.get(0);
		IRuntimeClasspathEntry[] original = new IRuntimeClasspathEntry[]{entry};
		IRuntimeClasspathEntry[] delegtes = new IRuntimeClasspathEntry[original.length];
		IClasspathEntry[] parents = new IClasspathEntry[original.length];
		for (int i = 0; i < delegtes.length; i++) {
			ClasspathEntry classpathEntry = (ClasspathEntry)original[i];
			delegtes[i] = classpathEntry.getDelegate();
			parents[i] = classpathEntry.getParent();
		}
		IClasspathEditor editor = getEditor(entry);
		if (editor != null) {
			IRuntimeClasspathEntry[] replacements = editor.edit(getShell(), fConfiguration, delegtes);
			if (replacements != null) {
				IRuntimeClasspathEntry[] wrappers = new IRuntimeClasspathEntry[replacements.length];
				List<IRuntimeClasspathEntry> list = getEntriesAsList();
				int index = 0;
				for (int i = 0; i < list.size(); i++) {
					Object element = list.get(i);
					if (element == original[index]) {
						wrappers[index] = new ClasspathEntry(replacements[index], parents[index]);
						list.set(i, wrappers[index]);
						index++;
					}
				}
				setEntries(list);
				for (int i = 0; i < wrappers.length; i++) {
					getViewer().refresh(wrappers[i]);
				}
			}
		}
		
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object element = selection.getFirstElement();
			if (element instanceof IRuntimeClasspathEntry) {
				IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) element;
				IClasspathEditor editor = getEditor(entry);
				if (editor != null) {
					return editor.canEdit(fConfiguration, new IRuntimeClasspathEntry[]{((ClasspathEntry)entry).getDelegate()});
				}
			}
		}
		return false;
	}
	
	protected IClasspathEditor getEditor(IRuntimeClasspathEntry entry) {
		if (entry instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) entry;
			return (IClasspathEditor) adaptable.getAdapter(IClasspathEditor.class);
		}		
		return null;
	}
	
}
