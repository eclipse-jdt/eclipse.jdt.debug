/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.launching;

 
import java.util.Map;

import org.eclipse.jdt.internal.launching.LaunchingMessages;

/**
 * Holder for various arguments passed to a VM runner.
 * Mandatory parameters are passed in the constructor; optional arguments, via setters.
 * <p>
 * Clients may instantiate this class; it is not intended to be subclassed.
 * </p>
 */
public class VMRunnerConfiguration {
	private String fClassToLaunch;
	private String[] fVMArgs;
	private String[] fProgramArgs;
	private String[] fClassPath;
	private String[] fBootClassPath;
	private String fWorkingDirectory;
	private Map fVMSpecificAttributesMap;
	
	private static final String[] fgEmpty= new String[0];
	
	/**
	 * Creates a new configuration for launching a VM to run the given main class
	 * using the given class path.
	 *
	 * @param classToLaunch The fully qualified name of the class to launch. May not be null.
	 * @param classPath 	The classpath. May not be null.
	 */
	public VMRunnerConfiguration(String classToLaunch, String[] classPath) {
		if (classToLaunch == null) {
			throw new IllegalArgumentException(LaunchingMessages.getString("vmRunnerConfig.assert.classNotNull")); //$NON-NLS-1$
		}
		if (classPath == null) {
			throw new IllegalArgumentException(LaunchingMessages.getString("vmRunnerConfig.assert.classPathNotNull")); //$NON-NLS-1$
		}
		fClassToLaunch= classToLaunch;
		fClassPath= classPath;
	}

	/**
	 * Sets the <code>Map</code> that contains String name/value pairs that represent
	 * VM-specific attributes.
	 * 
	 * @param map the <code>Map</code> of VM-specific attributes.
	 * @since 2.0
	 */
	public void setVMSpecificAttributesMap(Map map) {
		fVMSpecificAttributesMap = map;
	}

	/**
	 * Sets the custom VM arguments. These arguments will be appended to the list of 
	 * VM arguments that a VM runner uses when launching a VM. Typically, these VM arguments
	 * are set by the user.
	 * These arguments will not be interpreted by a VM runner, the client is responsible for
	 * passing arguments compatible with a particular VM runner.
	 *
	 * @param args the list of VM arguments
	 */
	public void setVMArguments(String[] args) {
		if (args == null) {
			throw new IllegalArgumentException(LaunchingMessages.getString("vmRunnerConfig.assert.vmArgsNotNull")); //$NON-NLS-1$
		}
		fVMArgs= args;
	}
	
	/**
	 * Sets the custom program arguments. These arguments will be appended to the list of 
	 * program arguments that a VM runner uses when launching a VM (in general: none). 
	 * Typically, these VM arguments are set by the user.
	 * These arguments will not be interpreted by a VM runner, the client is responsible for
	 * passing arguments compatible with a particular VM runner.
	 *
	 * @param args the list of arguments	
	 */
	public void setProgramArguments(String[] args) {
		if (args == null) {
			throw new IllegalArgumentException(LaunchingMessages.getString("vmRunnerConfig.assert.programArgsNotNull")); //$NON-NLS-1$
		}
		fProgramArgs= args;
	}
	
	/**
	 * Sets the boot classpath. Note that the boot classpath will be passed to the 
	 * VM "as is". This means it has to be complete. Interpretation of the boot class path
	 * is up to the VM runner this object is passed to.
	 *
	 * @param bootClassPath The boot classpath. An emptry array indicates an empty
	 *  bootpath and <code>null</code> indicates a default bootpah.
	 */
	public void setBootClassPath(String[] bootClassPath) {
		fBootClassPath= bootClassPath;
	}
	
	/**
	 * Returns the <code>Map</code> that contains String name/value pairs that represent
	 * VM-specific attributes.
	 * 
	 * @return The <code>Map</code> of VM-specific attributes or <code>null</code>.
	 * @since 2.0
	 */
	public Map getVMSpecificAttributesMap() {
		return fVMSpecificAttributesMap;
	}
	
	/**
	 * Returns the name of the class to launch.
	 *
	 * @return The fully qualified name of the class to launch. Will not be <code>null</code>.
	 */
	public String getClassToLaunch() {
		return fClassToLaunch;
	}
	
	/**
	 * Returns the classpath.
	 *
	 * @return the classpath
	 */
	public String[] getClassPath() {
		return fClassPath;
	}
	
	/**
	 * Returns the boot classpath. An emptry array indicates an empty
	 * bootpath and <code>null</code> indicates a default bootpah.
	 *
	 * @return The boot classpath. An emptry array indicates an empty
	 *  bootpath and <code>null</code> indicates a default bootpah.
	 * @see #setBootClassPath
	 */
	public String[] getBootClassPath() {
		return fBootClassPath;
	}

	/**
	 * Returns the arguments to the VM itself.
	 *
	 * @return The VM arguments. Default is an empty array. Will not be <code>null</code>.
	 * @see #setVMArguments
	 */
	public String[] getVMArguments() {
		if (fVMArgs == null) {
			return fgEmpty;
		}
		return fVMArgs;
	}
	
	/**
	 * Returns the arguments to the Java program.
	 *
	 * @return The Java program arguments. Default is an empty array. Will not be <code>null</code>.
	 * @see #setProgramArguments
	 */
	public String[] getProgramArguments() {
		if (fProgramArgs == null) {
			return fgEmpty;
		}
		return fProgramArgs;
	}
	
	/**
	 * Sets the working directory for a launched VM.
	 * 
	 * @param path the absolute path to the working directory
	 *  to be used by a launched VM, or <code>null</code> if
	 *  the default working directory is to be inherited from the
	 *  current process
	 * @since 2.0
	 */
	public void setWorkingDirectory(String path) {
		fWorkingDirectory = path;
	}
	
	/**
	 * Returns the working directory of a launched VM.
	 * 
	 * @return the absolute path to the working directory
	 *  of a launched VM, or <code>null</code> if the working
	 *  directory is inherited from the current process
	 * @since 2.0
	 */
	public String getWorkingDirectory() {
		return fWorkingDirectory;
	}	
	
}
