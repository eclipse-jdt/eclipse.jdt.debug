package org.eclipse.jdt.launching;

import org.eclipse.jdt.internal.launching.LaunchingPlugin;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Constant definitions for Java launch configurations.
 * 
 * @since 2.0
 */
public interface IJavaLaunchConfigurationConstants {

	/**
	 * Identifier for a Java Application launch configuration
	 * (value <code>org.eclipse.jdt.launching.localJavaApplication"</code>).
	 */
	public static final String ID_JAVA_APPLICATION = LaunchingPlugin.getUniqueIdentifier() + ".localJavaApplication"; //$NON-NLS-1$
	
	/**
	 * Identifier for a Remote Java Application launch configuration
	 * (value <code>org.eclipse.jdt.launching.remoteJavaApplication"</code>).
	 */
	public static final String ID_REMOTE_JAVA_APPLICATION = LaunchingPlugin.getUniqueIdentifier() + ".remoteJavaApplication"; //$NON-NLS-1$	

	/**
	 * Identifier for the standard socket attaching VM connector
	 * (value <code>org.eclipse.jdt.launching.socketAttachConnector"</code>).
	 */
	public static final String ID_SOCKET_ATTACH_VM_CONNECTOR = LaunchingPlugin.getUniqueIdentifier() + ".socketAttachConnector"; //$NON-NLS-1$	
			
	/**
	 * Name of project containing the main type.
	 */
	public static final String ATTR_PROJECT_NAME = LaunchingPlugin.getUniqueIdentifier() + ".PROJECT_ATTR"; //$NON-NLS-1$
	
	/**
	 * Main type launch configuration attribute name.
	 * The fully qualified name of the <code>IType</code> to launch.
	 */
	public static final String ATTR_MAIN_TYPE_NAME = LaunchingPlugin.getUniqueIdentifier() + ".MAIN_TYPE";	 //$NON-NLS-1$
	
	/**
	 * Program arguments launch configuration attribute name.
	 * The program arguments for a Java application are stored
	 * in a launch configuration with this key. Program
	 * arguments are stored as a raw string.
	 */
	public static final String ATTR_PROGRAM_ARGUMENTS = LaunchingPlugin.getUniqueIdentifier() + ".PROGRAM_ARGUMENTS"; //$NON-NLS-1$
	
	/**
	 * VM arguments launch configuration attribute name.
	 * The VM arguments for a Java application are stored
	 * in a launch configuration with this key. VM
	 * arguments are stored as a raw string.
	 */
	public static final String ATTR_VM_ARGUMENTS = LaunchingPlugin.getUniqueIdentifier() + ".VM_ARGUMENTS";	 //$NON-NLS-1$
	
	/**
	 * Working directory attribute name. The working directory
	 * to be used by the VM is stored with this key. The value
	 * is a string specifying an path to the working directory to use.
	 * When unspecified, the working directory is inherited from the current process.
	 * When specified as an absolute path, the path represents a path in the local
	 * file system. When specified as a full path, the path represents a workspace
	 * relative path.
	 */
	public static final String ATTR_WORKING_DIRECTORY = LaunchingPlugin.getUniqueIdentifier() + ".WORKING_DIRECTORY";	 //$NON-NLS-1$
	
	/**
	 * VM install launch configuration attribute name.
	 * The <code>id</code> of an <code>IVMInstall</code>
	 * identifying a VM to use for a launch.
	 * 
	 * @deprecated use <code>ATTR_VM_INSTALL_NAME</code>
	 */
	public static final String ATTR_VM_INSTALL = LaunchingPlugin.getUniqueIdentifier() + ".VM_INSTALL_ID"; //$NON-NLS-1$

	/**
	 * VM install launch configuration attribute name.
	 * The name of an <code>IVMInstall</code>
	 * identifying a VM to use for a launch.
	 */
	public static final String ATTR_VM_INSTALL_NAME = LaunchingPlugin.getUniqueIdentifier() + ".VM_INSTALL_NAME"; //$NON-NLS-1$
		
	/**
	 * VM install type launch configuration attribute name.
	 * The <code>id</code> of an <code>IVMInstallType</code>
	 * identifying a type of VM to use for a launch.
	 */
	public static final String ATTR_VM_INSTALL_TYPE = LaunchingPlugin.getUniqueIdentifier() + ".VM_INSTALL_TYPE_ID"; //$NON-NLS-1$
	
	/**
	 * A Map attribute for launch configurations containing attributes specific
	 * to a particular VM install type.
	 */
	public static final String ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP = LaunchingPlugin.getUniqueIdentifier() + "VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP"; //$NON-NLS-1$
	
	/**
	 * VM connector launch configuration attribute name.
	 * The <code>id</code> of an <code>IVMConnector</code>
	 * identifying a connector to use for a remote attach.
	 */
	public static final String ATTR_VM_CONNECTOR= LaunchingPlugin.getUniqueIdentifier() + ".VM_CONNECTOR_ID"; //$NON-NLS-1$
	
	/**
	 * Bootpath launch configuration attribute name.
	 * The bootpath for a Java application is stored
	 * in a launch configuration with this key. A bootpath
	 * is stored as a raw string.
	 * 
	 * @deprecated use ATTR_CLASSPATH - contains both use classes and bootstrap classes
	 */
	public static final String ATTR_BOOTPATH = LaunchingPlugin.getUniqueIdentifier() + ".BOOTPATH";	 //$NON-NLS-1$
	
	/**
	 * Classpath launch configuration attribute name.
	 * If this attribute is present on a launch configuration, its value becomes
	 * the sole classpath for the launch.  If not present, a default class runtime
	 * classpath is used. The attribute value is a list of strings which are mementos
	 * for runtime class path entries.
	 */
	public static final String ATTR_CLASSPATH = LaunchingPlugin.getUniqueIdentifier() + ".CLASSPATH";	 //$NON-NLS-1$
	
	/**
	 * Default classpath launch configuration boolean attribute name.
	 * If this attribute is present on a launch configuration, then the default
	 * class path is computed at runtime.  If this attribute is set, the contents
	 * of <code>ATTR_CLASSPATH</code> and <code>ATTR_BOOTPATH</code> are ignored.
	 */
	public static final String ATTR_DEFAULT_CLASSPATH = LaunchingPlugin.getUniqueIdentifier() + ".DEFAULT_CLASSPATH"; //$NON-NLS-1$

	/**
	 * Classpath launch configuration attribute name.
	 * If this attribute is present on a launch configuration, its value becomes
	 * the sole source lookup path for the launch. Its value is a list of
	 * strings which are mementos for associated runtime classpath entries.
	 */
	public static final String ATTR_SOURCE_PATH = LaunchingPlugin.getUniqueIdentifier() + ".SOURCE_PATH";	 //$NON-NLS-1$
		
	/**
	 * Default source lookup path launch configuration boolean attribute name.
	 * If this attribute is present on a launch configuration, then the default
	 * source lookup path is computed at runtime.  If this attribute is set, the contents
	 * of <code>ATTR_SOURCE_PATH</code> is ignored.
	 */
	public static final String ATTR_DEFAULT_SOURCE_PATH = LaunchingPlugin.getUniqueIdentifier() + ".DEFAULT_SOURCE_PATH"; //$NON-NLS-1$
			
	/**
	 * Host name launch configuration attribute name.
	 * This attribute is used for attach launching.
	 */
	public static final String ATTR_HOSTNAME = LaunchingPlugin.getUniqueIdentifier() + ".HOSTNAME";	 //$NON-NLS-1$

	/**
	 * Allow termination launch configuration attribute name.
	 * This attribute is used for attach launching.
	 */
	public static final String ATTR_ALLOW_TERMINATE = LaunchingPlugin.getUniqueIdentifier() + ".ALLOW_TERMINATE";	 //$NON-NLS-1$

	/**
	 * Port # launch configuration attribute name.
	 * This attribute is used for attach launching.
	 */
	public static final String ATTR_PORT_NUMBER = LaunchingPlugin.getUniqueIdentifier() + ".PORT";	 //$NON-NLS-1$

	/**
	 * Status code indicating a launch configuration does not
	 * specify a project that contains the main class to launch.
	 */
	public static final int ERR_UNSPECIFIED_PROJECT = 100;	
		
	/**
	 * Status code indicating a launch configuration does not
	 * specify a main class to launch.
	 */
	public static final int ERR_UNSPECIFIED_MAIN_TYPE = 101;	
		
	/**
	 * Status code indicating a launch configuration does not
	 * specify a VM Install Type.
	 */
	public static final int ERR_UNSPECIFIED_VM_INSTALL_TYPE = 102;
	
	/**
	 * Status code indicating a launch configuration does not
	 * specify a VM Install
	 */
	public static final int ERR_UNSPECIFIED_VM_INSTALL = 103;

	/**
	 * Status code indicating a launch configuration's VM install
	 * type could not be found.
	 */
	public static final int ERR_VM_INSTALL_TYPE_DOES_NOT_EXIST = 104;
		
	/**
	 * Status code indicating a launch configuration's VM install
	 * could not be found.
	 */
	public static final int ERR_VM_INSTALL_DOES_NOT_EXIST = 105;
	
	/**
	 * Status code indicating a VM runner could not be located
	 * for the VM install specified by a launch configuration.
	 */
	public static final int ERR_VM_RUNNER_DOES_NOT_EXIST = 106;	
	
	/**
	 * Status code indicating the project associated with
	 * a launch configuration is not a Java project.
	 */
	public static final int ERR_NOT_A_JAVA_PROJECT = 107;	
	
	/**
	 * Status code indicating the specified working directory
	 * does not exist.
	 */
	public static final int ERR_WORKING_DIRECTORY_DOES_NOT_EXIST = 108;	
		
	/**
	 * Status code indicating a launch configuration does not
	 * specify a host name value
	 */
	public static final int ERR_UNSPECIFIED_HOSTNAME = 109;

	/**
	 * Status code indicating a launch configuration has
	 * specified an invalid host name attribute
	 */
	public static final int ERR_INVALID_HOSTNAME = 110;

	/**
	 * Status code indicating a launch configuration does not
	 * specify a port number value
	 */
	public static final int ERR_UNSPECIFIED_PORT = 111;

	/**
	 * Status code indicating a launch configuration has
	 * specified an invalid port number attribute
	 */
	public static final int ERR_INVALID_PORT = 112;

	/**
	 * Status code indicating an attempt to connect to a remote VM
	 * has failed.
	 */
	public static final int ERR_REMOTE_VM_CONNECTION_FAILED = 113;

	/**
	 * Status code indicating that the shared memory attach connector
	 * could not be found.
	 */
	public static final int ERR_SHARED_MEMORY_CONNECTOR_UNAVAILABLE = 114;
	
	/**
	 * Status code indicating that the Eclipse runtime does not support
	 * launching a program with a working directory. This feature is only
	 * available if Eclipse is run on a 1.3 runtime or higher.
	 * <p>
	 * A status handler may be registered for this error condition,
	 * and should return a Boolean indicating whether the program
	 * should be relaunched with the default working directory.
	 * </p>
	 */
	public static final int ERR_WORKING_DIRECTORY_NOT_SUPPORTED = 115;	
	
	/**
	 * Status code indicating that an error occurred launching the VM
	 * in debug mode. The status error message is the text that
	 * the VM wrote to standard error before exiting.
	 */
	public static final int ERR_VM_LAUNCH_ERROR = 116;	
	
	/**
	 * Status code indicating that a timeout has occurred waiting for
	 * the VM to connect with the debugger.
	 * <p>
	 * A status handler may be registered for this error condition,
	 * and should return a Boolean indicating whether the program
	 * should continue waiting for a connection for the associated
	 * timeout period.
	 * </p>
	 */
	public static final int ERR_VM_CONNECT_TIMEOUT = 117;	
	
	/**
	 * Status code indicating that a free socket was not available to
	 * communicate with the VM.
	 */
	public static final int ERR_NO_SOCKET_AVAILABLE = 118;		
	
	/**
	 * Status code indicating that the JDI connector required for a
	 * debug launch was not available.
	 */
	public static final int ERR_CONNECTOR_NOT_AVAILABLE = 119;	
	
	/**
	 * Status code indicating that the debugger failed to connect
	 * to the VM.
	 */
	public static final int ERR_CONNECTION_FAILED = 120;		

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int ERR_INTERNAL_ERROR = 150;			
}
