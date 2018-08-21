/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.testplugin.launching;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

/**
 * Test launch shortcut implementation
 * @since 3.4
 */
public class ParticipantLaunchShortcut implements ILaunchShortcut2 {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchConfigurations(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		return getConfigurations();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchConfigurations(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		return getConfigurations();
	}

	/**
	 * Returns all of the launch configurations of type <code>org.eclipse.jdt.debug.tests.testConfigType</code>
	 * @return all of the launch configurations of type <code>org.eclipse.jdt.debug.tests.testConfigType</code>
	 */
	protected ILaunchConfiguration[] getConfigurations() {
		try {
			ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = lm.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType");
			return lm.getLaunchConfigurations(type);
		}
		catch(CoreException ce) {DebugUIPlugin.log(ce);}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchableResource(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public IResource getLaunchableResource(ISelection selection) {
		return null;//ResourcesPlugin.getWorkspace().getRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchableResource(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
	 */
	@Override
	public void launch(ISelection selection, String mode) {
		performLaunch(mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
	 */
	@Override
	public void launch(IEditorPart editor, String mode) {
		performLaunch(mode);
	}

	protected void performLaunch(String mode) {
		//first try to find a config
		try {
			ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = lm.getLaunchConfigurationType("org.eclipse.jdt.debug.tests.testConfigType");
			ILaunchConfiguration config = null;
			if(type != null) {
				ILaunchConfiguration[] configs = lm.getLaunchConfigurations(type);
				if(configs.length > 0) {
					config = configs[0];
				}
				if(config == null) {
					//create a new one
					ILaunchConfigurationWorkingCopy copy = type.newInstance(null, lm.generateLaunchConfigurationName("New_Test_Config"));
					copy.setAttribute("testconfig", true);
					config = copy.doSave();
				}
				if(config != null) {
					config.launch(mode, new NullProgressMonitor());
				}
			}
		}
		catch(CoreException ce) {DebugPlugin.log(ce);}

	}
}
