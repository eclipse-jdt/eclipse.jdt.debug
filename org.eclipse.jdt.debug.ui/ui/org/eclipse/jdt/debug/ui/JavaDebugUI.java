package org.eclipse.jdt.debug.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Defines attributes for Java launch configurations
 */
public class JavaDebugUI {
	
	/**
	 * Plug-in identifier for the Java Debug UI
	 */
	public static final String PLUGIN_ID = "org.eclipse.jdt.debug.ui"; //$NON-NLS-1$

	/**
	 * Identifier for a Java Application launch configuration
	 * (value <code>org.eclipse.jdt.debug.ui.localJavaApplication"</code>).
	 */
	public static final String JAVA_APPLICATION_LAUNCH_CONFIGURATION_ID = "org.eclipse.jdt.debug.ui.localJavaApplication"; //$NON-NLS-1$
		
	/**
	 * Name of project containing the main type.
	 */
	public static final String PROJECT_ATTR = PLUGIN_ID + "PROJECT_ATTR"; //$NON-NLS-1$
	
	/**
	 * Main type launch configuration attribute name.
	 * The fully qualified name of the <code>IType</code> to launch.
	 */
	public static final String MAIN_TYPE_ATTR = PLUGIN_ID + "MAIN_TYPE";	 //$NON-NLS-1$
	
	/**
	 * Program arguments launch configuration attribute name.
	 * The program arguments for a Java application are stored
	 * in a launch configuration with this key. Program
	 * arguments are stored as a raw string.
	 */
	public static final String PROGRAM_ARGUMENTS_ATTR = PLUGIN_ID + "PROGRAM_ARGUMENTS"; //$NON-NLS-1$
	
	/**
	 * VM arguments launch configuration attribute name.
	 * The VM arguments for a Java application are stored
	 * in a launch configuration with this key. VM
	 * arguments are stored as a raw string.
	 */
	public static final String VM_ARGUMENTS_ATTR = PLUGIN_ID + "VM_ARGUMENTS";	 //$NON-NLS-1$
	
	/**
	 * Working directory attribute name. The working directory
	 * to be used by the VM is stored with this key. The value
	 * is a string specifying an absolute path to the working
	 * directory to use. When unspecified, the working directory
	 * is inherited from the current process.
	 */
	public static final String WORKING_DIRECTORY_ATTR = PLUGIN_ID + "WORKING_DIRECTORY";	 //$NON-NLS-1$
	
	/**
	 * VM install launch configuration attribute name.
	 * The <code>id</code> of an <code>IVMInstall</code>
	 * identifying a VM to use for a launch.
	 */
	public static final String VM_INSTALL_ATTR = PLUGIN_ID + "VM_INSTALL_ID"; //$NON-NLS-1$
	
	/**
	 * VM install type launch configuration attribute name.
	 * The <code>id</code> of an <code>IVMInstallType</code>
	 * identifying a type of VM to use for a launch.
	 */
	public static final String VM_INSTALL_TYPE_ATTR = PLUGIN_ID + "VM_INSTALL_TYPE_ID"; //$NON-NLS-1$
	
	/**
	 * Bootpath launch configuration attribute name.
	 * The bootpath for a Java application is stored
	 * in a launch configuration with this key. A bootpath
	 * is stored as a raw string.
	 */
	public static final String BOOTPATH_ATTR = PLUGIN_ID + "BOOTPATH";	 //$NON-NLS-1$
	
	/**
	 * Classpath launch configuration attribute name.
	 * If this attribute is present on a launch configuration, its value becomes
	 * the sole classpath for the launch.  If not present, a default class runtime
	 * classpath is used.
	 */
	public static final String CLASSPATH_ATTR = PLUGIN_ID + "CLASSPATH";	 //$NON-NLS-1$
	
	/**
	 * Environment variables launch configuration attribute name.
	 * This attribute contains name value pairs specifiying environment variables
	 * that will be set in the launched environment.
	 */
	public static final String ENVIRONMENT_VARIABLES_ATTR = PLUGIN_ID + "ENVIRONMENT_VARIABLES";	 //$NON-NLS-1$
	
	/**
	 * Host name launch configuration attribute name.
	 * This attribute is used for attach launching.
	 */
	public static final String HOSTNAME_ATTR = PLUGIN_ID + "HOSTNAME";	 //$NON-NLS-1$

	/**
	 * Allow termination launch configuration attribute name.
	 * This attribute is used for attach launching.
	 */
	public static final String ALLOW_TERMINATE_ATTR = PLUGIN_ID + "ALLOW_TERMINATE";	 //$NON-NLS-1$

	/**
	 * Port # launch configuration attribute name.
	 * This attribute is used for attach launching.
	 */
	public static final String PORT_ATTR = PLUGIN_ID + "PORT";	 //$NON-NLS-1$

	/**
	 * 
	 * Build the workspace launch configuration attribute name.
	 * If true, the workspace is incrementally built before the launch happens.
	 */
	public static final String BUILD_BEFORE_LAUNCH_ATTR = PLUGIN_ID + "BUILD_BEFORE_LAUNCH";	 //$NON-NLS-1$

	/**
	 * Status code indicating a launch configuration does not
	 * specify a project that contains the main class to launch.
	 */
	public static final int UNSPECIFIED_PROJECT = 100;	
		
	/**
	 * Status code indicating a launch configuration does not
	 * specify a main class to launch.
	 */
	public static final int UNSPECIFIED_MAIN_TYPE = 101;	
		
	/**
	 * Status code indicating a launch configuration does not
	 * specify a VM Install Type.
	 */
	public static final int UNSPECIFIED_VM_INSTALL_TYPE = 102;
	
	/**
	 * Status code indicating a launch configuration does not
	 * specify a VM Install
	 */
	public static final int UNSPECIFIED_VM_INSTALL = 103;

	/**
	 * Status code indicating a launch configuration's VM install
	 * type could not be found.
	 */
	public static final int VM_INSTALL_TYPE_DOES_NOT_EXIST = 104;
		
	/**
	 * Status code indicating a launch configuration's VM install
	 * could not be found.
	 */
	public static final int VM_INSTALL_DOES_NOT_EXIST = 105;
	
	/**
	 * Status code indicating a VM runner could not be located
	 * for the VM install specified by a launch configuration.
	 */
	public static final int VM_RUNNER_DOES_NOT_EXIST = 106;	
	
	/**
	 * Status code indicating the project associated with
	 * a launch configuration is not a Java project.
	 */
	public static final int NOT_A_JAVA_PROJECT = 107;	
	
	/**
	 * Status code indicating the specified working directory
	 * does not exist.
	 */
	public static final int WORKING_DIRECTORY_DOES_NOT_EXIST = 108;	
		
	/**
	 * Status code indicating a launch configuration does not
	 * specify a host name value
	 */
	public static final int UNSPECIFIED_HOSTNAME = 109;

	/**
	 * Status code indicating a launch configuration has
	 * specified an invalid host name attribute
	 */
	public static final int INVALID_HOSTNAME = 110;

	/**
	 * Status code indicating a launch configuration does not
	 * specify a port number value
	 */
	public static final int UNSPECIFIED_PORT = 111;

	/**
	 * Status code indicating a launch configuration has
	 * specified an invalid port number attribute
	 */
	public static final int INVALID_PORT = 112;

	/**
	 * Status code indicating an attempt to connect to a remote VM
	 * has failed.
	 */
	public static final int REMOTE_VM_CONNECTION_FAILED = 113;

	/**
	 * Status code indicating that the shared memory attach connector
	 * could not be found.
	 */
	public static final int SHARED_MEMORY_CONNECTOR_UNAVAILABLE = 114;

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 150;			

}

