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


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.osgi.service.prefs.BackingStoreException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.osgi.util.NLS;

import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.BundleDefaultsScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;

import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.internal.launching.CompositeId;
import org.eclipse.jdt.internal.launching.DefaultEntryResolver;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;
import org.eclipse.jdt.internal.launching.EEVMInstall;
import org.eclipse.jdt.internal.launching.EEVMType;
import org.eclipse.jdt.internal.launching.JREContainerInitializer;
import org.eclipse.jdt.internal.launching.JavaSourceLookupUtil;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntryResolver;
import org.eclipse.jdt.internal.launching.RuntimeClasspathProvider;
import org.eclipse.jdt.internal.launching.SocketAttachConnector;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.internal.launching.VMListener;
import org.eclipse.jdt.internal.launching.VariableClasspathEntry;
import org.eclipse.jdt.internal.launching.environments.EnvironmentsManager;

import org.eclipse.jdt.launching.environments.ExecutionEnvironmentDescription;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;

/**
 * The central access point for launching support. This class manages
 * the registered VM types contributed through the
 * <code>"org.eclipse.jdt.launching.vmType"</code> extension point.
 * As well, this class provides VM install change notification,
 * and computes class paths and source lookup paths for launch
 * configurations.
 * <p>
 * This class provides static methods only.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class JavaRuntime {
	
	/**
	 * Classpath variable name used for the default JRE's library
	 * (value <code>"JRE_LIB"</code>).
	 */
	public static final String JRELIB_VARIABLE= "JRE_LIB"; //$NON-NLS-1$

	/**
	 * Classpath variable name used for the default JRE's library source
	 * (value <code>"JRE_SRC"</code>).
	 */
	public static final String JRESRC_VARIABLE= "JRE_SRC"; //$NON-NLS-1$
	
	/**
	 * Classpath variable name used for the default JRE's library source root
	 * (value <code>"JRE_SRCROOT"</code>).
	 */
	public static final String JRESRCROOT_VARIABLE= "JRE_SRCROOT"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"runtimeClasspathEntryResolvers"</code>) for the
	 * runtime classpath entry resolvers extension point.
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_RUNTIME_CLASSPATH_ENTRY_RESOLVERS= "runtimeClasspathEntryResolvers";	 //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"classpathProviders"</code>) for the
	 * runtime classpath providers extension point.
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_RUNTIME_CLASSPATH_PROVIDERS= "classpathProviders";	 //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"executionEnvironments"</code>) for the
	 * execution environments extension point.
	 * 
	 * @since 3.2
	 */
	public static final String EXTENSION_POINT_EXECUTION_ENVIRONMENTS= "executionEnvironments";	 //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"vmInstalls"</code>) for the
	 * VM installs extension point.
	 * 
	 * @since 3.2
	 */
	public static final String EXTENSION_POINT_VM_INSTALLS = "vmInstalls";	 //$NON-NLS-1$
		
	/**
	 * Classpath container used for a project's JRE
	 * (value <code>"org.eclipse.jdt.launching.JRE_CONTAINER"</code>). A
	 * container is resolved in the context of a specific Java project, to one
	 * or more system libraries contained in a JRE. The container can have zero
	 * or two path segments following the container name. When no segments
	 * follow the container name, the workspace default JRE is used to build a
	 * project. Otherwise the segments identify a specific JRE used to build a
	 * project:
	 * <ol>
	 * <li>VM Install Type Identifier - identifies the type of JRE used to build the
	 * 	project. For example, the standard VM.</li>
	 * <li>VM Install Name - a user defined name that identifies that a specific VM
	 * 	of the above kind. For example, <code>IBM 1.3.1</code>. This information is
	 *  shared in a projects classpath file, so teams must agree on JRE naming
	 * 	conventions.</li>
	 * </ol>
	 * <p>
	 * Since 3.2, the path may also identify an execution environment as follows:
	 * <ol>
	 * <li>Execution environment extension point name
	 * (value <code>executionEnvironments</code>)</li>
	 * <li>Identifier of a contributed execution environment</li>
	 * </ol>
	 * </p>
	 * @since 2.0
	 */
	public static final String JRE_CONTAINER = LaunchingPlugin.getUniqueIdentifier() + ".JRE_CONTAINER"; //$NON-NLS-1$
	
	/**
	 * Marker type identifier for JRE container problems.
	 * 
	 * @since 3.6
	 */
	public static final String JRE_CONTAINER_MARKER = LaunchingPlugin.getUniqueIdentifier() + ".jreContainerMarker"; //$NON-NLS-1$
	
	/**
	 * A status code indicating that a JRE could not be resolved for a project.
	 * When a JRE cannot be resolved for a project by this plug-in's container
	 * initializer, an exception is thrown with this status code. A status handler
	 * may be registered for this status code. The <code>source</code> object provided
	 * to the status handler is the Java project for which the path could not be
	 * resolved. The status handler must return an <code>IVMInstall</code> or <code>null</code>.
	 * The container resolver will re-set the project's classpath if required.
	 * 
	 * @since 2.0
	 */
	public static final int ERR_UNABLE_TO_RESOLVE_JRE = 160;
	
	/**
	 * Preference key for launch/connect timeout. VM Runners should honor this timeout
	 * value when attempting to launch and connect to a debuggable VM. The value is
	 * an int, indicating a number of milliseconds.
	 * 
	 * @since 2.0
	 */
	public static final String PREF_CONNECT_TIMEOUT = LaunchingPlugin.getUniqueIdentifier() + ".PREF_CONNECT_TIMEOUT"; //$NON-NLS-1$
	
	/**
	 * Preference key for the String of XML that defines all installed VMs.
	 * 
	 * @since 2.1
	 */
	public static final String PREF_VM_XML = LaunchingPlugin.getUniqueIdentifier() + ".PREF_VM_XML"; //$NON-NLS-1$
	
	/**
	 * Preference key for the default problem severity when an execution environment is bound
	 * to a project's build path for which there is no strictly compatible JRE available in the
	 * workspace. Value is one of {@link JavaCore#ERROR}, {@link JavaCore#WARNING}, or
	 * {@link JavaCore#IGNORE}
	 * 
	 * @since 3.5
	 */
	public static final String PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE = LaunchingPlugin.getUniqueIdentifier() + ".PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE"; //$NON-NLS-1$
	
	/**
	 * Unique identifier constant (value <code>"org.eclipse.jdt.launching"</code>)
	 * for the Java launching plug-in.
	 * 
	 * @since 3.5
	 */
	public static final String ID_PLUGIN = LaunchingPlugin.ID_PLUGIN;
	
	/**
	 * Default launch/connect timeout (milliseconds).
	 * 
	 * @since 2.0
	 */
	public static final int DEF_CONNECT_TIMEOUT = 20000;
	
	/**
	 * Attribute key for a process property. The class
	 * <code>org.eclipse.debug.core.model.IProcess</code> allows attaching
	 * String properties to processes.
	 * The value of this attribute is the command line a process
	 * was launched with. Implementers of <code>IVMRunner</code> should use
	 * this attribute key to attach the command lines to the processes they create.
	 * 
	 * @deprecated - use <code>IProcess.ATTR_CMDLINE</code>
	 */
	@Deprecated
	public final static String ATTR_CMDLINE= LaunchingPlugin.getUniqueIdentifier() + ".launcher.cmdLine"; //$NON-NLS-1$
	
	/**
	 * Attribute key for a classpath attribute referencing a
	 * list of shared libraries that should appear on the
	 * <code>-Djava.library.path</code> system property.
	 * <p>
	 * The factory methods <code>newLibraryPathsAttribute(String[])</code>
	 * and <code>getLibraryPaths(IClasspathAttribute)</code> should be used to
	 * encode and decode the attribute value.
	 * </p>
	 * <p>
	 * Each string is used to create an <code>IPath</code> using the constructor
	 * <code>Path(String)</code>, and may contain <code>IStringVariable</code>'s.
	 * Variable substitution is performed on the string prior to constructing
	 * a path from the string.
	 * If the resulting <code>IPath</code> is a relative path, it is interpreted
	 * as relative to the workspace location. If the path is absolute, it is
	 * interpreted as an absolute path in the local file system.
	 * </p>
	 * @since 3.1
	 * @see org.eclipse.jdt.core.IClasspathAttribute
	 */
	public static final String CLASSPATH_ATTR_LIBRARY_PATH_ENTRY =  LaunchingPlugin.getUniqueIdentifier() + ".CLASSPATH_ATTR_LIBRARY_PATH_ENTRY"; //$NON-NLS-1$
	
	// lock for VM initialization
	private static Object fgVMLock = new Object();
	private static boolean fgInitializingVMs = false;
	
	private static HashSet<Object> fgVMTypes = null;
	private static String fgDefaultVMId = null;
	private static String fgDefaultVMConnectorId = null;
	
	/**
	 * Resolvers keyed by variable name, container id,
	 * and runtime classpath entry id.
	 */
	private static Map<String, IRuntimeClasspathEntryResolver> fgVariableResolvers = null;
	private static Map<String, IRuntimeClasspathEntryResolver> fgContainerResolvers = null;
	private static Map<String, RuntimeClasspathEntryResolver> fgRuntimeClasspathEntryResolvers = null;
	
	/**
	 * Path providers keyed by id
	 */
	private static Map<String, RuntimeClasspathProvider> fgPathProviders = null;
	
	/**
	 * Default classpath and source path providers.
	 */
	private static IRuntimeClasspathProvider fgDefaultClasspathProvider = new StandardClasspathProvider();
	private static IRuntimeClasspathProvider fgDefaultSourcePathProvider = new StandardSourcePathProvider();
	
	/**
	 * VM change listeners
	 */
	private static ListenerList fgVMListeners = new ListenerList();
	
	/**
	 * Cache of already resolved projects in container entries. Used to avoid
	 * cycles in project dependencies when resolving classpath container entries.
	 * Counters used to know when entering/exiting to clear cache
	 */
	private static ThreadLocal<List<IJavaProject>> fgProjects = new ThreadLocal<List<IJavaProject>>(); // Lists
	private static ThreadLocal<Integer> fgEntryCount = new ThreadLocal<Integer>(); // Integers
	
    /**
     *  Set of IDs of VMs contributed via vmInstalls extension point.
     */
    private static Set<String> fgContributedVMs = new HashSet<String>();
    
	/**
	 * This class contains only static methods, and is not intended
	 * to be instantiated.
	 */
	private JavaRuntime() {
	}

	/**
	 * Initializes VM type extensions.
	 */
	private static void initializeVMTypeExtensions() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, "vmInstallTypes"); //$NON-NLS-1$
		if(extensionPoint != null) {
			IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
			MultiStatus status = new MultiStatus(LaunchingPlugin.getUniqueIdentifier(), IStatus.OK, "Exceptions occurred", null);  //$NON-NLS-1$
			fgVMTypes = new HashSet<Object>();
			for (int i= 0; i < configs.length; i++) {
				try {
					fgVMTypes.add(configs[i].createExecutableExtension("class")); //$NON-NLS-1$
				}
				catch (CoreException e) {status.add(e.getStatus());}
			}
			if (!status.isOK()) {
				//only happens on a CoreException
				LaunchingPlugin.log(status);
			}
		}
		else {
			LaunchingPlugin.log(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), "VM Install extension point not found", null)); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the VM assigned to build the given Java project.
	 * The project must exist. The VM assigned to a project is
	 * determined from its build path.
	 * 
	 * @param project the project to retrieve the VM from
	 * @return the VM instance that is assigned to build the given Java project
	 * 		   Returns <code>null</code> if no VM is referenced on the project's build path.
	 * @throws CoreException if unable to determine the project's VM install
	 */
	public static IVMInstall getVMInstall(IJavaProject project) throws CoreException {
		// check the classpath
		IVMInstall vm = null;
		IClasspathEntry[] classpath = project.getRawClasspath();
		IRuntimeClasspathEntryResolver resolver = null;
		IClasspathEntry entry = null;
		for (int i = 0; i < classpath.length; i++) {
			entry = classpath[i];
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_VARIABLE:
					resolver = getVariableResolver(entry.getPath().segment(0));
					if (resolver != null) {
						vm = resolver.resolveVMInstall(entry);
					}
					break;
				case IClasspathEntry.CPE_CONTAINER:
					resolver = getContainerResolver(entry.getPath().segment(0));
					if (resolver != null) {
						vm = resolver.resolveVMInstall(entry);
					}
					break;
			}
			if (vm != null) {
				return vm;
			}
		}
		return null;
	}
	
	/**
	 * Returns the VM install type with the given unique id.
	 * @param id the VM install type unique id
	 * @return	The VM install type for the given id, or <code>null</code> if no
	 * 			VM install type with the given id is registered.
	 */
	public static IVMInstallType getVMInstallType(String id) {
		IVMInstallType[] vmTypes= getVMInstallTypes();
		for (int i= 0; i < vmTypes.length; i++) {
			if (vmTypes[i].getId().equals(id)) {
				return vmTypes[i];
			}
		}
		return null;
	}
	
	/**
	 * Sets a VM as the system-wide default VM, and notifies registered VM install
	 * change listeners of the change.
	 * 
	 * @param vm	The VM to make the default. May be <code>null</code> to clear
	 * 				the default.
	 * @param monitor progress monitor or <code>null</code>
	 * @throws CoreException if trying to set the default VM install encounters problems
	 */
	public static void setDefaultVMInstall(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		setDefaultVMInstall(vm, monitor, true);
	}
	
	/**
	 * Sets a VM as the system-wide default VM, and notifies registered VM install
	 * change listeners of the change.
	 * 
	 * @param vm	The VM to make the default. May be <code>null</code> to clear
	 * 				the default.
	 * @param monitor progress monitor or <code>null</code>
	 * @param savePreference If <code>true</code>, update workbench preferences to reflect
	 * 		   				  the new default VM.
	 * @throws CoreException if trying to set the default VM install encounters problems
	 * @since 2.1
	 */
	public static void setDefaultVMInstall(IVMInstall vm, IProgressMonitor monitor, boolean savePreference) throws CoreException {
		IVMInstall previous = null;
		if (fgDefaultVMId != null) {
			previous = getVMFromCompositeId(fgDefaultVMId);
		}
		fgDefaultVMId= getCompositeIdFromVM(vm);
		if (savePreference) {
			saveVMConfiguration();
		}
		IVMInstall current = null;
		if (fgDefaultVMId != null) {
			current = getVMFromCompositeId(fgDefaultVMId);
		}
		if (previous != current) {
			notifyDefaultVMChanged(previous, current);
		}
	}
	
	/**
	 * Sets a VM connector as the system-wide default VM. This setting is persisted when
	 * saveVMConfiguration is called.
	 * @param	connector The connector to make the default. May be <code>null</code> to clear
	 * 				the default.
	 * @param monitor The progress monitor to use
	 * @since 2.0
	 * @throws CoreException Thrown if saving the new default setting fails
	 */
	public static void setDefaultVMConnector(IVMConnector connector, IProgressMonitor monitor) throws CoreException {
		fgDefaultVMConnectorId= connector.getIdentifier();
		saveVMConfiguration();
	}
	
	/**
	 * Return the default VM set with <code>setDefaultVM()</code>.
	 * @return	Returns the default VM. May return <code>null</code> when no default
	 * 			VM was set or when the default VM has been disposed.
	 */
	public static IVMInstall getDefaultVMInstall() {
		IVMInstall install= getVMFromCompositeId(getDefaultVMId());
		if (install != null) {
			File location = install.getInstallLocation();
			if (location != null) {
				if (location.exists()) {
					return install;
				}
			}
		}
		// if the default JRE goes missing, re-detect
		if (install != null) {
			install.getVMInstallType().disposeVMInstall(install.getId());
		}
		synchronized (fgVMLock) {
			fgDefaultVMId = null;
			fgVMTypes = null;
			initializeVMs();
		}
		return getVMFromCompositeId(getDefaultVMId());
	}
	
	/**
	 * Return the default VM connector.
	 * @return	Returns the default VM connector.
	 * @since 2.0
	 */
	public static IVMConnector getDefaultVMConnector() {
		String id = getDefaultVMConnectorId();
		IVMConnector connector = null;
		if (id != null) {
			connector = getVMConnector(id);
		}
		if (connector == null) {
			connector = new SocketAttachConnector();
		}
		return connector;
	}
	
	/**
	 * Returns the list of registered VM types. VM types are registered via
	 * <code>"org.eclipse.jdt.launching.vmTypes"</code> extension point.
	 * Returns an empty list if there are no registered VM types.
	 * 
	 * @return the list of registered VM types
	 */
	public static IVMInstallType[] getVMInstallTypes() {
		initializeVMs();
		return fgVMTypes.toArray(new IVMInstallType[fgVMTypes.size()]);
	}
	
	/**
	 * Returns the default VM id determined during the initialization of the VM types
	 * @return the id of the default VM
	 */
	private static String getDefaultVMId() {
		initializeVMs();
		return fgDefaultVMId;
	}
	
	/**
	 * Returns the default VM connector id determined during the initialization of the VM types
	 * @return the id of the default VM connector
	 */
	private static String getDefaultVMConnectorId() {
		initializeVMs();
		return fgDefaultVMConnectorId;
	}
	
	/**
	 * Returns a String that uniquely identifies the specified VM across all VM types.
	 * 
	 * @param vm the instance of IVMInstallType to be identified
	 * @return the unique identifier for the specified VM
	 * 
	 * @since 2.1
	 */
	public static String getCompositeIdFromVM(IVMInstall vm) {
		if (vm == null) {
			return null;
		}
		IVMInstallType vmType = vm.getVMInstallType();
		String typeID = vmType.getId();
		CompositeId id = new CompositeId(new String[] { typeID, vm.getId() });
		return id.toString();
	}
	
	/**
	 * Return the VM corresponding to the specified composite Id.  The id uniquely
	 * identifies a VM across all VM types.
	 * 
	 * @param idString the composite id that specifies an instance of IVMInstall
	 * @return the VM corresponding to the specified composite Id.
	 * 
	 * @since 2.1
	 */
	public static IVMInstall getVMFromCompositeId(String idString) {
		if (idString == null || idString.length() == 0) {
			return null;
		}
		CompositeId id= CompositeId.fromString(idString);
		if (id.getPartCount() == 2) {
			IVMInstallType vmType= getVMInstallType(id.get(0));
			if (vmType != null) {
				return vmType.findVMInstall(id.get(1));
			}
		}
		return null;
	}

	/**
	 * Returns a new runtime classpath entry for the given expression that
	 * may contain string substitution variable references. The resulting expression
	 * refers to an archive (jar or directory) containing class files.
	 * 
	 * @param expression an expression that resolves to the location of an archive
	 * @return runtime classpath entry
	 * @since 3.0
	 */
	public static IRuntimeClasspathEntry newStringVariableClasspathEntry(String expression) {
		return new VariableClasspathEntry(expression);
	}
	
	/**
	 * Returns a new runtime classpath entry containing the default classpath
	 * for the specified Java project.
	 * 
	 * @param project Java project
	 * @return runtime classpath entry
	 * @since 3.0
	 */
	public static IRuntimeClasspathEntry newDefaultProjectClasspathEntry(IJavaProject project) {
		return new DefaultProjectClasspathEntry(project);
	}
	
	/**
	 * Returns a new runtime classpath entry for the given project.
	 * 
	 * @param project Java project
	 * @return runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newProjectRuntimeClasspathEntry(IJavaProject project) {
		return newRuntimeClasspathEntry(JavaCore.newProjectEntry(project.getProject().getFullPath()));
	}
	
	
	/**
	 * Returns a new runtime classpath entry for the given archive.
	 * 
	 * @param resource archive resource
	 * @return runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newArchiveRuntimeClasspathEntry(IResource resource) {
		return newRuntimeClasspathEntry(JavaCore.newLibraryEntry(resource.getFullPath(), null, null));
	}
	
	/**
	 * Returns a new runtime classpath entry for the given archive (possibly
	 * external).
	 * 
	 * @param path absolute path to an archive
	 * @return runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newArchiveRuntimeClasspathEntry(IPath path) {
		return newRuntimeClasspathEntry(JavaCore.newLibraryEntry(path, null, null));
	}

	/**
	 * Returns a new runtime classpath entry for the classpath
	 * variable with the given path.
	 * 
	 * @param path variable path; first segment is the name of the variable;
	 * 	trailing segments are appended to the resolved variable value
	 * @return runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newVariableRuntimeClasspathEntry(IPath path) {
		return newRuntimeClasspathEntry(JavaCore.newVariableEntry(path, null, null));
	}

	/**
	 * Returns a runtime classpath entry for the given container path with the given
	 * classpath property.
	 * 
	 * @param path container path
	 * @param classpathProperty the type of entry - one of <code>USER_CLASSES</code>,
	 * 	<code>BOOTSTRAP_CLASSES</code>, or <code>STANDARD_CLASSES</code>
	 * @return runtime classpath entry
	 * @exception CoreException if unable to construct a runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newRuntimeContainerClasspathEntry(IPath path, int classpathProperty) throws CoreException {
		return newRuntimeContainerClasspathEntry(path, classpathProperty, null);
	}
	
	/**
	 * Returns a runtime classpath entry for the given container path with the given
	 * classpath property to be resolved in the context of the given Java project.
	 * 
	 * @param path container path
	 * @param classpathProperty the type of entry - one of <code>USER_CLASSES</code>,
	 * 	<code>BOOTSTRAP_CLASSES</code>, or <code>STANDARD_CLASSES</code>
	 * @param project Java project context used for resolution, or <code>null</code>
	 *  if to be resolved in the context of the launch configuration this entry
	 *  is referenced in
	 * @return runtime classpath entry
	 * @exception CoreException if unable to construct a runtime classpath entry
	 * @since 3.0
	 */
	public static IRuntimeClasspathEntry newRuntimeContainerClasspathEntry(IPath path, int classpathProperty, IJavaProject project) throws CoreException {
		RuntimeClasspathEntry entry = new RuntimeClasspathEntry(JavaCore.newContainerEntry(path), classpathProperty);
		entry.setJavaProject(project);
		return entry;
	}
		
	/**
	 * Returns a runtime classpath entry constructed from the given memento.
	 * 
	 * @param memento a memento for a runtime classpath entry
	 * @return runtime classpath entry
	 * @exception CoreException if unable to construct a runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newRuntimeClasspathEntry(String memento) throws CoreException {
		try {
			Element root = null;
			DocumentBuilder parser = LaunchingPlugin.getParser();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
												
			String id = root.getAttribute("id"); //$NON-NLS-1$
			if (id == null || id.length() == 0) {
				// assume an old format
				return new RuntimeClasspathEntry(root);
			}
			// get the extension & create a new one
			IRuntimeClasspathEntry2 entry = LaunchingPlugin.getDefault().newRuntimeClasspathEntry(id);
			NodeList list = root.getChildNodes();
			Node node = null;
			Element element = null;
			for (int i = 0; i < list.getLength(); i++) {
				node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					element = (Element)node;
					if ("memento".equals(element.getNodeName())) { //$NON-NLS-1$
						entry.initializeFrom(element);
					}
				}
			}
			return entry;
		} catch (SAXException e) {
			abort(LaunchingMessages.JavaRuntime_32, e);
		} catch (IOException e) {
			abort(LaunchingMessages.JavaRuntime_32, e);
		}
		return null;
	}
	
	/**
	 * Returns a runtime classpath entry that corresponds to the given
	 * classpath entry. The classpath entry may not be of type <code>CPE_SOURCE</code>
	 * or <code>CPE_CONTAINER</code>.
	 * 
	 * @param entry a classpath entry
	 * @return runtime classpath entry
	 * @since 2.0
	 */
	private static IRuntimeClasspathEntry newRuntimeClasspathEntry(IClasspathEntry entry) {
		return new RuntimeClasspathEntry(entry);
	}
			
	/**
	 * Computes and returns the default unresolved runtime classpath for the
	 * given project.
	 * 
	 * @param project the {@link IJavaProject} to compute the unresolved runtime classpath for
	 * @return runtime classpath entries
	 * @exception CoreException if unable to compute the runtime classpath
	 * @see IRuntimeClasspathEntry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] computeUnresolvedRuntimeClasspath(IJavaProject project) throws CoreException {
		IClasspathEntry[] entries = project.getRawClasspath();
		List<IRuntimeClasspathEntry> classpathEntries = new ArrayList<IRuntimeClasspathEntry>(3);
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_CONTAINER:
					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
					if (container != null) {
						switch (container.getKind()) {
							case IClasspathContainer.K_APPLICATION:
								// don't look at application entries
								break;
							case IClasspathContainer.K_DEFAULT_SYSTEM:
								classpathEntries.add(newRuntimeContainerClasspathEntry(container.getPath(), IRuntimeClasspathEntry.STANDARD_CLASSES, project));
								break;
							case IClasspathContainer.K_SYSTEM:
								classpathEntries.add(newRuntimeContainerClasspathEntry(container.getPath(), IRuntimeClasspathEntry.BOOTSTRAP_CLASSES, project));
								break;
						}
					}
					break;
				case IClasspathEntry.CPE_VARIABLE:
					if (JRELIB_VARIABLE.equals(entry.getPath().segment(0))) {
						IRuntimeClasspathEntry jre = newVariableRuntimeClasspathEntry(entry.getPath());
						jre.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
						classpathEntries.add(jre);
					}
					break;
				default:
					break;
			}
		}
		classpathEntries.add(newDefaultProjectClasspathEntry(project));
		return classpathEntries.toArray(new IRuntimeClasspathEntry[classpathEntries.size()]);
	}
	
	/**
	 * Computes and returns the unresolved source lookup path for the given launch
	 * configuration.
	 * 
	 * @param configuration launch configuration
	 * @return runtime classpath entries
	 * @exception CoreException if unable to compute the source lookup path
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] computeUnresolvedSourceLookupPath(ILaunchConfiguration configuration) throws CoreException {
		return getSourceLookupPathProvider(configuration).computeUnresolvedClasspath(configuration);
	}
	
	/**
	 * Resolves the given source lookup path, returning the resolved source lookup path
	 * in the context of the given launch configuration.
	 * 
	 * @param entries unresolved entries
	 * @param configuration launch configuration
	 * @return resolved entries
	 * @exception CoreException if unable to resolve the source lookup path
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] resolveSourceLookupPath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		return getSourceLookupPathProvider(configuration).resolveClasspath(entries, configuration);
	}
	
	/**
	 * Returns the classpath provider for the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @return classpath provider
	 * @exception CoreException if unable to resolve the path provider
	 * @since 2.0
	 */
	public static IRuntimeClasspathProvider getClasspathProvider(ILaunchConfiguration configuration) throws CoreException {
		String providerId = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String)null);
		IRuntimeClasspathProvider provider = null;
		if (providerId == null) {
			provider = fgDefaultClasspathProvider;
		} else {
			provider = getClasspathProviders().get(providerId);
			if (provider == null) {
				abort(NLS.bind(LaunchingMessages.JavaRuntime_26, new String[]{providerId}), null);
			}
		}
		return provider;
	}
		
	/**
	 * Returns the source lookup path provider for the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @return source lookup path provider
	 * @exception CoreException if unable to resolve the path provider
	 * @since 2.0
	 */
	public static IRuntimeClasspathProvider getSourceLookupPathProvider(ILaunchConfiguration configuration) throws CoreException {
		String providerId = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, (String)null);
		IRuntimeClasspathProvider provider = null;
		if (providerId == null) {
			provider = fgDefaultSourcePathProvider;
		} else {
			provider = getClasspathProviders().get(providerId);
			if (provider == null) {
				abort(NLS.bind(LaunchingMessages.JavaRuntime_27, new String[]{providerId}), null);
			}
		}
		return provider;
	}
		
	/**
	 * Returns resolved entries for the given entry in the context of the given
	 * launch configuration. If the entry is of kind
	 * <code>VARIABLE</code> or <code>CONTAINER</code>, variable and container
	 * resolvers are consulted. If the entry is of kind <code>PROJECT</code>,
	 * and the associated Java project specifies non-default output locations,
	 * the corresponding output locations are returned. Otherwise, the given
	 * entry is returned.
	 * <p>
	 * If the given entry is a variable entry, and a resolver is not registered,
	 * the entry itself is returned. If the given entry is a container, and a
	 * resolver is not registered, resolved runtime classpath entries are calculated
	 * from the associated container classpath entries, in the context of the project
	 * associated with the given launch configuration.
	 * </p>
	 * @param entry runtime classpath entry
	 * @param configuration launch configuration
	 * @return resolved runtime classpath entry
	 * @exception CoreException if unable to resolve
	 * @see IRuntimeClasspathEntryResolver
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.PROJECT:
				// if the project has multiple output locations, they must be returned
				IResource resource = entry.getResource();
				if (resource instanceof IProject) {
					IProject p = (IProject)resource;
					IJavaProject project = JavaCore.create(p);
					if (project == null || !p.isOpen() || !project.exists()) {
						return new IRuntimeClasspathEntry[0];
					}
					IRuntimeClasspathEntry[] entries = resolveOutputLocations(project, entry.getClasspathProperty());
					if (entries != null) {
						return entries;
					}
				} else {
					if (isOptional(entry.getClasspathEntry()))
						return new IRuntimeClasspathEntry[] {};
					abort(NLS.bind(LaunchingMessages.JavaRuntime_Classpath_references_non_existant_project___0__3, new String[]{entry.getPath().lastSegment()}), null);
				}
				break;
			case IRuntimeClasspathEntry.VARIABLE:
				IRuntimeClasspathEntryResolver resolver = getVariableResolver(entry.getVariableName());
				if (resolver == null) {
					IRuntimeClasspathEntry[] resolved = resolveVariableEntry(entry, null, configuration);
					if (resolved != null) {
						return resolved;
					}
					break;
				}
				return resolver.resolveRuntimeClasspathEntry(entry, configuration);
			case IRuntimeClasspathEntry.CONTAINER:
				resolver = getContainerResolver(entry.getVariableName());
				if (resolver == null) {
					return computeDefaultContainerEntries(entry, configuration);
				}
				return resolver.resolveRuntimeClasspathEntry(entry, configuration);
			case IRuntimeClasspathEntry.ARCHIVE:
				// verify the archive exists
				String location = entry.getLocation();
				if (location != null) {
					File file = new File(location);
					if (file.exists())
						break;
				}
				if (isOptional(entry.getClasspathEntry()))
					return new IRuntimeClasspathEntry[] {};
				abort(NLS.bind(LaunchingMessages.JavaRuntime_Classpath_references_non_existant_archive___0__4, new String[] { entry.getPath().toString() }), null);
			case IRuntimeClasspathEntry.OTHER:
				resolver = getContributedResolver(((IRuntimeClasspathEntry2)entry).getTypeId());
				return resolver.resolveRuntimeClasspathEntry(entry, configuration);
			default:
				break;
		}
		return new IRuntimeClasspathEntry[] {entry};
	}

	private static boolean isOptional(IClasspathEntry entry) {
		IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
		for (int i = 0, length = extraAttributes.length; i < length; i++) {
			IClasspathAttribute attribute = extraAttributes[i];
			if (IClasspathAttribute.OPTIONAL.equals(attribute.getName()) && "true".equals(attribute.getValue())) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	/**
	 * Default resolution for a classpath variable - resolve to an archive. Only
	 * one of project/configuration can be non-null.
	 * 
	 * @param entry the {@link IRuntimeClasspathEntry} to try and resolve
	 * @param project the project context or <code>null</code>
	 * @param configuration configuration context or <code>null</code>
	 * @return IRuntimeClasspathEntry[]
	 * @throws CoreException if a problem is encountered trying to resolve the given classpath entry
	 */
	private static IRuntimeClasspathEntry[] resolveVariableEntry(IRuntimeClasspathEntry entry, IJavaProject project, ILaunchConfiguration configuration) throws CoreException {
		// default resolution - an archive
		IPath archPath = JavaCore.getClasspathVariable(entry.getVariableName());
		if (archPath != null) {
			if (entry.getPath().segmentCount() > 1) {
				archPath = archPath.append(entry.getPath().removeFirstSegments(1));
			}
			IPath srcPath = null;
			IPath srcVar = entry.getSourceAttachmentPath();
			IPath srcRootPath = null;
			IPath srcRootVar = entry.getSourceAttachmentRootPath();
			if (archPath != null && !archPath.isEmpty()) {
				if (srcVar != null && !srcVar.isEmpty()) {
					srcPath = JavaCore.getClasspathVariable(srcVar.segment(0));
					if (srcPath != null) {
						if (srcVar.segmentCount() > 1) {
							srcPath = srcPath.append(srcVar.removeFirstSegments(1));
						}
						if (srcRootVar != null && !srcRootVar.isEmpty()) {
							srcRootPath = JavaCore.getClasspathVariable(srcRootVar.segment(0));
							if (srcRootPath != null) {
								if (srcRootVar.segmentCount() > 1) {
									srcRootPath = srcRootPath.append(srcRootVar.removeFirstSegments(1));
								}
							}
						}
					}
				}
				// now resolve the archive (recursively)
				IClasspathEntry archEntry = JavaCore.newLibraryEntry(archPath, srcPath, srcRootPath, entry.getClasspathEntry().isExported());
				IRuntimeClasspathEntry runtimeArchEntry = newRuntimeClasspathEntry(archEntry);
				runtimeArchEntry.setClasspathProperty(entry.getClasspathProperty());
				if (configuration == null) {
					return resolveRuntimeClasspathEntry(runtimeArchEntry, project);
				}
				return resolveRuntimeClasspathEntry(runtimeArchEntry, configuration);
			}
		}
		return null;
	}
	
	/**
	 * Returns runtime classpath entries corresponding to the output locations
	 * of the given project, or null if the project only uses the default
	 * output location.
	 * 
	 * @param project the {@link IJavaProject} to resolve the output locations for
	 * @param classpathProperty the type of classpath entries to create
	 * @return IRuntimeClasspathEntry[] or <code>null</code>
	 * @throws CoreException if output resolution encounters a problem
	 */
	private static IRuntimeClasspathEntry[] resolveOutputLocations(IJavaProject project, int classpathProperty) throws CoreException {
		List<IPath> nonDefault = new ArrayList<IPath>();
		if (project.exists() && project.getProject().isOpen()) {
			IClasspathEntry entries[] = project.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry classpathEntry = entries[i];
				if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = classpathEntry.getOutputLocation();
					if (path != null) {
						nonDefault.add(path);
					}
				}
			}
		}
		if (nonDefault.isEmpty()) {
			return null;
		}
		// add the default location if not already included
		IPath def = project.getOutputLocation();
		if (!nonDefault.contains(def)) {
			nonDefault.add(def);
		}
		IRuntimeClasspathEntry[] locations = new IRuntimeClasspathEntry[nonDefault.size()];
		for (int i = 0; i < locations.length; i++) {
			IClasspathEntry newEntry = JavaCore.newLibraryEntry(nonDefault.get(i), null, null);
			locations[i] = new RuntimeClasspathEntry(newEntry);
			locations[i].setClasspathProperty(classpathProperty);
		}
		return locations;
	}
	
	/**
	 * Returns resolved entries for the given entry in the context of the given
	 * Java project. If the entry is of kind
	 * <code>VARIABLE</code> or <code>CONTAINER</code>, variable and container
	 * resolvers are consulted. If the entry is of kind <code>PROJECT</code>,
	 * and the associated Java project specifies non-default output locations,
	 * the corresponding output locations are returned. Otherwise, the given
	 * entry is returned.
	 * <p>
	 * If the given entry is a variable entry, and a resolver is not registered,
	 * the entry itself is returned. If the given entry is a container, and a
	 * resolver is not registered, resolved runtime classpath entries are calculated
	 * from the associated container classpath entries, in the context of the
	 * given project.
	 * </p>
	 * @param entry runtime classpath entry
	 * @param project Java project context
	 * @return resolved runtime classpath entry
	 * @exception CoreException if unable to resolve
	 * @see IRuntimeClasspathEntryResolver
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.PROJECT:
				// if the project has multiple output locations, they must be returned
				IResource resource = entry.getResource();
				if (resource instanceof IProject) {
					IProject p = (IProject)resource;
					IJavaProject jp = JavaCore.create(p);
					if (jp != null && p.isOpen() && jp.exists()) {
						IRuntimeClasspathEntry[] entries = resolveOutputLocations(jp, entry.getClasspathProperty());
						if (entries != null) {
							return entries;
						}
					} else {
						return new IRuntimeClasspathEntry[0];
					}
				}
				break;
			case IRuntimeClasspathEntry.VARIABLE:
				IRuntimeClasspathEntryResolver resolver = getVariableResolver(entry.getVariableName());
				if (resolver == null) {
					IRuntimeClasspathEntry[] resolved = resolveVariableEntry(entry, project, null);
					if (resolved != null) {
						return resolved;
					}
					break;
				}
				return resolver.resolveRuntimeClasspathEntry(entry, project);
			case IRuntimeClasspathEntry.CONTAINER:
				resolver = getContainerResolver(entry.getVariableName());
				if (resolver == null) {
					return computeDefaultContainerEntries(entry, project);
				}
				return resolver.resolveRuntimeClasspathEntry(entry, project);
			case IRuntimeClasspathEntry.OTHER:
				resolver = getContributedResolver(((IRuntimeClasspathEntry2)entry).getTypeId());
				return resolver.resolveRuntimeClasspathEntry(entry, project);
			default:
				break;
		}
		return new IRuntimeClasspathEntry[] {entry};
	}
		
	/**
	 * Performs default resolution for a container entry.
	 * Delegates to the Java model.
	 * @param entry the {@link IRuntimeClasspathEntry} to compute default container entries for
	 * @param config the backing {@link ILaunchConfiguration}
	 * @return the complete listing of default container entries or an empty list, never <code>null</code>
	 * @throws CoreException if the computation encounters a problem
	 */
	private static IRuntimeClasspathEntry[] computeDefaultContainerEntries(IRuntimeClasspathEntry entry, ILaunchConfiguration config) throws CoreException {
		IJavaProject project = entry.getJavaProject();
		if (project == null) {
			project = getJavaProject(config);
		}
		return computeDefaultContainerEntries(entry, project);
	}
	
	/**
	 * Performs default resolution for a container entry.
	 * Delegates to the Java model.
	 * @param entry the {@link IRuntimeClasspathEntry} to compute default container entries for
	 * @param project the backing {@link IJavaProject}
	 * @return the complete listing of default container entries or an empty list, never <code>null</code>
	 * @throws CoreException if the computation encounters a problem
	 */
	private static IRuntimeClasspathEntry[] computeDefaultContainerEntries(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		if (project == null || entry == null) {
			// cannot resolve without entry or project context
			return new IRuntimeClasspathEntry[0];
		}
		IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
		if (container == null) {
			abort(NLS.bind(LaunchingMessages.JavaRuntime_Could_not_resolve_classpath_container___0__1, new String[]{entry.getPath().toString()}), null);
			// execution will not reach here - exception will be thrown
			return null;
		}
		IClasspathEntry[] cpes = container.getClasspathEntries();
		int property = -1;
		switch (container.getKind()) {
			case IClasspathContainer.K_APPLICATION:
				property = IRuntimeClasspathEntry.USER_CLASSES;
				break;
			case IClasspathContainer.K_DEFAULT_SYSTEM:
				property = IRuntimeClasspathEntry.STANDARD_CLASSES;
				break;
			case IClasspathContainer.K_SYSTEM:
				property = IRuntimeClasspathEntry.BOOTSTRAP_CLASSES;
				break;
		}
		List<IRuntimeClasspathEntry> resolved = new ArrayList<IRuntimeClasspathEntry>(cpes.length);
		List<IJavaProject> projects = fgProjects.get();
		Integer count = fgEntryCount.get();
		if (projects == null) {
			projects = new ArrayList<IJavaProject>();
			fgProjects.set(projects);
			count = new Integer(0);
		}
		int intCount = count.intValue();
		intCount++;
		fgEntryCount.set(new Integer(intCount));
		try {
			for (int i = 0; i < cpes.length; i++) {
				IClasspathEntry cpe = cpes[i];
				if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(cpe.getPath().segment(0));
					IJavaProject jp = JavaCore.create(p);
					if (!projects.contains(jp)) {
						projects.add(jp);
						IRuntimeClasspathEntry classpath = newDefaultProjectClasspathEntry(jp);
						IRuntimeClasspathEntry[] entries = resolveRuntimeClasspathEntry(classpath, jp);
						for (int j = 0; j < entries.length; j++) {
							IRuntimeClasspathEntry e = entries[j];
							if (!resolved.contains(e)) {
								resolved.add(entries[j]);
							}
						}
					}
				} else {
					IRuntimeClasspathEntry e = newRuntimeClasspathEntry(cpe);
					if (!resolved.contains(e)) {
						resolved.add(e);
					}
				}
			}
		} finally {
			intCount--;
			if (intCount == 0) {
				fgProjects.set(null);
				fgEntryCount.set(null);
			} else {
				fgEntryCount.set(new Integer(intCount));
			}
		}
		// set classpath property
		IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[resolved.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = resolved.get(i);
			result[i].setClasspathProperty(property);
		}
		return result;
	}
			
	/**
	 * Computes and returns the unresolved class path for the given launch configuration.
	 * Variable and container entries are unresolved.
	 * 
	 * @param configuration launch configuration
	 * @return unresolved runtime classpath entries
	 * @exception CoreException if unable to compute the classpath
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] computeUnresolvedRuntimeClasspath(ILaunchConfiguration configuration) throws CoreException {
		return getClasspathProvider(configuration).computeUnresolvedClasspath(configuration);
	}
	
	/**
	 * Resolves the given classpath, returning the resolved classpath
	 * in the context of the given launch configuration.
	 *
	 * @param entries unresolved classpath
	 * @param configuration launch configuration
	 * @return resolved runtime classpath entries
	 * @exception CoreException if unable to compute the classpath
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] resolveRuntimeClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		return getClasspathProvider(configuration).resolveClasspath(entries, configuration);
	}
	
	/**
	 * Return the <code>IJavaProject</code> referenced in the specified configuration or
	 * <code>null</code> if none. This method looks for the existence of the {@link IJavaLaunchConfigurationConstants#ATTR_PROJECT_NAME}
	 * attribute in the given configuration.
	 * 
	 * @param configuration the {@link ILaunchConfiguration} to try and compute the {@link IJavaProject} from
	 * @return the referenced {@link IJavaProject} or <code>null</code>
	 * @exception CoreException if the referenced Java project does not exist
	 * @since 2.0
	 */
	public static IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) {
			return null;
		}
		IJavaProject javaProject = getJavaModel().getJavaProject(projectName);
		if (javaProject != null && javaProject.getProject().exists() && !javaProject.getProject().isOpen()) {
			abort(NLS.bind(LaunchingMessages.JavaRuntime_28, new String[] {configuration.getName(), projectName}), IJavaLaunchConfigurationConstants.ERR_PROJECT_CLOSED, null);
		}
		if ((javaProject == null) || !javaProject.exists()) {
			abort(NLS.bind(LaunchingMessages.JavaRuntime_Launch_configuration__0__references_non_existing_project__1___1, new String[] {configuration.getName(), projectName}), IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT, null);
		}
		return javaProject;
	}
				
	/**
	 * Convenience method to get the java model.
	 * @return the {@link IJavaModel} made against the {@link IWorkspaceRoot}
	 */
	private static IJavaModel getJavaModel() {
		return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	/**
	 * Returns the VM install for the given launch configuration.
	 * The VM install is determined in the following prioritized way:
	 * <ol>
	 * <li>The VM install is explicitly specified on the launch configuration
	 *  via the <code>ATTR_JRE_CONTAINER_PATH</code> attribute (since 3.2).</li>
	 * <li>The VM install is explicitly specified on the launch configuration
	 * 	via the <code>ATTR_VM_INSTALL_TYPE</code> and <code>ATTR_VM_INSTALL_ID</code>
	 *  attributes.</li>
	 * <li>If no explicit VM install is specified, the VM install associated with
	 * 	the launch configuration's project is returned.</li>
	 * <li>If no project is specified, or the project does not specify a custom
	 * 	VM install, the workspace default VM install is returned.</li>
	 * </ol>
	 * 
	 * @param configuration launch configuration
	 * @return VM install
	 * @exception CoreException if unable to compute a VM install
	 * @since 2.0
	 */
	public static IVMInstall computeVMInstall(ILaunchConfiguration configuration) throws CoreException {
		String jreAttr = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, (String)null);
		if (jreAttr == null) {
			@SuppressWarnings("deprecation")
			String type = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
			if (type == null) {
				IJavaProject proj = getJavaProject(configuration);
				if (proj != null) {
					IVMInstall vm = getVMInstall(proj);
					if (vm != null) {
						return vm;
					}
				}
			} else {
				@SuppressWarnings("deprecation")
				String name = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
				return resolveVM(type, name, configuration);
			}
		} else {
			IPath jrePath = Path.fromPortableString(jreAttr);
			IClasspathEntry entry = JavaCore.newContainerEntry(jrePath);
			IRuntimeClasspathEntryResolver2 resolver = getVariableResolver(jrePath.segment(0));
			if (resolver != null) {
				return resolver.resolveVMInstall(entry);
			}
			resolver = getContainerResolver(jrePath.segment(0));
			if (resolver != null) {
				return resolver.resolveVMInstall(entry);
			}
		}
		
		return getDefaultVMInstall();
	}
	/**
	 * Returns the VM of the given type with the specified name.
	 * 
	 * @param type VM type identifier
	 * @param name VM name
	 * @param configuration the backing {@link ILaunchConfiguration}
	 * @return VM install
	 * @exception CoreException if unable to resolve
	 * @since 3.2
	 */
	private static IVMInstall resolveVM(String type, String name, ILaunchConfiguration configuration) throws CoreException {
		IVMInstallType vt = getVMInstallType(type);
		if (vt == null) {
			// error type does not exist
			abort(NLS.bind(LaunchingMessages.JavaRuntime_Specified_VM_install_type_does_not_exist___0__2, new String[] {type}), null);
		}
		IVMInstall vm = null;
		// look for a name
		if (name == null) {
			// error - type specified without a specific install (could be an old config that specified a VM ID)
			// log the error, but choose the default VM.
			LaunchingPlugin.log(new Status(IStatus.WARNING, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_VM_INSTALL, NLS.bind("VM not fully specified in launch configuration {0} - missing VM name. Reverting to default VM.", new String[] {configuration.getName()}), null));  //$NON-NLS-1$
			return getDefaultVMInstall();
		}
		vm = vt.findVMInstallByName(name);
		if (vm == null) {
			// error - install not found
			abort(NLS.bind(LaunchingMessages.JavaRuntime_Specified_VM_install_not_found__type__0___name__1__2, new String[] {vt.getName(), name}), null);
		} else {
			return vm;
		}
		// won't reach here
		return null;
	}
	
	/**
	 * Throws a core exception with an internal error status.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @throws CoreException a {@link CoreException} wrapper
	 */
	private static void abort(String message, Throwable exception) throws CoreException {
		abort(message, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, exception);
	}
		
		
	/**
	 * Throws a core exception with an internal error status.
	 * 
	 * @param message the status message
	 * @param code status code
	 * @param exception lower level exception associated with the
	 * 
	 *  error, or <code>null</code> if none
	 * @throws CoreException a {@link CoreException} wrapper
	 */
	private static void abort(String message, int code, Throwable exception) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), code, message, exception));
	}
		
	/**
	 * Computes the default application classpath entries for the given
	 * project.
	 * 
	 * @param	jproject The project to compute the classpath for
	 * @return	The computed classpath. May be empty, but not null.
	 * @throws	CoreException if unable to compute the default classpath
	 */
	public static String[] computeDefaultRuntimeClassPath(IJavaProject jproject) throws CoreException {
		IRuntimeClasspathEntry[] unresolved = computeUnresolvedRuntimeClasspath(jproject);
		// 1. remove bootpath entries
		// 2. resolve & translate to local file system paths
		List<String> resolved = new ArrayList<String>(unresolved.length);
		for (int i = 0; i < unresolved.length; i++) {
			IRuntimeClasspathEntry entry = unresolved[i];
			if (entry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				IRuntimeClasspathEntry[] entries = resolveRuntimeClasspathEntry(entry, jproject);
				for (int j = 0; j < entries.length; j++) {
					String location = entries[j].getLocation();
					if (location != null) {
						resolved.add(location);
					}
				}
			}
		}
		return resolved.toArray(new String[resolved.size()]);
	}
		
	/**
	 * Saves the VM configuration information to the preferences. This includes
	 * the following information:
	 * <ul>
	 * <li>The list of all defined IVMInstall instances.</li>
	 * <li>The default VM</li>
	 * <ul>
	 * This state will be read again upon first access to VM
	 * configuration information.
	 * @throws CoreException if trying to save the current state of VMs encounters a problem
	 */
	public static void saveVMConfiguration() throws CoreException {
		if (fgVMTypes == null) {
			// if the VM types have not been instantiated, there can be no changes.
			return;
		}
		String xml = getVMsAsXML();
		InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN).put(PREF_VM_XML, xml);
		savePreferences();
	}
	
	/**
	 * Returns the listing of currently installed VMs as a single XML file
	 * @return an XML representation of all of the currently installed VMs
	 * @throws CoreException if trying to compute the XML for the VM state encounters a problem
	 */
	private static String getVMsAsXML() throws CoreException {
		VMDefinitionsContainer container = new VMDefinitionsContainer();
		container.setDefaultVMInstallCompositeID(getDefaultVMId());
		container.setDefaultVMInstallConnectorTypeID(getDefaultVMConnectorId());
		IVMInstallType[] vmTypes = getVMInstallTypes();
		IVMInstall[] vms = null;
		for (int i = 0; i < vmTypes.length; ++i) {
			vms = vmTypes[i].getVMInstalls();
			for (int j = 0; j < vms.length; j++) {
				container.addVM(vms[j]);
			}
		}
		return container.getAsXML();
	}
	
	/**
	 * This method loads installed JREs based an existing user preference
	 * or old VM configurations file. The VMs found in the preference
	 * or VM configurations file are added to the given VM definitions container.
	 * 
	 * Returns whether the user preferences should be set - i.e. if it was
	 * not already set when initialized.
	 * @param vmDefs the container to add persisted VM information to
	 * @return whether the user preferences should be set
	 * @throws IOException if reading the {@link #PREF_VM_XML} preference stream encounters a problem
	 */
	private static boolean addPersistedVMs(VMDefinitionsContainer vmDefs) throws IOException {
		// Try retrieving the VM preferences from the preference store
		String vmXMLString = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN).get(PREF_VM_XML, ""); //$NON-NLS-1$
		
		// If the preference was found, load VMs from it into memory
		if (vmXMLString.length() > 0) {
			try {
				ByteArrayInputStream inputStream = new ByteArrayInputStream(vmXMLString.getBytes("UTF8")); //$NON-NLS-1$
				VMDefinitionsContainer.parseXMLIntoContainer(inputStream, vmDefs);
				return false;
			} catch (IOException ioe) {
				LaunchingPlugin.log(ioe);
			}
		} else {
			// Otherwise, look for the old file that previously held the VM definitions
			IPath stateLocation= LaunchingPlugin.getDefault().getStateLocation();
			IPath stateFile= stateLocation.append("vmConfiguration.xml"); //$NON-NLS-1$
			File file = new File(stateFile.toOSString());
			
			if (file.exists()) {
				// If file exists, load VM definitions from it into memory and write the definitions to
				// the preference store WITHOUT triggering any processing of the new value
				InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
				VMDefinitionsContainer.parseXMLIntoContainer(fileInputStream, vmDefs);
			}
		}
		return true;
	}
	
	/**
	 * Loads contributed VM installs
	 * @param vmDefs the container to add contributed VM install information to
	 * @since 3.2
	 */
	private static void addVMExtensions(VMDefinitionsContainer vmDefs) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, JavaRuntime.EXTENSION_POINT_VM_INSTALLS);
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements();
		for (int i = 0; i < configs.length; i++) {
			IConfigurationElement element = configs[i];
			try {
				if ("vmInstall".equals(element.getName())) { //$NON-NLS-1$
					String vmType = element.getAttribute("vmInstallType"); //$NON-NLS-1$
					if (vmType == null) {
						abort(NLS.bind("Missing required vmInstallType attribute for vmInstall contributed by {0}", //$NON-NLS-1$
								new String[]{element.getContributor().getName()}), null);
					}
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						abort(NLS.bind("Missing required id attribute for vmInstall contributed by {0}", //$NON-NLS-1$
								new String[]{element.getContributor().getName()}), null);
					}
					IVMInstallType installType = getVMInstallType(vmType);
					if (installType == null) {
						abort(NLS.bind("vmInstall {0} contributed by {1} references undefined VM install type {2}", //$NON-NLS-1$
								new String[]{id, element.getContributor().getName(), vmType}), null);
					}
					IVMInstall install = installType.findVMInstall(id);
					if (install == null) {
						// only load/create if first time we've seen this VM install
						String name = element.getAttribute("name"); //$NON-NLS-1$
						if (name == null) {
							abort(NLS.bind("vmInstall {0} contributed by {1} missing required attribute name", //$NON-NLS-1$
									new String[]{id, element.getContributor().getName()}), null);
						}
						String home = element.getAttribute("home"); //$NON-NLS-1$
						if (home == null) {
							abort(NLS.bind("vmInstall {0} contributed by {1} missing required attribute home", //$NON-NLS-1$
									new String[]{id, element.getContributor().getName()}), null);
						}
						String javadoc = element.getAttribute("javadocURL"); //$NON-NLS-1$
						String vmArgs = element.getAttribute("vmArgs"); //$NON-NLS-1$
						VMStandin standin = null;
						home = substitute(home);
						File homeDir = new File(home);
                        if (homeDir.exists()) {
                            try {
                            	// adjust for relative path names
                                home = homeDir.getCanonicalPath();
                                homeDir = new File(home);
                            } catch (IOException e) {
                            }
                        }
						if (EEVMType.ID_EE_VM_TYPE.equals(installType.getId())) {
							standin = createVMFromDefinitionFile(homeDir, name, id);
						} else {
							standin = new VMStandin(installType, id);
							standin.setName(name);
	                        IStatus status = installType.validateInstallLocation(homeDir);
	                        if (!status.isOK()) {
	                        	abort(NLS.bind("Illegal install location {0} for vmInstall {1} contributed by {2}: {3}", //$NON-NLS-1$
	                        			new String[]{home, id, element.getContributor().getName(), status.getMessage()}), null);
	                        }
	                        standin.setInstallLocation(homeDir);
							if (javadoc != null) {
								try {
									standin.setJavadocLocation(new URL(javadoc));
								} catch (MalformedURLException e) {
									abort(NLS.bind("Illegal javadocURL attribute for vmInstall {0} contributed by {1}", //$NON-NLS-1$
											new String[]{id, element.getContributor().getName()}), e);
								}
							}
							// allow default arguments to be specified by VM install type if no explicit arguments
							if (vmArgs == null) {
								if (installType instanceof AbstractVMInstallType) {
									AbstractVMInstallType type = (AbstractVMInstallType) installType;
									vmArgs = type.getDefaultVMArguments(homeDir);
								}
							}
							if (vmArgs != null) {
								standin.setVMArgs(vmArgs);
							}
	                        IConfigurationElement[] libraries = element.getChildren("library"); //$NON-NLS-1$
	                        LibraryLocation[] locations = null;
	                        if (libraries.length > 0) {
	                            locations = new LibraryLocation[libraries.length];
	                            for (int j = 0; j < libraries.length; j++) {
	                                IConfigurationElement library = libraries[j];
	                                String libPathStr = library.getAttribute("path"); //$NON-NLS-1$
	                                if (libPathStr == null) {
	                                    abort(NLS.bind("library for vmInstall {0} contributed by {1} missing required attribute libPath", //$NON-NLS-1$
	                                            new String[]{id, element.getContributor().getName()}), null);
	                                }
	                                String sourcePathStr = library.getAttribute("sourcePath"); //$NON-NLS-1$
	                                String packageRootStr = library.getAttribute("packageRootPath"); //$NON-NLS-1$
	                                String javadocOverride = library.getAttribute("javadocURL"); //$NON-NLS-1$
	                                URL url = null;
	                                if (javadocOverride != null) {
	                                    try {
	                                        url = new URL(javadocOverride);
	                                    } catch (MalformedURLException e) {
	                                        abort(NLS.bind("Illegal javadocURL attribute specified for library {0} for vmInstall {1} contributed by {2}" //$NON-NLS-1$
	                                                ,new String[]{libPathStr, id, element.getContributor().getName()}), e);
	                                    }
	                                }
	                                IPath homePath = new Path(home);
	                                IPath libPath = homePath.append(substitute(libPathStr));
	                                IPath sourcePath = Path.EMPTY;
	                                if (sourcePathStr != null) {
	                                    sourcePath = homePath.append(substitute(sourcePathStr));
	                                }
	                                IPath packageRootPath = Path.EMPTY;
	                                if (packageRootStr != null) {
	                                    packageRootPath = new Path(substitute(packageRootStr));
	                                }
	                                locations[j] = new LibraryLocation(libPath, sourcePath, packageRootPath, url);
	                            }
	                        }
	                        standin.setLibraryLocations(locations);
						}
                        // in case the contributed JRE attributes changed, remove it first, then add
                        vmDefs.removeVM(standin);
                        vmDefs.addVM(standin);
					}
                    fgContributedVMs.add(id);
				} else {
					abort(NLS.bind("Illegal element {0} in vmInstalls extension contributed by {1}", //$NON-NLS-1$
							new String[]{element.getName(), element.getContributor().getName()}), null);
				}
			} catch (CoreException e) {
				LaunchingPlugin.log(e);
			}
		}
	}
    
    /**
     * Performs string substitution on the given expression.
     * 
     * @param expression the expression to evaluate
     * @return expression after string substitution
     * @throws CoreException if the substitution encounters a problem
     * @since 3.2
     */
    private static String substitute(String expression) throws CoreException {
        return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression);
    }
    
    /**
     * Returns whether the VM install with the specified id was contributed via
     * the vmInstalls extension point.
     * 
     * @param id VM id
     * @return whether the VM install was contributed via extension point
     * @since 3.2
     */
    public static boolean isContributedVMInstall(String id) {
        getVMInstallTypes(); // ensure VMs are initialized
        return fgContributedVMs.contains(id);
    }
	
	/**
	 * Evaluates library locations for a IVMInstall. If no library locations are set on the install, a default
	 * location is evaluated and checked if it exists.
	 * @param vm the {@link IVMInstall} to compute locations for
	 * @return library locations with paths that exist or are empty
	 * @since 2.0
	 */
	public static LibraryLocation[] getLibraryLocations(IVMInstall vm)  {
		IPath[] libraryPaths;
		IPath[] sourcePaths;
		IPath[] sourceRootPaths;
		URL[] javadocLocations;
		LibraryLocation[] locations= vm.getLibraryLocations();
		if (locations == null) {
            URL defJavaDocLocation = vm.getJavadocLocation();
			File installLocation = vm.getInstallLocation();
			if (installLocation == null) {
				return new LibraryLocation[0];
			}
			LibraryLocation[] dflts= vm.getVMInstallType().getDefaultLibraryLocations(installLocation);
			libraryPaths = new IPath[dflts.length];
			sourcePaths = new IPath[dflts.length];
			sourceRootPaths = new IPath[dflts.length];
			javadocLocations= new URL[dflts.length];
			for (int i = 0; i < dflts.length; i++) {
				libraryPaths[i]= dflts[i].getSystemLibraryPath();
                if (defJavaDocLocation == null) {
                    javadocLocations[i]= dflts[i].getJavadocLocation();
                } else {
                    javadocLocations[i]= defJavaDocLocation;
                }
				if (!libraryPaths[i].toFile().isFile()) {
					libraryPaths[i]= Path.EMPTY;
				}
				
				sourcePaths[i]= dflts[i].getSystemLibrarySourcePath();
				if (sourcePaths[i].toFile().isFile()) {
					sourceRootPaths[i]= dflts[i].getPackageRootPath();
				} else {
					sourcePaths[i]= Path.EMPTY;
					sourceRootPaths[i]= Path.EMPTY;
				}
			}
		} else {
			libraryPaths = new IPath[locations.length];
			sourcePaths = new IPath[locations.length];
			sourceRootPaths = new IPath[locations.length];
			javadocLocations= new URL[locations.length];
			for (int i = 0; i < locations.length; i++) {
				libraryPaths[i]= locations[i].getSystemLibraryPath();
				sourcePaths[i]= locations[i].getSystemLibrarySourcePath();
				sourceRootPaths[i]= locations[i].getPackageRootPath();
				javadocLocations[i]= locations[i].getJavadocLocation();
			}
		}
		locations = new LibraryLocation[sourcePaths.length];
		for (int i = 0; i < sourcePaths.length; i++) {
			locations[i] = new LibraryLocation(libraryPaths[i], sourcePaths[i], sourceRootPaths[i], javadocLocations[i]);
		}
		return locations;
	}
	
	/**
	 * Detect the VM that Eclipse is running on.
	 * 
	 * @return a VM stand-in representing the VM that Eclipse is running on, or
	 * <code>null</code> if unable to detect the runtime VM
	 */
	private static VMStandin detectEclipseRuntime() {
		// Try to detect a VM for each declared VM type
		IVMInstallType[] vmTypes= getVMInstallTypes();
		// If we are running from an EE file, setup the VM from it
		for (int i = 0; i < vmTypes.length; i++) {
			if (vmTypes[i] instanceof EEVMType){
				String eeFileName = System.getProperty("ee.filename"); //$NON-NLS-1$
				if (eeFileName != null){
					File vmFile = new File(eeFileName);
					if (vmFile.isDirectory()){
						vmFile = new File(vmFile, "default.ee"); //$NON-NLS-1$
					}
					if (vmFile.isFile()){
						// Make sure the VM id is unique
						long unique = System.currentTimeMillis();
						while (vmTypes[i].findVMInstall(String.valueOf(unique)) != null) {
							unique++;
						}

						// Create a stand-in for the detected VM and add it to the result collector
						String vmID = String.valueOf(unique);
						try{
							return createVMFromDefinitionFile(vmFile, "", vmID); //$NON-NLS-1$
						} catch (CoreException e){
							// The file was not a valid EE file, continue the detection process
						}
					}
				}
			}
		}

		// Try to create a VM install using the install location
		for (int i = 0; i < vmTypes.length; i++) {
			File detectedLocation= vmTypes[i].detectInstallLocation();
			if (detectedLocation != null) {
				
				// Make sure the VM id is unique
				long unique = System.currentTimeMillis();
				IVMInstallType vmType = vmTypes[i];
				while (vmType.findVMInstall(String.valueOf(unique)) != null) {
					unique++;
				}

				// Create a stand-in for the detected VM and add it to the result collector
				String vmID = String.valueOf(unique);
				VMStandin detectedVMStandin = new VMStandin(vmType, vmID);
				detectedVMStandin.setInstallLocation(detectedLocation);
				detectedVMStandin.setName(generateDetectedVMName(detectedVMStandin));
				if (vmType instanceof AbstractVMInstallType) {
				    AbstractVMInstallType abs = (AbstractVMInstallType)vmType;
				    URL url = abs.getDefaultJavadocLocation(detectedLocation);
				    detectedVMStandin.setJavadocLocation(url);
				    String arguments = abs.getDefaultVMArguments(detectedLocation);
					if (arguments != null) {
						detectedVMStandin.setVMArgs(arguments);
					}
				}
				return detectedVMStandin;
			}
		}
		return null;
	}
	
	/**
	 * Returns whether the specified option is the same in the given
	 * map and preference store.
	 * 
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>Returns <code>false</code> if the option is only contained in one map</li>
	 * <li>Returns <code>true</code> if the option is not contained in either map</li>
	 * </ul> 
	 * </p>
	 * 
	 * @param optionName name of option to test
	 * @param options map of options
	 * @param prefStore preferences node
	 * @return whether the options are the same in both maps
	 */
	private static boolean equals(String optionName, Map<?, ?> options, org.osgi.service.prefs.Preferences prefStore) {
		String dummy= new String();
		String prefValue= prefStore.get(optionName, dummy);
		if (prefValue != null && prefValue != dummy) {
			return options.containsKey(optionName) &&
				equals(prefValue, options.get(optionName));
		}
		return !options.containsKey(optionName);
	}
	
	/**
	 * Returns whether the objects are equal or both <code>null</code>
	 * 
	 * @param o1 an object
	 * @param o2 an object
	 * @return whether the objects are equal or both <code>null</code>
	 */
	private static boolean equals(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}
	
	/**
	 * Make the name of a detected VM stand out.
	 * @param vm the VM to generate a name for
	 * @return the new name or <code>JRE</code>
	 */
	private static String generateDetectedVMName(IVMInstall vm) {
		String name = vm.getInstallLocation().getName();
		name = name.trim();
		if (name.length() == 0) {
			name = LaunchingMessages.JavaRuntime_25;
		}
		return name;
	}
	
	/**
	 * Creates and returns a classpath entry describing
	 * the JRE_LIB classpath variable.
	 * 
	 * @return a new IClasspathEntry that describes the JRE_LIB classpath variable
	 */
	public static IClasspathEntry getJREVariableEntry() {
		return JavaCore.newVariableEntry(
			new Path(JRELIB_VARIABLE),
			new Path(JRESRC_VARIABLE),
			new Path(JRESRCROOT_VARIABLE)
		);
	}
	
	/**
	 * Creates and returns a classpath entry describing
	 * the default JRE container entry.
	 * 
	 * @return a new IClasspathEntry that describes the default JRE container entry
	 * @since 2.0
	 */
	public static IClasspathEntry getDefaultJREContainerEntry() {
		return JavaCore.newContainerEntry(newDefaultJREContainerPath());
	}
	
	/**
	 * Returns a path for the JRE classpath container identifying the
	 * default VM install.
	 * 
	 * @return classpath container path
	 * @since 3.2
	 */
	public static IPath newDefaultJREContainerPath() {
		return new Path(JRE_CONTAINER);
	}
	
	/**
	 * Returns a path for the JRE classpath container identifying the
	 * specified VM install by type and name.
	 * 
	 * @param vm VM install
	 * @return classpath container path
	 * @since 3.2
	 */
	public static IPath newJREContainerPath(IVMInstall vm) {
		return newJREContainerPath(vm.getVMInstallType().getId(), vm.getName());
	}
	
	/**
	 * Returns a path for the JRE classpath container identifying the
	 * specified VM install by type and name.
	 * 
	 * @param typeId VM install type identifier
	 * @param name VM install name
	 * @return classpath container path
	 * @since 3.2
	 */
	public static IPath newJREContainerPath(String typeId, String name) {
		IPath path = newDefaultJREContainerPath();
		path = path.append(typeId);
		path = path.append(name);
		return path;
	}
	
	/**
	 * Returns a path for the JRE classpath container identifying the
	 * specified execution environment.
	 * 
	 * @param environment execution environment
	 * @return classpath container path
	 * @since 3.2
	 */
	public static IPath newJREContainerPath(IExecutionEnvironment environment) {
		IPath path = newDefaultJREContainerPath();
		path = path.append(StandardVMType.ID_STANDARD_VM_TYPE);
		path = path.append(JREContainerInitializer.encodeEnvironmentId(environment.getId()));
		return path;
	}
	
	/**
	 * Returns the JRE referenced by the specified JRE classpath container
	 * path or <code>null</code> if none.
	 * 
	 * @param jreContainerPath the path to the container to try and resolve the {@link IVMInstall} from
	 * @return JRE referenced by the specified JRE classpath container
	 *  path or <code>null</code>
	 * @since 3.2
	 */
	public static IVMInstall getVMInstall(IPath jreContainerPath) {
		return JREContainerInitializer.resolveVM(jreContainerPath);
	}
	
	/**
	 * Returns the identifier of the VM install type referenced by the
	 * given JRE classpath container path, or <code>null</code> if none.
	 * 
	 * @param jreContainerPath the path to the container to try and resolve the {@link IVMInstallType} id from
	 * @return VM install type identifier or <code>null</code>
	 * @since 3.2
	 */
	public static String getVMInstallTypeId(IPath jreContainerPath) {
		if (JREContainerInitializer.isExecutionEnvironment(jreContainerPath)) {
			return null;
		}
		return JREContainerInitializer.getVMTypeId(jreContainerPath);
	}

	/**
	 * Returns the name of the VM install referenced by the
	 * given JRE classpath container path, or <code>null</code> if none.
	 * 
	 * @param jreContainerPath the path to the container to try an resolve the {@link IVMInstall} name from
	 * @return VM name or <code>null</code>
	 * @since 3.2
	 */
	public static String getVMInstallName(IPath jreContainerPath) {
		if (JREContainerInitializer.isExecutionEnvironment(jreContainerPath)) {
			return null;
		}
		return JREContainerInitializer.getVMName(jreContainerPath);
	}
	
	/**
	 * Returns the execution environment identifier in the following JRE
	 * classpath container path, or <code>null</code> if none.
	 * 
	 * @param jreContainerPath classpath container path
	 * @return execution environment identifier or <code>null</code>
	 * @since 3.2
	 */
	public static String getExecutionEnvironmentId(IPath jreContainerPath) {
		return JREContainerInitializer.getExecutionEnvironmentId(jreContainerPath);
	}
	
	/**
	 * Returns a runtime classpath entry identifying the JRE to use when launching the specified
	 * configuration or <code>null</code> if none is specified. The entry returned represents a
	 * either a classpath variable or classpath container that resolves to a JRE.
	 * <p>
	 * The entry is resolved as follows:
	 * <ol>
	 * <li>If the <code>ATTR_JRE_CONTAINER_PATH</code> is present, it is used to create
	 *  a classpath container referring to a JRE.</li>
	 * <li>Next, if the <code>ATTR_VM_INSTALL_TYPE</code> and <code>ATTR_VM_INSTALL_NAME</code>
	 * attributes are present, they are used to create a classpath container.</li>
	 * <li>When none of the above attributes are specified, a default entry is
	 * created which refers to the JRE referenced by the build path of the configuration's
	 * associated Java project. This could be a classpath variable or classpath container.</li>
	 * <li>When there is no Java project associated with a configuration, the workspace
	 * default JRE is used to create a container path.</li>
	 * </ol>
	 * </p>
	 * @param configuration the backing {@link ILaunchConfiguration}
	 * @return classpath container path identifying a JRE or <code>null</code>
	 * @exception org.eclipse.core.runtime.CoreException if an exception occurs retrieving
	 *  attributes from the specified launch configuration
	 * @since 3.2
	 */
	public static IRuntimeClasspathEntry computeJREEntry(ILaunchConfiguration configuration) throws CoreException {
		String jreAttr = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, (String)null);
		IPath containerPath = null;
		if (jreAttr == null) {
			@SuppressWarnings("deprecation")
			String type = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
			if (type == null) {
				// default JRE for the launch configuration
				IJavaProject proj = getJavaProject(configuration);
				if (proj == null) {
					containerPath = newDefaultJREContainerPath();
				} else {
					return computeJREEntry(proj);
				}
			} else {
				@SuppressWarnings("deprecation")
				String name = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
				if (name != null) {
					containerPath = newDefaultJREContainerPath().append(type).append(name);
				}
			}
		} else {
			containerPath = Path.fromPortableString(jreAttr);
		}
		if (containerPath != null) {
			return newRuntimeContainerClasspathEntry(containerPath, IRuntimeClasspathEntry.STANDARD_CLASSES);
		}
		return null;
	}
	
	/**
	 * Returns a runtime classpath entry identifying the JRE referenced by the specified
	 * project, or <code>null</code> if none. The entry returned represents a either a
	 * classpath variable or classpath container that resolves to a JRE.
	 * 
	 * @param project Java project
	 * @return JRE runtime classpath entry or <code>null</code>
	 * @exception org.eclipse.core.runtime.CoreException if an exception occurs
	 * 	accessing the project's classpath
	 * @since 3.2
	 */
	public static IRuntimeClasspathEntry computeJREEntry(IJavaProject project) throws CoreException {
		IClasspathEntry[] rawClasspath = project.getRawClasspath();
		IRuntimeClasspathEntryResolver2 resolver = null;
		for (int i = 0; i < rawClasspath.length; i++) {
			IClasspathEntry entry = rawClasspath[i];
			switch (entry.getEntryKind()) {
				case IClasspathEntry.CPE_VARIABLE:
					resolver = getVariableResolver(entry.getPath().segment(0));
					if (resolver != null) {
						if (resolver.isVMInstallReference(entry)) {
							return newRuntimeClasspathEntry(entry);
						}
					}
					break;
				case IClasspathEntry.CPE_CONTAINER:
					resolver = getContainerResolver(entry.getPath().segment(0));
					if (resolver != null) {
						if (resolver.isVMInstallReference(entry)) {
							IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
							if (container != null) {
								switch (container.getKind()) {
									case IClasspathContainer.K_APPLICATION:
										break;
									case IClasspathContainer.K_DEFAULT_SYSTEM:
										return newRuntimeContainerClasspathEntry(entry.getPath(), IRuntimeClasspathEntry.STANDARD_CLASSES);
									case IClasspathContainer.K_SYSTEM:
										return newRuntimeContainerClasspathEntry(entry.getPath(), IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
								}
							}
						}
					}
					break;
			}
			
		}
		return null;
	}
	
	/**
	 * Returns whether the given runtime classpath entry refers to a VM install.
	 * 
	 * @param entry the entry to check
	 * @return whether the given runtime classpath entry refers to a VM install
	 * @since 3.2
	 */
	public static boolean isVMInstallReference(IRuntimeClasspathEntry entry) {
		IClasspathEntry classpathEntry = entry.getClasspathEntry();
		if (classpathEntry != null) {
			switch (classpathEntry.getEntryKind()) {
				case IClasspathEntry.CPE_VARIABLE:
					IRuntimeClasspathEntryResolver2 resolver = getVariableResolver(classpathEntry.getPath().segment(0));
					if (resolver != null) {
						return resolver.isVMInstallReference(classpathEntry);
					}
					break;
				case IClasspathEntry.CPE_CONTAINER:
					resolver = getContainerResolver(classpathEntry.getPath().segment(0));
					if (resolver != null) {
						return resolver.isVMInstallReference(classpathEntry);
					}
					break;
				}
		}
		return false;
	}
	
	/**
	 * Returns the VM connector defined with the specified identifier,
	 * or <code>null</code> if none.
	 * 
	 * @param id VM connector identifier
	 * @return VM connector or <code>null</code> if none
	 * @since 2.0
	 */
	public static IVMConnector getVMConnector(String id) {
		return LaunchingPlugin.getDefault().getVMConnector(id);
	}
	
	/**
	 * Returns all VM connector extensions.
	 *
	 * @return VM connectors
	 * @since 2.0
	 */
	public static IVMConnector[] getVMConnectors() {
		return LaunchingPlugin.getDefault().getVMConnectors();
	}
	
	/**
	 * Returns the preference store for the launching plug-in.
	 * 
	 * @return the preference store for the launching plug-in
	 * @since 2.0
	 */
	@SuppressWarnings("deprecation")
	public static Preferences getPreferences() {
		return LaunchingPlugin.getDefault().getPluginPreferences();
	}
	
	/**
	 * Saves the preferences for the launching plug-in.
	 * 
	 * @since 2.0
	 */
	public static void savePreferences() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			LaunchingPlugin.log(e);
		}
	}
	
	/**
	 * Registers the given resolver for the specified variable.
	 * 
	 * @param resolver runtime classpath entry resolver
	 * @param variableName variable name to register for
	 * @since 2.0
	 */
	public static void addVariableResolver(IRuntimeClasspathEntryResolver resolver, String variableName) {
		Map<String, IRuntimeClasspathEntryResolver> map = getVariableResolvers();
		map.put(variableName, resolver);
	}
	
	/**
	 * Registers the given resolver for the specified container.
	 * 
	 * @param resolver runtime classpath entry resolver
	 * @param containerIdentifier identifier of the classpath container to register for
	 * @since 2.0
	 */
	public static void addContainerResolver(IRuntimeClasspathEntryResolver resolver, String containerIdentifier) {
		Map<String, IRuntimeClasspathEntryResolver> map = getContainerResolvers();
		map.put(containerIdentifier, resolver);
	}
	
	/**
	 * Returns all registered variable resolvers.
	 * @return the initialized map of {@link RuntimeClasspathEntryResolver}s for variables
	 */
	private static Map<String, IRuntimeClasspathEntryResolver> getVariableResolvers() {
		if (fgVariableResolvers == null) {
			initializeResolvers();
		}
		return fgVariableResolvers;
	}
	
	/**
	 * Returns all registered container resolvers.
	 * @return the initialized map of {@link RuntimeClasspathEntryResolver}s for containers
	 */
	private static Map<String, IRuntimeClasspathEntryResolver> getContainerResolvers() {
		if (fgContainerResolvers == null) {
			initializeResolvers();
		}
		return fgContainerResolvers;
	}
	
	/**
	 * Returns all registered runtime classpath entry resolvers.
	 * @return the initialized map of {@link RuntimeClasspathEntryResolver}s for classpath entries
	 */
	private static Map<String, RuntimeClasspathEntryResolver> getEntryResolvers() {
		if (fgRuntimeClasspathEntryResolvers == null) {
			initializeResolvers();
		}
		return fgRuntimeClasspathEntryResolvers;
	}

	/**
	 * Initializes the listing of runtime classpath entry resolvers
	 */
	private static void initializeResolvers() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, EXTENSION_POINT_RUNTIME_CLASSPATH_ENTRY_RESOLVERS);
		IConfigurationElement[] extensions = point.getConfigurationElements();
		fgVariableResolvers = new HashMap<String, IRuntimeClasspathEntryResolver>(extensions.length);
		fgContainerResolvers = new HashMap<String, IRuntimeClasspathEntryResolver>(extensions.length);
		fgRuntimeClasspathEntryResolvers = new HashMap<String, RuntimeClasspathEntryResolver>(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			RuntimeClasspathEntryResolver res = new RuntimeClasspathEntryResolver(extensions[i]);
			String variable = res.getVariableName();
			String container = res.getContainerId();
			String entryId = res.getRuntimeClasspathEntryId();
			if (variable != null) {
				fgVariableResolvers.put(variable, res);
			}
			if (container != null) {
				fgContainerResolvers.put(container, res);
			}
			if (entryId != null) {
				fgRuntimeClasspathEntryResolvers.put(entryId, res);
			}
		}
	}

	/**
	 * Returns all registered classpath providers.
	 * @return the initialized map of {@link RuntimeClasspathProvider}s
	 */
	private static Map<String, RuntimeClasspathProvider> getClasspathProviders() {
		if (fgPathProviders == null) {
			initializeProviders();
		}
		return fgPathProviders;
	}
		
	/**
	 * Initializes the listing of classpath providers
	 */
	private static void initializeProviders() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, EXTENSION_POINT_RUNTIME_CLASSPATH_PROVIDERS);
		IConfigurationElement[] extensions = point.getConfigurationElements();
		fgPathProviders = new HashMap<String, RuntimeClasspathProvider>(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			RuntimeClasspathProvider res = new RuntimeClasspathProvider(extensions[i]);
			fgPathProviders.put(res.getIdentifier(), res);
		}
	}
		
	/**
	 * Returns the resolver registered for the given variable, or
	 * <code>null</code> if none.
	 * 
	 * @param variableName the variable to determine the resolver for
	 * @return the resolver registered for the given variable, or
	 * <code>null</code> if none
	 */
	private static IRuntimeClasspathEntryResolver2 getVariableResolver(String variableName) {
		return (IRuntimeClasspathEntryResolver2)getVariableResolvers().get(variableName);
	}
	
	/**
	 * Returns the resolver registered for the given container id, or
	 * <code>null</code> if none.
	 * 
	 * @param containerId the container to determine the resolver for
	 * @return the resolver registered for the given container id, or
	 * <code>null</code> if none
	 */
	private static IRuntimeClasspathEntryResolver2 getContainerResolver(String containerId) {
		return (IRuntimeClasspathEntryResolver2)getContainerResolvers().get(containerId);
	}
	
	/**
	 * Returns the resolver registered for the given contributed classpath
	 * entry type.
	 * 
	 * @param typeId the id of the contributed classpath entry
	 * @return the resolver registered for the given classpath entry
	 */
	private static IRuntimeClasspathEntryResolver getContributedResolver(String typeId) {
		IRuntimeClasspathEntryResolver resolver = getEntryResolvers().get(typeId);
		if (resolver == null) {
			return new DefaultEntryResolver();
		}
		return resolver;
	}
	
	/**
	 * Adds the given listener to the list of registered VM install changed
	 * listeners. Has no effect if an identical listener is already registered.
	 * 
	 * @param listener the listener to add
	 * @since 2.0
	 */
	public static void addVMInstallChangedListener(IVMInstallChangedListener listener) {
		fgVMListeners.add(listener);
	}
	
	/**
	 * Removes the given listener from the list of registered VM install changed
	 * listeners. Has no effect if an identical listener is not already registered.
	 * 
	 * @param listener the listener to remove
	 * @since 2.0
	 */
	public static void removeVMInstallChangedListener(IVMInstallChangedListener listener) {
		fgVMListeners.remove(listener);
	}
	
	/**
	 * Notifies registered listeners that the default VM has changed
	 * @param previous the previous VM
	 * @param current the new current default VM
	 */
	private static void notifyDefaultVMChanged(IVMInstall previous, IVMInstall current) {
		Object[] listeners = fgVMListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IVMInstallChangedListener listener = (IVMInstallChangedListener)listeners[i];
			listener.defaultVMInstallChanged(previous, current);
		}
	}
	
	/**
	 * Notifies all VM install changed listeners of the given property change.
	 * 
	 * @param event event describing the change.
	 * @since 2.0
	 */
	public static void fireVMChanged(PropertyChangeEvent event) {
		Object[] listeners = fgVMListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IVMInstallChangedListener listener = (IVMInstallChangedListener)listeners[i];
			listener.vmChanged(event);
		}
	}
	
	/**
	 * Notifies all VM install changed listeners of the VM addition
	 * 
	 * @param vm the VM that has been added
	 * @since 2.0
	 */
	public static void fireVMAdded(IVMInstall vm) {
		if (!fgInitializingVMs) {
			Object[] listeners = fgVMListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				IVMInstallChangedListener listener = (IVMInstallChangedListener)listeners[i];
				listener.vmAdded(vm);
			}
		}
	}
	
	/**
	 * Notifies all VM install changed listeners of the VM removal
	 * 
	 * @param vm the VM that has been removed
	 * @since 2.0
	 */
	public static void fireVMRemoved(IVMInstall vm) {
		Object[] listeners = fgVMListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IVMInstallChangedListener listener = (IVMInstallChangedListener)listeners[i];
			listener.vmRemoved(vm);
		}
	}
	
	/**
	 * Return the String representation of the default output directory of the
	 * launch config's project or <code>null</code> if there is no configuration, no
	 * project or some sort of problem.
	 * @param config the {@link ILaunchConfiguration}
	 * 
	 * @return the default output directory for the specified launch
	 * configuration's project
	 * @since 2.1
	 */
	public static String getProjectOutputDirectory(ILaunchConfiguration config) {
		try {
			if (config != null) {
				IJavaProject javaProject = JavaRuntime.getJavaProject(config);
				if (javaProject != null) {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					IPath outputLocation = javaProject.getOutputLocation();
					IResource resource = root.findMember(outputLocation);
					if (resource != null) {
						IPath path = resource.getFullPath();
						if (path != null)  {
							return path.makeRelative().toString();
						}
					}
				}
			}
		} catch (CoreException ce) {
		}
		return null;
	}
	
	/**
	 * Returns a collection of source containers corresponding to the given
	 * resolved runtime classpath entries.
	 * <p>
	 * Note that the entries must be resolved to ARCHIVE and PROJECT entries,
	 * as source containers cannot be determined for unresolved entries.
	 * </p>
	 * @param entries entries to translate
	 * @return source containers corresponding to the given runtime classpath entries
	 * @since 3.1
	 */
	public static ISourceContainer[] getSourceContainers(IRuntimeClasspathEntry[] entries) {
		return JavaSourceLookupUtil.translate(entries);
	}
	
	/**
	 * Returns a collection of paths that should be appended to the given project's
	 * <code>java.library.path</code> system property when launched. Entries are
	 * searched for on the project's build path as extra classpath attributes.
	 * Each entry represents an absolute path in the local file system.
	 *
	 * @param project the project to compute the <code>java.library.path</code> for
	 * @param requiredProjects whether to consider entries in required projects
	 * @return a collection of paths representing entries that should be appended
	 *  to the given project's <code>java.library.path</code>
	 * @throws CoreException if unable to compute the Java library path
	 * @since 3.1
	 * @see org.eclipse.jdt.core.IClasspathAttribute
	 * @see JavaRuntime#CLASSPATH_ATTR_LIBRARY_PATH_ENTRY
	 */
	public static String[] computeJavaLibraryPath(IJavaProject project, boolean requiredProjects) throws CoreException {
		Set<IJavaProject> visited = new HashSet<IJavaProject>();
		List<String> entries = new ArrayList<String>();
		gatherJavaLibraryPathEntries(project, requiredProjects, visited, entries);
		List<String> resolved = new ArrayList<String>(entries.size());
		Iterator<String> iterator = entries.iterator();
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		while (iterator.hasNext()) {
			String entry = iterator.next();
			String resolvedEntry = manager.performStringSubstitution(entry);
			IPath path = new Path(resolvedEntry);
			if (path.isAbsolute()) {
				File file = path.toFile();
				resolved.add(file.getAbsolutePath());
			} else {
				IResource resource = root.findMember(path);
				if (resource != null) {
					IPath location = resource.getLocation();
					if (location != null) {
						resolved.add(location.toFile().getAbsolutePath());
					}
				}
			}
		}
		return resolved.toArray(new String[resolved.size()]);
	}

	/**
	 * Gathers all Java library entries for the given project and optionally its required
	 * projects.
	 * 
	 * @param project project to gather entries for
	 * @param requiredProjects whether to consider required projects
	 * @param visited projects already considered
	 * @param entries collection to add library entries to
	 * @throws CoreException if unable to gather classpath entries
	 * @since 3.1
	 */
	private static void gatherJavaLibraryPathEntries(IJavaProject project, boolean requiredProjects, Set<IJavaProject> visited, List<String> entries) throws CoreException {
		if (visited.contains(project)) {
			return;
		}
		visited.add(project);
		IClasspathEntry[] rawClasspath = project.getRawClasspath();
		IClasspathEntry[] required = processJavaLibraryPathEntries(project, requiredProjects, rawClasspath, entries);
		if (required != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			for (int i = 0; i < required.length; i++) {
				IClasspathEntry entry = required[i];
				String projectName = entry.getPath().segment(0);
				IProject p = root.getProject(projectName);
				if (p.exists()) {
					IJavaProject requiredProject = JavaCore.create(p);
					if (requiredProject != null) {
						gatherJavaLibraryPathEntries(requiredProject, requiredProjects, visited, entries);
					}
				}
			}
		}
	}
	
	/**
	 * Adds all java library path extra classpath entry values to the given entries collection
	 * specified on the given project's classpath, and returns a collection of required
	 * projects, or <code>null</code>.
	 * 
	 * @param project project being processed
	 * @param collectRequired whether to collect required projects
	 * @param classpathEntries the project's raw classpath
	 * @param entries collection to add java library path entries to
	 * @return required project classpath entries or <code>null</code>
	 * @throws CoreException if an exception occurs
	 * @since 3.1
	 */
	private static IClasspathEntry[] processJavaLibraryPathEntries(IJavaProject project, boolean collectRequired, IClasspathEntry[] classpathEntries, List<String> entries) throws CoreException {
		List<IClasspathEntry> req = null;
		for (int i = 0; i < classpathEntries.length; i++) {
			IClasspathEntry entry = classpathEntries[i];
			IClasspathAttribute[] extraAttributes = entry.getExtraAttributes();
			for (int j = 0; j < extraAttributes.length; j++) {
				String[] paths = getLibraryPaths(extraAttributes[j]);
				if (paths != null) {
					for (int k = 0; k < paths.length; k++) {
						entries.add(paths[k]);
					}
				}
			}
			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
				if (container != null) {
					IClasspathEntry[] requiredProjects = processJavaLibraryPathEntries(project, collectRequired, container.getClasspathEntries(), entries);
					if (requiredProjects != null) {
						if (req == null) {
							req = new ArrayList<IClasspathEntry>();
						}
						for (int j = 0; j < requiredProjects.length; j++) {
							req.add(requiredProjects[j]);
						}
					}
				}
			} else if (collectRequired && entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				if (req == null) {
					req = new ArrayList<IClasspathEntry>();
				}
				req.add(entry);
			}
		}
		if (req != null) {
			return req.toArray(new IClasspathEntry[req.size()]);
		}
		return null;
	}
	
	/**
	 * Creates a new classpath attribute referencing a list of shared libraries that should
	 * appear on the <code>-Djava.library.path</code> system property at runtime
	 * for an associated {@link IClasspathEntry}.
	 * <p>
	 * The factory methods <code>newLibraryPathsAttribute(String[])</code>
	 * and <code>getLibraryPaths(IClasspathAttribute)</code> should be used to
	 * encode and decode the attribute value.
	 * </p>
	 * @param paths an array of strings representing paths of shared libraries.
	 * Each string is used to create an <code>IPath</code> using the constructor
	 * <code>Path(String)</code>, and may contain <code>IStringVariable</code>'s.
	 * Variable substitution is performed on each string before a path is constructed
	 * from a string.
	 * @return a classpath attribute with the name <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code>
	 * and an value encoded to the specified paths.
	 * @since 3.1
	 */
	public static IClasspathAttribute newLibraryPathsAttribute(String[] paths) {
		StringBuffer value = new StringBuffer();
		for (int i = 0; i < paths.length; i++) {
			value.append(paths[i]);
			if (i < (paths.length - 1)) {
				value.append("|"); //$NON-NLS-1$
			}
		}
		return JavaCore.newClasspathAttribute(CLASSPATH_ATTR_LIBRARY_PATH_ENTRY, value.toString());
	}
	
	/**
	 * Returns an array of strings referencing shared libraries that should
	 * appear on the <code>-Djava.library.path</code> system property at runtime
	 * for an associated {@link IClasspathEntry}, or <code>null</code> if the
	 * given attribute is not a <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code>.
	 * Each string is used to create an <code>IPath</code> using the constructor
	 * <code>Path(String)</code>, and may contain <code>IStringVariable</code>'s.
	 * <p>
	 * The factory methods <code>newLibraryPathsAttribute(String[])</code>
	 * and <code>getLibraryPaths(IClasspathAttribute)</code> should be used to
	 * encode and decode the attribute value.
	 * </p>
	 * @param attribute a <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code> classpath attribute
	 * @return an array of strings referencing shared libraries that should
	 * appear on the <code>-Djava.library.path</code> system property at runtime
	 * for an associated {@link IClasspathEntry}, or <code>null</code> if the
	 * given attribute is not a <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code>.
	 * Each string is used to create an <code>IPath</code> using the constructor
	 * <code>Path(String)</code>, and may contain <code>IStringVariable</code>'s.
	 * @since 3.1
	 */
	public static String[] getLibraryPaths(IClasspathAttribute attribute) {
		if (CLASSPATH_ATTR_LIBRARY_PATH_ENTRY.equals(attribute.getName())) {
			String value = attribute.getValue();
			return value.split("\\|"); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Returns the execution environments manager.
	 * 
	 * @return execution environments manager
	 * @since 3.2
	 */
	public static IExecutionEnvironmentsManager getExecutionEnvironmentsManager() {
		return EnvironmentsManager.getDefault();
	}
	
	/**
	 * Perform VM type and VM install initialization. Does not hold locks
	 * while performing change notification.
	 * 
	 * @since 3.2
	 */
	private static void initializeVMs() {
		VMDefinitionsContainer vmDefs = null;
		boolean setPref = false;
		boolean updateCompliance = false;
		synchronized (fgVMLock) {
			if (fgVMTypes == null) {
				try {
					fgInitializingVMs = true;
					// 1. load VM type extensions
					initializeVMTypeExtensions();
					try {
						vmDefs = new VMDefinitionsContainer();
						// 2. add persisted VMs
						setPref = addPersistedVMs(vmDefs);
						IStatus status = vmDefs.getStatus();
						if (status != null) {
							if (status.isMultiStatus()) {
								MultiStatus multi = (MultiStatus) status;
								IStatus[] children = multi.getChildren();
								for (int i = 0; i < children.length; i++) {
									IStatus child = children[i];
									if (!child.isOK()) {
										LaunchingPlugin.log(child);
									}
								}
							} else if (!status.isOK()) {
								LaunchingPlugin.log(status);
							}
						}
						
						// 3. if there are none, detect the eclipse runtime
						if (vmDefs.getValidVMList().isEmpty()) {
							// calling out to detectEclipseRuntime() could allow clients to change
							// VM settings (i.e. call back into change VM settings).
							VMListener listener = new VMListener();
							addVMInstallChangedListener(listener);
							setPref = true;
							VMStandin runtime = detectEclipseRuntime();
							removeVMInstallChangedListener(listener);
							if (!listener.isChanged()) {
								if (runtime != null) {
									updateCompliance = true;
									vmDefs.addVM(runtime);
									vmDefs.setDefaultVMInstallCompositeID(getCompositeIdFromVM(runtime));
								}
							} else {
								// VMs were changed - reflect current settings
								addPersistedVMs(vmDefs);
								vmDefs.setDefaultVMInstallCompositeID(fgDefaultVMId);
								updateCompliance = fgDefaultVMId != null;
							}
						}
						// 4. load contributed VM installs
						addVMExtensions(vmDefs);
						// 5. verify default VM is valid
						String defId = vmDefs.getDefaultVMInstallCompositeID();
						boolean validDef = false;
						if (defId != null) {
							Iterator<IVMInstall> iterator = vmDefs.getValidVMList().iterator();
							while (iterator.hasNext()) {
								IVMInstall vm = iterator.next();
								if (getCompositeIdFromVM(vm).equals(defId)) {
									validDef = true;
									break;
								}
							}
						}
						if (!validDef) {
							// use the first as the default
							setPref = true;
							List<IVMInstall> list = vmDefs.getValidVMList();
							if (!list.isEmpty()) {
								IVMInstall vm = list.get(0);
								vmDefs.setDefaultVMInstallCompositeID(getCompositeIdFromVM(vm));
							}
						}
						fgDefaultVMId = vmDefs.getDefaultVMInstallCompositeID();
						fgDefaultVMConnectorId = vmDefs.getDefaultVMInstallConnectorTypeID();
						
						// Create the underlying VMs for each valid VM
						List<IVMInstall> vmList = vmDefs.getValidVMList();
						Iterator<IVMInstall> vmListIterator = vmList.iterator();
						while (vmListIterator.hasNext()) {
							VMStandin vmStandin = (VMStandin) vmListIterator.next();
							vmStandin.convertToRealVM();
						}
						

					} catch (IOException e) {
						LaunchingPlugin.log(e);
					}
				} finally {
					fgInitializingVMs = false;
				}
			}
		}
		if (vmDefs != null) {
			// notify of initial VMs for backwards compatibility
			IVMInstallType[] installTypes = getVMInstallTypes();
			for (int i = 0; i < installTypes.length; i++) {
				IVMInstallType type = installTypes[i];
				IVMInstall[] installs = type.getVMInstalls();
				for (int j = 0; j < installs.length; j++) {
					fireVMAdded(installs[j]);
				}
			}
			
			// save settings if required
			if (setPref) {
				try {
					String xml = vmDefs.getAsXML();
					InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN).put(PREF_VM_XML, xml);
				}  catch (CoreException e) {
					LaunchingPlugin.log(e);
				}
			}
			// update compliance if required
			if (updateCompliance) {
				updateCompliance(getDefaultVMInstall());
			}
		}
	}
	
	/**
	 * Update compiler compliance settings based on the given VM.
	 * 
	 * @param vm the backing {@link IVMInstall}
	 */
	private static void updateCompliance(IVMInstall vm) {
        if (vm instanceof IVMInstall2) {
            String javaVersion = ((IVMInstall2)vm).getJavaVersion();
            if (javaVersion != null) {
            	String compliance = null;
            	if (javaVersion.startsWith(JavaCore.VERSION_1_5)) {
            		compliance = JavaCore.VERSION_1_5;
            	} else if (javaVersion.startsWith(JavaCore.VERSION_1_6)) {
            		compliance = JavaCore.VERSION_1_6;
            	} else if (javaVersion.startsWith(JavaCore.VERSION_1_7)) {
            		compliance = JavaCore.VERSION_1_7;
            	}
            	if (compliance != null) {
	                Hashtable<String, String> options= JavaCore.getOptions();

	                org.osgi.service.prefs.Preferences bundleDefaults = BundleDefaultsScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);

	                boolean isDefault =
	                	equals(JavaCore.COMPILER_COMPLIANCE, options, bundleDefaults) &&
	                	equals(JavaCore.COMPILER_SOURCE, options, bundleDefaults) &&
	                	equals(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, options, bundleDefaults) &&
	                	equals(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, options, bundleDefaults) &&
	                	equals(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, options, bundleDefaults);
	                // only update the compliance settings if they are default settings, otherwise the
	                // settings have already been modified by a tool or user
	                if (isDefault) {
	                	JavaCore.setComplianceOptions(compliance, options);
	                    JavaCore.setOptions(options);
	                }
            	}
            }
        }
	}
	
	/**
	 * Creates a new VM based on the attributes specified in the given execution
	 * environment description file. The format of the file is defined by
	 * <code>http://wiki.eclipse.org/Execution_Environment_Descriptions</code>.
	 * 
	 * @param eeFile VM definition file
	 * @param name name for the VM, or <code>null</code> if a default name should be assigned
	 * @param id id to assign to the new VM
	 * @return VM stand-in
	 * @exception CoreException if unable to create a VM from the given definition file
	 * @since 3.4
	 */
	public static VMStandin createVMFromDefinitionFile(File eeFile, String name, String id) throws CoreException {
		ExecutionEnvironmentDescription description = new ExecutionEnvironmentDescription(eeFile);
		IStatus status = EEVMType.validateDefinitionFile(description);
		if (status.isOK()) {
			VMStandin standin = new VMStandin(getVMInstallType(EEVMType.ID_EE_VM_TYPE), id);
			if (name != null && name.length() > 0){
				standin.setName(name);
			} else {
				name = description.getProperty(ExecutionEnvironmentDescription.EE_NAME);
				if (name == null) {
					name = eeFile.getName();
				}
				standin.setName(name);
			}
			String home = description.getProperty(ExecutionEnvironmentDescription.JAVA_HOME);
			standin.setInstallLocation(new File(home));
			standin.setLibraryLocations(description.getLibraryLocations());
			standin.setVMArgs(description.getVMArguments());
			standin.setJavadocLocation(EEVMType.getJavadocLocation(description.getProperties()));
			standin.setAttribute(EEVMInstall.ATTR_EXECUTION_ENVIRONMENT_ID, description.getProperty(ExecutionEnvironmentDescription.CLASS_LIB_LEVEL));
			File exe = description.getExecutable();
			if (exe == null) {
				exe = description.getConsoleExecutable();
			}
			if (exe != null) {
				try {
					standin.setAttribute(EEVMInstall.ATTR_JAVA_EXE, exe.getCanonicalPath());
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(),
							LaunchingMessages.JavaRuntime_24, e));
				}
			}
			standin.setAttribute(EEVMInstall.ATTR_JAVA_VERSION, description.getProperty(ExecutionEnvironmentDescription.LANGUAGE_LEVEL));
			standin.setAttribute(EEVMInstall.ATTR_DEFINITION_FILE, eeFile.getPath());
			standin.setAttribute(EEVMInstall.ATTR_DEBUG_ARGS, description.getProperty(ExecutionEnvironmentDescription.DEBUG_ARGS));
			return standin;
		}
		throw new CoreException(status);
	}
}
