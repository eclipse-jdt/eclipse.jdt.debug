/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.launching.JRERuntimeClasspathEntryResolver;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.osgi.util.NLS;
/**
 * Abstract implementation of a Java launch configuration delegate. Provides
 * convenience methods for accessing and verifying launch configuration
 * attributes.
 * <p>
 * Clients implementing Java launch configuration delegates should subclass this
 * class.
 * </p>
 * 
 * @since 2.0
 */
public abstract class AbstractJavaLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements IDebugEventSetListener {
	/**
	 * A list of prerequisite projects ordered by their build order.
	 */
	private IProject[] fOrderedProjects;
	/**
	 * Convenience method to get the launch manager.
	 * 
	 * @return the launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 * @throws CoreException
	 *             the "abort" core exception
	 */
	protected void abort(String message, Throwable exception, int code)
			throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin
				.getUniqueIdentifier(), code, message, exception));
	}
	/**
	 * Returns the VM install specified by the given launch configuration, or
	 * <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the VM install specified by the given launch configuration, or
	 *         <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public IVMInstall getVMInstall(ILaunchConfiguration configuration)
			throws CoreException {
		return JavaRuntime.computeVMInstall(configuration);
	}
	/**
	 * Returns the VM install name specified by the given launch configuration,
	 * or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the VM install name specified by the given launch configuration,
	 *         or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	@SuppressWarnings("deprecation")
	public String getVMInstallName(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,
				(String) null);
	}
	/**
	 * Returns the VM install type specified by the given launch configuration,
	 * or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the VM install type specified by the given launch configuration,
	 *         or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public IVMInstallType getVMInstallType(ILaunchConfiguration configuration)
			throws CoreException {
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
	 * Returns the VM install type identifier specified by the given launch
	 * configuration, or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the VM install type identifier specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	@SuppressWarnings("deprecation")
	public String getVMInstallTypeId(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE,
				(String) null);
	}
	/**
	 * Verifies the VM install specified by the given launch configuration
	 * exists and returns the VM install.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the VM install specified by the given launch configuration
	 * @exception CoreException
	 *                if unable to retrieve the attribute, the attribute is
	 *                unspecified, or if the home location is unspecified or
	 *                does not exist
	 */
	public IVMInstall verifyVMInstall(ILaunchConfiguration configuration)
			throws CoreException {
		IVMInstall vm = getVMInstall(configuration);
		if (vm == null) {
			abort(
					LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_The_specified_JRE_installation_does_not_exist_4, 
					null,
					IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); 
		}
		File location = vm.getInstallLocation();
		if (location == null) {
			abort(
					NLS.bind(LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_JRE_home_directory_not_specified_for__0__5, new String[]{vm.getName()}),
					null,
					IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); 
		}
		if (!location.exists()) {
			abort(
					NLS.bind(LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_JRE_home_directory_for__0__does_not_exist___1__6, 
									new String[]{vm.getName(),
											location.getAbsolutePath()}),
					null,
					IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST); 
		}
		return vm;
	}
	/**
	 * Returns the VM connector identifier specified by the given launch
	 * configuration, or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the VM connector identifier specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String getVMConnectorId(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
				(String) null);
	}
	/**
	 * Returns entries that should appear on the bootstrap portion of the
	 * classpath as specified by the given launch configuration, as an array of
	 * resolved strings. The returned array is <code>null</code> if all
	 * entries are standard (i.e. appear by default), or empty to represent an
	 * empty bootpath.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the bootpath specified by the given launch configuration. An
	 *         empty bootpath is specified by an empty array, and
	 *         <code>null</code> represents a default bootpath.
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String[] getBootpath(ILaunchConfiguration configuration)
			throws CoreException {
		String[][] paths = getBootpathExt(configuration);
		String[] pre = paths[0];
		String[] main = paths[1];
		String[] app = paths[2];
		if (pre == null && main == null && app == null) {
			// default
			return null;
		}
		IRuntimeClasspathEntry[] entries = JavaRuntime
				.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		List<String> bootEntries = new ArrayList<String>(entries.length);
		boolean empty = true;
		boolean allStandard = true;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entries[i].getLocation();
				if (location != null) {
					empty = false;
					bootEntries.add(location);
					allStandard = allStandard
							&& entries[i].getClasspathProperty() == IRuntimeClasspathEntry.STANDARD_CLASSES;
				}
			}
		}
		if (empty) {
			return new String[0];
		} else if (allStandard) {
			return null;
		} else {
			return bootEntries
					.toArray(new String[bootEntries.size()]);
		}
	}
	/**
	 * Returns three sets of entries which represent the boot classpath
	 * specified in the launch configuration, as an array of three arrays of
	 * resolved strings. The first array represents the classpath that should be
	 * prepended to the boot classpath. The second array represents the main
	 * part of the boot classpath -<code>null</code> represents the default
	 * bootclasspath. The third array represents the classpath that should be
	 * appended to the boot classpath.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return a description of the boot classpath specified by the given launch
	 *         configuration.
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 * @since 3.0
	 */
	public String[][] getBootpathExt(ILaunchConfiguration configuration)
			throws CoreException {
		String[][] bootpathInfo = new String[3][];
		IRuntimeClasspathEntry[] entries = JavaRuntime
				.computeUnresolvedRuntimeClasspath(configuration);
		List<IRuntimeClasspathEntry> bootEntriesPrepend = new ArrayList<IRuntimeClasspathEntry>();
		int index = 0;
		IRuntimeClasspathEntry jreEntry = null;
		while (jreEntry == null && index < entries.length) {
			IRuntimeClasspathEntry entry = entries[index++];
			if (entry.getClasspathProperty() == IRuntimeClasspathEntry.BOOTSTRAP_CLASSES
					|| entry.getClasspathProperty() == IRuntimeClasspathEntry.STANDARD_CLASSES) {
				if (JavaRuntime.isVMInstallReference(entry)) {
					jreEntry = entry;
				} else {
					bootEntriesPrepend.add(entry);
				}
			}
		}
		IRuntimeClasspathEntry[] bootEntriesPrep = JavaRuntime
				.resolveRuntimeClasspath(
						bootEntriesPrepend
								.toArray(new IRuntimeClasspathEntry[bootEntriesPrepend
										.size()]), configuration);
		String[] entriesPrep = null;
		if (bootEntriesPrep.length > 0) {
			entriesPrep = new String[bootEntriesPrep.length];
			for (int i = 0; i < bootEntriesPrep.length; i++) {
				entriesPrep[i] = bootEntriesPrep[i].getLocation();
			}
		}
		if (jreEntry != null) {
			List<IRuntimeClasspathEntry> bootEntriesAppend = new ArrayList<IRuntimeClasspathEntry>();
			for (; index < entries.length; index++) {
				IRuntimeClasspathEntry entry = entries[index];
				if (entry.getClasspathProperty() == IRuntimeClasspathEntry.BOOTSTRAP_CLASSES) {
					bootEntriesAppend.add(entry);
				}
			}
			bootpathInfo[0] = entriesPrep;
			IRuntimeClasspathEntry[] bootEntriesApp = JavaRuntime
					.resolveRuntimeClasspath(
							bootEntriesAppend
									.toArray(new IRuntimeClasspathEntry[bootEntriesAppend
											.size()]), configuration);
			if (bootEntriesApp.length > 0) {
				bootpathInfo[2] = new String[bootEntriesApp.length];
				for (int i = 0; i < bootEntriesApp.length; i++) {
					bootpathInfo[2][i] = bootEntriesApp[i].getLocation();
				}
			}
			IVMInstall install = getVMInstall(configuration);
			LibraryLocation[] libraryLocations = install.getLibraryLocations();
			if (libraryLocations != null) {
				// determine if explicit bootpath should be used
				// TODO: this test does not tell us if the bootpath entries are different (could still be
				// the same, as a non-bootpath entry on the JRE may have been removed/added)
				// We really need a way to ask a VM type for its default bootpath library locations and
				// compare that to the resolved entries for the "jreEntry" to see if they 
				// are different (requires explicit bootpath)
				if (!JRERuntimeClasspathEntryResolver.isSameArchives(libraryLocations, install.getVMInstallType().getDefaultLibraryLocations(install.getInstallLocation()))) {
					// resolve bootpath entries in JRE entry
					IRuntimeClasspathEntry[] bootEntries = null; 
					if (jreEntry.getType() == IRuntimeClasspathEntry.CONTAINER) {
						IRuntimeClasspathEntry bootEntry = JavaRuntime.newRuntimeContainerClasspathEntry(
								jreEntry.getPath(),
								IRuntimeClasspathEntry.BOOTSTRAP_CLASSES,
								getJavaProject(configuration));
						bootEntries = JavaRuntime.resolveRuntimeClasspathEntry(bootEntry, configuration);
					} else {
						bootEntries = JavaRuntime.resolveRuntimeClasspathEntry(jreEntry, configuration);
					}
					
					// non-default JRE libraries - use explicit bootpath only
					String[] bootpath = new String[bootEntriesPrep.length
							+ bootEntries.length + bootEntriesApp.length];
					if (bootEntriesPrep.length > 0) {
						System.arraycopy(bootpathInfo[0], 0, bootpath, 0,
								bootEntriesPrep.length);
					}
					int dest = bootEntriesPrep.length;
					for (int i = 0; i < bootEntries.length; i++) {
						bootpath[dest] = bootEntries[i].getLocation();
						dest++;
					}
					if (bootEntriesApp.length > 0) {
						System.arraycopy(bootpathInfo[2], 0, bootpath, dest,
								bootEntriesApp.length);
					}
					bootpathInfo[0] = null;
					bootpathInfo[1] = bootpath;
					bootpathInfo[2] = null;
				}
			}
		} else {
			if (entriesPrep == null) {
				bootpathInfo[1] = new String[0];
			} else {
				bootpathInfo[1] = entriesPrep;
			}
		}
		return bootpathInfo;
	}
	/**
	 * Returns the entries that should appear on the user portion of the
	 * classpath as specified by the given launch configuration, as an array of
	 * resolved strings. The returned array is empty if no classpath is
	 * specified.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the classpath specified by the given launch configuration,
	 *         possibly an empty array
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String[] getClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime
				.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		List<String> userEntries = new ArrayList<String>(entries.length);
		Set<String> set = new HashSet<String>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entries[i].getLocation();
				if (location != null) {
					if (!set.contains(location)) {
						userEntries.add(location);
						set.add(location);
					}
				}
			}
		}
		return userEntries.toArray(new String[userEntries.size()]);
	}
	/**
	 * Returns the Java project specified by the given launch configuration, or
	 * <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the Java project specified by the given launch configuration, or
	 *         <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public IJavaProject getJavaProject(ILaunchConfiguration configuration)
			throws CoreException {
		String projectName = getJavaProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(projectName);
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null && javaProject.exists()) {
					return javaProject;
				}
			}
		}
		return null;
	}
	/**
	 * Returns the Java project name specified by the given launch
	 * configuration, or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the Java project name specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String getJavaProjectName(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				(String) null);
	}
	/**
	 * Returns the main type name specified by the given launch configuration,
	 * or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the main type name specified by the given launch configuration,
	 *         or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String getMainTypeName(ILaunchConfiguration configuration)
			throws CoreException {
		String mainType = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
				(String) null);
		if (mainType == null) {
			return null;
		}
		return VariablesPlugin.getDefault().getStringVariableManager()
				.performStringSubstitution(mainType);
	}
	/**
	 * Returns the program arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if no program
	 * arguments are specified.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the program arguments specified by the given launch
	 *         configuration, possibly an empty string
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String getProgramArguments(ILaunchConfiguration configuration)
			throws CoreException {
		String arguments = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		return VariablesPlugin.getDefault().getStringVariableManager()
				.performStringSubstitution(arguments);
	}
	/**
	 * Returns the VM arguments specified by the given launch configuration, as
	 * a string. The returned string is empty if no VM arguments are specified.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the VM arguments specified by the given launch configuration,
	 *         possibly an empty string
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		String arguments = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""); //$NON-NLS-1$
		String args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(arguments);
		int libraryPath = args.indexOf("-Djava.library.path"); //$NON-NLS-1$
		if (libraryPath < 0) {
			// if a library path is already specified, do not override
			String[] javaLibraryPath = getJavaLibraryPath(configuration);
			if (javaLibraryPath != null && javaLibraryPath.length > 0) {
				StringBuffer path = new StringBuffer(args);
				path.append(" -Djava.library.path="); //$NON-NLS-1$
				path.append("\""); //$NON-NLS-1$
				for (int i = 0; i < javaLibraryPath.length; i++) {
					if (i > 0) {
						path.append(File.pathSeparatorChar);
					}
					path.append(javaLibraryPath[i]);
				}
				path.append("\""); //$NON-NLS-1$
				args = path.toString();
			}
		}
		return args;
	}
	/**
	 * Returns the Map of VM-specific attributes specified by the given launch
	 * configuration, or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the <code>Map</code> of VM-specific attributes
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public Map<String, Object> getVMSpecificAttributesMap(ILaunchConfiguration configuration) throws CoreException {
		Map<String, Object> map = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP,(Map<String, Object>) null);
		String[][] paths = getBootpathExt(configuration);
		String[] pre = paths[0];
		String[] boot = paths[1];
		String[] app = paths[2];
		if (pre != null || app != null || boot != null) {
			if (map == null) {
				map = new HashMap<String, Object>(3);
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
	 * Returns the working directory specified by the given launch
	 * configuration, or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the working directory specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public File getWorkingDirectory(ILaunchConfiguration configuration)
			throws CoreException {
		return verifyWorkingDirectory(configuration);
	}
	/**
	 * Returns the working directory path specified by the given launch
	 * configuration, or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the working directory path specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public IPath getWorkingDirectoryPath(ILaunchConfiguration configuration)
			throws CoreException {
		String path = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				(String) null);
		if (path != null) {
			path = VariablesPlugin.getDefault().getStringVariableManager()
					.performStringSubstitution(path);
			return new Path(path);
		}
		return null;
	}
	/**
	 * Verifies a Java project is specified by the given launch configuration,
	 * and returns the Java project.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the Java project specified by the given launch configuration
	 * @exception CoreException
	 *                if unable to retrieve the attribute or the attribute is
	 *                unspecified
	 */
	public IJavaProject verifyJavaProject(ILaunchConfiguration configuration)
			throws CoreException {
		String name = getJavaProjectName(configuration);
		if (name == null) {
			abort(
					LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_Java_project_not_specified_9, 
					null,
					IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT); 
		}
		IJavaProject project = getJavaProject(configuration);
		if (project == null) {
			abort(
					LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_Project_does_not_exist_or_is_not_a_Java_project_10, 
					null,
					IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT); 
		}
		return project;
	}
	/**
	 * Verifies a main type name is specified by the given launch configuration,
	 * and returns the main type name.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the main type name specified by the given launch configuration
	 * @exception CoreException
	 *                if unable to retrieve the attribute or the attribute is
	 *                unspecified
	 */
	public String verifyMainTypeName(ILaunchConfiguration configuration)
			throws CoreException {
		String name = getMainTypeName(configuration);
		if (name == null) {
			abort(
					LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_Main_type_not_specified_11, 
					null,
					IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE); 
		}
		return name;
	}
	/**
	 * Verifies the working directory specified by the given launch
	 * configuration exists, and returns the working directory, or
	 * <code>null</code> if none is specified.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the working directory specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public File verifyWorkingDirectory(ILaunchConfiguration configuration)
			throws CoreException {
		IPath path = getWorkingDirectoryPath(configuration);
		if (path == null) {
			File dir = getDefaultWorkingDirectory(configuration);
			if (dir != null) {
				if (!dir.isDirectory()) {
					abort(
							NLS.bind(
									LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_Working_directory_does_not_exist___0__12, 
									new String[]{dir.toString()}),
									null,
									IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); 
				}
				return dir;
			}
		} else {
			if (path.isAbsolute()) {
				File dir = new File(path.toOSString());
				if (dir.isDirectory()) {
					return dir;
				}
				// This may be a workspace relative path returned by a variable.
				// However variable paths start with a slash and thus are thought to
				// be absolute
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
				if (res instanceof IContainer && res.exists()) {
					return res.getLocation().toFile();
				}
				abort(
					NLS.bind(
									LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_Working_directory_does_not_exist___0__12, 
									new String[]{path.toString()}),
					null,
					IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); 
			} else {
				IResource res = ResourcesPlugin.getWorkspace().getRoot()
						.findMember(path);
				if (res instanceof IContainer && res.exists()) {
					return res.getLocation().toFile();
				}
				abort(
					NLS.bind(
									LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_Working_directory_does_not_exist___0__12, 
									new String[]{path.toString()}),
					null,
					IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST); 
			}
		}
		return null;
	}
	/**
	 * Returns whether the given launch configuration specifies that termination
	 * is allowed.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return whether termination is allowed
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public boolean isAllowTerminate(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
	}
	/**
	 * Returns whether the given launch configuration specifies that execution
	 * should suspend on entry of the main method.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return whether execution should suspend in main
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 * @since 2.1
	 */
	public boolean isStopInMain(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
	}
	/**
	 * Assigns a default source locator to the given launch if a source locator
	 * has not yet been assigned to it, and the associated launch configuration
	 * does not specify a source locator.
	 * 
	 * @param launch
	 *            launch object
	 * @param configuration
	 *            configuration being launched
	 * @exception CoreException
	 *                if unable to set the source locator
	 */
	protected void setDefaultSourceLocator(ILaunch launch,
			ILaunchConfiguration configuration) throws CoreException {
		//  set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			ISourceLookupDirector sourceLocator = new JavaSourceLookupDirector();
			sourceLocator
					.setSourcePathComputer(getLaunchManager()
							.getSourcePathComputer(
									"org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer")); //$NON-NLS-1$
			sourceLocator.initializeDefaults(configuration);
			launch.setSourceLocator(sourceLocator);
		}
	}
	/**
	 * Determines if the given launch configuration specifies the "stop-in-main"
	 * attribute, and sets up an event listener to handle the option if
	 * required.
	 * 
	 * @param configuration
	 *            configuration being launched
	 * @exception CoreException
	 *                if unable to access the attribute
	 * @since 2.1
	 */
	protected void prepareStopInMain(ILaunchConfiguration configuration)
			throws CoreException {
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
	 * @param events
	 *            the debug events.
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.CREATE
					&& event.getSource() instanceof IJavaDebugTarget) {
				IJavaDebugTarget target = (IJavaDebugTarget) event.getSource();
				ILaunch launch = target.getLaunch();
				if (launch != null) {
					ILaunchConfiguration configuration = launch
							.getLaunchConfiguration();
					if (configuration != null) {
						try {
							if (isStopInMain(configuration)) {
								String mainType = getMainTypeName(configuration);
								if (mainType != null) {
									Map<String, Object> map = new HashMap<String, Object>();
									map
											.put(
													IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN,
													IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN);
									IJavaMethodBreakpoint bp = JDIDebugModel
											.createMethodBreakpoint(
													ResourcesPlugin
															.getWorkspace()
															.getRoot(),
													mainType, "main", //$NON-NLS-1$
													"([Ljava/lang/String;)V", //$NON-NLS-1$
													true, false, false, -1, -1,
													-1, 1, false, map); 
									bp.setPersisted(false);
									target.breakpointAdded(bp);
									DebugPlugin.getDefault()
											.removeDebugEventListener(this);
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getBuildOrder(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String)
	 */
	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration,
			String mode) throws CoreException {
		return fOrderedProjects;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getProjectsForProblemSearch(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String)
	 */
	@Override
	protected IProject[] getProjectsForProblemSearch(
			ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return fOrderedProjects;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#isLaunchProblem(org.eclipse.core.resources.IMarker)
	 */
	@Override
	protected boolean isLaunchProblem(IMarker problemMarker) throws CoreException {
		return super.isLaunchProblem(problemMarker) && problemMarker.getType().equals(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		// build project list
		if (monitor != null) {
			monitor.subTask(LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_20); 
		}
		fOrderedProjects = null;
		IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
		if (javaProject != null) {
			fOrderedProjects = computeReferencedBuildOrder(new IProject[]{javaProject
					.getProject()});
		}
		// do generic launch checks
		return super.preLaunchCheck(configuration, mode, monitor);
	}
    
     /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getBreakpoints(org.eclipse.debug.core.ILaunchConfiguration)
     */
    @Override
	protected IBreakpoint[] getBreakpoints(ILaunchConfiguration configuration) {
         IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
         if (!breakpointManager.isEnabled()) {
             // no need to check breakpoints individually.
             return null;
         }
         return breakpointManager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
     }
	
	/**
	 * Returns the VM runner for the given launch mode to use when launching the
	 * given configuration.
	 *  
	 * @param configuration launch configuration
	 * @param mode launch node
	 * @return VM runner to use when launching the given configuration in the given mode
	 * @throws CoreException if a VM runner cannot be determined
	 * @since 3.1
	 */
	public IVMRunner getVMRunner(ILaunchConfiguration configuration, String mode) throws CoreException {
		IVMInstall vm = verifyVMInstall(configuration);
		IVMRunner runner = vm.getVMRunner(mode);
		if (runner == null) {
			abort(NLS.bind(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_0, new String[]{vm.getName(), mode}), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST); 
		}
		return runner;
	}
	
	/** 
	 * Returns an array of environment variables to be used when
	 * launching the given configuration or <code>null</code> if unspecified.
	 * 
	 * @param configuration launch configuration
	 * @return an array of environment variables to use when launching the given configuration or null if unspecified
	 * @throws CoreException if unable to access associated attribute or if
	 * unable to resolve a variable in an environment variable's value
	 * @since 3.1
	 */	
	public String[] getEnvironment(ILaunchConfiguration configuration) throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
	}
	
	/**
	 * Returns an array of paths to be used for the <code>java.library.path</code>
	 * system property, or <code>null</code> if unspecified.
	 * 
	 * @param configuration the config
	 * @return an array of paths to be used for the <code>java.library.path</code>
	 * system property, or <code>null</code>
	 * @throws CoreException if unable to determine the attribute
	 * @since 3.1
	 */
	public String[] getJavaLibraryPath(ILaunchConfiguration configuration) throws CoreException {
		IJavaProject project = getJavaProject(configuration);
		if (project != null) {
			String[] paths = JavaRuntime.computeJavaLibraryPath(project, true);
			if (paths.length > 0) {
				return paths;
			}
		}
		return null;
	}
	
	/**
	 * Returns the default working directory for the given launch configuration,
	 * or <code>null</code> if none. Subclasses may override as necessary.
	 * 
	 * @param configuration the config
	 * @return default working directory or <code>null</code> if none
	 * @throws CoreException if an exception occurs computing the default working
	 * 	 directory
	 * @since 3.2
	 */
	protected File getDefaultWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		// default working directory is the project if this config has a project
		IJavaProject jp = getJavaProject(configuration);
		if (jp != null) {
			IProject p = jp.getProject();
			return p.getLocation().toFile();
		}
		return null;
	}
}
