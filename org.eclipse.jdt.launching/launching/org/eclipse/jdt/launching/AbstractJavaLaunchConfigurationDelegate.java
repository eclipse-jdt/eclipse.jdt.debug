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


import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

/**
 * Abstract implementation of a Java launch configuration delegate.
 * Provides convenience methods for accessing and verifying launch
 * configuration attributes.
 * <p>
 * Clients implementing Java launch configuration delegates should
 * subclass this class.
 * </p>
 * @since 2.0
 */
public abstract class AbstractJavaLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements IDebugEventSetListener {
	
	protected static final IStatus complileErrorPromptStatus = new Status(IStatus.INFO, "org.eclipse.jdt.debug", 202, "", null); //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * The project containing the class file being launched
	 */
	private IProject project;
	/**
	 * A list of prequisite projects ordered by their build order.
	 */
	private List orderedProjects;
	
	
	/**
	 * Convenience method to get the launch manager.
	 * 
	 * @return the launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 * @throws CoreException the "abort" core exception
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), code, message, exception));
	}	
	
	/**
	 * Returns the VM install specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public IVMInstall getVMInstall(ILaunchConfiguration configuration) throws CoreException {
		return JavaRuntime.computeVMInstall(configuration);
	}

	/**
	 * Returns the VM install name specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install name specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public String getVMInstallName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
	}
	
	/**
	 * Returns the VM install type specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install type specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public IVMInstallType getVMInstallType(ILaunchConfiguration configuration) throws CoreException {
		String id = getVMInstallTypeId(configuration);
		if (id != null) {
			IVMInstallType type = JavaRuntime.getVMInstallType(id);
			if (type != null) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Returns the VM install type identifier specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install type identifier specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public String getVMInstallTypeId(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
	}

	/**
	 * Verifies the VM install specified by the given 
	 * launch configuration exists and returns the VM install.
	 * 
	 * @param configuration launch configuration
	 * @return the VM install specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute,
	 * 	the attribute is unspecified, or if the home location is
	 *  unspecified or does not exist
	 */	
	public IVMInstall verifyVMInstall(ILaunchConfiguration configuration) throws CoreException {
		IVMInstall vm = getVMInstall(configuration);
		if (vm == null) {
			abort(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.The_specified_JRE_installation_does_not_exist_4"), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		File location = vm.getInstallLocation();
		if (location == null) {
			abort(MessageFormat.format(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.JRE_home_directory_not_specified_for_{0}_5"), new String[]{vm.getName()}), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); //$NON-NLS-1$
		}
		if (!location.exists()) {
			abort(MessageFormat.format(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.JRE_home_directory_for_{0}_does_not_exist__{1}_6"), new String[]{vm.getName(), location.getAbsolutePath()}), null, IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); //$NON-NLS-1$
		}
				
		return vm;
	}	

	/**
	 * Returns the VM connector identifier specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the VM connector identifier specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public String getVMConnectorId(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, (String)null);
	}
		
	/**
	 * Returns entries that should appear on the bootstrap portion
	 * of the classpath as specified by the given launch
	 * configuration, as an array of resolved strings. The returned array
	 * is <code>null</code> if all entries are standard (i.e. appear by
	 * default), or empty to represent an empty bootpath.
	 * 
	 * @param configuration launch configuration
	 * @return the bootpath specified by the given 
	 *  launch configuration. An empty bootpath is specfied by
	 *  an empty array, and <code>null</code> represents a default
	 * 	boothpath.
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	public String[] getBootpath(ILaunchConfiguration configuration) throws CoreException {
		String[][] paths = getBootpathExt(configuration);
		String[] pre = paths[0];
		String[] main = paths[1];
		String[] app = paths[2];
		if (pre == null && main == null && app == null) {
			// default
			return null;
		}		
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		List bootEntries = new ArrayList(entries.length);
		boolean empty = true;
		boolean allStandard = true;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entries[i].getLocation();
				if (location != null) {
					empty = false;
					bootEntries.add(location);
					allStandard = allStandard && entries[i].getClasspathProperty() == IRuntimeClasspathEntry.STANDARD_CLASSES;
				}
			}
		}
		if (empty) {
			return new String[0];
		} else if (allStandard) {
			return null;
		} else {
			return (String[])bootEntries.toArray(new String[bootEntries.size()]);		
		}
	}
	
	/**
	 * Returns three sets of entries which represent the boot classpath specified in the
	 * launch configuration, as an array of three arrays of resolved strings.
	 * The first array represents the classpath that should be prepended to the
	 * boot classpath.
	 * The second array represents the main part of the boot classpath - <code>null</code>
	 * represents the default bootclasspath.
	 * The third array represents the classpath that should be appended to the
	 * boot classpath.
	 * 
	 * @param configuration launch configuration
	 * @return a description of the boot classpath specified by the given launch
	 *  configuration.
	 * @exception CoreException if unable to retrieve the attribute
	 * @since 3.0
	 */
	public String[][] getBootpathExt(ILaunchConfiguration configuration) throws CoreException {
		String [][] bootpathInfo= new String[3][];
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		List bootEntriesPrepend= new ArrayList();
		int index= 0;
		boolean jreContainerFound= false;
		while (!jreContainerFound && index < entries.length) {
			IRuntimeClasspathEntry entry= entries[index++];
			if (entry.getClasspathProperty() == IRuntimeClasspathEntry.BOOTSTRAP_CLASSES || entry.getClasspathProperty() == IRuntimeClasspathEntry.STANDARD_CLASSES) {
				int entryKind= entry.getClasspathEntry().getEntryKind();
				String segment0= entry.getPath().segment(0);
				if (entryKind == IClasspathEntry.CPE_CONTAINER && JavaRuntime.JRE_CONTAINER.equals(segment0)
						|| entryKind == IClasspathEntry.CPE_VARIABLE && JavaRuntime.JRELIB_VARIABLE.equals(segment0)) {
					jreContainerFound= true;
				} else {
					bootEntriesPrepend.add(entry);
				}
			}
		}
		IRuntimeClasspathEntry[] bootEntriesPrep= JavaRuntime.resolveRuntimeClasspath((IRuntimeClasspathEntry[])bootEntriesPrepend.toArray(new IRuntimeClasspathEntry[bootEntriesPrepend.size()]), configuration);
		String[] entriesPrep= null;
		if (bootEntriesPrep.length > 0) {
			entriesPrep= new String[bootEntriesPrep.length];
			for (int i= 0; i < bootEntriesPrep.length; i++) {
				entriesPrep[i]= bootEntriesPrep[i].getLocation();
			}
		}
		if (jreContainerFound) {
			List bootEntriesAppend= new ArrayList();
			for (; index < entries.length; index ++) {
				IRuntimeClasspathEntry entry= entries[index];
				if (entry.getClasspathProperty() == IRuntimeClasspathEntry.BOOTSTRAP_CLASSES) {
						bootEntriesAppend.add(entry);
				}
			}
			bootpathInfo[0]= entriesPrep;
			IRuntimeClasspathEntry[] bootEntriesApp= JavaRuntime.resolveRuntimeClasspath((IRuntimeClasspathEntry[])bootEntriesAppend.toArray(new IRuntimeClasspathEntry[bootEntriesAppend.size()]), configuration);
			if (bootEntriesApp.length > 0) {
				bootpathInfo[2]= new String[bootEntriesApp.length];
				for (int i= 0; i < bootEntriesApp.length; i++) {
					bootpathInfo[2][i]= bootEntriesApp[i].getLocation();
				}
			}
			IVMInstall install = getVMInstall(configuration);
			LibraryLocation[] libraryLocations = install.getLibraryLocations();
			if (libraryLocations != null) {
				// non-default JRE libaries - use explicit bootpath only
				String[] bootpath = new String[bootEntriesPrep.length + libraryLocations.length + bootEntriesApp.length];
				if (bootEntriesPrep.length > 0) {
					System.arraycopy(bootpathInfo[0], 0, bootpath, 0, bootEntriesPrep.length);
				}
				int dest = bootEntriesPrep.length;
				for (int i = 0; i < libraryLocations.length; i++) {
					bootpath[dest] = libraryLocations[i].getSystemLibraryPath().toOSString();
					dest++;
				}
				if (bootEntriesApp.length > 0) {
					System.arraycopy(bootpathInfo[2], 0, bootpath, dest, bootEntriesApp.length);
				}
				bootpathInfo[0] = null;
				bootpathInfo[1] = bootpath;
				bootpathInfo[2] = null;
			}
		} else {
			if (entriesPrep == null) {
				bootpathInfo[1]= new String[0];
			} else {
				bootpathInfo[1]= entriesPrep;
			}
		}
		return bootpathInfo;
	}

	/**
	 * Returns the entries that should appear on the user portion of
	 * the classpath as specified by the given launch
	 * configuration, as an array of resolved strings. The returned array
	 * is empty if no classpath is specified.
	 * 
	 * @param configuration launch configuration
	 * @return the classpath specified by the given 
	 *  launch configuration, possibly an empty array
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		List userEntries = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entries[i].getLocation();
				if (location != null) {
					userEntries.add(location);
				}
			}
		}
		return (String[])userEntries.toArray(new String[userEntries.size()]);
	}
		
	/**
	 * Returns the Java project specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getJavaProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null && javaProject.exists()) {
					return javaProject;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the Java project name specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project name specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public String getJavaProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}

	/**
	 * Returns the main type name specified by the given 
	 * launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the main type name specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		String mainType= configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
		if (mainType == null) {
			return null;
		}
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(mainType);
	}

	/**
	 * Returns the program arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if
	 * no program arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the program arguments specified by the given 
	 *  launch configuration, possibly an empty string
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String arguments= configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(arguments);
	}	

	/**
	 * Returns the VM arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if
	 * no VM arguments are specified.
	 * 
	 * @param configuration launch configuration
	 * @return the VM arguments specified by the given 
	 *  launch configuration, possibly an empty string
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		String arguments= configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
		return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(arguments);
	}	

	/**
	 * Returns the Map of VM-specific attributes specified by the given launch configuration,
	 * or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the <code>Map</code> of VM-specific attributes
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public Map getVMSpecificAttributesMap(ILaunchConfiguration configuration) throws CoreException {
		Map map = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
		String[][] paths = getBootpathExt(configuration);
		String[] pre = paths[0];
		String[] boot = paths[1];
		String[] app = paths[2];
		if (pre != null || app != null || boot != null) {
			if (map == null) {
				map = new HashMap(3);
			}
			if (pre != null) {
				map.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND, pre);
			}
			if (app != null) {
				map.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND, app);
			}
			if (boot != null) {
				map.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, boot);
			}
		}
		return map;
	}
	
	/**
	 * Returns the working directory specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public File getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		return verifyWorkingDirectory(configuration);
	}

	/**
	 * Returns the working directory path specified by
	 * the given launch configuration, or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory path specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public IPath getWorkingDirectoryPath(ILaunchConfiguration configuration) throws CoreException {
		String path = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		if (path != null) {
			path= VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(path);
			return new Path(path);
		}
		return null;
	}

	/**
	 * Verifies a Java project is specified by the given 
	 * launch configuration, and returns the Java project.
	 * 
	 * @param configuration launch configuration
	 * @return the Java project specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute
	 * 	or the attribute is unspecified
	 */
	public IJavaProject verifyJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String name = getJavaProjectName(configuration);
		if (name == null) {
			abort(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.Java_project_not_specified_9"), null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT); //$NON-NLS-1$
		}
		IJavaProject project = getJavaProject(configuration);
		if (project == null) {
			abort(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.Project_does_not_exist_or_is_not_a_Java_project_10"), null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT); //$NON-NLS-1$
		}
		return project;
	}

	/**
	 * Verifies a main type name is specified by the given 
	 * launch configuration, and returns the main type name.
	 * 
	 * @param configuration launch configuration
	 * @return the main type name specified by the given 
	 *  launch configuration
	 * @exception CoreException if unable to retrieve the attribute
	 * 	or the attribute is unspecified
	 */
	public String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		String name = getMainTypeName(configuration);
		if (name == null) {
			abort(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.Main_type_not_specified_11"), null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		return name;
	}

	/**
	 * Verifies the working directory specified by the given 
	 * launch configuration exists, and returns the working
	 * directory, or <code>null</code> if none is specified.
	 * 
	 * @param configuration launch configuration
	 * @return the working directory specified by the given 
	 *  launch configuration, or <code>null</code> if none
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	public File verifyWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		IPath path = getWorkingDirectoryPath(configuration);
		if (path == null) {
			// default working dir is the project if this config has a project
			IJavaProject jp = getJavaProject(configuration);
			if (jp != null) {
				IProject p = jp.getProject();
				return p.getLocation().toFile();
			}
		} else {
			if (path.isAbsolute()) {
				File dir = new File(path.toOSString());
				if (dir.isDirectory()) {
					return dir;
				} else {
					abort(MessageFormat.format(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.Working_directory_does_not_exist__{0}_12"), new String[] {path.toString()}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); //$NON-NLS-1$
				}
			} else {
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
				if (res instanceof IContainer && res.exists()) {
					return res.getLocation().toFile();
				} else {
					abort(MessageFormat.format(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.Working_directory_does_not_exist__{0}_12"), new String[] {path.toString()}), null, IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); //$NON-NLS-1$
				}
			}
		}
		return null;		
	}	
				
	/**
	 * Returns whether the given launch configuration
	 * specifies that termination is allowed.
	 * 
	 * @param configuration launch configuration
	 * @return whether termination is allowed
	 * @exception CoreException if unable to retrieve the attribute
	 */
	public boolean isAllowTerminate(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
	}	
	
	/**
	 * Returns whether the given launch configuration
	 * specifies that execution should suspend on entry of the
	 * main method.
	 * 
	 * @param configuration launch configuration
	 * @return whether execution should suspend in main
	 * @exception CoreException if unable to retrieve the attribute
	 * @since 2.1
	 */
	public boolean isStopInMain(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
	}						
	
	/**
	 * Assigns a default source locator to the given launch if a source
	 * locator has not yet been assigned to it, and the associated launch
	 * configuration does not specify a source locator.
	 * 
	 * @param launch launch object
	 * @param configuration configuration being launched
	 * @exception CoreException if unable to set the source locator
	 */
	protected void setDefaultSourceLocator(ILaunch launch, ILaunchConfiguration configuration) throws CoreException {
		//  set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (id == null) {
				IJavaProject javaProject = getJavaProject(configuration);
				if (javaProject != null) {
					ISourceLocator sourceLocator = new JavaSourceLocator(javaProject);
					launch.setSourceLocator(sourceLocator);					
				}
			}
		}
	}
	
	/**
	 * Determines if the given launch configuration specifies the
	 * "stop-in-main" attribute, and sets up an event listener to
	 * handle the option if required.
	 * 
	 * @param configuration configuration being launched
	 * @exception CoreException if unable to access the attribute
	 * @since 2.1
	 */
	protected void prepareStopInMain(ILaunchConfiguration configuration) throws CoreException {
		if (isStopInMain(configuration)) {
			// This listener does not remove itself from the debug plug-in
			// as an event listener (there is no dispose notification for
			// launch delegates). However, since there is only one delegate
			// instantiated per config type, this is tolerable.
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
	}
	
	/**
	 * Handles the "stop-in-main" option.
	 * 
	 * @param events the debug events.
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.CREATE && event.getSource() instanceof IJavaDebugTarget) {
				IJavaDebugTarget target = (IJavaDebugTarget)event.getSource();
				ILaunch launch = target.getLaunch();
				if (launch != null) {
					ILaunchConfiguration configuration = launch.getLaunchConfiguration();
					if (configuration != null) {
						try {
							if (isStopInMain(configuration)) {
								String mainType = getMainTypeName(configuration);
								if (mainType != null) {
									Map map = new HashMap();
									map.put(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN);
									IJavaMethodBreakpoint bp = JDIDebugModel.createMethodBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), mainType, "main",  //$NON-NLS-1$
										"([Ljava/lang/String;)V", true, false, false, -1, -1, -1, 1, false, map); //$NON-NLS-1$
									bp.setPersisted(false);
									target.breakpointAdded(bp);
									DebugPlugin.getDefault().removeDebugEventListener(this);
								}
							}
						} catch (CoreException e) {
							LaunchingPlugin.log(e);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Recursively creates a set of projects referenced by the current project
	 * @param project The current project
	 * @param referencedProjSet A set of referenced projects
	 * @throws CoreException if an error occurs while getting referenced projects from the current project
	 */
	private void getReferencedProjectSet(IProject project, HashSet referencedProjSet) throws CoreException{
		IProject[] projects = project.getReferencedProjects();
		for (int i = 0; i < projects.length; i++) {
			if (!referencedProjSet.contains(projects[i])) {
				referencedProjSet.add(projects[i]);
				getReferencedProjectSet(projects[i], referencedProjSet);
			}
		}
		
	}
	
	/**
	 * creates a list of project ordered by their build order from an unordered list of projects.
	 * @param resourceCollection The list of projects to sort.
	 * @return A new list of projects, ordered by build order.
	 */
	private List getBuildOrder(List resourceCollection) {
		String[] orderedNames = ResourcesPlugin.getWorkspace().getDescription().getBuildOrder();
		if (orderedNames != null) {
			List orderedProjects = new ArrayList(resourceCollection.size());
			//Projects may not be in the build order but should be built if selected
			List unorderedProjects = new ArrayList(resourceCollection.size());
			unorderedProjects.addAll(resourceCollection);
		
			for (int i = 0; i < orderedNames.length; i++) {
				String projectName = orderedNames[i];
				for (int j = 0; j < resourceCollection.size(); j++) {
					IProject project = (IProject) resourceCollection.get(j);
					if (project.getName().equals(projectName)) {
						orderedProjects.add(project);
						unorderedProjects.remove(project);
						break;
					}
				}
			}
			//Add anything not specified before we return
			orderedProjects.addAll(unorderedProjects);
			return orderedProjects;
		}

		// Try the project prerequisite order then
		IProject[] projects = new IProject[resourceCollection.size()];
		projects = (IProject[]) resourceCollection.toArray(projects);
		IWorkspace.ProjectOrder po = ResourcesPlugin.getWorkspace().computeProjectOrder(projects);
		ArrayList orderedProjects = new ArrayList();
		orderedProjects.addAll(Arrays.asList(po.projects));
		return orderedProjects;		
	}
	
	/**
	 * Builds the current project and all of it's prerequisite projects if necessary. Respects 
	 * specified build order if any exists.
	 * 
	 * @param configuration the configuration being launched
	 * @param mode the mode the configuration is being launched in
	 * @param monitor progress monitor
	 * @return whether the debug platform should perform an incremental workspace
	 *  build before the launch
	 * @throws CoreException if an exception occurrs while building
	 */
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {

		monitor.subTask(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.20")); //$NON-NLS-1$
		IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
		project = javaProject.getProject();
		HashSet projectSet = new HashSet();
		getReferencedProjectSet(project, projectSet);
		orderedProjects = getBuildOrder(new ArrayList(projectSet));
		
		if (orderedProjects != null) {
			monitor.beginTask(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.22"), orderedProjects.size() + 1); //$NON-NLS-1$
			
			for (Iterator i = orderedProjects.iterator(); i.hasNext(); ) {
				IProject proj = (IProject)i.next();
				monitor.subTask(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.23") + proj.getName()); //$NON-NLS-1$
				proj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
			}
			
			monitor.subTask(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.24") + project.getName()); //$NON-NLS-1$
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		}
		monitor.done();
		return false; //don't build. I already did it or I threw an exception. 
	}
	

	
	/**
	 * Searches for compile errors in the current project and any of its prerequisite
	 * projects. If any compile errors, give the user a chance to abort the launch and correct
	 * the errors.
	 * 
	 * @param configuration
	 * @param mode
	 * @param monitor
	 * @return whether the launch should proceed
	 * @throws CoreException if an exception occurs while checking for compile errors.
	 */
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		try {
			boolean continueLaunch = true;
			if (orderedProjects != null) {
				monitor.beginTask(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.25"), orderedProjects.size() + 1); //$NON-NLS-1$
				
				boolean compileErrorsInProjs = false;
				
				//check prerequisite projects for compile errors.
				for(Iterator i = orderedProjects.iterator(); i.hasNext(); ) {
					IProject proj = (IProject)i.next();
					monitor.subTask(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.26") + proj.getName()); //$NON-NLS-1$
					compileErrorsInProjs = existsErrors(proj);
					if (compileErrorsInProjs) {
						break;
					}
				}
				
				//check current project, if prerequite projects were ok
				if (!compileErrorsInProjs) {
					monitor.subTask(LaunchingMessages.getString("AbstractJavaLaunchConfigurationDelegate.27") + project.getName()); //$NON-NLS-1$
					compileErrorsInProjs = existsErrors(project);
				}
				
				//if compile errors exist, ask the user before continuing.
				if (compileErrorsInProjs) {
					IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
					if (prompter != null) {
						continueLaunch = ((Boolean)prompter.handleStatus(complileErrorPromptStatus, null)).booleanValue();
					}
				}
			}
			return continueLaunch;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Searches for compile errors in the specified project
	 * @param proj The project to search
	 * @return true if compile errors exist, otherwise false
	 */
	private boolean existsErrors(IProject proj) throws CoreException {
		IMarker[] markers = proj.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		
		if (markers.length > 0) {
			for (int j = 0; j < markers.length; j++) {
				if (((Integer)markers[j].getAttribute(IMarker.SEVERITY)).intValue() == IMarker.SEVERITY_ERROR) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	

}

