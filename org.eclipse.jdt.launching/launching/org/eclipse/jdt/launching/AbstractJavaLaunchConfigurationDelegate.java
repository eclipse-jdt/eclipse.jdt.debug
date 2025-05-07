/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;

import static org.eclipse.jdt.internal.launching.sourcelookup.advanced.AdvancedSourceLookupSupport.createAdvancedLaunch;
import static org.eclipse.jdt.internal.launching.sourcelookup.advanced.AdvancedSourceLookupSupport.getJavaagentString;
import static org.eclipse.jdt.internal.launching.sourcelookup.advanced.AdvancedSourceLookupSupport.isAdvancedSourcelookupEnabled;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
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
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
	private boolean allowAdvancedSourcelookup;

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
					NLS.bind(LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_JRE_home_directory_not_specified_for__0__5, vm.getName()),
					null,
					IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST);
		}
		if (!location.exists()) {
			abort(
					NLS.bind(LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_JRE_home_directory_for__0__does_not_exist___1__6,
							vm.getName(), location.getAbsolutePath()),
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
		if (JavaRuntime.isModularConfiguration(configuration)) {
			return null;
		}
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
		List<String> bootEntries = new ArrayList<>(entries.length);
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
		List<IRuntimeClasspathEntry> bootEntriesPrepend = new ArrayList<>();
		int index = 0;
		IRuntimeClasspathEntry jreEntry = null;
		while (jreEntry == null && index < entries.length) {
			IRuntimeClasspathEntry entry = entries[index++];
			if (JavaRuntime.isVMInstallReference(entry)) {
				jreEntry = entry;
			} else if (entry.getClasspathProperty() == IRuntimeClasspathEntry.BOOTSTRAP_CLASSES) {
				bootEntriesPrepend.add(entry);
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
			List<IRuntimeClasspathEntry> bootEntriesAppend = new ArrayList<>();
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
	 * Returns the entries that should appear on the user portion of the classpath as specified by the given launch configuration, as an array of
	 * resolved strings. The returned array is empty if no classpath is specified.
	 *
	 * @param configuration
	 *            launch configuration
	 * @return the classpath specified by the given launch configuration, possibly an empty array
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 * @deprecated use getClasspathAndModulepath
	 */
	@Deprecated
	public String[] getClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime
				.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);

		List<String> userEntries = new ArrayList<>(entries.length);
		Set<String> set = new HashSet<>(entries.length);
		for (IRuntimeClasspathEntry entry : entries) {
			if (entry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES
					|| entry.getClasspathProperty() == IRuntimeClasspathEntry.CLASS_PATH) {
				String location = entry.getLocation();
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
	 * Returns the entries that should appear on the user portion of the classpath and modulepath as specified by the given launch configuration, as
	 * an array of resolved strings. The returned array is empty if no classpath and modulepath is specified.
	 *
	 * @param config
	 *            launch configuration
	 * @return the classpath and modulepath specified by the given launch configuration, possibly an empty array
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 * @since 3.10
	 */
	public String[][] getClasspathAndModulepath(ILaunchConfiguration config) throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(config);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, config);
		String[][] path = new String[2][entries.length];
		List<String> classpathEntries = new ArrayList<>(entries.length);
		List<String> modulepathEntries = new ArrayList<>(entries.length);
		Set<String> classpathSet = new HashSet<>(entries.length);
		Set<String> modulepathSet = new HashSet<>(entries.length);
		for (IRuntimeClasspathEntry entry : entries) {
			String location = entry.getLocation();
			if (location != null) {
				switch (entry.getClasspathProperty()) {
				case IRuntimeClasspathEntry.USER_CLASSES:
					if (!classpathSet.contains(location)) {
						classpathEntries.add(location);
						classpathSet.add(location);
					}
					break;
				case IRuntimeClasspathEntry.CLASS_PATH:
					if (!classpathSet.contains(location)) {
						classpathEntries.add(location);
						classpathSet.add(location);
					}
					break;
				case IRuntimeClasspathEntry.MODULE_PATH:
					if (!modulepathSet.contains(location)) {
						modulepathEntries.add(location);
						modulepathSet.add(location);
					}
					break;
				default:
					break;
				}

			}
		}
		path[0] = classpathEntries.toArray(new String[classpathEntries.size()]);
		path[1] = modulepathEntries.toArray(new String[modulepathEntries.size()]);
		return path;
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
				StringBuilder path = new StringBuilder(args);
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
	 * Returns the VM arguments specified by the given launch configuration, as a string. The returned string is empty if no VM arguments are
	 * specified.
	 *
	 * @param configuration
	 *            launch configuration
	 * @param mode
	 *            the mode in which to launch, one of the mode constants defined by <code>ILaunchManager</code> - <code>RUN_MODE</code> or
	 *            <code>DEBUG_MODE</code>.
	 * @return the VM arguments specified by the given launch configuration, possibly an empty string
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 * @since 3.10
	 */
	public String getVMArguments(ILaunchConfiguration configuration, String mode) throws CoreException {
		if (!isAdvancedSourcelup(mode)) {
			return ""; //$NON-NLS-1$
		}
		if (!isJavaagentOptionSupported(configuration)) {
			return ""; //$NON-NLS-1$
		}
		return getJavaagentString();
	}

	private boolean isJavaagentOptionSupported(ILaunchConfiguration configuration) {
		try {
			IVMInstall vm = JavaRuntime.computeVMInstall(configuration);
			if (JavaRuntime.compareJavaVersions(vm, JavaCore.VERSION_1_4) > 0) {
				return true;
			}
		} catch (CoreException e) {
			LaunchingPlugin.log(e);
		}
		return false;
	}

	private boolean isAdvancedSourcelup(String mode) {
		return allowAdvancedSourcelookup && ILaunchManager.DEBUG_MODE.equals(mode) && isAdvancedSourcelookupEnabled();
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
		Map<String, String> map = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP,(Map<String, String>) null);
		Map<String, Object> attributes = new HashMap<>();
		if(map != null) {
			attributes.putAll(map);
		}
		String[][] paths = getBootpathExt(configuration);
		String[] pre = paths[0];
		String[] boot = paths[1];
		String[] app = paths[2];
		if (pre != null) {
			attributes.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND, pre);
		}
		if (app != null) {
			attributes.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND, app);
		}
		if (boot != null) {
			attributes.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, boot);
		}
		return attributes;
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
									dir.toString()),
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
								path.toString()),
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
								path.toString()),
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
	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.CREATE
					&& event.getSource() instanceof IJavaDebugTarget target) {
				ILaunch launch = target.getLaunch();
				if (launch != null) {
					ILaunchConfiguration configuration = launch
							.getLaunchConfiguration();
					if (configuration != null) {
						try {
							if (isStopInMain(configuration)) {
								String mainType = getMainTypeName(configuration);
								if (mainType != null) {
									Map<String, Object> map = new HashMap<>();
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
			abort(NLS.bind(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_0, vm.getName(), mode), null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);
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
			// p.getLocation() will be null in the case where the location is relative to an undefined workspace path variable.
			if (p.getLocation() != null) {
				return p.getLocation().toFile();
			}
		}
		return null;
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		if (!isAdvancedSourcelup(mode)) {
			return null;
		}
		return createAdvancedLaunch(configuration, mode);
	}

	/**
	 * Enabled advanced sourcelookup for this launch delegate. Advanced source lookup is disabled by default. This call has not effect if advanced
	 * source lookup is disabled at workspace level, i.e. advanced source lookup will remain disabled even if this method is called for the launch
	 * delegate instance.
	 *
	 * @since 3.10
	 */
	protected final void allowAdvancedSourcelookup() {
		this.allowAdvancedSourcelookup = true;
	}

	/**
	 * Returns the module-related command line options for the configuration that are needed at runtime as equivalents of those options specified by
	 * {@link IClasspathAttribute}s of the following names:
	 * <ul>
	 * <li>{@link IClasspathAttribute#ADD_EXPORTS}</li>
	 * <li>{@link IClasspathAttribute#ADD_READS}</li>
	 * <li>{@link IClasspathAttribute#LIMIT_MODULES}</li>
	 * <li>{@link IClasspathAttribute#PATCH_MODULE}</li>
	 * </ul>
	 *
	 * @throws CoreException
	 *
	 * @since 3.10
	 */
	public String getModuleCLIOptions(ILaunchConfiguration configuration) throws CoreException {
		String moduleCLIOptions = JavaRuntime.getModuleCLIOptions(configuration);

		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		LinkedHashMap<String, String> moduleToLocations = new LinkedHashMap<>();

		for (IRuntimeClasspathEntry entry : entries) {
			String location = entry.getLocation();
			if (location != null) {
				if (entry.getClasspathProperty() == IRuntimeClasspathEntry.PATCH_MODULE) {
					IJavaProject javaProject = entry.getJavaProject();
					IModuleDescription moduleDescription = javaProject == null ? null : javaProject.getModuleDescription();
					if (moduleDescription != null) {
						String moduleName = moduleDescription.getElementName();
						addPatchModuleLocations(moduleToLocations, moduleName, location);
					} else {
						// should not happen, log?
					}
				} else {
					for (IClasspathAttribute attribute : entry.getClasspathEntry().getExtraAttributes()) {
						if (IClasspathAttribute.PATCH_MODULE.equals(attribute.getName())) {
							String patchModules = attribute.getValue();
							for (String patchModule : patchModules.split("::")) { //$NON-NLS-1$
								int equalsIdx = patchModule.indexOf('=');
								if (equalsIdx != -1) {
									if (equalsIdx < patchModule.length() - 1) { // otherwise malformed?
										String locations = toAbsolutePathsString(patchModule.substring(equalsIdx + 1));
										String moduleName = patchModule.substring(0, equalsIdx);
										addPatchModuleLocations(moduleToLocations, moduleName, locations);
									}
								} else {
									// old (discouraged) format without explicit paths: list all output locations of the current project
									IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
									addPatchModuleLocations(moduleToLocations, patchModule, toAbsolutePathsString(javaProject.getOutputLocation().toString()));
									for (IClasspathEntry cpEntry : javaProject.getRawClasspath()) {
										if (cpEntry.getOutputLocation() != null) {
											addPatchModuleLocations(moduleToLocations, patchModule, toAbsolutePathsString(cpEntry.getOutputLocation().toString()));
										}
									}
								}
							}

							break;
						}
					}
				}
			}
		}
		String addModules = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_SPECIAL_ADD_MODULES, ""); //$NON-NLS-1$
		if (moduleToLocations.isEmpty() && addModules.isEmpty()) {
			return moduleCLIOptions;
		}

		ArrayList<String> list = new ArrayList<>();

		Collections.addAll(list, DebugPlugin.parseArguments(moduleCLIOptions));

		for (Entry<String, String> entry : moduleToLocations.entrySet()) {
			list.add("--patch-module"); //$NON-NLS-1$
			list.add(entry.getKey() + '=' + entry.getValue());
		}

		boolean excludeTestCode = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_EXCLUDE_TEST_CODE, false);
		if (!excludeTestCode) {
			// TODO: revisit to examine other possible solutions
			IJavaProject project = getJavaProject(configuration);
			if (project != null) {
				for (String moduleName : project.determineModulesOfProjectsWithNonEmptyClasspath()) {
					list.add("--add-reads"); //$NON-NLS-1$
					list.add(moduleName + '=' + "ALL-UNNAMED"); //$NON-NLS-1$
				}
			}
		}
		if (!addModules.isEmpty()) {
			list.add("--add-modules"); //$NON-NLS-1$
			list.add(addModules);
		}
		return DebugPlugin.renderArguments(list.toArray(new String[list.size()]), null);
	}

	private void addPatchModuleLocations(Map<String, String> moduleToLocations, String moduleName, String locations) {
		String existing = moduleToLocations.get(moduleName);
		if (existing == null) {
			moduleToLocations.put(moduleName, locations);
		} else {
			moduleToLocations.put(moduleName, existing + File.pathSeparator + locations);
		}
	}

	private static String toAbsolutePathsString(String fPaths) {
		String[] paths = fPaths.split(File.pathSeparator);
		String[] absPaths = new String[paths.length];
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = 0; i < paths.length; i++) {
			IResource resource = root.findMember(new Path(paths[i]));
			try {
				absPaths[i] = toAbsolutePath(resource, root);
			} catch (CoreException e) {
				LaunchingPlugin.log(e);
			}
			if (absPaths[i] == null) {
				absPaths[i] = paths[i];
			}
		}
		String allPaths = String.join(File.pathSeparator, absPaths);
		return allPaths;
	}

	private static String toAbsolutePath(IResource resource, IWorkspaceRoot root) throws CoreException {
		IJavaElement element = JavaCore.create(resource);
		if (element != null && element.exists()) {
			if (element instanceof IJavaProject project) {
				Set<String> paths = new HashSet<>();
				paths.add(absPath(root, project.getOutputLocation()));
				for (IClasspathEntry entry : project.getResolvedClasspath(true)) {
					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getOutputLocation() != null) {
						paths.add(absPath(root, entry.getOutputLocation()));
					}
				}
				return String.join(File.pathSeparator, paths);
			} else if (element instanceof IPackageFragmentRoot packageRoot) {
				IClasspathEntry entry = packageRoot.getJavaProject().getClasspathEntryFor(resource.getFullPath());
				return absPath(root, entry.getOutputLocation());
			}
		}
		if (resource != null) {
			// non-source location as-is:
			return resource.getLocation().toString();
		}
		return null;
	}

	private static String absPath(IWorkspaceRoot root, IPath path) {
		return root.findMember(path).getLocation().toString();
	}

	/**
	 * Supports modular class for launching.
	 *
	 * @since 3.12
	 */
	protected boolean supportsModule() {
		return true;
	}

	/**
	 * Supports Preview Features for launching.
	 *
	 * @since 3.14
	 */
	protected boolean supportsPreviewFeatures(ILaunchConfiguration configuration) {
		try {
			IJavaProject javaProject = getJavaProject(configuration);
			if (javaProject != null) { // Maven project returns null
				String id = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
				if (JavaCore.ENABLED.equals(id)) {
					return true;
				}
			}
		} catch (CoreException e) {
			// Not a java project
		}
		return false;
	}

}
