
package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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
	 * VM arguments that a IVMRunner uses when launching a VM. Typically, these VM arguments
	 * are set by the user.
	 * These arguments will not be interpreted by the IVMRunner, the client is responsible for
	 * passing arguments compatible with a particular IVMRunner.
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
	 * program arguments that a IVMRunner uses when launching a VM (in general: none). 
	 * Typically, these VM arguments are set by the user.
	 * These arguments will not be interpreted by the IVMRunner, the client is responsible for
	 * passing arguments compatible with a particular IVMRunner.
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
	 * @param bootClassPath The boot classpath. May not be null.
	 */
	public void setBootClassPath(String[] bootClassPath) {
		if (bootClassPath == null) {
			throw new IllegalArgumentException(LaunchingMessages.getString("vmRunnerConfig.assert.bootClassPathNotNull")); //$NON-NLS-1$
		}
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
	 * @return The fully qualified name of the class to launch. Will not be null.
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
	 * Returns the boot classpath.
	 *
	 * @return The boot classpath. Default is an empty array. Will not be null.
	 * @see #setBootClassPath
	 */
	public String[] getBootClassPath() {
		if (fBootClassPath == null) {
			return fgEmpty;
		}
		return fBootClassPath;
	}

	/**
	 * Returns the arguments to the VM itself.
	 *
	 * @return The VM arguments. Default is an empty array. Will not be null.
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
	 * @return The Java program arguments. Default is an empty array. Will not be null.
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
	 *  the working directory is to be inherited from the
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