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
package org.eclipse.jdt.internal.launching;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
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

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
		subMonitor.beginTask(LaunchingMessages.StandardVMRunner_Launching_VM____1, 2); 
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Constructing_command_line____2); //		
		
		String program= constructProgramString(config);
		
		List<String> arguments= new ArrayList<String>();
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
		addArguments(programArgs, arguments);
				
		String[] cmdLine= new String[arguments.size()];
		arguments.toArray(cmdLine);

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		
		subMonitor.worked(1);
		subMonitor.subTask(LaunchingMessages.StandardVMRunner_Starting_virtual_machine____3); 
		
		Process p= null;
		File workingDir = getWorkingDir(config);
		p= exec(cmdLine, workingDir);
		if (p == null) {
			return;
		}
		
		// check for cancellation
		if (monitor.isCanceled()) {
			p.destroy();
			return;
		}		
		
		IProcess process= DebugPlugin.newProcess(launch, p, renderProcessLabel(cmdLine));
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));
		subMonitor.worked(1);
	}
}

