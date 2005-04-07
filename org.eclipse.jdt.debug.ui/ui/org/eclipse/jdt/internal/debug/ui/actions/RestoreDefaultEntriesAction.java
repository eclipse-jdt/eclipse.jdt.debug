/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Restores default entries in the runtime classpath.
 */
public class RestoreDefaultEntriesAction extends RuntimeClasspathAction {
	
	private JavaClasspathTab fTab;
	
	public RestoreDefaultEntriesAction(IClasspathViewer viewer, JavaClasspathTab tab) {
		super(ActionMessages.RestoreDefaultEntriesAction_0, viewer); //$NON-NLS-1$
		fTab = tab;
	}	

	/**
	 * Prompts for a project to add.
	 * 
	 * @see IAction#run()
	 */	
	public void run() {
		IRuntimeClasspathEntry[] entries= null;
		try {
			ILaunchConfigurationWorkingCopy copy= (ILaunchConfigurationWorkingCopy) fTab.getLaunchConfiguration();
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
			entries= JavaRuntime.computeUnresolvedRuntimeClasspath(copy);
		} catch (CoreException e) {
			//TODO set error message
			return;
		}	
		getViewer().setEntries(entries);
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return true;
	}
	
}
