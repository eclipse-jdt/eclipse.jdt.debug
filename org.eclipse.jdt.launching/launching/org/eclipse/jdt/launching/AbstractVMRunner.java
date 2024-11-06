/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Pascal Gruen (pascal.gruen@googlemail.com) - Bug 217994 Run/Debug honors JRE VM args before Launcher VM args
 *******************************************************************************/
package org.eclipse.jdt.launching;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.CommandLineQuoting;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;

/**
 * Abstract implementation of a VM runner.
 * <p>
 * Clients implementing VM runners should subclass this class.
 * </p>
 * @see IVMRunner
 * @since 2.0
 */
public abstract class AbstractVMRunner implements IVMRunner {

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 *
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 * @throws CoreException The exception encapsulating the reason for the abort
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, getPluginIdentifier(), code, message, exception));
	}

	/**
	 * Returns the identifier of the plug-in this VM runner
	 * originated from.
	 *
	 * @return plug-in identifier
	 */
	protected abstract String getPluginIdentifier();

	/**
	 * Executes the given command line using the given working directory
	 *
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory
	 * @return the {@link Process}
	 * @throws CoreException if the execution fails
	 * @see DebugPlugin#exec(String[], File)
	 */
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		cmdLine = quoteWindowsArgs(cmdLine);
		return DebugPlugin.exec(cmdLine, workingDirectory);
	}

	/**
	 * Executes the given command line using the given working directory and environment
	 *
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory
	 * @param envp the environment
	 * @return the {@link Process}
	 * @throws CoreException is the execution fails
	 * @since 3.0
	 * @see DebugPlugin#exec(String[], File, String[])
	 */
	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		cmdLine = quoteWindowsArgs(cmdLine);
		return DebugPlugin.exec(cmdLine, workingDirectory, envp);
	}

	/**
	 * Executes the given command line using the given working directory and environment
	 *
	 * @param cmdLine
	 *            the command line
	 * @param workingDirectory
	 *            the working directory
	 * @param envp
	 *            the environment
	 * @param mergeOutput
	 *            if <code>true</code> the error stream will be merged with standard output stream and both can be read through the same output stream
	 * @return the {@link Process}
	 * @throws CoreException
	 *             is the execution fails
	 * @since 3.15
	 * @see DebugPlugin#exec(String[], File, String[])
	 */
	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp, boolean mergeOutput) throws CoreException {
		cmdLine = quoteWindowsArgs(cmdLine);
		return DebugPlugin.exec(cmdLine, workingDirectory, envp, mergeOutput);
	}

	/**
	 * @since 3.11
	 */
	protected static String[] quoteWindowsArgs(String[] cmdLine) {
		return CommandLineQuoting.quoteWindowsArgs(cmdLine);
	}

	/**
	 * Returns the given array of strings as a single space-delimited string.
	 *
	 * @param cmdLine array of strings
	 * @return a single space-delimited string
	 */
	protected String getCmdLineAsString(String[] cmdLine) {
		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < cmdLine.length; i++) {
			buff.append(cmdLine[i]);
			if (i != cmdLine.length - 1) {
				if (cmdLine[i + 1].startsWith("-")) { //$NON-NLS-1$
					buff.append("\n"); //$NON-NLS-1$
				} else {
					buff.append(' ');
				}
			}
		}
		return buff.toString();
	}

	/**
	 * Returns the default process attribute map for Java processes.
	 *
	 * @return default process attribute map for Java processes
	 */
	protected Map<String, String> getDefaultProcessMap() {
		Map<String, String> map = new HashMap<>();
		map.put(IProcess.ATTR_PROCESS_TYPE, IJavaLaunchConfigurationConstants.ID_JAVA_PROCESS_TYPE);
		return map;
	}

	/**
	 * Returns a new process aborting if the process could not be created.
	 * @param launch the launch the process is contained in
	 * @param p the system process to wrap
	 * @param label the label assigned to the process
	 * @param attributes values for the attribute map
	 * @return the new process
	 * @throws CoreException problems occurred creating the process
	 * @since 3.0
	 */
	protected IProcess newProcess(ILaunch launch, Process p, String label, Map<String, String> attributes) throws CoreException {
		IProcess process= DebugPlugin.newProcess(launch, p, label, attributes);
		if (process == null) {
			p.destroy();
			abort(LaunchingMessages.AbstractVMRunner_0, null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		return process;
	}

	/**
	 * Combines and returns VM arguments specified by the runner configuration,
	 * with those specified by the VM install, if any.
	 *
	 * @param configuration runner configuration
	 * @param vmInstall VM install
	 * @return combined VM arguments specified by the runner configuration
	 *  and VM install
	 * @since 3.0
	 */
	protected String[] combineVmArgs(VMRunnerConfiguration configuration, IVMInstall vmInstall) {
		String[] launchVMArgs= configuration.getVMArguments();
		String[] vmVMArgs = vmInstall.getVMArguments();
		if (vmVMArgs == null || vmVMArgs.length == 0) {
			return launchVMArgs;
		}
		// string substitution
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		for (int i = 0; i < vmVMArgs.length; i++) {
			try {
				vmVMArgs[i] = manager.performStringSubstitution(vmVMArgs[i], false);
			} catch (CoreException e) {
				LaunchingPlugin.log(e.getStatus());
			}
		}
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=217994
		// merge default VM + launcher VM arguments. Make sure to pass launcher arguments in last so that
		// they can take precedence over the default VM args!
		String[] allVMArgs = new String[launchVMArgs.length + vmVMArgs.length];
		System.arraycopy(vmVMArgs, 0, allVMArgs, 0, vmVMArgs.length);
		System.arraycopy(launchVMArgs, 0, allVMArgs, vmVMArgs.length, launchVMArgs.length);
		return allVMArgs;
	}

	/**
	 * Examines the project and install for presence of module and execution support.
	 *
	 * @param config
	 *            runner configuration
	 * @param vmInstall
	 *            VM install
	 * @return <code>true</code> if project is a module and uses JRE version 9 or more, or <code>false</code> otherwise
	 * @since 3.10
	 */
	protected boolean isModular(VMRunnerConfiguration config, IVMInstall vmInstall) {
		if (config.getModuleDescription() != null && config.getModuleDescription().length() > 0 && vmInstall instanceof AbstractVMInstall install) {
			String vmver = install.getJavaVersion();
			// versionToJdkLevel only handles 3 char versions = 1.5, 1.6, 1.9, etc
			if (vmver.length() > 3) {
				vmver = vmver.substring(0, 3);
			}
			if (JavaCore.compareJavaVersions(vmver, JavaCore.VERSION_9) >= 0) {
				return true;
			}
		}
		return false;
	}
}
