package org.eclipse.jdt.launching;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
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
public abstract class AbstractJavaLaunchConfigurationDelegate implements ILaunchConfigurationDelegate, IDebugEventSetListener {
	
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
	 * launch configuration - i.e. that its home location
	 * is specified and exists, and returns the VM install.
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
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
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
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
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
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
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
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
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
				IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
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

}

