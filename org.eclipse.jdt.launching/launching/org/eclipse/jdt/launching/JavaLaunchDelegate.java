/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
package org.eclipse.jdt.launching;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.sourcelookup.advanced.AdvancedJavaLaunchDelegate;
import org.eclipse.osgi.util.NLS;

/**
 * A launch delegate for launching local Java applications.
 * <p>
 * Clients may subclass and instantiate this class.
 * </p>
 *
 * @see AdvancedJavaLaunchDelegate
 * @since 3.1
 */
public class JavaLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {

	@Override
	public String showCommandLine(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			VMRunnerConfiguration runConfig = getVMRunnerConfiguration(configuration, mode, monitor);
			if (runConfig == null) {
				return ""; //$NON-NLS-1$
			}
			IVMRunner runner = getVMRunner(configuration, mode);
			String cmdLine = runner.showCommandLine(runConfig, launch, monitor);

			// check for cancellation
			if (monitor.isCanceled()) {
				return ""; //$NON-NLS-1$
			}
			return cmdLine;
		} finally {
			monitor.done();
		}
	}

	@SuppressWarnings("deprecation")
	private VMRunnerConfiguration getVMRunnerConfiguration(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {

		monitor.beginTask(NLS.bind("{0}...", configuration.getName()), 3); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);

		String mainTypeName = verifyMainTypeName(configuration);

		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}

		// Environment variables
		String[] envp = getEnvironment(configuration);

		// Program & VM arguments
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = concat(getVMArguments(configuration), getVMArguments(configuration, mode));
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

		// VM-specific attributes
		Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);

		// Create VM config
		// Bug 529435 :to move to getClasspathAndModulepath after java 8 is sunset
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, getClasspath(configuration));
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setEnvironment(envp);
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);
		runConfig.setPreviewEnabled(supportsPreviewFeatures(configuration));
		// Module name not required for Scrapbook page
		if (supportsModule() && !mainTypeName.equals("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain")) { //$NON-NLS-1$
			// Module name need not be the same as project name
			try {
				IJavaProject proj = JavaRuntime.getJavaProject(configuration);
				if (proj != null) {
					IModuleDescription module = proj == null ? null : proj.getModuleDescription();
					String modName = module == null ? null : module.getElementName();
					if (modName != null && modName.length() > 0) {
						String defaultModuleName = null;
						String moduleName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_NAME, defaultModuleName);
						if (moduleName != null && !moduleName.isEmpty()) {
							runConfig.setModuleDescription(moduleName);
						} else {
							runConfig.setModuleDescription(modName);
						}
					}
				}
			} catch (CoreException e) {
				// Not a java Project so no need to set module description
			}
		}

		// Launch Configuration should be launched by Java 9 or above for modulepath setting
		if (!JavaRuntime.isModularConfiguration(configuration)) {
			// Bootpath
			runConfig.setBootClassPath(getBootpath(configuration));
		} else if (supportsModule()) {
			// module path
			String[][] paths = getClasspathAndModulepath(configuration);
			if (paths != null && paths.length > 1) {
				runConfig.setModulepath(paths[1]);
			}
			if (!configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, true)) {
				runConfig.setOverrideDependencies(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_CLI_OPTIONS, "")); //$NON-NLS-1$
			} else {
				runConfig.setOverrideDependencies(getModuleCLIOptions(configuration));
			}
		}
		runConfig.setMergeOutput(configuration.getAttribute(DebugPlugin.ATTR_MERGE_OUTPUT, false));

		// check for cancellation
		if (monitor.isCanceled()) {
			return null;
		}
		monitor.worked(1);

		return runConfig;
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {

			VMRunnerConfiguration runConfig = getVMRunnerConfiguration(configuration, mode, monitor);
			if (runConfig == null) {
				return;
			}
			// stop in main
			prepareStopInMain(configuration);

			// done the verification phase
			monitor.worked(1);

			monitor.subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Creating_source_locator____2);
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);
			// Launch the configuration - 1 unit of work
			IVMRunner runner = getVMRunner(configuration, mode);
			runner.run(runConfig, launch, monitor);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}
		}
		finally {
			monitor.done();
		}
	}

	private static String concat(String args1, String args2) {
		StringBuilder args = new StringBuilder();
		if (args1 != null && !args1.isEmpty()) {
			args.append(args1);
		}
		if (args2 != null && !args2.isEmpty()) {
			args.append(" "); //$NON-NLS-1$
			args.append(args2);
		}
		return args.toString();
	}
}
