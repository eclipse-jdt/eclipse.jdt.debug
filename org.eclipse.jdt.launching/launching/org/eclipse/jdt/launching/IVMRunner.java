/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;



/**
 * A VM runner starts a Java VM running a Java program.
 * <p>
 * Clients may implement this interface to launch a new kind of VM.
 * </p>
 */
public interface IVMRunner {
		
	/**
	 * Launches a Java VM as specified in the given configuration,
	 * contributing results (debug targets and processes), to the
	 * given launch.
	 *
	 * @param configuration the configuration settings for this run
	 * @param launch the launch to contribute to
	 * @param monitor progress monitor or <code>null</code>
	 * @exception CoreException if an exception occurs while launching
	 */
	public void run(VMRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException;	
	
}