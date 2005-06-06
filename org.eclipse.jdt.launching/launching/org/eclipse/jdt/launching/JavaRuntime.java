/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
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
import org.eclipse.jdt.internal.launching.JavaSourceLookupUtil;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.ListenerList;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntryResolver;
import org.eclipse.jdt.internal.launching.RuntimeClasspathProvider;
import org.eclipse.jdt.internal.launching.SocketAttachConnector;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.internal.launching.VariableClasspathEntry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	 * If the resulting <code>IPath</code> is a relative path, it is interpretted
	 * as relative to the workspace location. If the path is absolute, it is 
	 * interpretted as an absolute path in the local file system.
	 * </p>
	 * @since 3.1
	 * @see org.eclipse.jdt.core.IClasspathAttribute
	 */
	public static final String CLASSPATH_ATTR_LIBRARY_PATH_ENTRY =  LaunchingPlugin.getUniqueIdentifier() + ".CLASSPATH_ATTR_LIBRARY_PATH_ENTRY"; //$NON-NLS-1$

	private static IVMInstallType[] fgVMTypes= null;
	private static String fgDefaultVMId= null;
	private static String fgDefaultVMConnectorId = null;
	
	/**
	 * Resolvers keyed by variable name, container id,
	 * and runtime classpath entry id.
	 */
	private static Map fgVariableResolvers = null;
	private static Map fgContainerResolvers = null;
	private static Map fgRuntimeClasspathEntryResolvers = null;
	
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
	 * Cache of already resolved projects in container entries. Used to avoid
	 * cycles in project dependencies when resolving classpath container entries.
	 */
	private static ThreadLocal fgProjects = new ThreadLocal();
	
	/**
	 * This class contains only static methods, and is not intended
	 * to be instantiated.
	 */
	private JavaRuntime() {
	}

	private static synchronized void initializeVMTypes() {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, "vmInstallTypes"); //$NON-NLS-1$
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements(); 
		MultiStatus status= new MultiStatus(LaunchingPlugin.getUniqueIdentifier(), IStatus.OK, LaunchingMessages.JavaRuntime_exceptionOccurred, null); //$NON-NLS-1$
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
		} catch (ParserConfigurationException e) {
			LaunchingPlugin.log(e);
		} catch (TransformerException e) {
			LaunchingPlugin.log(e);
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
		if (install != null && install.getInstallLocation().exists()) {
			return install;
		}
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
	public static synchronized IVMInstallType[] getVMInstallTypes() {
		if (fgVMTypes == null) {
			initializeVMTypes();
		}
		return fgVMTypes; 
	}
	
	private static synchronized String getDefaultVMId() {
		if (fgVMTypes == null) {
			initializeVMTypes();
		}
		return fgDefaultVMId;
	}
	
	private static synchronized String getDefaultVMConnectorId() {
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
	 * @since 2.1
	 */
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
	 * @param idString the composite id that specifies an instance of IVMInstall
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
		IClasspathEntry cpe = JavaCore.newContainerEntry(path);
		RuntimeClasspathEntry entry = new RuntimeClasspathEntry(cpe, classpathProperty);
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
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element)node;
					if ("memento".equals(element.getNodeName())) { //$NON-NLS-1$
						entry.initializeFrom(element);
					}
				}
			}
			return entry;
		} catch (SAXException e) {
			abort(LaunchingMessages.JavaRuntime_31, e); //$NON-NLS-1$
		} catch (IOException e) {
			abort(LaunchingMessages.JavaRuntime_32, e); //$NON-NLS-1$
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
	 * Computes and returns the default unresolved runtime claspath for the
	 * given project.
	 * 
	 * @return runtime classpath entries
	 * @exception CoreException if unable to compute the runtime classpath
	 * @see IRuntimeClasspathEntry
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] computeUnresolvedRuntimeClasspath(IJavaProject project) throws CoreException {
		IClasspathEntry[] entries = project.getRawClasspath();
		List classpathEntries = new ArrayList(3);
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
		return (IRuntimeClasspathEntry[]) classpathEntries.toArray(new IRuntimeClasspathEntry[classpathEntries.size()]);
		

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
			if (provider == null) {
				abort(MessageFormat.format(LaunchingMessages.JavaRuntime_26, new String[]{providerId}), null); //$NON-NLS-1$
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
			provider = (IRuntimeClasspathProvider)getClasspathProviders().get(providerId);
			if (provider == null) {
				abort(MessageFormat.format(LaunchingMessages.JavaRuntime_27, new String[]{providerId}), null); //$NON-NLS-1$
			}
		}
		return provider;
	}	
		
	/**
	 * Returns resolved entries for the given entry in the context of the given
	 * launch configuration. If the entry is of kind
	 * <code>VARIABLE</code> or <code>CONTAINER</code>, variable and contanier
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
					// could not resolve project
					abort(MessageFormat.format(LaunchingMessages.JavaRuntime_Classpath_references_non_existant_project___0__3, new String[]{entry.getPath().lastSegment()}), null); //$NON-NLS-1$
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
				if (location == null) {
					abort(MessageFormat.format(LaunchingMessages.JavaRuntime_Classpath_references_non_existant_archive___0__4, new String[]{entry.getPath().toString()}), null); //$NON-NLS-1$
				}
				File file = new File(location);
				if (!file.exists()) {
					abort(MessageFormat.format(LaunchingMessages.JavaRuntime_Classpath_references_non_existant_archive___0__4, new String[]{entry.getPath().toString()}), null); //$NON-NLS-1$
				}
				break;
			case IRuntimeClasspathEntry.OTHER:
				resolver = getContributedResolver(((IRuntimeClasspathEntry2)entry).getTypeId());
				return resolver.resolveRuntimeClasspathEntry(entry, configuration);
			default:
				break;
		}
		return new IRuntimeClasspathEntry[] {entry};
	}
	
	/**
	 * Default resolution for a classpath variable - resolve to an archive. Only
	 * one of project/configuration can be non-null.
	 * 
	 * @param entry
	 * @param project the project context or <code>null</code>
	 * @param configuration configuration context or <code>null</code>
	 * @return IRuntimeClasspathEntry[]
	 * @throws CoreException
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
	 * @param project
	 * @param classpathProperty the type of classpath entries to create
	 * @return IRuntimeClasspathEntry[] or <code>null</code>
	 * @throws CoreException
	 */
	private static IRuntimeClasspathEntry[] resolveOutputLocations(IJavaProject project, int classpathProperty) throws CoreException {
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
		} 
		// add the default location if not already included
		IPath def = project.getOutputLocation();
		if (!nonDefault.contains(def)) {
			nonDefault.add(def);						
		}
		IRuntimeClasspathEntry[] locations = new IRuntimeClasspathEntry[nonDefault.size()];
		for (int i = 0; i < locations.length; i++) {
			IClasspathEntry newEntry = JavaCore.newLibraryEntry((IPath)nonDefault.get(i), null, null);
			locations[i] = new RuntimeClasspathEntry(newEntry);
			locations[i].setClasspathProperty(classpathProperty);
		}
		return locations;						
	}
	
	/**
	 * Returns resolved entries for the given entry in the context of the given
	 * Java project. If the entry is of kind
	 * <code>VARIABLE</code> or <code>CONTAINER</code>, variable and contanier
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
	 */
	private static IRuntimeClasspathEntry[] computeDefaultContainerEntries(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		if (project == null || entry == null) {
			// cannot resolve without entry or project context
			return new IRuntimeClasspathEntry[0];
		} 
		IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
		if (container == null) {
			abort(MessageFormat.format(LaunchingMessages.JavaRuntime_Could_not_resolve_classpath_container___0__1, new String[]{entry.getPath().toString()}), null); //$NON-NLS-1$
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
		List resolved = new ArrayList(cpes.length);
		for (int i = 0; i < cpes.length; i++) {
			IClasspathEntry cpe = cpes[i];
			if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(cpe.getPath().segment(0));
				IJavaProject jp = JavaCore.create(p);
				List projects = (List) fgProjects.get();
				if (projects == null) {
					projects = new ArrayList();
					fgProjects.set(projects);
				}
				if (!projects.contains(jp)) {
					try {
						projects.add(jp);
						IRuntimeClasspathEntry classpath = newDefaultProjectClasspathEntry(jp);
						IRuntimeClasspathEntry[] entries = resolveRuntimeClasspathEntry(classpath, jp);
						for (int j = 0; j < entries.length; j++) {
							IRuntimeClasspathEntry e = entries[j];
							if (!resolved.contains(e)) {
								resolved.add(entries[j]);
							}
						}
					} finally {
						projects.remove(jp);
					}
				}
				if (projects.isEmpty()) {
					fgProjects.set(null);
				}
			} else {
				IRuntimeClasspathEntry e = newRuntimeClasspathEntry(cpe);
				if (!resolved.contains(e)) {
					resolved.add(e);
				}
			}
		}
		// set classpath property
		IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[resolved.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (IRuntimeClasspathEntry) resolved.get(i);
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
		if (javaProject != null && javaProject.getProject().exists() && !javaProject.getProject().isOpen()) {
			abort(MessageFormat.format(LaunchingMessages.JavaRuntime_28, new String[] {configuration.getName(), projectName}), IJavaLaunchConfigurationConstants.ERR_PROJECT_CLOSED, null); //$NON-NLS-1$
		}
		if ((javaProject == null) || !javaProject.exists()) {
			abort(MessageFormat.format(LaunchingMessages.JavaRuntime_Launch_configuration__0__references_non_existing_project__1___1, new String[] {configuration.getName(), projectName}), IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT, null); //$NON-NLS-1$
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
				abort(MessageFormat.format(LaunchingMessages.JavaRuntime_Specified_VM_install_type_does_not_exist___0__2, new String[] {type}), null); //$NON-NLS-1$
			}
			IVMInstall vm = null;
			// look for a name
			String name = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
			if (name == null) {
				// error - type specified without a specific install (could be an old config that specified a VM ID)
				// log the error, but choose the default VM.
				IStatus status = new Status(IStatus.WARNING, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_VM_INSTALL, MessageFormat.format(LaunchingMessages.JavaRuntime_VM_not_fully_specified_in_launch_configuration__0____missing_VM_name__Reverting_to_default_VM__1, new String[] {configuration.getName()}), null); //$NON-NLS-1$
				LaunchingPlugin.log(status);
				return getDefaultVMInstall();
			} 
			vm = vt.findVMInstallByName(name);
			if (vm == null) {
				// error - install not found
				abort(MessageFormat.format(LaunchingMessages.JavaRuntime_Specified_VM_install_not_found__type__0___name__1__2, new String[] {vt.getName(), name}), null);					 //$NON-NLS-1$
			} else {
				return vm;
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
		if (fgVMTypes == null) {
			// if the VM types have not been instantiated, there can be no changes.
			return;
		}
		try {
			String xml = getVMsAsXML();
			getPreferences().setValue(PREF_VM_XML, xml);
			savePreferences();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, LaunchingMessages.JavaRuntime_exceptionsOccurred, e)); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, LaunchingMessages.JavaRuntime_exceptionsOccurred, e)); //$NON-NLS-1$
		} catch (TransformerException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, LaunchingMessages.JavaRuntime_exceptionsOccurred, e)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Write out the specified String as the new value of the VM definitions preference
	 * and save all preferences.
	 */
	private static void saveVMDefinitions(final String vmDefXML) {
		Job prefJob = new Job(LaunchingMessages.JavaRuntime_0) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				LaunchingPlugin.getDefault().getPluginPreferences().setValue(PREF_VM_XML, vmDefXML);
				LaunchingPlugin.getDefault().savePluginPreferences();
				return Status.OK_STATUS;
			}
		};
		prefJob.setSystem(true);
		prefJob.schedule();

	}

	private static String getVMsAsXML() throws IOException, ParserConfigurationException, TransformerException {
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
	private static void initializeVMConfiguration() throws ParserConfigurationException, IOException, TransformerException {
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
	 * For each VMStandin object in the specified VM container, convert it into a 'real' VM.
	 */
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
		URL[] javadocLocations;
		LibraryLocation[] locations= vm.getLibraryLocations();
		if (locations == null) {
			LibraryLocation[] dflts= vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
			libraryPaths = new IPath[dflts.length];
			sourcePaths = new IPath[dflts.length];
			sourceRootPaths = new IPath[dflts.length];
			javadocLocations= new URL[dflts.length];
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
					if (vmType instanceof AbstractVMInstallType) {
						AbstractVMInstallType abs = (AbstractVMInstallType)vmType;
						URL url = abs.getDefaultJavadocLocation(detectedLocation);
						detectedVMStandin.setJavadocLocation(url);						
					}
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
		} catch (ParserConfigurationException e) {
			LaunchingPlugin.log(e);
		} catch (TransformerException e) {
			LaunchingPlugin.log(e);
		}
	}
	
	/**
	 * Make the name of a detected VM stand out.
	 */
	private static String generateDetectedVMName(IVMInstall vm) {
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
	
	/**
	 * Returns all registered runtime classpath entry resolvers.
	 */
	private static Map getEntryResolvers() {
		if (fgRuntimeClasspathEntryResolvers == null) {
			initializeResolvers();
		}
		return fgRuntimeClasspathEntryResolvers;
	}	

	private static void initializeResolvers() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, EXTENSION_POINT_RUNTIME_CLASSPATH_ENTRY_RESOLVERS);
		IConfigurationElement[] extensions = point.getConfigurationElements();
		fgVariableResolvers = new HashMap(extensions.length);
		fgContainerResolvers = new HashMap(extensions.length);
		fgRuntimeClasspathEntryResolvers = new HashMap(extensions.length);
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
	 */
	private static Map getClasspathProviders() {
		if (fgPathProviders == null) {
			initializeProviders();
		}
		return fgPathProviders;
	}
		
	private static void initializeProviders() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, EXTENSION_POINT_RUNTIME_CLASSPATH_PROVIDERS);
		IConfigurationElement[] extensions = point.getConfigurationElements();
		fgPathProviders = new HashMap(extensions.length);
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
	private static IRuntimeClasspathEntryResolver getVariableResolver(String variableName) {
		return (IRuntimeClasspathEntryResolver)getVariableResolvers().get(variableName);
	}
	
	/**
	 * Returns the resolver registered for the given container id, or
	 * <code>null</code> if none.
	 * 
	 * @param containerId the container to determine the resolver for
	 * @return the resolver registered for the given container id, or
	 * <code>null</code> if none
	 */	
	private static IRuntimeClasspathEntryResolver getContainerResolver(String containerId) {
		return (IRuntimeClasspathEntryResolver)getContainerResolvers().get(containerId);
	}
	
	/**
	 * Returns the resolver registered for the given contributed classpath
	 * entry type.
	 * 
	 * @param typeId the id of the contributed classpath entry
	 * @return the resolver registered for the given clsspath entry
	 */	
	private static IRuntimeClasspathEntryResolver getContributedResolver(String typeId) {
		IRuntimeClasspathEntryResolver resolver = (IRuntimeClasspathEntryResolver)getEntryResolvers().get(typeId);
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
		Object[] listeners = fgVMListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IVMInstallChangedListener listener = (IVMInstallChangedListener)listeners[i];
			listener.vmRemoved(vm);
		}		
	}		
	
	/**
	 * Return the String representation of the default output directory of the
	 * launch config's project or <code>null</code> if there is no config, no
	 * project or some sort of problem.
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
		Set visited = new HashSet();
		List entries = new ArrayList();
		gatherJavaLibraryPathEntries(project, requiredProjects, visited, entries);
		List resolved = new ArrayList(entries.size());
		Iterator iterator = entries.iterator();
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		while (iterator.hasNext()) {
			String entry = (String) iterator.next();
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
		return (String[])resolved.toArray(new String[resolved.size()]);
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
	private static void gatherJavaLibraryPathEntries(IJavaProject project, boolean requiredProjects, Set visited, List entries) throws CoreException {
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
	 * @throws CoreException
	 * @since 3.1
	 */
	private static IClasspathEntry[] processJavaLibraryPathEntries(IJavaProject project, boolean collectRequired, IClasspathEntry[] classpathEntries, List entries) throws CoreException {
		List req = null;
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
							req = new ArrayList();
						}
						for (int j = 0; j < requiredProjects.length; j++) {
							req.add(requiredProjects[j]);
						}
					}
				}
			} else if (collectRequired && entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				if (req == null) {
					req = new ArrayList();
				}
				req.add(entry);
			}
		}
		if (req != null) {
			return (IClasspathEntry[]) req.toArray(new IClasspathEntry[req.size()]);
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
	 * @return a claspath attribute with the name <code>CLASSPATH_ATTR_LIBRARY_PATH_ENTRY</code>
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

}
