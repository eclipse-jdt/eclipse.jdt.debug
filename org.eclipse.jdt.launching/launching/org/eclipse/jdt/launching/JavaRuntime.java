package org.eclipse.jdt.launching;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.CompositeId;
import org.eclipse.jdt.internal.launching.JREContainerInitializer;
import org.eclipse.jdt.internal.launching.JavaClasspathVariablesInitializer;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.ListenerList;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntryResolver;
import org.eclipse.jdt.internal.launching.RuntimeClasspathProvider;
import org.eclipse.jdt.internal.launching.SocketAttachConnector;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;

/**
 * The central access point for launching support. This class manages
 * the registered VM types contributed through the 
 * <code>"org.eclipse.jdt.launching.vmType"</code> extension point.
 * As well, this class provides VM install change notification,
 * and computes classpaths and source lookup paths for launch
 * configurations.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
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
	 * Classpath container used for a project's JRE
	 * (value <code>"org. eclipse.jdt.launching.JRE_CONTAINER"</code>). A
	 * container  is resolved in the context of a specific Java project, to one
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
	 * 
	 * @since 2.0
	 */
	public static final String JRE_CONTAINER = LaunchingPlugin.getUniqueIdentifier() + ".JRE_CONTAINER"; //$NON-NLS-1$
	
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
	 * Default launch/connect timeout (ms).
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
	public final static String ATTR_CMDLINE= LaunchingPlugin.getUniqueIdentifier() + ".launcher.cmdLine"; //$NON-NLS-1$

	private static IVMInstallType[] fgVMTypes= null;
	private static String fgDefaultVMId= null;
	private static String fgDefaultVMConnectorId = null;
	
	/**
	 * Resolvers keyed by variable name and container id.
	 */
	private static Map fgVariableResolvers = null;
	private static Map fgContainerResolvers = null;
	
	/**
	 * Path providers keyed by id
	 */
	private static Map fgPathProviders = null;
	
	/**
	 * Default classpath and source path providers.
	 */
	private static IRuntimeClasspathProvider fgDefaultClasspathProvider = new StandardClasspathProvider();
	private static IRuntimeClasspathProvider fgDefaultSourcePathProvider = new StandardSourcePathProvider();
	
	/**
	 * VM change listeners
	 */
	private static ListenerList fgVMListeners = new ListenerList(5);
	
	/**
	 * This class contains only static methods, and is not intended
	 * to be instantiated.
	 */
	private JavaRuntime() {
	}

	private static synchronized void initializeVMTypes() {
		IExtensionPoint extensionPoint= Platform.getPluginRegistry().getExtensionPoint(LaunchingPlugin.getUniqueIdentifier() + ".vmInstallTypes"); //$NON-NLS-1$
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements(); 
		MultiStatus status= new MultiStatus(LaunchingPlugin.getUniqueIdentifier(), IStatus.OK, LaunchingMessages.getString("JavaRuntime.exceptionOccurred"), null); //$NON-NLS-1$
		fgVMTypes= new IVMInstallType[configs.length];

		for (int i= 0; i < configs.length; i++) {
			try {
				IVMInstallType vmType= (IVMInstallType)configs[i].createExecutableExtension("class"); //$NON-NLS-1$
				fgVMTypes[i]= vmType;
			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}
		if (!status.isOK()) {
			//only happens on a CoreException
			LaunchingPlugin.log(status);
			//cleanup null entries in fgVMTypes
			List temp= new ArrayList(fgVMTypes.length);
			for (int i = 0; i < fgVMTypes.length; i++) {
				if(fgVMTypes[i] != null) {
					temp.add(fgVMTypes[i]);
				}
				fgVMTypes= new IVMInstallType[temp.size()];
				fgVMTypes= (IVMInstallType[])temp.toArray(fgVMTypes);
			}
		}
		
		try {
			initializeVMConfiguration();
		} catch (IOException e) {
			LaunchingPlugin.log(e);
		}
	}

	/**
	 * Returns the VM assigned to build the given Java project.
	 * The project must exist. The VM assigned to a project is
	 * determined from its build path.
	 * 
	 * @return the VM instance that is assigned to build the given Java project
	 * 		   Returns <code>null</code> if no VM is referenced on the project's build path.
	 * @throws CoreException if unable to determine the project's VM install
	 */
	public static IVMInstall getVMInstall(IJavaProject project) throws CoreException {
		// check the classpath
		IVMInstall vm = null;
		IClasspathEntry[] classpath = project.getRawClasspath();
		IRuntimeClasspathEntryResolver resolver = null;
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
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
	 * @param vm	The vm to make the default. May be <code>null</code> to clear 
	 * 				the default.
	 * @param monitor progress monitor or <code>null</code>
	 */
	public static void setDefaultVMInstall(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		setDefaultVMInstall(vm, monitor, true);
	}	
	
	/**
	 * Sets a VM as the system-wide default VM, and notifies registered VM install
	 * change listeners of the change.
	 * 
	 * @param vm	The vm to make the default. May be <code>null</code> to clear 
	 * 				the default.
	 * @param monitor progress monitor or <code>null</code>
	 * @param savePreference If <code>true</code>, update workbench preferences to reflect
	 * 		   				  the new default VM.
	 * @since 2.1
	 */
	public static void setDefaultVMInstall(IVMInstall vm, IProgressMonitor monitor, boolean savePreference) throws CoreException {
		IVMInstall previous = null;
		if (fgDefaultVMId != null) {
			previous = getVMFromCompositeId(fgDefaultVMId);
		}
		fgDefaultVMId= getCompositeIdFromVM(vm);
		updateJREVariables(monitor);
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
	 * @param	connector The connector to make the default. May be null to clear 
	 * 				the default.
	 * @since 2.0
	 */
	public static void setDefaultVMConnector(IVMConnector connector, IProgressMonitor monitor) throws CoreException {
		fgDefaultVMConnectorId= connector.getIdentifier();
		saveVMConfiguration();
	}		
	
	private static void updateJREVariables(IProgressMonitor monitor) throws CoreException {
		JavaClasspathVariablesInitializer updater= new JavaClasspathVariablesInitializer();
		updater.updateJREVariables(monitor);
		JREContainerInitializer conatinerUpdater = new JREContainerInitializer();
		conatinerUpdater.updateDefaultJREContainers(monitor);
	}
	
	/**
	 * Return the default VM set with <code>setDefaultVM()</code>.
	 * @return	Returns the default VM. May return <code>null</code> when no default
	 * 			VM was set or when the default VM has been disposed.
	 */
	public static IVMInstall getDefaultVMInstall() {
		IVMInstall install= getVMFromCompositeId(getDefaultVMId());
		if (install != null && install.getInstallLocation().exists()) {
			return install;
		} else {
			// if the default JRE goes missing, re-detect
			if (install != null) {
				install.getVMInstallType().disposeVMInstall(install.getId());
			}
			fgDefaultVMId = null;
			// re-detect
			detectDefaultVM();
			// update VM prefs 
			try {
				saveVMConfiguration();
			} catch(CoreException e) {
				LaunchingPlugin.log(e);
			}
			return getVMFromCompositeId(getDefaultVMId());
		}
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
		if (fgVMTypes == null) {
			initializeVMTypes();
		}
		return fgVMTypes; 
	}
	
	private static String getDefaultVMId() {
		if (fgVMTypes == null) {
			initializeVMTypes();
		}
		return fgDefaultVMId;
	}
	
	private static String getDefaultVMConnectorId() {
		if (fgVMTypes == null) {
			initializeVMTypes();
		}
		return fgDefaultVMConnectorId;
	}	
	
	/** 
	 * Returns a String that uniquely identifies the specified VM across all VM types.
	 * 
	 * @param vm the instance of IVMInstallType to be indentified
	 * 
	 * @since 2.1	 */
	public static String getCompositeIdFromVM(IVMInstall vm) {
		if (vm == null) {
			return null;
		}
		IVMInstallType vmType= vm.getVMInstallType();
		String typeID= vmType.getId();
		CompositeId id= new CompositeId(new String[] { typeID, vm.getId() });
		return id.toString();
	}
	
	/**
	 * Return the VM corrseponding to the specified composite Id.  The id uniquely
	 * identifies a VM across all vm types.  
	 * 
	 * @param idString the composite id that specifies an instance of IVMInstall	 * 
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
	 * Returns a new runtime classpath entry for the given project.
	 * 
	 * @param project Java project
	 * @return runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newProjectRuntimeClasspathEntry(IJavaProject project) {
		IClasspathEntry cpe = JavaCore.newProjectEntry(project.getProject().getFullPath());
		return newRuntimeClasspathEntry(cpe);
	}
	
	
	/**
	 * Returns a new runtime classpath entry for the given archive.
	 * 
	 * @param resource archive resource
	 * @return runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newArchiveRuntimeClasspathEntry(IResource resource) {
		IClasspathEntry cpe = JavaCore.newLibraryEntry(resource.getFullPath(), null, null);
		return newRuntimeClasspathEntry(cpe);
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
		IClasspathEntry cpe = JavaCore.newLibraryEntry(path, null, null);
		return newRuntimeClasspathEntry(cpe);
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
		IClasspathEntry cpe = JavaCore.newVariableEntry(path, null, null);
		return newRuntimeClasspathEntry(cpe);
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
		IClasspathEntry cpe = JavaCore.newContainerEntry(path);
		return new RuntimeClasspathEntry(cpe, classpathProperty);
	}
		
	/**
	 * Returns a runtime classpath entry constructed from the given memento.
	 * 
	 * @param memento a menento for a runtime classpath entry
	 * @return runtime classpath entry
	 * @exception CoreException if unable to construct a runtime classpath entry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry newRuntimeClasspathEntry(String memento) throws CoreException {
		return new RuntimeClasspathEntry(memento);
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
	 * Computes and returns the default unresolved runtime claspath for the
	 * given project.
	 * 
	 * @return runtime classpath entries
	 * @exception CoreException if unable to compute the runtime classpath
	 * @see IRuntimeClasspathEntry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] computeUnresolvedRuntimeClasspath(IJavaProject project) throws CoreException {
		IClasspathEntry entry = JavaCore.newProjectEntry(project.getProject().getFullPath());
		List classpathEntries = new ArrayList(5);
		List expanding = new ArrayList(5);
		expandProject(entry, classpathEntries, expanding);
		IRuntimeClasspathEntry[] runtimeEntries = new IRuntimeClasspathEntry[classpathEntries == null ? 0 : classpathEntries.size()];
		for (int i = 0; i < runtimeEntries.length; i++) {
			Object e = classpathEntries.get(i);
			if (e instanceof IClasspathEntry) {
				IClasspathEntry cpe = (IClasspathEntry)e;
				runtimeEntries[i] = newRuntimeClasspathEntry(cpe);
			} else {
				runtimeEntries[i] = (IRuntimeClasspathEntry)e;				
			}
		}
		// sort bootpath and standard entries first
		IRuntimeClasspathEntry[] ordered = new IRuntimeClasspathEntry[runtimeEntries.length];
		int index = 0;
		for (int i = 0; i < runtimeEntries.length; i++) {
			if (runtimeEntries[i].getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES) {
				ordered[index] = runtimeEntries[i];
				index++;
				runtimeEntries[i] = null;
			} 
		}
		for (int i = 0; i < runtimeEntries.length; i++) {
			if (runtimeEntries[i] != null) {
				ordered[index] = runtimeEntries[i];
				index++;
			}
		}
		return ordered;
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
			provider = (IRuntimeClasspathProvider)getClasspathProviders().get(providerId);	
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
			provider = (IRuntimeClasspathProvider)getClasspathProviders().get(providerId);
		}
		return provider;
	}	
		
	/**
	 * Returns resolved entries for the given entry in the context of the given
	 * launch configuration. If the entry is of kind
	 * <code>VARIABLE</code> or <code>CONTAINTER</code>, variable and contanier
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
					IJavaProject project = JavaCore.create((IProject)resource);
					if (project == null || !project.exists()) { 
						abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Classpath_references_non-existant_project__{0}_1"), new String[]{entry.getPath().lastSegment()}), null); //$NON-NLS-1$
					}
					if (!project.getProject().isOpen()) {
						abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Classpath_references_closed_project__{0}_2"), new String[]{entry.getPath().lastSegment()}), null); //$NON-NLS-1$
					}
					IRuntimeClasspathEntry[] entries = resolveOutputLocations(project);
					if (entries != null) {
						return entries;
					}
				} else {
					// could not resolve project
					abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Classpath_references_non-existant_project__{0}_3"), new String[]{entry.getPath().lastSegment()}), null); //$NON-NLS-1$
				}
				break;
			case IRuntimeClasspathEntry.VARIABLE:
				IRuntimeClasspathEntryResolver resolver = getVariableResolver(entry.getVariableName());
				if (resolver == null) {
					// no resolution by default
					break;
				} else {
					return resolver.resolveRuntimeClasspathEntry(entry, configuration);
				}				
			case IRuntimeClasspathEntry.CONTAINER:
				resolver = getContainerResolver(entry.getVariableName());
				if (resolver == null) {
					return computeDefaultContainerEntries(entry, configuration);
				} else {
					return resolver.resolveRuntimeClasspathEntry(entry, configuration);
				}
			case IRuntimeClasspathEntry.ARCHIVE:
				// verify the archive exists
				String location = entry.getLocation();
				if (location == null) {
					abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Classpath_references_non-existant_archive__{0}_4"), new String[]{entry.getPath().toString()}), null); //$NON-NLS-1$
				}
				File file = new File(location);
				if (!file.exists()) {
					abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Classpath_references_non-existant_archive__{0}_5"), new String[]{entry.getPath().toString()}), null); //$NON-NLS-1$
				}
				break;
			default:
				break;
		}
		return new IRuntimeClasspathEntry[] {entry};
	}
	
	/**
	 * Returns runtime classpath entries corresponding to the output locations
	 * of the given project, or null if the project only uses the default
	 * output location.
	 * 
	 * @param project
	 * @return IRuntimeClasspathEntry[] or <code>null</code>
	 * @throws CoreException
	 */
	private static IRuntimeClasspathEntry[] resolveOutputLocations(IJavaProject project) throws CoreException {
		List nonDefault = new ArrayList();
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
		} else {
			// add the default location if not already included
			IPath def = project.getOutputLocation();
			if (!nonDefault.contains(def)) {
				nonDefault.add(def);						
			}
			IRuntimeClasspathEntry[] locations = new IRuntimeClasspathEntry[nonDefault.size()];
			for (int i = 0; i < locations.length; i++) {
				IClasspathEntry newEntry = JavaCore.newLibraryEntry((IPath)nonDefault.get(i), null, null);
				locations[i] = new RuntimeClasspathEntry(newEntry);
			}
			return locations;						
		}
	}
	
	/**
	 * Returns resolved entries for the given entry in the context of the given
	 * Java project. If the entry is of kind
	 * <code>VARIABLE</code> or <code>CONTAINTER</code>, variable and contanier
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
					IJavaProject jp = JavaCore.create((IProject)resource);
					if (jp.exists() && jp.getProject().isOpen()) {
						IRuntimeClasspathEntry[] entries = resolveOutputLocations(jp);
						if (entries != null) {
							return entries;
						}
					}
				}
				break;			
			case IRuntimeClasspathEntry.VARIABLE:
				IRuntimeClasspathEntryResolver resolver = getVariableResolver(entry.getVariableName());
				if (resolver == null) {
					// no resolution by default
					break;
				} else {
					return resolver.resolveRuntimeClasspathEntry(entry, project);
				}				
			case IRuntimeClasspathEntry.CONTAINER:
				resolver = getContainerResolver(entry.getVariableName());
				if (resolver == null) {
					return computeDefaultContainerEntries(entry, project);
				} else {
					return resolver.resolveRuntimeClasspathEntry(entry, project);
				}
			default:
				break;
		}
		return new IRuntimeClasspathEntry[] {entry};
	}	
		
	/**
	 * Performs default resolution for a container entry.
	 * Delegates to the Java model.
	 */
	private static IRuntimeClasspathEntry[] computeDefaultContainerEntries(IRuntimeClasspathEntry entry, ILaunchConfiguration config) throws CoreException {
		return computeDefaultContainerEntries(entry, getJavaProject(config));
	}
	
	/**
	 * Performs default resolution for a container entry.
	 * Delegates to the Java model.
	 */
	private static IRuntimeClasspathEntry[] computeDefaultContainerEntries(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		if (project == null || entry == null) {
			// cannot resolve without entry or project context
			return new IRuntimeClasspathEntry[0];
		} else {							
			IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
			if (container == null) {
				abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Could_not_resolve_classpath_container__{0}_1"), new String[]{entry.getPath().toString()}), null); //$NON-NLS-1$
				// execution will not reach here - exception will be thrown
				return null;
			} else {
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
				IRuntimeClasspathEntry[] resolved = new IRuntimeClasspathEntry[cpes.length];
				for (int i = 0; i < resolved.length; i++) {
					resolved[i] = newRuntimeClasspathEntry(cpes[i]);
					resolved[i].setClasspathProperty(property);
				}
				return resolved;
			}
		}
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
	 * <code>null</code> if none.
	 *
	 * @exception CoreException if the referenced Java project does not exist
	 * @since 2.0
	 */
	public static IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) {
			return null;
		}			
		IJavaProject javaProject = getJavaModel().getJavaProject(projectName);		
		if ((javaProject == null) || !javaProject.exists()) {
			abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Launch_configuration_{0}_references_non-existing_project_{1}._1"), new String[] {configuration.getName(), projectName}), IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT, null); //$NON-NLS-1$
		}
		return javaProject;
	}
				
	/**
	 * Convenience method to get the java model.
	 */
	private static IJavaModel getJavaModel() {
		return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	/**
	 * Returns the VM install for the given launch configuration.
	 * The VM install is determined in the following prioritized way:
	 * <ol>
	 * <li>The VM install is explicitly specified on the launch configuration
	 * 	via the <code>ATTR_VM_INSTALL_TYPE</code> and <code>ATTR_VM_INSTALL_ID</code>
	 *  attributes.</li>
	 * <li>If no explicit VM install is specified, the VM install associated with
	 * 	the launch confiugration's project is returned.</li>
	 * <li>If no project is specified, or the project does not specify a custom
	 * 	VM install, the workspace default VM install is returned.</li>
	 * </ol>
	 * 
	 * @param configuration launch configuration
	 * @return vm install
	 * @exception CoreException if unable to compute a vm install
	 * @since 2.0
	 */
	public static IVMInstall computeVMInstall(ILaunchConfiguration configuration) throws CoreException {
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
			IVMInstallType vt = getVMInstallType(type);
			if (vt == null) {
				// error type does not exist
				abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Specified_VM_install_type_does_not_exist__{0}_2"), new String[] {type}), null); //$NON-NLS-1$
			}
			IVMInstall vm = null;
			// look for a name
			String name = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
			if (name == null) {
				// error - type specified without a specific install (could be an old config that specified a VM ID)
				// log the error, but choose the default VM.
				IStatus status = new Status(IStatus.WARNING, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_VM_INSTALL, MessageFormat.format(LaunchingMessages.getString("JavaRuntime.VM_not_fully_specified_in_launch_configuration_{0}_-_missing_VM_name._Reverting_to_default_VM._1"), new String[] {configuration.getName()}), null); //$NON-NLS-1$
				LaunchingPlugin.log(status);
				return getDefaultVMInstall();
			} else {
				vm = vt.findVMInstallByName(name);
				if (vm == null) {
					// error - install not found
					abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Specified_VM_install_not_found__type_{0},_name_{1}_2"), new String[] {type, name}), null);					 //$NON-NLS-1$
				} else {
					return vm;
				}
			}
		}
		
		return getDefaultVMInstall();
	}
	
	/**
	 * Throws a core exception with an internal error status.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
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
	 */
	private static void abort(String message, int code, Throwable exception) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), code, message, exception));
	}	
					
	/**
	 * Returns the transitive closure of classpath entries for the
	 * given project entry.
	 * 
	 * @param projectEntry project classpath entry
	 * @param expandedPath a list of entries already expanded, should be empty
	 * to begin, and contains the result
	 * @param expanding a list of projects that have been or are currently being
	 * expanded (to detect cycles)
	 * @exception CoreException if unable to expand the classpath
	 */
	private static void expandProject(IClasspathEntry projectEntry, List expandedPath, List expanding) throws CoreException {
		expanding.add(projectEntry);
		// 1. Get the raw classpath
		// 2. Replace source folder entries with a project entry
		IPath projectPath = projectEntry.getPath();
		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(projectPath.lastSegment());
		if (res == null) {
			// add project entry and return
			expandedPath.add(projectEntry);
			return;
		}
		IJavaProject project = (IJavaProject)JavaCore.create(res);
		if (project == null || !project.exists() || !project.getProject().isOpen()) {
			// add project entry and return
			expandedPath.add(projectEntry);
			return;
		}
		
		IClasspathEntry[] buildPath = project.getRawClasspath();
		List unexpandedPath = new ArrayList(buildPath.length);
		boolean projectAdded = false;
		for (int i = 0; i < buildPath.length; i++) {
			if (buildPath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				if (!projectAdded) {
					projectAdded = true;
					unexpandedPath.add(projectEntry);
				}
			} else {
				unexpandedPath.add(buildPath[i]);
			}
		}
		// 3. expand each project entry (except for the root project)
		// 4. replace each container entry with a runtime entry associated with the project
		Iterator iter = unexpandedPath.iterator();
		while (iter.hasNext()) {
			IClasspathEntry entry = (IClasspathEntry)iter.next();
			if (entry == projectEntry) {
				expandedPath.add(entry);
			} else {
				switch (entry.getEntryKind()) {
					case IClasspathEntry.CPE_PROJECT:
						if (!expanding.contains(entry)) {
							expandProject(entry, expandedPath, expanding);
						}
						break;
					case IClasspathEntry.CPE_CONTAINER:
						IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
						int property = -1;
						if (container != null) {
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
							IRuntimeClasspathEntry r = newRuntimeContainerClasspathEntry(entry.getPath(), property);
							// check for duplicate/redundant entries 
							boolean duplicate = false;
							for (int i = 0; i < expandedPath.size(); i++) {
								Object o = expandedPath.get(i);
								if (o instanceof IRuntimeClasspathEntry) {
									IRuntimeClasspathEntry re = (IRuntimeClasspathEntry)o;
									if (re.getType() == IRuntimeClasspathEntry.CONTAINER) {
										if (container instanceof IRuntimeContainerComparator) {
											duplicate = ((IRuntimeContainerComparator)container).isDuplicate(re.getPath());
											if (duplicate) {
												break;
											}
										} else if (re.getVariableName().equals(r.getVariableName())) {
											duplicate = true;
											break;
										}
									}
								}
							}
							if (!duplicate) {
								expandedPath.add(r);
							}	
						}
						break;
					case IClasspathEntry.CPE_VARIABLE:
						if (entry.getPath().segment(0).equals(JRELIB_VARIABLE)) {
							IRuntimeClasspathEntry r = newVariableRuntimeClasspathEntry(entry.getPath());
							r.setSourceAttachmentPath(entry.getSourceAttachmentPath());
							r.setSourceAttachmentRootPath(entry.getSourceAttachmentRootPath());
							r.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
							if (!expandedPath.contains(r)) {
								expandedPath.add(r);
							}
							break;
						}
						// fall through if not the special JRELIB variable
					default:
						if (!expandedPath.contains(entry)) {
							expandedPath.add(entry);
						}
						break;
				}
			}
		}
		return;
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
		List resolved = new ArrayList(unresolved.length);
		for (int i = 0; i < unresolved.length; i++) {
			IRuntimeClasspathEntry entry = unresolved[i];
			if (unresolved[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				String location = null;
				switch (entry.getType()) {
					case IRuntimeClasspathEntry.CONTAINER:
						IRuntimeClasspathEntry[] contained = computeDefaultContainerEntries(entry, jproject);
						for (int j = 0; j < contained.length; j++) {
							location = contained[j].getLocation();
							if (location != null) {
								resolved.add(location);
							}
						}
						break;
					default:
						location = entry.getLocation();
						if (location != null) {
							resolved.add(location);
						}
						break;
				}
			}
		}
		return (String[])resolved.toArray(new String[resolved.size()]);
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
	 */
	public static void saveVMConfiguration() throws CoreException {
		try {
			String xml = getVMsAsXML();
			getPreferences().setValue(PREF_VM_XML, xml);
			savePreferences();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, LaunchingMessages.getString("JavaRuntime.ioExceptionOccurred"), e)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Write out the specified String as the new value of the VM definitions preference
	 * and save all preferences.
	 */
	private static void saveVMDefinitions(String vmDefXML) {
		LaunchingPlugin.getDefault().getPluginPreferences().setValue(PREF_VM_XML, vmDefXML);
		LaunchingPlugin.getDefault().savePluginPreferences();		
	}

	private static String getVMsAsXML() throws IOException {
		VMDefinitionsContainer container = new VMDefinitionsContainer();	
		container.setDefaultVMInstallCompositeID(getDefaultVMId());
		container.setDefaultVMInstallConnectorTypeID(getDefaultVMConnectorId());	
		IVMInstallType[] vmTypes= getVMInstallTypes();
		for (int i = 0; i < vmTypes.length; ++i) {
			IVMInstall[] vms = vmTypes[i].getVMInstalls();
			for (int j = 0; j < vms.length; j++) {
				IVMInstall install = vms[j];
				container.addVM(install);
			}
		}
		return container.getAsXML();
	}
	
	/**
	 * This method loads the set of installed JREs.  This definition is stored in the
	 * workbench preferences, however older workspaces may store this information in
	 * a meta-data file.  In both cases, the VMs are described as an XML document.
	 * If neither the preference nor the meta-data file is found, the file system is
	 * searched for VMs.
	 */
	private static void initializeVMConfiguration() throws IOException {
		// Try retrieving the VM preferences from the preference store
		String vmXMLString = getPreferences().getString(PREF_VM_XML);
		
		// If the preference was found, load VMs from it into memory
		if (vmXMLString.length() > 0) {
			try {
				ByteArrayInputStream inputStream = new ByteArrayInputStream(vmXMLString.getBytes());
				VMDefinitionsContainer vmDefs = VMDefinitionsContainer.parseXMLIntoContainer(inputStream);
				loadVMDefsIntoMemory(vmDefs);
			} catch (IOException ioe) {
				LaunchingPlugin.log(ioe);
			}			
		} else {			
			// Otherwise, look for the old file that previously held the VM defs
			IPath stateLocation= LaunchingPlugin.getDefault().getStateLocation();
			IPath stateFile= stateLocation.append("vmConfiguration.xml"); //$NON-NLS-1$
			File file = new File(stateFile.toOSString());
			
			VMDefinitionsContainer vmDefs = null;
			if (file.exists()) {        
				// If file exists, load VM defs from it into memory and write the defs to
				// the preference store WITHOUT triggering any processing of the new value
				FileInputStream fileInputStream = new FileInputStream(file);
				vmDefs = VMDefinitionsContainer.parseXMLIntoContainer(fileInputStream);
				loadVMDefsIntoMemory(vmDefs);
				LaunchingPlugin.getDefault().setIgnoreVMDefPropertyChangeEvents(true);
				saveVMDefinitions(vmDefs.getAsXML());
				LaunchingPlugin.getDefault().setIgnoreVMDefPropertyChangeEvents(false);				
			} else {				 
				// Otherwise go looking for VMs in the file system.  Write the results
				// to the preference store.  This will be treated just like a user change
				// to the VM prefs with full notification to all VM listeners.
				detectAndSaveVMDefinitions();
			}			
		}
	}
	
	/**
	 * For each VMStandin object in the specified VM container, convert it into a 'real' VM.	 */
	private static void loadVMDefsIntoMemory(VMDefinitionsContainer vmContainer) {
		fgDefaultVMId = vmContainer.getDefaultVMInstallCompositeID();
		fgDefaultVMConnectorId = vmContainer.getDefaultVMInstallConnectorTypeID();
		
		// Create the underlying VMs for each VMStandin
		List vmList = vmContainer.getValidVMList();
		Iterator vmListIterator = vmList.iterator();
		while (vmListIterator.hasNext()) {
			VMStandin vmStandin = (VMStandin) vmListIterator.next();
			vmStandin.convertToRealVM();
		}
		
	}
	
	/**
	 * Evaluates library locations for a IVMInstall. If no library locations are set on the install, a default
	 * location is evaluated and checked if it exists.
	 * @return library locations with paths that exist or are empty
	 * @since 2.0
	 */
	public static LibraryLocation[] getLibraryLocations(IVMInstall vm)  {
		IPath[] libraryPaths;
		IPath[] sourcePaths;
		IPath[] sourceRootPaths;
		LibraryLocation[] locations= vm.getLibraryLocations();
		if (locations == null) {
			LibraryLocation[] dflts= vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
			libraryPaths = new IPath[dflts.length];
			sourcePaths = new IPath[dflts.length];
			sourceRootPaths = new IPath[dflts.length];
			for (int i = 0; i < dflts.length; i++) {
				libraryPaths[i]= dflts[i].getSystemLibraryPath();
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
			for (int i = 0; i < locations.length; i++) {			
				libraryPaths[i]= locations[i].getSystemLibraryPath();
				sourcePaths[i]= locations[i].getSystemLibrarySourcePath();
				sourceRootPaths[i]= locations[i].getPackageRootPath();
			}
		}
		locations = new LibraryLocation[sourcePaths.length];
		for (int i = 0; i < sourcePaths.length; i++) {
			locations[i] = new LibraryLocation(libraryPaths[i], sourcePaths[i], sourceRootPaths[i]);
		}
		return locations;
	}
	
	/**
	 * Detect the VM that Eclipse is running on.
	 * 
	 * @return a VM standin representing the VM that Eclipse is running on, or
	 * <code>null</code> if unable to detect the runtime VM
	 */
	private static VMStandin detectEclipseRuntime() {
		VMStandin detectedVMStandin = null;
		// Try to detect a VM for each declared VM type
		IVMInstallType[] vmTypes= getVMInstallTypes();
		for (int i = 0; i < vmTypes.length; i++) {
			
			File detectedLocation= vmTypes[i].detectInstallLocation();
			if (detectedLocation != null && detectedVMStandin == null) {
				
				// Make sure the VM id is unique
				int unique = i;
				IVMInstallType vmType = vmTypes[i];
				while (vmType.findVMInstall(String.valueOf(unique)) != null) {
					unique++;
				}

				// Create a standin for the detected VM and add it to the result collector
				String vmID = String.valueOf(unique);
				detectedVMStandin = new VMStandin(vmType, vmID);
				if (detectedVMStandin != null) {
					detectedVMStandin.setInstallLocation(detectedLocation);
					detectedVMStandin.setName(generateDetectedVMName(detectedVMStandin));						
				}				
			}
		}
		return detectedVMStandin;
	}

	/**
	 * Tries to locate a default VM (if one is not currently set). Sets the
	 * default VM to be the Eclipse runtime or the first VM found. Log an error
	 * with the workspace if a no VMs can be located.
	 */
	private static void detectDefaultVM() {
		if (getDefaultVMId() == null) {
			VMStandin eclipseRuntime = detectEclipseRuntime();
			IVMInstall defaultVM = null;
			IVMInstallType[] vmTypes= getVMInstallTypes();
			if  (eclipseRuntime == null) {
				// No default VM or Eclipse runtime. Set the first VM as the default (if any)
				for (int i = 0; i < vmTypes.length; i++) {
					IVMInstallType type = vmTypes[i];
					IVMInstall[] vms = type.getVMInstalls();
					for (int j = 0; j < vms.length; j++) {
						defaultVM = vms[j];
						break;
					}
					if (defaultVM != null) {
						break;
					}
				}
				if (defaultVM == null) {
					// did not detect default VM - register error with the workspace root
					final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					try {
						IMarker[] markers = root.findMarkers(LaunchingPlugin.NO_DEFAULT_JRE_MARKER_TYPE, false, IResource.DEPTH_ZERO);
						if (markers.length == 0) {
							// create a new error - none exist
							IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
								/**
								 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
								 */
								public void run(IProgressMonitor monitor) throws CoreException {
									IMarker error = root.createMarker(LaunchingPlugin.NO_DEFAULT_JRE_MARKER_TYPE);
									error.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
									error.setAttribute(IMarker.MESSAGE, LaunchingMessages.getString("JavaRuntime.Unable_to_detect_default_JRE._See___Preferences_->_Java_->_Installed_JREs___1")); //$NON-NLS-1$
								}
							};
							root.getWorkspace().run(runnable, null);
						}
					} catch (CoreException e) {
					}
				}
			} else {
				// if there is no default VM, set the Eclipse runtime to be the default
				// VM. First search for an existing VM install with the same
				// install location as the detected runtime
				IVMInstallType type = eclipseRuntime.getVMInstallType();
				IVMInstall[] vms = type.getVMInstalls();
				for (int j = 0; j < vms.length; j++) {
					IVMInstall install = vms[j];
					if (install.getInstallLocation().equals(eclipseRuntime.getInstallLocation())) {
						defaultVM = install;
						break;
					}
				}
				if (defaultVM == null) {
					// There is no VM install that corresponds to the Eclipse runtime.
					// Create a VM install for the Eclipse runtime.
					defaultVM = eclipseRuntime.convertToRealVM();
				}
			}
			if (defaultVM != null) {
				fgDefaultVMId = getCompositeIdFromVM(defaultVM);
			}
		}		
	}
	/**
	 * Detects VM installations, and a default VM (if required). Saves the
	 * results.
	 */
	private static void detectAndSaveVMDefinitions() {
		detectDefaultVM();
		try {
			String vmDefXML = getVMsAsXML();
			saveVMDefinitions(vmDefXML);
		} catch (IOException ioe) {
			LaunchingPlugin.log(ioe);
		}
	}
	
	/**
	 * Make the name of a detected VM stand out.
	 */	private static String generateDetectedVMName(IVMInstall vm) {
		return vm.getInstallLocation().getName();
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
		return JavaCore.newContainerEntry(new Path(JRE_CONTAINER));
	}	
	
	/**
	 * Returns the VM connetor defined with the specified identifier,
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
	public static Preferences getPreferences() {
		return LaunchingPlugin.getDefault().getPluginPreferences();
	}
	
	/**
	 * Saves the preferences for the launching plug-in.
	 * 
	 * @since 2.0
	 */
	public static void savePreferences() {
		LaunchingPlugin.getDefault().savePluginPreferences();
	}
	
	/**
	 * Registers the given resolver for the specified variable.
	 * 
	 * @param resolver runtime classpathe entry resolver
	 * @param variableName variable name to register for
	 * @since 2.0
	 */
	public static void addVariableResolver(IRuntimeClasspathEntryResolver resolver, String variableName) {
		Map map = getVariableResolvers();
		map.put(variableName, resolver);
	}
	
	/**
	 * Registers the given resolver for the specified container.
	 * 
	 * @param resolver runtime classpathe entry resolver
	 * @param containerIdentifier identifier of the classpath container to register for
	 * @since 2.0
	 */
	public static void addContainerResolver(IRuntimeClasspathEntryResolver resolver, String containerIdentifier) {
		Map map = getContainerResolvers();
		map.put(containerIdentifier, resolver);
	}	
	
	/**
	 * Returns all registered variable resolvers.
	 */
	private static Map getVariableResolvers() {
		if (fgVariableResolvers == null) {
			initializeResolvers();
		}
		return fgVariableResolvers;
	}
	
	/**
	 * Returns all registered container resolvers.
	 */
	private static Map getContainerResolvers() {
		if (fgContainerResolvers == null) {
			initializeResolvers();
		}
		return fgContainerResolvers;
	}

	private static void initializeResolvers() {
		IExtensionPoint point = LaunchingPlugin.getDefault().getDescriptor().getExtensionPoint(EXTENSION_POINT_RUNTIME_CLASSPATH_ENTRY_RESOLVERS);
		IConfigurationElement[] extensions = point.getConfigurationElements();
		fgVariableResolvers = new HashMap(extensions.length);
		fgContainerResolvers = new HashMap(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			RuntimeClasspathEntryResolver res = new RuntimeClasspathEntryResolver(extensions[i]);
			String variable = res.getVariableName();
			String container = res.getContainerId();
			if (variable != null) {
				fgVariableResolvers.put(variable, res);
			}
			if (container != null) {
				fgContainerResolvers.put(container, res);
			}
		}		
	}

	/**
	 * Returns all registered classpath providers.
	 */
	private static Map getClasspathProviders() {
		if (fgPathProviders == null) {
			initializeProviders();
		}
		return fgPathProviders;
	}
		
	private static void initializeProviders() {
		IExtensionPoint point = LaunchingPlugin.getDefault().getDescriptor().getExtensionPoint(EXTENSION_POINT_RUNTIME_CLASSPATH_PROVIDERS);
		IConfigurationElement[] extensions = point.getConfigurationElements();
		fgPathProviders = new HashMap(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			RuntimeClasspathProvider res = new RuntimeClasspathProvider(extensions[i]);
			fgPathProviders.put(res.getIdentifier(), res);
		}		
	}
		
	/**
	 * Returns the resovler registered for the give variable, or
	 * <code>null</code> if none.
	 * 
	 * @return the resovler registered for the give variable, or
	 * <code>null</code> if none
	 */
	private static IRuntimeClasspathEntryResolver getVariableResolver(String variableName) {
		return (IRuntimeClasspathEntryResolver)getVariableResolvers().get(variableName);
	}
	
	/**
	 * Returns the resovler registered for the give container id, or
	 * <code>null</code> if none.
	 * 
	 * @return the resovler registered for the give container id, or
	 * <code>null</code> if none
	 */	
	private static IRuntimeClasspathEntryResolver getContainerResolver(String containerId) {
		return (IRuntimeClasspathEntryResolver)getContainerResolvers().get(containerId);
	}	
	
	/**
	 * Returns the provider registered for the given identifier, or
	 * <code>null</code> if none.
	 * 
	 * @return the provider registered for the given identifier, or
	 * <code>null</code> if none
	 */
	private static IRuntimeClasspathProvider getClasspathProvider(String identifier) {
		return (IRuntimeClasspathProvider)getClasspathProviders().get(identifier);
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
	 * @param vm the VM that has changed
	 * @param event event desribing the change.
	 * @since 2.0
	 */
	public static void fireVMChanged(PropertyChangeEvent event) {
		if (event.getSource() instanceof VMStandin) {
			// do not fire events for VM standins
			return;
		}		
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
		if (vm instanceof VMStandin) {
			// do not fire events for VM standins
			return;
		}
		Object[] listeners = fgVMListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IVMInstallChangedListener listener = (IVMInstallChangedListener)listeners[i];
			listener.vmAdded(vm);
		}		
	}	
	
	/**
	 * Notifies all VM install changed listeners of the VM removal
	 * 
	 * @param vm the VM that has been removed
	 * @since 2.0
	 */
	public static void fireVMRemoved(IVMInstall vm) {
		if (vm instanceof VMStandin) {
			// do not fire events for VM standins
			return;
		}
		Object[] listeners = fgVMListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IVMInstallChangedListener listener = (IVMInstallChangedListener)listeners[i];
			listener.vmRemoved(vm);
		}		
	}		
	
}