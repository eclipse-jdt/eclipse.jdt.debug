package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
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
public abstract class AbstractJavaLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
	
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
	protected IVMInstall getVMInstall(ILaunchConfiguration configuration) throws CoreException {
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
	protected String getVMInstallName(ILaunchConfiguration configuration) throws CoreException {
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
	protected IVMInstallType getVMInstallType(ILaunchConfiguration configuration) throws CoreException {
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
	protected String getVMInstallTypeId(ILaunchConfiguration configuration) throws CoreException {
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
	protected IVMInstall verifyVMInstall(ILaunchConfiguration configuration) throws CoreException {
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
	protected String getVMConnectorId(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR, (String)null);
	}
		
	/**
	 * Returns entries that should appear on the bootstrap portion
	 * of the classpath as specified by the given launch
	 * configuration, as an array of resolved strings. The returned array
	 * is empty if no bootpath is specified, or if all entries are standard
	 * (i.e. appear by default).
	 * 
	 * @param configuration launch configuration
	 * @return the bootpath specified by the given 
	 *  launch configuration, possibly an empty array
	 * @exception CoreException if unable to retrieve the attribute
	 */	
	protected String[] getBootpath(ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		List bootEntries = new ArrayList(entries.length);
		boolean allStandard = true;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES) {
				bootEntries.add(entries[i].getLocation());
				allStandard = allStandard && entries[i].getClasspathProperty() == IRuntimeClasspathEntry.STANDARD_CLASSES;
			}
		}
		if (allStandard) {
			return new String[0];
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
	protected String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		List userEntries = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				userEntries.add(entries[i].getLocation());
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
	protected IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
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
	protected String getJavaProjectName(ILaunchConfiguration configuration) throws CoreException {
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
	protected String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
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
	protected String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
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
	protected String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
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
	protected Map getVMSpecificAttributesMap(ILaunchConfiguration configuration) throws CoreException {
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
	protected File getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
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
	protected IPath getWorkingDirectoryPath(ILaunchConfiguration configuration) throws CoreException {
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
	protected IJavaProject verifyJavaProject(ILaunchConfiguration configuration) throws CoreException {
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
	protected String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException {
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
	protected File verifyWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
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
	protected boolean isAllowTerminate(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
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

}

