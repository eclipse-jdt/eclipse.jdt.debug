/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationTabGroupViewer;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaDependenciesTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.launching.JavaRuntime;

@SuppressWarnings("restriction")
public class LocalJavaApplicationTabGroup extends AbstractLaunchConfigurationTabGroup {

	/**
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		JavaClasspathTab tab = null;
		if (dialog instanceof LaunchConfigurationsDialog) {
			LaunchConfigurationTabGroupViewer tabViewer = ((LaunchConfigurationsDialog) dialog).getTabViewer();
			if (tabViewer != null) {
				Object input = tabViewer.getInput();
				if (input instanceof ILaunchConfiguration) {
					ILaunchConfiguration configuration = (ILaunchConfiguration) input;
					if (JavaRuntime.isModularConfiguration(configuration)) {
						tab = new JavaDependenciesTab();
					}
				}
			}
		}
		if (tab == null) {
			tab = new JavaClasspathTab();
		}
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new JavaMainTab(),
			new JavaArgumentsTab(),
			new JavaJRETab(),
				tab,
			new SourceLookupTab(),
			new EnvironmentTab(),
			new CommonTab()
		};
		setTabs(tabs);
	}

}
