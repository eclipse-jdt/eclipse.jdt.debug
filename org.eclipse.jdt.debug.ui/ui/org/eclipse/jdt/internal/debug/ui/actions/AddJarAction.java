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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
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
		
		ISelectionStatusValidator validator= new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection.length == 0) {
					return new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
				}
				for (int i= 0; i < selection.length; i++) {
					if (selection[i] instanceof IFile) {
						IFile file = (IFile)selection[i];
						String ext = file.getFileExtension();
						if (ext.equalsIgnoreCase("jar") || ext.equalsIgnoreCase("zip")) { //$NON-NLS-1$ //$NON-NLS-2$
							// OK
						} else {
							return new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, ActionMessages.getString("AddJarAction.Selection_must_be_a_jar_or_zip_4"), null); //$NON-NLS-1$
						}
					} else {
						return new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
					}					
				}
				return new Status(IStatus.OK, JDIDebugPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
			}			
		};
		ViewerFilter filter= new ArchiveFilter(getSelectedJars());
		
		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();

		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.setValidator(validator);
		dialog.setTitle(ActionMessages.getString("AddJarAction.JAR_Selection_7")); //$NON-NLS-1$
		dialog.setMessage(ActionMessages.getString("AddJarAction.Choose_jars_to_add__8")); //$NON-NLS-1$
		dialog.addFilter(filter);
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());	
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));

		if (dialog.open() == ElementTreeSelectionDialog.OK) {
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
		List list = getEntiresAsList();
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
