/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;



/**
 * A VM runner starts a Java VM running a Java program.
 * <p>
 * Clients may implement this interface to launch a new kind of VM.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IVMRunner {
	
	/**
	 * Launches a Java VM as specified in the given configuration.
	 *
	 * @param configuration the configuration settings for this run
	 * @return a result object containing the create processes and 
	 *   debug target if the launch was successful, and <code>null</code> otherwise
	 * @deprecated use run(ILaunchConfiguration)
	 */
	public VMRunnerResult run(VMRunnerConfiguration configuration) throws CoreException;

	/**
	 * Launches a Java VM as specified in the given configuration.
	 *
	 * @param configuration a launch configuration of type
	 * 	<code>ID_JAVA_APPLICATION</code>
	 * @return a result object containing the launched processes and 
	 *   debug target
	 * @exception CoreException if launching fails
	 */
	public VMRunnerResult run(ILaunchConfiguration configuration) throws CoreException;
	
}