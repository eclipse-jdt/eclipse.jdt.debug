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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

public class MacOSXVMRunner extends StandardVMRunner {
	
	static boolean startonfirstthread = false;
	
	/**
	 * Constructor
	 * @param vmInstance
	 */
	public MacOSXVMRunner(IVMInstall vmInstance) {
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
	
	@Override
	protected String[] validateCommandLine(String[] cmdLine) {
		return MacOSXLaunchingPlugin.wrap(cmdLine, startonfirstthread);
	}
}
