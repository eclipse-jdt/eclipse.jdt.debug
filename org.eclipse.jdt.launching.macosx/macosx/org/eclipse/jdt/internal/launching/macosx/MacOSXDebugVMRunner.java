/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.macosx;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.StandardVMDebugger;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

/**
 * Special override for MacOSX wrapping
 */
public class MacOSXDebugVMRunner extends StandardVMDebugger {
	
	static boolean startonfirstthread = false;
	
	/**
	 * Constructor
	 * @param vmInstance
	 */
	public MacOSXDebugVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.StandardVMDebugger#run(org.eclipse.jdt.launching.VMRunnerConfiguration, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(VMRunnerConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration lconfig = launch.getLaunchConfiguration();
		if(lconfig != null) {
			startonfirstthread = lconfig.getAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_START_ON_FIRST_THREAD, true);
		}
		super.run(config, launch, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMRunner#exec(java.lang.String[], java.io.File)
	 */
	@Override
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		return super.exec(MacOSXLaunchingPlugin.wrap(getClass(), cmdLine, startonfirstthread), workingDirectory);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMRunner#exec(java.lang.String[], java.io.File, java.lang.String[])
	 */
	@Override
	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		return super.exec(MacOSXLaunchingPlugin.wrap(getClass(), cmdLine, startonfirstthread), workingDirectory, envp);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.StandardVMRunner#renderCommandLine(java.lang.String[])
	 */
	@Override
	protected String renderCommandLine(String[] commandLine) {
		return super.renderCommandLine(MacOSXLaunchingPlugin.wrap(getClass(), commandLine, startonfirstthread));
	}
}
