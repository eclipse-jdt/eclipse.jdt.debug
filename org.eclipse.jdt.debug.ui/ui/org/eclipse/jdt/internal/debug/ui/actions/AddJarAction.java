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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;

/**
 * Adds an internal jar to the runtime class path.
 */
public class AddJarAction extends RuntimeClasspathAction {

	public AddJarAction(IClasspathViewer viewer) {
		super(ActionMessages.AddJarAction_Add__JARs_1, viewer); 
	}	

	/**
	 * Prompts for a jar to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
		
		IPath[] paths = BuildPathDialogAccess.chooseJAREntries(getShell(), null, getSelectedJars());

		if (paths != null && paths.length > 0) {
			IRuntimeClasspathEntry[] res= new IRuntimeClasspathEntry[paths.length];
			for (int i= 0; i < res.length; i++) {
				IResource elem= ResourcesPlugin.getWorkspace().getRoot().getFile(paths[i]);
				res[i]= JavaRuntime.newArchiveRuntimeClasspathEntry(elem);
			}
			getViewer().addEntries(res);
		}	
	}
	
	/**
	 * Returns a list of resources of currently selected jars
	 */
	protected IPath[] getSelectedJars() {
		List list = getEntriesAsList();
		List jars = new ArrayList();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)iter.next();
			if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE) {
				IResource res = entry.getResource();
				if (res != null && res instanceof IFile) {
					jars.add(res.getFullPath());
				}
			}
		}
		return (IPath[]) jars.toArray(new IPath[jars.size()]);
	}
	
	protected int getActionType() {
		return ADD;
	}
}
