package org.eclipse.jdt.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.CompositeId;
import org.eclipse.jdt.internal.launching.JavaClasspathVariablesInitializer;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntryResolver;
import org.eclipse.jdt.internal.launching.SocketAttachConnector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The central access point for launching support. This class manages
 * the registered VM types contributed through the 
 * <code>"org.eclipse.jdt.launching.vmType"</code> extension point, and
 * supports associating particular VMs with Java projects.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 * 
 * @see IVMInstallType
 */
public final class JavaRuntime {
	
	/**
	 * Classpath variable name used for the default JRE's library.
	 */
	public static final String JRELIB_VARIABLE= "JRE_LIB"; //$NON-NLS-1$

	/**
	 * Classpath variable name used for the default JRE's library source.
	 */
	public static final String JRESRC_VARIABLE= "JRE_SRC"; //$NON-NLS-1$
	
	/**
	 * Classpath variable name used for the default JRE's library source root.
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
	 * Classpath container used for a project's JRE. A container
	 * is resolved in the context of a specific Java project, to
	 * one or more system libraries contained in a JRE. The container
	 * can have zero or two path segments following the container name. When
	 * no segments follow the container name, the workspace default JRE is used
	 * to build a project. Otherwise the segments identify a specific JRE used
	 * to build a project:
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
	 * Preference key for launch/connect timeout. VM Runners should honor this timeout
	 * value when attempting to launch and connect to a debuggable VM. The value is
	 * an int, indicating a number of millieseconds.
	 * 
	 * @since 2.0
	 */
	public static final String PREF_CONNECT_TIMEOUT = LaunchingPlugin.getUniqueIdentifier() + ".PREF_CONNECT_TIMEOUT"; //$NON-NLS-1$
	
	/**
	 * Default launch/connect timeout (ms).
	 * 
	 * @since 2.0
	 */
	public static final int DEF_CONNECT_TIMEOUT = 20000;
	
	/**
	 * The class org.eclipse.debug.core.model.IProcess allows attaching
	 * String properties to processes.
	 * The intent of this property is to show the command line a process
	 * was launched with. Implementers of IVMRunners use this property
	 * key to attach the command line to the IProcesses they create.
	 */
	public final static String ATTR_CMDLINE= LaunchingPlugin.getUniqueIdentifier() + ".launcher.cmdLine"; //$NON-NLS-1$

	private static final String PROPERTY_BUILD_VM= LaunchingPlugin.getUniqueIdentifier() + ".build_vm"; //$NON-NLS-1$

	private static IVMInstallType[] fgVMTypes= null;
	private static String fgDefaultVMId= null;
	private static String fgDefaultVMConnectorId = null;
	
	/**
	 * Resolvers keyed by variable name and container id.
	 */
	private static Map fVariableResolvers = null;
	private static Map fContainerResolvers = null;
	
	/**
	 * Not intended to be instantiated.
	 */
	private JavaRuntime() {
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
	 * The project must exist.
	 * 
	 * @return the VM instance that is selected for the given Java project
	 * 		   Returns null if no VM was previously set.
	 * @throws CoreException	If reading the property from the underlying
	 * 							property failed or if the property value was
	 * 							corrupt.
	 */
	public static IVMInstall getVMInstall(IJavaProject project) throws CoreException {
		String idString= project.getProject().getPersistentProperty(new QualifiedName(LaunchingPlugin.getUniqueIdentifier(), PROPERTY_BUILD_VM));
		return getVMFromId(idString);
	}
	
	private static IVMInstall getVMFromId(String idString) {
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
	 * Returns the IVMInstallType with the given unique id. 
	 * @return	The IVMInstallType for the given id, or null if no
	 * 			IVMInstallType with the given type is registered.
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
	 * Sets the VM to be used for building the given Java project.
	 * Note that this setting will be persisted between workbench sessions.
	 * The project needs to exist.
	 * 
	 * @param project the Java project
	 * @param javaRuntime the VM instance. May be null to clear the
	 * 					  property.
	 * @throws	CoreException	If the propety could not be set to
	 * 							the underlying project.
	 */
	public static void setVM(IJavaProject project, IVMInstall javaRuntime) throws CoreException {
		String idString= getIdFromVM(javaRuntime);
		project.getProject().setPersistentProperty(new QualifiedName(LaunchingPlugin.getUniqueIdentifier(), PROPERTY_BUILD_VM), idString);
	}
	
	/**
	 * Sets a VM as the system-wide default VM. This setting is persisted when
	 * saveVMConfiguration is called. 
	 * @param	vm	The vm to make the default. May be null to clear 
	 * 				the default.
	 * @deprecated Use setDefaultVMInstall(IVMInstall, IProgressMonitor) instead
	 */
	public static void setDefaultVMInstall(IVMInstall vm) {
		try {
			setDefaultVMInstall(vm, null);
		} catch (CoreException e) {
			LaunchingPlugin.getDefault().getLog().log(e.getStatus());
		}
	}
	
	/**
	 * Sets a VM as the system-wide default VM. This setting is persisted when
	 * saveVMConfiguration is called. 
	 * @param	vm	The vm to make the default. May be null to clear 
	 * 				the default.
	 */
	public static void setDefaultVMInstall(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		fgDefaultVMId= getIdFromVM(vm);
		updateJREVariables(monitor);
		saveVMConfiguration();
	}	
	
	/**
	 * Sets a VM connector as the system-wide default VM. This setting is persisted when
	 * saveVMConfiguration is called. 
	 * @param	connector The connector to make the default. May be null to clear 
	 * 				the default.
	 */
	public static void setDefaultVMConnector(IVMConnector connector, IProgressMonitor monitor) throws CoreException {
		fgDefaultVMConnectorId= connector.getIdentifier();
		saveVMConfiguration();
	}		
	
	private static void updateJREVariables(IProgressMonitor monitor) throws CoreException {
		JavaClasspathVariablesInitializer updater= new JavaClasspathVariablesInitializer();
		updater.updateJREVariables(monitor);
	}
	/**
	 * Return the default VM set with <code>setDefaultVM()</code>.
	 * @return	Returns the default VM. May return null when no default
	 * 			VM was set or when the default VM has been disposed.
	 */
	public static IVMInstall getDefaultVMInstall() {
		IVMInstall install= getVMFromId(getDefaultVMId());
		if (install != null && install.getInstallLocation().exists()) {
			return install;
		} else {
			// if the default JRE goes missing, re-detect
			if (install != null) {
				install.getVMInstallType().disposeVMInstall(install.getId());
			}
			fgDefaultVMId = null;
			try {
				//get rid of bad values on disk
				saveVMConfiguration();
			} catch(CoreException e) {
				LaunchingPlugin.log(e);
			}
			detectVMConfiguration();
			return getVMFromId(getDefaultVMId());
		}
	}
	
	/**
	 * Return the default VM connector.
	 * @return	Returns the default VM connector.
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
	
	private static String getIdFromVM(IVMInstall vm) {
		if (vm == null) {
			return null;
		}
		IVMInstallType vmType= vm.getVMInstallType();
		String typeID= vmType.getId();
		CompositeId id= new CompositeId(new String[] { typeID, vm.getId() });
		return id.toString();
	}
	
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE<b>
	 * 
	 * Returns a new runtime classpath entry for the given project.
	 * 
	 * @param project Java project
	 * @return runtime classpath entry
	 */
	public static IRuntimeClasspathEntry newProjectRuntimeClasspathEntry(IJavaProject project) {
		IClasspathEntry cpe = JavaCore.newProjectEntry(project.getProject().getFullPath());
		return newRuntimeClasspathEntry(cpe);
	}
	
	
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE<b>
	 * 
	 * Returns a new runtime classpath entry for the given archive.
	 * 
	 * @param resource archive resource
	 * @return runtime classpath entry
	 */
	public static IRuntimeClasspathEntry newArchiveRuntimeClasspathEntry(IResource resource) {
		IClasspathEntry cpe = JavaCore.newLibraryEntry(resource.getLocation(), null, null);
		return newRuntimeClasspathEntry(cpe);
	}
	
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE<b>
	 * 
	 * Returns a new runtime classpath entry for the given archive (possibly
	 * external).
	 * 
	 * @param path absolute path to an archive
	 * @return runtime classpath entry
	 */
	public static IRuntimeClasspathEntry newArchiveRuntimeClasspathEntry(IPath path) {
		IClasspathEntry cpe = JavaCore.newLibraryEntry(path, null, null);
		return newRuntimeClasspathEntry(cpe);
	}

	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE<b>
	 * 
	 * Returns a new runtime classpath entry for the classpath
	 * variable with the given name.
	 * 
	 * @param name class path variable name
	 * @return runtime classpath entry
	 */
	public static IRuntimeClasspathEntry newVariableRuntimeClasspathEntry(String name) {
		IClasspathEntry cpe = JavaCore.newVariableEntry(new Path(name), null, null);
		return newRuntimeClasspathEntry(cpe);
	}

	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE<b>
	 * 
	 * Returns a runtime classpath entry for the given container path in the
	 * conext of the project with the given name.
	 * 
	 * @param path container path
	 * @param classpathProperty the type of entry - one of <code>USER_CLASSES</code>,
	 * 	<code>BOOTSTRAP_CLASSES</code>, or <code>STANDARD_CLASSES</code>
	 * @return runtime classpath entry
	 * @exception CoreException if unable to construct a runtime classpath entry
	 */
	public static IRuntimeClasspathEntry newRuntimeContainerClasspathEntry(IPath path, int classpathProperty) throws CoreException {
		IClasspathEntry cpe = JavaCore.newContainerEntry(path);
		return new RuntimeClasspathEntry(cpe, classpathProperty);
	}
		
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE<b>
	 * 
	 * Returns a runtime classpath entry constructed from the given memento.
	 * 
	 * @param memento a menento for a runtime classpath entry
	 * @return runtime classpath entry
	 * @exception CoreException if unable to construct a runtime classpath entry
	 */
	public static IRuntimeClasspathEntry newRuntimeClasspathEntry(String memento) throws CoreException {
		return new RuntimeClasspathEntry(memento);
	}
	
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE<b>
	 * 
	 * Returns a runtime classpath entry that corresponds to the given
	 * classpath entry. The classpath entry may not be of type <code>CPE_SOURCE</code>
	 * or <code>CPE_CONTAINER</code>.
	 * 
	 * @param entry a classpath entry
	 * @return runtime classpath entry
	 */
	private static IRuntimeClasspathEntry newRuntimeClasspathEntry(IClasspathEntry entry) {
		return new RuntimeClasspathEntry(entry);
	}	
			
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Computes the default (unresolved) runtime claspath for a given project.
	 * 
	 * @return runtime classpath entries
	 * @exception CoreException if unable to compute the runtime classpath
	 */
	public static IRuntimeClasspathEntry[] computeRuntimeClasspath(IJavaProject project) throws CoreException {
		IClasspathEntry entry = JavaCore.newProjectEntry(project.getProject().getFullPath());
		List classpathEntries = expandProject(entry);
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
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Returns the (unresolved) source lookup path for the given (Java) launch configuration,
	 * computing the path if required (i.e. is not explicitly set on the
	 * launch configuration).
	 * 
	 * @param configuration launch configuration
	 * @return runtime classpath entries that should be on the source lookup path
	 * @exception CoreException if unable to compute the source lookup path
	 */
	public static IRuntimeClasspathEntry[] computeSourceLookupPath(ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, true);
		IRuntimeClasspathEntry[] entries = null;
		if (useDefault) {
			// the default source lookup path is the same as the classpath
			entries = computeRuntimeClasspath(configuration);
		} else {
			// recover persisted source path
			entries = recoverRuntimePath(configuration, IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH);
		}
		return entries;
	}
	
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Returns a resolved entries for the given entry in the context of the given
	 * launch configuration for a runtime classpath.
	 * 
	 * @param entry runtime classpath entry
	 * @param configuration launch configuration
	 * @return resolved runtime classpath entry
	 * @exception CoreException if unable to resolve
	 * @see IRuntimeClasspathEntryResolver
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] resolveForClasspath(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.VARIABLE:
				IRuntimeClasspathEntryResolver resolver = getVariableResolver(entry.getVariableName());
				if (resolver == null) {
					// no resolution by default
					break;
				} else {
					return resolver.resolveForClasspath(entry, configuration);
				}				
			case IRuntimeClasspathEntry.CONTAINER:
				resolver = getContainerResolver(entry.getVariableName());
				if (resolver == null) {
					
				} else {
					return resolver.resolveForClasspath(entry, configuration);
				}
			default:
				break;
		}
		return new IRuntimeClasspathEntry[] {entry};
	}
	
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Returns a resolved entries for the given entry in the context of the given
	 * launch configuration for a source lookup path.
	 * 
	 * @param entry runtime classpath entry
	 * @param configuration launch configuration
	 * @return resolved runtime classpath entry
	 * @exception CoreException if unable to resolve
	 * @see IRuntimeClasspathEntryResolver
	 * @since 2.0
	 */
	public static IRuntimeClasspathEntry[] resolveForSourceLookupPath(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.VARIABLE:
				IRuntimeClasspathEntryResolver resolver = getVariableResolver(entry.getVariableName());
				if (resolver == null) {
					// no resolution by default
					break;
				} else {
					return resolver.resolveForSourceLookupPath(entry, configuration);
				}				
			case IRuntimeClasspathEntry.CONTAINER:
				resolver = getContainerResolver(entry.getVariableName());
				if (resolver == null) {
					
				} else {
					return resolver.resolveForSourceLookupPath(entry, configuration);
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
		IJavaProject project = getJavaProject(config);
		if (project == null) {
			// cannot resolve without project context
			return new IRuntimeClasspathEntry[0];
		} else {
			IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
			IClasspathEntry[] cpes = container.getClasspathEntries();
			IRuntimeClasspathEntry[] resolved = new IRuntimeClasspathEntry[cpes.length];
			for (int i = 0; i < resolved.length; i++) {
				resolved[i] = newRuntimeClasspathEntry(cpes[i]);
			}
			return resolved;
		}
	}
		
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Returns the (unresolved) class path for the given (Java) launch configuration,
	 * computing the default path if required (i.e. is not explicitly set on the
	 * launch configuration).
	 * 
	 * @param configuration launch configuration
	 * @return runtime classpath entries that should be on the class path
	 * @exception CoreException if unable to compute the class path
	 */
	public static IRuntimeClasspathEntry[] computeRuntimeClasspath(ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		if (useDefault) {
			IJavaProject proj = getJavaProject(configuration);
			if (proj == null) {
				// no project - use JRE's libraries by default
				IVMInstall vm = computeVMInstall(configuration);
				LibraryLocation[] libs = vm.getLibraryLocations();
				if (libs == null) {
					return new IRuntimeClasspathEntry[0];
				} else {
					IRuntimeClasspathEntry[] rtes = new IRuntimeClasspathEntry[libs.length];
					for (int i = 0; i < libs.length; i++) {
						IRuntimeClasspathEntry r = newArchiveRuntimeClasspathEntry(libs[i].getSystemLibraryPath());
						r.setSourceAttachmentPath(libs[i].getSystemLibrarySourcePath());
						r.setSourceAttachmentRootPath(libs[i].getPackageRootPath());
					}
					return rtes;
				}				
			} else {
				return JavaRuntime.computeRuntimeClasspath(proj);						
			}
		} else {
			// recover persisted classpath
			return recoverRuntimePath(configuration, IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
		}
	}
	
	/**
	 * Return the <code>IJavaProject</code> referenced in the specified configuration or
	 * <code>null</code> if none.
	 *
	 * @exception CoreException if the referenced Java project does not exist
	 */
	public static IJavaProject getJavaProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if ((projectName == null) || (projectName.trim().length() < 1)) {
			return null;
		}			
		IJavaProject javaProject = getJavaModel().getJavaProject(projectName);		
		if ((javaProject == null) || !javaProject.exists()) {
			abort(LaunchingMessages.getString("JavaLaunchConfigurationUtils.Invalid_project_specified_2"), IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT, null); //$NON-NLS-1$
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
	 * Returns a collection of runtime classpath entries that are defined in the
	 * specified attribute of the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @param attribute attribute name containing the list of entries
	 * @return collection of runtime classpath entries that are defined in the
	 *  specified attribute of the given launch configuration
	 * @exception CoreException if unable to retrieve the list
	 */
	private static IRuntimeClasspathEntry[] recoverRuntimePath(ILaunchConfiguration configuration, String attribute) throws CoreException {
		List entries = (List)configuration.getAttribute(attribute, Collections.EMPTY_LIST);
		IRuntimeClasspathEntry[] rtes = new IRuntimeClasspathEntry[entries.size()];
		Iterator iter = entries.iterator();
		int i = 0;
		while (iter.hasNext()) {
			rtes[i] = newRuntimeClasspathEntry((String)iter.next());
			i++;
		}
		return rtes;		
	}
	
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Returns the VMInstall for the given (Java) launch configuration.
	 * The VM install is computed in the following way:
	 * <ol>
	 * <li>The VM install is explicitly specified on the launch configuration
	 * 	via the <code>ATTR_VM_INSTALL_TYPE</code> and <code>ATTR_VM_INSTALL_ID</code>
	 *  attributes.</li>
	 * <li>If no explicit VM install is specified, the VM install associated with
	 * 	the launch confiugration's project is used.<li>
	 * <li>If no project is specified, or the project does not specify a custom
	 * 	VM install, the workspace default VM install is used.</li>
	 * </ol>
	 * 
	 * @param configuration launch configuration
	 * @return vm install
	 * @exception CoreException if unable to compute a vm install
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
			IVMInstall[] vms = vt.getVMInstalls();
				// look for a name
			String name = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
			if (name == null) {
				// error - type specified without a specific install (could be an old config that specified a VM ID)
				// log the error, but choose the default VM.
				IStatus status = new Status(IStatus.WARNING, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_VM_INSTALL, MessageFormat.format(LaunchingMessages.getString("JavaRuntime.VM_not_fully_specified_in_launch_configuration_{0}_-_missing_VM_name._Reverting_to_default_VM._1"), new String[] {configuration.getName()}), null); //$NON-NLS-1$
				LaunchingPlugin.log(status);
				return getDefaultVMInstall();
			} else {
				for (int i = 0; i < vms.length; i++) {
					if (name.equals(vms[i].getName())) {
						return vms[i];
					}
				}
			// error - install not found
			abort(MessageFormat.format(LaunchingMessages.getString("JavaRuntime.Specified_VM_install_not_found__type_{0},_name_{1}_2"), new String[] {type, name}), null);					 //$NON-NLS-1$
			}
		}
		
		return getDefaultVMInstall();
	}
	/**
	 * <b>THIS METHOD IS YET EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Returns the VMInstall used to build the given Java project - either an
	 * explicitly set VM install, or the workspace default.
	 * 
	 * @param project Java proect
	 * @return vm install
	 * @exception CoreException if unable to compute a vm install
	 */
	public static IVMInstall computeVMInstall(IJavaProject project) throws CoreException {
		IVMInstall vm = getVMInstall(project);
		if (vm == null) {
			vm = getDefaultVMInstall();
		}
		return vm;
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
	 * @return list of classpath entries and runtime classpath entries for containers
	 * @exception CoreException if unable to expand the classpath
	 */
	protected static List expandProject(IClasspathEntry projectEntry) throws CoreException {
		// 1. Get the raw classpath
		// 2. Replace source folder entries with a project entry
		IPath projectPath = projectEntry.getPath();
		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(projectPath.lastSegment());
		if (res == null) {
			return null;
		}
		IJavaProject project = (IJavaProject)JavaCore.create(res);
		if (project == null) {
			return null;
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
		List expandedPath = new ArrayList(unexpandedPath.size());
		Iterator iter = unexpandedPath.iterator();
		while (iter.hasNext()) {
			IClasspathEntry entry = (IClasspathEntry)iter.next();
			if (entry == projectEntry) {
				expandedPath.add(entry);
			} else {
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					if (!expandedPath.contains(entry)) {
						List projectEntries = expandProject(entry);
						if (projectEntries != null) {
							Iterator entries = projectEntries.iterator();
							while (entries.hasNext()) {
								IClasspathEntry e = (IClasspathEntry)entries.next();
								if (!expandedPath.contains(e)) {
									expandedPath.add(e);
								}
							}
						}
					}
				} else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IClasspathContainer conatiner = JavaCore.getClasspathContainer(entry.getPath(), project);
					int property = -1;
					switch (conatiner.getKind()) {
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
					if (!expandedPath.contains(r)) {
						expandedPath.add(r);
					}
				} else {
					if (!expandedPath.contains(entry)) {
						expandedPath.add(entry);
					}
				}
			}
		}
		return expandedPath;
	}
		
	/**
	 * Computes the default classpath for a given <code>project</code> 
	 * from it's build classpath following this algorithm:
	 * <ul>
	 * <li>traverse the build class path:</li>
	 * <li>if it's the first source folder found, add the projects output location
	 * <li>if it's a project entry, recursively compute it's classpath and append the result to the classpath</li>
	 * <li>if it's a library entry, append the entry to the classpath.</li>
	 * </ul>
	 * @param	jproject	The project to compute the classpath for
	 * @return	The computed classpath. May be empty, but not null.
	 * @throws	CoreException	When accessing the underlying resources or when a project
	 					has no output folder.
	 */
	public static String[] computeDefaultRuntimeClassPath(IJavaProject jproject) throws CoreException {
		ArrayList visited= new ArrayList();
		ArrayList resultingPaths= new ArrayList();
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		collectClasspathEntries(root, jproject, visited, resultingPaths);
		return (String[]) resultingPaths.toArray(new String[resultingPaths.size()]);
	}	
	
	private static void collectClasspathEntries(IWorkspaceRoot root, IJavaProject jproject, List visited, List resultingPaths) throws CoreException {
		if (visited.contains(jproject)) {
			return;
		}
		visited.add(jproject);
	
		boolean sourceFolderFound= false;
		
		IClasspathEntry[] entries= jproject.getResolvedClasspath(true);
		for (int i= 0; i < entries.length; i++) {
			IClasspathEntry curr= entries[i];
			switch (curr.getEntryKind()) {
				case IClasspathEntry.CPE_LIBRARY:
					IResource library= root.findMember(curr.getPath());
					// can be external or in workspace
					String libraryLocation= (library != null) ? library.getLocation().toOSString() : curr.getPath().toOSString();
					if (!resultingPaths.contains(libraryLocation)) {
						resultingPaths.add(libraryLocation);
					}
					break;
				case IClasspathEntry.CPE_PROJECT:
					IProject reqProject= (IProject) root.findMember(curr.getPath().lastSegment());
					IJavaProject javaProject = JavaCore.create(reqProject);
					if (javaProject != null && javaProject.getProject().isOpen()) {
						collectClasspathEntries(root, javaProject, visited, resultingPaths);
					}
					break;
				case IClasspathEntry.CPE_SOURCE:
					if (!sourceFolderFound) {
						// add the output location for the first source folder found
						IPath outputLocation= jproject.getOutputLocation();
						IResource resource= root.findMember(outputLocation);
						if (resource != null) {
							resultingPaths.add(resource.getLocation().toOSString());
						}
						sourceFolderFound= true;
					}			
					break;
			}
		}		
	}
	
	/**
	 * Saves the VM configuration information to disk. This includes
	 * the following information:
	 * <ul>
	 * <li>The list of all defined IVMInstall instances.</li>
	 * <li>The default VM</li>
	 * <ul>
	 * This state will be read again upon first access to VM
	 * configuration information.
	 */
	public static void saveVMConfiguration() throws CoreException {
		IPath stateLocation= LaunchingPlugin.getDefault().getStateLocation();
		IPath stateFile= stateLocation.append("vmConfiguration.xml"); //$NON-NLS-1$
		File f= new File(stateFile.toOSString());
		try {
			OutputStream stream= new BufferedOutputStream(new FileOutputStream(f));
			Writer writer= new OutputStreamWriter(stream);
			writeVMs(writer);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IStatus.ERROR, LaunchingMessages.getString("JavaRuntime.ioExceptionOccurred"), e)); //$NON-NLS-1$
		}
		
	}
	
	private static void writeVMs(Writer writer) throws IOException {
		Document doc = new DocumentImpl();
		Element config = doc.createElement("vmSettings"); //$NON-NLS-1$
		if (fgDefaultVMId != null) {
			config.setAttribute("defaultVM", fgDefaultVMId); //$NON-NLS-1$
		}
		if (fgDefaultVMConnectorId != null) {
			config.setAttribute("defaultVMConnector", fgDefaultVMConnectorId); //$NON-NLS-1$
		}
		doc.appendChild(config);
		
		IVMInstallType[] vmTypes= getVMInstallTypes();

		for (int i = 0; i < vmTypes.length; ++i) {
			Element vmTypeElement = vmTypeAsElement(doc, vmTypes[i]);
			config.appendChild(vmTypeElement);
		}



		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				writer,
				format);
		serializer.asDOMSerializer().serialize(doc);
	}
	
	private static Element vmTypeAsElement(Document doc, IVMInstallType vmType) {
		Element element= doc.createElement("vmType"); //$NON-NLS-1$
		element.setAttribute("id", vmType.getId()); //$NON-NLS-1$
		IVMInstall[] vms= vmType.getVMInstalls();
		for (int i= 0; i < vms.length; i++) {
			Element vmElement= vmAsElement(doc, vms[i]);
			element.appendChild(vmElement);
		}
		return element;
	}
	
	private static Element vmAsElement(Document doc, IVMInstall vm) {
		Element element= doc.createElement("vm"); //$NON-NLS-1$
		element.setAttribute("id", vm.getId());	 //$NON-NLS-1$
		element.setAttribute("name", vm.getName()); //$NON-NLS-1$
		String installPath= ""; //$NON-NLS-1$
		File installLocation= vm.getInstallLocation();
		if (installLocation != null) {
			installPath= installLocation.getAbsolutePath();
		}
		element.setAttribute("path", installPath); //$NON-NLS-1$
		LibraryLocation[] libraryLocations= vm.getLibraryLocations();
		if (libraryLocations != null) {
			Element libLocationElement=  libraryLocationsAsElement(doc, libraryLocations);
			element.appendChild(libLocationElement);
		}
		return element;
	}
	
	private static Element libraryLocationsAsElement(Document doc, LibraryLocation[] locations) {
		Element root = doc.createElement("libraryLocations"); //$NON-NLS-1$
		for (int i = 0; i < locations.length; i++) {
			Element element= doc.createElement("libraryLocation"); //$NON-NLS-1$
			element.setAttribute("jreJar", locations[i].getSystemLibraryPath().toString()); //$NON-NLS-1$
			element.setAttribute("jreSrc", locations[i].getSystemLibrarySourcePath().toString()); //$NON-NLS-1$
			element.setAttribute("pkgRoot", locations[i].getPackageRootPath().toString()); //$NON-NLS-1$
			root.appendChild(element);
		}
		return root;
	}
	
	private static void initializeVMConfiguration() throws IOException {
		IPath stateLocation= LaunchingPlugin.getDefault().getStateLocation();
		IPath stateFile= stateLocation.append("vmConfiguration.xml"); //$NON-NLS-1$
		File f= new File(stateFile.toOSString());
		if (f.isFile()) {
			loadVMConfiguration(f);
		} else {
			detectVMConfiguration();
		}
	}
	
	private static void loadVMConfiguration(File f) throws IOException {
		InputStream stream= new BufferedInputStream(new FileInputStream(f));
		Reader reader= new InputStreamReader(stream);
		readVMs(reader);
	}
	
	private static void detectVMConfiguration() {
		IVMInstallType[] vmTypes= getVMInstallTypes();
		boolean defaultSet= false;
		for (int i= 0; i < vmTypes.length; i++) {
			File detectedLocation= vmTypes[i].detectInstallLocation();
			if (detectedLocation != null) {
				IVMInstall detected= vmTypes[i].createVMInstall(String.valueOf(i));
				detected.setName(vmTypes[i].getName()+LaunchingMessages.getString("JavaRuntime.detectedSuffix")); //$NON-NLS-1$
				detected.setInstallLocation(detectedLocation);
				if (detected != null && !defaultSet) {
					setDefaultVMInstall(detected);
					defaultSet= true;
				}
			}
		}
	}
	
	private static void readVMs(Reader reader) throws IOException {
		Element config= null;
		try {
			DocumentBuilder parser= DocumentBuilderFactory.newInstance().newDocumentBuilder();
			config = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(LaunchingMessages.getString("JavaRuntime.badFormat")); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			reader.close();
			throw new IOException(LaunchingMessages.getString("JavaRuntime.badFormat")); //$NON-NLS-1$
		} finally {
			reader.close();
		}
		if (!config.getNodeName().equalsIgnoreCase("vmSettings")) { //$NON-NLS-1$
			throw new IOException(LaunchingMessages.getString("JavaRuntime.badFormat")); //$NON-NLS-1$
		}
		fgDefaultVMId= config.getAttribute("defaultVM"); //$NON-NLS-1$
		fgDefaultVMConnectorId = config.getAttribute("defaultVMConnector"); //$NON-NLS-1$
		NodeList list = config.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element vmTypeElement = (Element) node;
				if (vmTypeElement.getNodeName().equalsIgnoreCase("vmType")) { //$NON-NLS-1$
					createFromVMType(vmTypeElement);
				}
			}
		}
	}
	
	private static void createFromVMType(Element vmTypeElement) {
		String id = vmTypeElement.getAttribute("id"); //$NON-NLS-1$
		IVMInstallType vmType= getVMInstallType(id);
		if (vmType != null) {
			NodeList list = vmTypeElement.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element vmElement = (Element) node;
					if (vmElement.getNodeName().equalsIgnoreCase("vm")) { //$NON-NLS-1$
						createVM(vmType, vmElement);
					}
				}
			}
		} else {
			LaunchingPlugin.log(LaunchingMessages.getString("JavaRuntime.VM_type_element_with_unknown_id_1")); //$NON-NLS-1$
		}
	}

	private static void createVM(IVMInstallType vmType, Element vmElement) {
		String id= vmElement.getAttribute("id"); //$NON-NLS-1$
		if (id != null) {
			String installPath= vmElement.getAttribute("path"); //$NON-NLS-1$
			if (installPath == null) {
				return;
			}
			File installLocation= new File(installPath);
			if (!installLocation.exists()) {
				return;
			}
			IVMInstall vm= vmType.createVMInstall(id);
			vm.setName(vmElement.getAttribute("name")); //$NON-NLS-1$
			vm.setInstallLocation(installLocation);
			
			NodeList list = vmElement.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element libraryLocationElement= (Element)node;
					if (libraryLocationElement.getNodeName().equals("libraryLocation")) { //$NON-NLS-1$
						LibraryLocation loc = getLibraryLocation(vm, libraryLocationElement);
						vm.setLibraryLocations(new LibraryLocation[]{loc});
						break;
					} else if (libraryLocationElement.getNodeName().equals("libraryLocations")) { //$NON-NLS-1$
						setLibraryLocations(vm, libraryLocationElement);
						break;
					}
				}
			}

		} else {
			LaunchingPlugin.log(LaunchingMessages.getString("JavaRuntime.VM_element_specified_with_no_id_attribute_2")); //$NON-NLS-1$
		}
	}
	
	private static LibraryLocation getLibraryLocation(IVMInstall vm, Element libLocationElement) {
		String jreJar= libLocationElement.getAttribute("jreJar"); //$NON-NLS-1$
		String jreSrc= libLocationElement.getAttribute("jreSrc"); //$NON-NLS-1$
		String pkgRoot= libLocationElement.getAttribute("pkgRoot"); //$NON-NLS-1$
		if (jreJar != null && jreSrc != null && pkgRoot != null) {
			return new LibraryLocation(new Path(jreJar), new Path(jreSrc), new Path(pkgRoot));
		} else {
			LaunchingPlugin.log(LaunchingMessages.getString("JavaRuntime.Library_location_element_incorrectly_specified_3")); //$NON-NLS-1$
		}
		return null;
	}
	
	private static void setLibraryLocations(IVMInstall vm, Element libLocationsElement) {
		NodeList list = libLocationsElement.getChildNodes();
		int length = list.getLength();
		List locations = new ArrayList(length);
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element libraryLocationElement= (Element)node;
				if (libraryLocationElement.getNodeName().equals("libraryLocation")) { //$NON-NLS-1$
					locations.add(getLibraryLocation(vm, libraryLocationElement));
				}
			}
		}	
		vm.setLibraryLocations((LibraryLocation[])locations.toArray(new LibraryLocation[locations.size()]));
	}
		
	/**
	 * Evaluates library locations for a IVMInstall. If no library locations are set on the install, a default
	 * location is evaluated and checked if it exists.
	 * @return Returns library locations with paths that exist or are empty
	 */
	public static LibraryLocation[] getLibraryLocations(IVMInstall defaultVM)  {
		IPath[] libraryPaths;
		IPath[] sourcePaths;
		IPath[] sourceRootPaths;
		LibraryLocation[] locations= defaultVM.getLibraryLocations();
		if (locations == null) {
			LibraryLocation[] dflts= defaultVM.getVMInstallType().getDefaultLibraryLocations(defaultVM.getInstallLocation());
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
	 * Returns the VM connetor defined with the specified identifier,
	 * or <code>null</code> if none.
	 * 
	 * @param id VM connector identifier
	 * @return VM connector or <code>null</code> if none
	 */
	public static IVMConnector getVMConnector(String id) {
		return LaunchingPlugin.getDefault().getVMConnector(id);
	}
	
	/**
	 * Returns all VM connector extensions.
	 *
	 * @return VM connectors
	 */
	public static IVMConnector[] getVMConnectors() {
		return LaunchingPlugin.getDefault().getVMConnectors();
	}	
	
	/**
	 * Returns the preference store for the launching plug-in.
	 * 
	 * @return the preference store for the launching plug-in
	 */
	public static Preferences getPreferences() {
		return LaunchingPlugin.getDefault().getPluginPreferences();
	}
	
	/**
	 * <b>EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Registers the given resolver for the specified variable.
	 * 
	 * @param resolver runtime classpathe entry resolver
	 * @param variableName variable name to register for
	 */
	public static void addVariableResolver(IRuntimeClasspathEntryResolver resolver, String variableName) {
		Map map = getVariableResolvers();
		map.put(variableName, resolver);
	}
	
	/**
	 * <b>EXPERIMENTAL AND SUBJECT TO CHANGE</b>
	 * 
	 * Registers the given resolver for the specified container.
	 * 
	 * @param resolver runtime classpathe entry resolver
	 * @param containerIdentifier identifier of the classpath container to register for
	 */
	public static void addContainerResolver(IRuntimeClasspathEntryResolver resolver, String containerIdentifier) {
		Map map = getContainerResolvers();
		map.put(containerIdentifier, resolver);
	}	
	
	/**
	 * Returns all registered variable resolvers.
	 */
	private static Map getVariableResolvers() {
		if (fVariableResolvers == null) {
			initializeResolvers();
		}
		return fVariableResolvers;
	}
	
	/**
	 * Returns all registered container resolvers.
	 */
	private static Map getContainerResolvers() {
		if (fContainerResolvers == null) {
			initializeResolvers();
		}
		return fContainerResolvers;
	}
	
	private static void initializeResolvers() {
		IExtensionPoint point = LaunchingPlugin.getDefault().getDescriptor().getExtensionPoint(EXTENSION_POINT_RUNTIME_CLASSPATH_ENTRY_RESOLVERS);
		IConfigurationElement[] extensions = point.getConfigurationElements();
		fVariableResolvers = new HashMap(extensions.length);
		fContainerResolvers = new HashMap(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			RuntimeClasspathEntryResolver res = new RuntimeClasspathEntryResolver(extensions[i]);
			String variable = res.getVariableName();
			String container = res.getContainerId();
			if (variable != null) {
				fVariableResolvers.put(variable, res);
			}
			if (container != null) {
				fContainerResolvers.put(container, res);
			}
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
}