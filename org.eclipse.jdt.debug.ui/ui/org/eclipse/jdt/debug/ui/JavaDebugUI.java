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
	 * Main type launch configuration attribute name.
	 * The memento of the <code>IType</code> to launch.
	 */
	public static final String MAIN_TYPE_ATTR = "MAIN_TYPE";	 //$NON-NLS-1$
	
	/**
	 * Program arguments launch configuration attribute name.
	 * The program arguments for a Java application are stored
	 * in a launch configuration with this key. Program
	 * arguments are stored as a raw string.
	 */
	public static final String PROGRAM_ARGUMENTS_ATTR = "PROGRAM_ARGUMENTS"; //$NON-NLS-1$
	
	/**
	 * VM arguments launch configuration attribute name.
	 * The VM arguments for a Java application are stored
	 * in a launch configuration with this key. VM
	 * arguments are stored as a raw string.
	 */
	public static final String VM_ARGUMENTS_ATTR = "VM_ARGUMENTS";	 //$NON-NLS-1$
	
	/**
	 * Working directory attribute name. The working directory
	 * to be used by the VM is stored with this key. The value
	 * is a string specifying an absolute path to the working
	 * directory to use. When unspecified, the working directory
	 * is inherited from the current process.
	 */
	public static final String WORKING_DIRECTORY_ATTR = "WORKING_DIRECTORY";	 //$NON-NLS-1$
	
	/**
	 * VM install launch configuration attribute name.
	 * The <code>id</code> of an <code>IVMInstall</code>
	 * identifying a VM to use for a launch.
	 */
	public static final String VM_INSTALL_ATTR = "VM_INSTALL_ID"; //$NON-NLS-1$
	
	/**
	 * VM install type launch configuration attribute name.
	 * The <code>id</code> of an <code>IVMInstallType</code>
	 * identifying a type of VM to use for a launch.
	 */
	public static final String VM_INSTALL_TYPE_ATTR = "VM_INSTALL_TYPE_ID"; //$NON-NLS-1$
	
	/**
	 * Bootpath launch configuration attribute name.
	 * The bootpath for a Java application is stored
	 * in a launch configuration with this key. A bootpath
	 * is stored as a raw string.
	 */
	public static final String BOOTPATH_ATTR = "BOOTPATH";	 //$NON-NLS-1$
	
	/**
	 * Status code indicating a launch configuration does not
	 * specify a main class to launch.
	 */
	public static final int UNSPECIFIED_MAIN_TYPE = 100;	
		
	/**
	 * Status code indicating a launch configuration does not
	 * specify a VM Install Type.
	 */
	public static final int UNSPECIFIED_VM_INSTALL_TYPE = 101;
	
	/**
	 * Status code indicating a launch configuration does not
	 * specify a VM Install
	 */
	public static final int UNSPECIFIED_VM_INSTALL = 102;

	/**
	 * Status code indicating a launch configuration's VM install
	 * could not be found.
	 */
	public static final int VM_INSTALL_TYPE_DOES_NOT_EXIST = 103;
		
	/**
	 * Status code indicating a launch configuration's VM install
	 * could not be found.
	 */
	public static final int VM_INSTALL_DOES_NOT_EXIST = 104;
	
	/**
	 * Status code indicating a VM runner could not be located
	 * for the VM install specified by a lanuch configuration.
	 */
	public static final int VM_RUNNER_DOES_NOT_EXIST = 105;	
	
	/**
	 * Status code indicating the project associated with
	 * a launch configuration is not a Java project.
	 */
	public static final int NOT_A_JAVA_PROJECT = 106;	
	
	/**
	 * Status code indicating the specified working directory
	 * does not exist.
	 */
	public static final int WORKING_DIRECTORY_DOES_NOT_EXIST = 107;	
		
	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 120;			

}

