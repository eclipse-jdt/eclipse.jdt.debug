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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Restores default entries in the runtime classpath.
 */
public class RestoreDefaultEntriesAction extends RuntimeClasspathAction {
	
	private JavaClasspathTab fTab;
	
	/**
	 * Constructor
	 * @param viewer the associated classpath viewer
	 * @param tab the tab the viewer resides in
	 */
	public RestoreDefaultEntriesAction(IClasspathViewer viewer, JavaClasspathTab tab) {
		super(ActionMessages.RestoreDefaultEntriesAction_0, viewer); 
		fTab = tab;
	}	

	/**
	 * Only does work if we are not currently using the default classpath
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		try {
			ILaunchConfiguration config = fTab.getLaunchConfiguration();
			if(!config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false)) {
				ILaunchConfigurationWorkingCopy copy = null;
				if(config.isWorkingCopy()) {
					copy = (ILaunchConfigurationWorkingCopy) config;
				}
				else {
					copy = config.getWorkingCopy();
				}
				copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
				getViewer().setEntries(JavaRuntime.computeUnresolvedRuntimeClasspath(copy));
			}
		} 
		catch (CoreException e) {return;}	
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return true;
	}
}
