/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.launching;

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
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.CompositeId;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
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
 * <p>
 * [Issue: This class should be declared final, and have a private constructor
 *  to block instantiation.
 * ]
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
	 * The class org.eclipse.debug.core.model.IProcess allows attaching
	 * String properties to processes.
	 * The intent of this property is to show the command line a process
	 * was launched with. Implementers of IVMRunners use this property
	 * key to attach the command line to the IProcesses they create.
	 */
	public final static String ATTR_CMDLINE= LaunchingPlugin.PLUGIN_ID + ".launcher.cmdLine"; //$NON-NLS-1$

	
	private static final String PROPERTY_VM= LaunchingPlugin.PLUGIN_ID + ".vm"; //$NON-NLS-1$

	private static IVMInstallType[] fgVMTypes= null;
	private static String fgDefaultVMId= null;
	
	/**
	 * Returns the list of registered VM types. VM types are registered via
	 * <code>"org.eclipse.jdt.launching.vmTypes"</code> extension point.
	 * Returns an empty list if there are no registered VM types.
	 * 
	 * @return the list of registered VM types
	 */
	public static IVMInstallType[] getVMInstallTypes() {
		if (fgVMTypes == null)
			initializeVMTypes();
		return fgVMTypes; 
	}
	
	private static synchronized void initializeVMTypes() {
		IExtensionPoint extensionPoint= Platform.getPluginRegistry().getExtensionPoint(LaunchingPlugin.PLUGIN_ID + ".vmInstallTypes"); //$NON-NLS-1$
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements(); 
		MultiStatus status= new MultiStatus(LaunchingPlugin.PLUGIN_ID, IStatus.OK, LaunchingMessages.getString("javaRuntime.exceptionOccurred"), null); //$NON-NLS-1$
		ArrayList vmTypes= new ArrayList(configs.length);

		for (int i= 0; i < configs.length; i++) {
			try {
				IVMInstallType vmType= (IVMInstallType)configs[i].createExecutableExtension("class"); //$NON-NLS-1$
				vmTypes.add(vmType);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}
		fgVMTypes= new IVMInstallType[vmTypes.size()];
		fgVMTypes= (IVMInstallType[])vmTypes.toArray(fgVMTypes);
		if (!status.isOK()) {
			LaunchingPlugin.log(status);
		}
		
		try {
			initializeVMConfiguration();
		} catch (IOException e) {
			LaunchingPlugin.log(e);
		}
	}

	/**
	 * Returns the VM assigned to run the given Java project.
	 * The project must exist.
	 * 
	 * @return the VM instance that is selected for the given Java project
	 * 		   Returns null if no VM was previously set.
	 * @throws CoreException	If reading the property from the underlying
	 * 							property failed or if the property value was
	 * 							corrupt.
	 */
	public static IVMInstall getVMInstall(IJavaProject project) throws CoreException {
		String idString= project.getProject().getPersistentProperty(new QualifiedName(LaunchingPlugin.PLUGIN_ID, PROPERTY_VM));
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
			if (vmTypes[i].getId().equals(id))
				return vmTypes[i];
		}
		return null;
	}
	/**
	 * Sets the VM to be used for running the given Java project.
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
		project.getProject().setPersistentProperty(new QualifiedName(LaunchingPlugin.PLUGIN_ID, PROPERTY_VM), idString);
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
			LaunchingPlugin.getPlugin().getLog().log(e.getStatus());
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
		initializeJREVariables(monitor);
	}	
	
	/**
	 * Return the default VM set with <code>setDefaultVM()</code>.
	 * @return	Returns the default VM. May return null when no default
	 * 			VM was set or when the default VM has been disposed.
	 */
	public static IVMInstall getDefaultVMInstall() {
		return getVMFromId(getDefaultVMId());
	}
	
	private static String getDefaultVMId() {
		if (fgVMTypes == null)
			initializeVMTypes();
		return fgDefaultVMId;
	}
	
	private static String getIdFromVM(IVMInstall vm) {
		if (vm == null)
			return null;
		IVMInstallType vmType= vm.getVMInstallType();
		String typeID= vmType.getId();
		CompositeId id= new CompositeId(new String[] { typeID, vm.getId() });
		return id.toString();
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
		if (visited.contains(jproject))
			return;
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
	
	// saving
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
		IPath stateLocation= LaunchingPlugin.getPlugin().getStateLocation();
		IPath stateFile= stateLocation.append("vmConfiguration.xml"); //$NON-NLS-1$
		File f= new File(stateFile.toOSString());
		try {
			OutputStream stream= new BufferedOutputStream(new FileOutputStream(f));
			Writer writer= new OutputStreamWriter(stream);
			writeVMs(writer);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.PLUGIN_ID, IStatus.ERROR, LaunchingMessages.getString("javaRuntime.ioExceptionOccurred"), e)); //$NON-NLS-1$
		}
		
	}
	
	private static void writeVMs(Writer writer) throws IOException {
		Document doc = new DocumentImpl();
		Element config = doc.createElement("vmSettings"); //$NON-NLS-1$
		if (fgDefaultVMId != null)
			config.setAttribute("defaultVM", fgDefaultVMId); //$NON-NLS-1$
		doc.appendChild(config);
		
		IVMInstallType[] vmTypes= getVMInstallTypes();

		for (int i = 0; i < vmTypes.length; ++i) {
			Element vmTypeElement =
				vmTypeAsElement(doc, vmTypes[i]);
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
		element.setAttribute("timeout", String.valueOf(vm.getDebuggerTimeout())); //$NON-NLS-1$
		String installPath= ""; //$NON-NLS-1$
		File installLocation= vm.getInstallLocation();
		if (installLocation != null)
			installPath= installLocation.getAbsolutePath();
		element.setAttribute("path", installPath); //$NON-NLS-1$
		LibraryLocation libraryLocation= vm.getLibraryLocation();
		if (libraryLocation != null) {
			Element libLocationElement=  libraryLocationAsElement(doc, libraryLocation);
			element.appendChild(libLocationElement);
		}
		return element;
	}
	
	private static Element libraryLocationAsElement(Document doc, LibraryLocation location) {
		Element element= doc.createElement("libraryLocation"); //$NON-NLS-1$
		element.setAttribute("jreJar", location.getSystemLibraryPath().toString()); //$NON-NLS-1$
		element.setAttribute("jreSrc", location.getSystemLibrarySourcePath().toString()); //$NON-NLS-1$
		element.setAttribute("pkgRoot", location.getPackageRootPath().toString()); //$NON-NLS-1$
		return element;
	}
	
	// loading
	
	private static void initializeVMConfiguration() throws IOException {
		IPath stateLocation= LaunchingPlugin.getPlugin().getStateLocation();
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
				detected.setName(vmTypes[i].getName()+LaunchingMessages.getString("javaRuntime.detectedSuffix")); //$NON-NLS-1$
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
			throw new IOException(LaunchingMessages.getString("javaRuntime.badFormat")); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			reader.close();
			throw new IOException(LaunchingMessages.getString("javaRuntime.badFormat")); //$NON-NLS-1$
		} finally {
			reader.close();
		}
		if (!config.getNodeName().equalsIgnoreCase("vmSettings")) { //$NON-NLS-1$
			throw new IOException(LaunchingMessages.getString("javaRuntime.badFormat")); //$NON-NLS-1$
		}
		fgDefaultVMId= config.getAttribute("defaultVM"); //$NON-NLS-1$
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
			//"log it".
		}
	}

	private static void createVM(IVMInstallType vmType, Element vmElement) {
		String id= vmElement.getAttribute("id"); //$NON-NLS-1$
		if (id != null) {
			IVMInstall vm= vmType.createVMInstall(id);
			vm.setName(vmElement.getAttribute("name")); //$NON-NLS-1$
			String installPath= vmElement.getAttribute("path"); //$NON-NLS-1$
			String timeoutString= vmElement.getAttribute("timeout"); //$NON-NLS-1$
			try {
				if (timeoutString != null)
					vm.setDebuggerTimeout(Integer.parseInt(timeoutString));
			} catch (NumberFormatException e) {
			}
			if (installPath == null)
				installPath= ""; //$NON-NLS-1$
			vm.setInstallLocation(new File(installPath));
			NodeList list = vmElement.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element libraryLocationElement= (Element)node;
					if (libraryLocationElement.getNodeName().equals("libraryLocation")) { //$NON-NLS-1$
						setLibraryLocation(vm, libraryLocationElement);
						break;
					}
				}
			}

		} else {
			//log it.
		}
	}
	
	private static void setLibraryLocation(IVMInstall vm, Element libLocationElement) {
		String jreJar= libLocationElement.getAttribute("jreJar"); //$NON-NLS-1$
		String jreSrc= libLocationElement.getAttribute("jreSrc"); //$NON-NLS-1$
		String pkgRoot= libLocationElement.getAttribute("pkgRoot"); //$NON-NLS-1$
		if (jreJar != null && jreSrc != null && pkgRoot != null) {
			vm.setLibraryLocation(new LibraryLocation(new Path(jreJar), new Path(jreSrc), new Path(pkgRoot)));
		} else {
			// log it
		}
	}
	
	public static synchronized void initializeJREVariables(IProgressMonitor monitor) throws CoreException {
		IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
		if (vmInstall != null) {
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			boolean wasAutobuild= setAutobuild(workspace, false);
			try {
				LibraryLocation desc= getLibraryLocation(vmInstall);
				IPath library= desc.getSystemLibraryPath();
				IPath source= desc.getSystemLibrarySourcePath();
				IPath pkgRoot= desc.getPackageRootPath();				

				setJREVariables(library, source, pkgRoot, monitor);
			} finally {
				setAutobuild(workspace, wasAutobuild);
			}
		}
	}
	
	private static boolean setAutobuild(IWorkspace ws, boolean newState) throws CoreException {
		IWorkspaceDescription wsDescription= ws.getDescription();
		boolean oldState= wsDescription.isAutoBuilding();
		if (oldState != newState) {
			wsDescription.setAutoBuilding(newState);
			ws.setDescription(wsDescription);
		}
		return oldState;
	}
	
	/**
	 * Evaluates a library location for a IVMInstall. If no library location is set on the install, a default
	 * location is evaluated and checked if it exists.
	 * @return Returns a library location with paths that exist or ar empty
	 */
	public static LibraryLocation getLibraryLocation(IVMInstall defaultVM)  {
		IPath libraryPath;
		IPath sourcePath;
		IPath sourceRootPath;
		LibraryLocation location= defaultVM.getLibraryLocation();
		if (location == null) {
			LibraryLocation dflt= defaultVM.getVMInstallType().getDefaultLibraryLocation(defaultVM.getInstallLocation());
			
			libraryPath= dflt.getSystemLibraryPath();
			if (!libraryPath.toFile().isFile()) {
				libraryPath= Path.EMPTY;
			}
			
			sourcePath= dflt.getSystemLibrarySourcePath();
			if (sourcePath.toFile().isFile()) {
				sourceRootPath= dflt.getPackageRootPath();
			} else {
				sourcePath= Path.EMPTY;
				sourceRootPath= Path.EMPTY;
			}
		} else {
			libraryPath= location.getSystemLibraryPath();
			sourcePath= location.getSystemLibrarySourcePath();
			sourceRootPath= location.getPackageRootPath();
		}
		return new LibraryLocation(libraryPath, sourcePath, sourceRootPath);
	}
	
	private static void setJREVariables(IPath library, IPath source, IPath root, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		
		monitor.beginTask(LaunchingMessages.getString("JavaRuntime.Setting_JRE_classpath_variables"), 3); //$NON-NLS-1$
		try {	
			IPath oldLibrary= JavaCore.getClasspathVariable(JRELIB_VARIABLE);
			IPath oldSource= JavaCore.getClasspathVariable(JRESRC_VARIABLE);
			IPath oldPkgRoot= JavaCore.getClasspathVariable(JRESRCROOT_VARIABLE);
	
			if (!library.equals(oldLibrary)) {
				JavaCore.setClasspathVariable(JRELIB_VARIABLE, library, new SubProgressMonitor(monitor, 1));
			}
			if (!source.equals(oldSource)) {
				JavaCore.setClasspathVariable(JRESRC_VARIABLE, source, new SubProgressMonitor(monitor, 1));
			}
			if (!root.equals(oldPkgRoot)) {
				JavaCore.setClasspathVariable(JRESRCROOT_VARIABLE, root, new SubProgressMonitor(monitor, 1));
			}
		} finally {
			monitor.done();
		}
	}
	
	public static IClasspathEntry getJREVariableEntry() {
		return JavaCore.newVariableEntry(
			new Path(JRELIB_VARIABLE),
			new Path(JRESRC_VARIABLE),
			new Path(JRESRCROOT_VARIABLE)
		);
	}

}