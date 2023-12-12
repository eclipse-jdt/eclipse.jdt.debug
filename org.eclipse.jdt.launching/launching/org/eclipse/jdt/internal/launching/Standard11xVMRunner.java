/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;


import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMRunnerConfiguration;


/**
 * A 1.1.x VM runner
 */
public class Standard11xVMRunner extends StandardVMRunner {

	public Standard11xVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMRunner#run(org.eclipse.jdt.launching.VMRunnerConfiguration, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, LaunchingMessages.StandardVMRunner_Launching_VM____1, 2);
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Constructing_command_line____2); //

		String program= constructProgramString(config);

		List<String> arguments= new ArrayList<>();
		arguments.add(program);

		// VM arguments are the first thing after the java program so that users can specify
		// options like '-client' & '-server' which are required to be the first option
		String[] vmArgs= combineVmArgs(config, fVMInstance);
		addArguments(vmArgs, arguments);

		String[] bootCP= config.getBootClassPath();
		String[] classPath = config.getClassPath();

		String[] combinedPath = null;
		if (bootCP == null) {
			LibraryLocation[] locs = JavaRuntime.getLibraryLocations(fVMInstance);
			bootCP = new String[locs.length];
			for (int i = 0; i < locs.length; i++) {
				bootCP[i] = locs[i].getSystemLibraryPath().toOSString();
			}
		}

		combinedPath = new String[bootCP.length + classPath.length];
		int offset = 0;
		for (int i = 0; i < bootCP.length; i++) {
			combinedPath[offset] = bootCP[i];
			offset++;
		}
		for (int i = 0; i < classPath.length; i++) {
			combinedPath[offset] = classPath[i];
			offset++;
		}
		if (combinedPath.length > 0) {
			arguments.add("-classpath"); //$NON-NLS-1$
			arguments.add(convertClassPath(combinedPath));
		}
		arguments.add(config.getClassToLaunch());

		String[] programArgs= config.getProgramArguments();

		String[] envp = prependJREPath(config.getEnvironment());
		int lastVMArgumentIndex = arguments.size() - 1;
		addArguments(programArgs, arguments);

		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);

		// check for cancellation
		if (subMonitor.isCanceled()) {
			return;
		}
		File workingDir = getWorkingDir(config);
		ClasspathShortener classpathShortener = new ClasspathShortener(fVMInstance, launch, cmdLine, lastVMArgumentIndex, workingDir, envp);
		if (classpathShortener.shortenCommandLineIfNecessary()) {
			cmdLine = classpathShortener.getCmdLine();
			envp = classpathShortener.getEnvp();
		}

		subMonitor.worked(1);
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Starting_virtual_machine____3);

		Process p= null;
		String[] newCmdLine = validateCommandLine(launch.getLaunchConfiguration(), cmdLine);
		if(newCmdLine != null) {
			cmdLine = newCmdLine;
		}
		p= exec(cmdLine, workingDir, envp);
		if (p == null) {
			return;
		}

		// check for cancellation
		if (subMonitor.isCanceled()) {
			p.destroy();
			return;
		}
		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
		IProcess process = DebugPlugin.newProcess(launch, p, renderProcessLabel(p, cmdLine, timestamp));
		process.setAttribute(DebugPlugin.ATTR_PATH, cmdLine[0]);
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
		String ltime = launch.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		process.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, ltime != null ? ltime : timestamp);
		if(workingDir != null) {
			process.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, workingDir.getAbsolutePath());
		}
		if (!classpathShortener.getProcessTempFiles().isEmpty()) {
			String tempFiles = classpathShortener.getProcessTempFiles().stream().map(file -> file.getAbsolutePath()).collect(Collectors.joining(File.pathSeparator));
			process.setAttribute(LaunchingPlugin.ATTR_LAUNCH_TEMP_FILES, tempFiles);
		}
		subMonitor.worked(1);
	}
}

