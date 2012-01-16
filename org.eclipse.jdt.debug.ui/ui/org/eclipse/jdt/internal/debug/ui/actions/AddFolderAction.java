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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * Adds an internal folder to the runtime class path.
 */
public class AddFolderAction extends RuntimeClasspathAction {

	/**
	 * provides a filter to remove the files from the ElementSelectionDialog
	 * 
	 * @since 3.2
	 *
	 */
	class FileFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if(element instanceof IProject) {
				return true;
			}
			if(element instanceof IFolder) {
				return true;
			}
			return false;
		}
		
	}
	
	public AddFolderAction(IClasspathViewer viewer) {
		super(ActionMessages.AddFolderAction_Add__Folders_1, viewer); 
	}	

	/**
	 * Prompts for folder(s) to add.
	 * 
	 * @see IAction#run()
	 */	
	@Override
	public void run() {
		
		ISelectionStatusValidator validator= new ISelectionStatusValidator() {
			List<IResource> fAlreadySelected = getSelectedFolders();
			public IStatus validate(Object[] selection) {
				for (int i= 0; i < selection.length; i++) {
					if (!(selection[i] instanceof IContainer)) {
						return new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Selection must be a folder", null);  //$NON-NLS-1$
					} else if (fAlreadySelected.contains(selection[i])) {
						return new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Classpath already includes selected folder(s).", null);  //$NON-NLS-1$
					}
					
				}
				return new Status(IStatus.OK, JDIDebugPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$
			}			
		};
		
		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();

		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.addFilter(new FileFilter());
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
		dialog.setValidator(validator);
		dialog.setTitle(ActionMessages.AddFolderAction_Folder_Selection_4); 
		dialog.setMessage(ActionMessages.AddFolderAction_Choose_folders_to_add__5); 
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());	

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
	 * Returns a list of resources of currently selected folders
	 * @return the list of {@link IResource}s
	 */
	protected List<IResource> getSelectedFolders() {
		List<IRuntimeClasspathEntry> list = getEntriesAsList();
		List<IResource> folders = new ArrayList<IResource>();
		Iterator<IRuntimeClasspathEntry> iter = list.iterator();
		while (iter.hasNext()) {
			IRuntimeClasspathEntry entry = iter.next();
			if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE) {
				IResource res = entry.getResource();
				if (res != null && res instanceof IContainer) {
					folders.add(res);
				}
			}
		}
		return folders;
	}
	
	@Override
	protected int getActionType() {
		return ADD;
	}
}
