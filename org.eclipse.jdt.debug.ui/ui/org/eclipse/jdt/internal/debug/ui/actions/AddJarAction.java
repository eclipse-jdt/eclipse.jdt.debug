/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Adds an internal jar to the runtime class path.
 */
public class AddJarAction extends RuntimeClasspathAction {

	public AddJarAction(RuntimeClasspathViewer viewer) {
		super(ActionMessages.getString("AddJarAction.Add_&JARs_1"), viewer); //$NON-NLS-1$
	}	

	/**
	 * Prompts for a jar to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
		
		ViewerFilter filter= new ArchiveFilter(getSelectedJars());
		
		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();

		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.setTitle(ActionMessages.getString("AddJarAction.JAR_Selection_7")); //$NON-NLS-1$
		dialog.setMessage(ActionMessages.getString("AddJarAction.Choose_jars_to_add__8")); //$NON-NLS-1$
		dialog.addFilter(filter);
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());	
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));

		if (dialog.open() == Window.OK) {
			Object[] elements= dialog.getResult();
			IRuntimeClasspathEntry[] res= new IRuntimeClasspathEntry[elements.length];
			for (int i= 0; i < res.length; i++) {
				IResource elem= (IResource)elements[i];
				res[i]= JavaRuntime.newArchiveRuntimeClasspathEntry(elem);
			}
			getViewer().addEntries(res);
		}
					
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return getViewer().isEnabled();
	}
	
	/**
	 * Returns a list of resources of currently selected jars
	 */
	protected List getSelectedJars() {
		List list = getEntriesAsList();
		List jars = new ArrayList();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)iter.next();
			if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE) {
				IResource res = entry.getResource();
				if (res != null && res instanceof IFile) {
					jars.add(res);
				}
			}
		}
		return jars;
	}
}
