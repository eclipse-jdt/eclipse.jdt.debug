package org.eclipse.jdt.internal.launching;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.core.internal.events.ResourceChangeEvent;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LaunchingPlugin extends Plugin implements Preferences.IPropertyChangeListener, IVMInstallChangedListener, IResourceChangeListener {
	
	/**
	 * Identifier for 'vmConnectors' extension point
	 */
	public static final String ID_EXTENSION_POINT_VM_CONNECTORS = getUniqueIdentifier() + ".vmConnectors"; //$NON-NLS-1$
	
	private static LaunchingPlugin fgLaunchingPlugin;
	
	private HashMap fVMConnectors = null;

	private String fOldVMPrefString = EMPTY_STRING;
	
	private boolean fIgnoreVMDefPropertyChangeEvents = false;
	
	/**
	 * Status code indicating that a workspace runnable needs to be run.
	 */
	public static final int WORKSPACE_RUNNABLE_STATUS = 191;
		
	private static final String EMPTY_STRING = "";    //$NON-NLS-1$
	
	/**
	 * Marker used to indicate a default JRE could not be found/detected
	 */
	public static final String NO_DEFAULT_JRE_MARKER_TYPE = getUniqueIdentifier() + ".noDefaultJRE"; //$NON-NLS-1$
		
	/**
	 * Mapping of top-level VM installation directories to library info for that
	 * VM.
	 */
	private static Map fgLibraryInfoMap = null;	
	
	/**
	 * Whether changes in VM preferences are being batched. When being batched
	 * the plug-in can ignore processing and changes.
	 */
	private boolean fBatchingChanges = false;
	
	/**
	 * Stores VM changes resulting from a JRE preference change.
	 */
	class VMChanges implements IVMInstallChangedListener {
		
		private boolean fDefaultChanged = false;
		private IPath fOldDefaultConatinerId = null;
		
		private List fRemovedContainerIds = new ArrayList();
		private List fChangedContainerIds = new ArrayList();
		// old container ids to new
		private HashMap fRenamedContainerIds = new HashMap();
		
		private IPath getConatinerId(IVMInstall vm) {
			IPath path = new Path(JavaRuntime.JRE_CONTAINER);
			path = path.append(new Path(vm.getVMInstallType().getId()));
			path = path.append(new Path(vm.getName()));
			return path;			
		}
		
		/**
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
		 */
		public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
			fDefaultChanged = true;
			fOldDefaultConatinerId = getConatinerId(previous);
		}

		/**
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
		 */
		public void vmAdded(IVMInstall vm) {
		}

		/**
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
		 */
		public void vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent event) {
			String property = event.getProperty();
			IVMInstall vm = (IVMInstall)event.getSource();
			if (property.equals(IVMInstallChangedListener.PROPERTY_INSTALL_LOCATION)) {
				fChangedContainerIds.add(getConatinerId(vm));
			} else if (property.equals(IVMInstallChangedListener.PROPERTY_NAME)) {
				IPath newId = getConatinerId(vm);
				IPath oldId = new Path(JavaRuntime.JRE_CONTAINER);
				oldId = oldId.append(vm.getVMInstallType().getId());
				oldId = oldId.append((String)event.getOldValue());
				fRenamedContainerIds.put(oldId, newId);
			} else if (property.equals(IVMInstallChangedListener.PROPERTY_LIBRARY_LOCATIONS)) {
				// determine if it is more than a source attachment change
				LibraryLocation[] prevs = (LibraryLocation[])event.getOldValue();
				LibraryLocation[] currs = (LibraryLocation[])event.getNewValue();
				if (prevs.length == currs.length) {
					for (int i = 0; i < currs.length; i++) {
						if (!currs[i].getSystemLibraryPath().equals(prevs[i].getSystemLibraryPath())) {
							fChangedContainerIds.add(getConatinerId(vm));
							return;
						}
					}
				} else {
					fChangedContainerIds.add(getConatinerId(vm));
				}
			}
		}

		/**
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
		 */
		public void vmRemoved(IVMInstall vm) {
			fRemovedContainerIds.add(getConatinerId(vm));
		}
	
		/**
		 * Determine the projects that have been affected by the JRE changes and
		 * re-build them. Re-bind any classpath variables or containers that
		 * have changed.
		 */
		public void process() throws CoreException {
						
			IWorkspace ws = ResourcesPlugin.getWorkspace();
			boolean wasAutobuild= setAutobuild(ws, false);
			try {
				Set buildList = new HashSet();
				IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
				 
				if (fDefaultChanged) {
					// re-bin JRELIB if the default VM changed
					JavaClasspathVariablesInitializer initializer = new JavaClasspathVariablesInitializer();
					initializer.initialize(JavaRuntime.JRELIB_VARIABLE);
					initializer.initialize(JavaRuntime.JRESRC_VARIABLE);
					initializer.initialize(JavaRuntime.JRESRCROOT_VARIABLE);
				} else {
					// the old default is the same as the current default
					fOldDefaultConatinerId = getConatinerId(JavaRuntime.getDefaultVMInstall());				
				}
															
				// re-bind all container entries, noting which project need to be re-built
				for (int i = 0; i < projects.length; i++) {
					IJavaProject project = projects[i];
					IClasspathEntry[] entries = project.getRawClasspath();
					for (int j = 0; j < entries.length; j++) {
						IClasspathEntry entry = entries[j];
						switch (entry.getEntryKind()) {
							case IClasspathEntry.CPE_CONTAINER:
								IPath reference = entry.getPath();
								IPath newBinding = reference;
								boolean defRef = false;
								String firstSegment = reference.segment(0);
								if (JavaRuntime.JRE_CONTAINER.equals(firstSegment)) {
									if (reference.segmentCount() == 1) {
										// resolve to explicit reference
										defRef = true;
										reference = fOldDefaultConatinerId;
										if (fDefaultChanged) {
											buildList.add(project);
										}
									}
									if (requiresRebuild(reference)) {
										buildList.add(project);
									}
									IPath renamed = (IPath)fRenamedContainerIds.get(reference);
									if (renamed != null) {
										if (!defRef) {
											// The JRE was re-named. This changes the identifier of
											// the container entry, so we must re-build.
											buildList.add(project);
										}
									}
									JREContainerInitializer initializer = new JREContainerInitializer();
									initializer.initialize(newBinding, project);
								}
								break;
							case IClasspathEntry.CPE_VARIABLE:
								reference = entry.getPath();
								if (JavaRuntime.JRELIB_VARIABLE.equals(reference.segment(0))) {
									reference = fOldDefaultConatinerId;
									if (fDefaultChanged) {
										buildList.add(project);
									}
									if (requiresRebuild(reference)) {
										buildList.add(project);
									}
								}
								break;								
							default:
								break;
						}
					}
				}

				buildProjects(buildList);
				
			} finally {
				setAutobuild(ws, wasAutobuild);
			}
		}
		
		private boolean requiresRebuild(IPath containerId) {
			return  fChangedContainerIds.contains(containerId) || fRemovedContainerIds.contains(containerId);
		}
		
		private boolean setAutobuild(IWorkspace ws, boolean newState) throws CoreException {
			IWorkspaceDescription wsDescription= ws.getDescription();
			boolean oldState= wsDescription.isAutoBuilding();
			if (oldState != newState) {
				wsDescription.setAutoBuilding(newState);
				ws.setDescription(wsDescription);
			}
			return oldState;
		}			
	}
	
	
	
	public LaunchingPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgLaunchingPlugin= this;
	}
	
	/**
	 * Returns the library info that corresponds to the specified JRE install
	 * path, or <code>null</code> if none.
	 * 
	 * @return the library info that corresponds to the specified JRE install
	 * path, or <code>null</code> if none
	 */
	public static LibraryInfo getLibraryInfo(String javaInstallPath) {
		if (fgLibraryInfoMap == null) {
			restoreLibraryInfo(); 
		}
		return (LibraryInfo) fgLibraryInfoMap.get(javaInstallPath);
	}
	
	/**
	 * Sets the library info that corresponds to the specified JRE install
	 * path.
	 * 
	 * @param javaInstallPath home location for a JRE
	 * @param info the libary information, or <code>null</code> to remove
	 */
	public static void setLibraryInfo(String javaInstallPath, LibraryInfo info) {
		if (fgLibraryInfoMap == null) {
			restoreLibraryInfo(); 
		}		
		if (info == null) {
			fgLibraryInfoMap.remove(javaInstallPath);
		} else {
			fgLibraryInfoMap.put(javaInstallPath, info);
		}
		saveLibraryInfo();
	}	
		
	/**
	 * Return a <code>java.io.File</code> object that corresponds to the specified
	 * <code>IPath</code> in the plugin directory.	 */
	public static File getFileInPlugin(IPath path) {
		try {
			URL installURL =
				new URL(getDefault().getDescriptor().getInstallURL(), path.toString());
			URL localURL = Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException ioe) {
			return null;
		}
	}
		
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.jdt.launching"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	public static LaunchingPlugin getDefault() {
		return fgLaunchingPlugin;
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void log(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}	
		
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}	
	
	/**
	 * Clears zip file cache.
	 * Shutdown the launch config helper.
	 * 
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		ArchiveSourceLocation.closeArchives();
		getPluginPreferences().removePropertyChangeListener(this);
		JavaRuntime.removeVMInstallChangedListener(this);
		JavaRuntime.saveVMConfiguration();
		savePluginPreferences();
		super.shutdown();
	}
		
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		
		// Exclude launch configurations from being copied to the output directory
		String launchFilter = "*." + ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION; //$NON-NLS-1$
		Hashtable optionsMap = JavaCore.getOptions();
		String filters= (String)optionsMap.get("org.eclipse.jdt.core.builder.resourceCopyExclusionFilter"); //$NON-NLS-1$
		boolean modified = false;
		if (filters == null || filters.length() == 0) {
			filters= launchFilter;
			modified = true;
		} else if (filters.indexOf(launchFilter) == -1) {
			filters= filters + ',' + launchFilter; //$NON-NLS-1$
			modified = true;
		}

		if (modified) {
			optionsMap.put("org.eclipse.jdt.core.builder.resourceCopyExclusionFilter", filters);  //$NON-NLS-1$
			JavaCore.setOptions(optionsMap);
		}		

		// set default preference values
		getPluginPreferences().setDefault(JavaRuntime.PREF_CONNECT_TIMEOUT, JavaRuntime.DEF_CONNECT_TIMEOUT);
		getPluginPreferences().addPropertyChangeListener(this);

		JavaRuntime.addVMInstallChangedListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, ResourceChangeEvent.PRE_DELETE | ResourceChangeEvent.PRE_CLOSE);
	}
	
	/**
	 * Returns the VM connector with the specified id, or <code>null</code>
	 * if none. 
	 * 
	 * @param id connector identifier
	 * @return VM connector
	 */
	public IVMConnector getVMConnector(String id) {
		if (fVMConnectors == null) {
			initializeVMConnectors();
		}
		return (IVMConnector)fVMConnectors.get(id); 
	}
	
	/**
	 * Returns all VM connector extensions.
	 *
	 * @return VM connectors
	 */
	public IVMConnector[] getVMConnectors() {
		if (fVMConnectors == null) {
			initializeVMConnectors();
		}
		return (IVMConnector[])fVMConnectors.values().toArray(new IVMConnector[fVMConnectors.size()]); 
	}	
	
	/**
	 * Loads VM connector extensions
	 */
	private void initializeVMConnectors() {
		IExtensionPoint extensionPoint= Platform.getPluginRegistry().getExtensionPoint(ID_EXTENSION_POINT_VM_CONNECTORS);
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements(); 
		MultiStatus status= new MultiStatus(getUniqueIdentifier(), IStatus.OK, LaunchingMessages.getString("LaunchingPlugin.Exception_occurred_reading_vmConnectors_extensions_1"), null); //$NON-NLS-1$
		fVMConnectors = new HashMap(configs.length);
		for (int i= 0; i < configs.length; i++) {
			try {
				IVMConnector vmConnector= (IVMConnector)configs[i].createExecutableExtension("class"); //$NON-NLS-1$
				fVMConnectors.put(vmConnector.getIdentifier(), vmConnector);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}
		if (!status.isOK()) {
			LaunchingPlugin.log(status);
		}			
	}
	
	/**
	 * Save preferences whenever the connect timeout changes.
	 * Process changes to the list of installed JREs.
	 * 
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(JavaRuntime.PREF_CONNECT_TIMEOUT)) {
			savePluginPreferences();
		} else if (property.equals(JavaRuntime.PREF_VM_XML)) {
			if (!isIgnoreVMDefPropertyChangeEvents()) {
				processVMPrefsChanged((String)event.getOldValue(), (String)event.getNewValue());
				// remove error marker if there is now a default VM
				if (JavaRuntime.getDefaultVMInstall() != null) {
					IMarker[] markers;
					try {
						markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(NO_DEFAULT_JRE_MARKER_TYPE,	false,	IResource.DEPTH_ZERO);
						if (markers.length > 0) {
							for (int i = 0; i < markers.length; i++) {
								IMarker marker = markers[i];
								marker.delete();
							}
						}
					} catch (CoreException e) {
					}					
				}
			}
		}
	}

	/**
	 * Check for differences between the old & new sets of installed JREs.  
	 * Differences may include additions, deletions and changes.  Take
	 * appropriate action for each type of difference.
	 * 
	 * When importing preferences, TWO propertyChange events are fired.  The first
	 * has an old value but an empty new value.  The second has a new value, but an empty
	 * old value.  Normal user changes to the preferences result in a single propertyChange
	 * event, with both old and new values populated.  This method handles both types
	 * of notification.
	 */
	protected void processVMPrefsChanged(String oldValue, String newValue) {
		
		// batch changes
		fBatchingChanges = true;
		VMChanges vmChanges = null;
		try {

			String oldPrefString;
			String newPrefString;
			
			// If empty new value, save the old value and wait for 2nd propertyChange notification
			if (newValue == null || newValue.equals(EMPTY_STRING)) {        
				fOldVMPrefString = oldValue;
				return;
			}					
			// An empty old value signals the second notification in the import preferences
			// sequence.  Now that we have both old & new prefs, we can parse and compare them.
			else if (oldValue == null || oldValue.equals(EMPTY_STRING)) {    	
				oldPrefString = fOldVMPrefString;
				newPrefString = newValue;
			} 
			// If both old & new values are present, this is a normal user change
			else {
				oldPrefString = oldValue;
				newPrefString = newValue;
			}
			
			vmChanges = new VMChanges();
			JavaRuntime.addVMInstallChangedListener(vmChanges);
			
			// Generate the previous VMs
			VMDefinitionsContainer oldResults = getVMDefinitions(oldPrefString);
			
			// Generate the current
			VMDefinitionsContainer newResults = getVMDefinitions(newPrefString);
			
			// Determine the deteled VMs
			List deleted = oldResults.getVMList();
			List current = newResults.getVMList();
			deleted.removeAll(current);
			
			// Dispose deleted VMs.  The 'disposeVMInstall' method fires notification of the 
			// deletion.
			Iterator deletedIterator = deleted.iterator();
			while (deletedIterator.hasNext()) {
				VMStandin deletedVMStandin = (VMStandin) deletedIterator.next();
				deletedVMStandin.getVMInstallType().disposeVMInstall(deletedVMStandin.getId());			
			}			
			
			// Fire change notification for added and changed VMs. The 'convertToRealVM'
			// fires the appropriate notification.
			Iterator iter = current.iterator();
			while (iter.hasNext()) {
				VMStandin standin = (VMStandin)iter.next();
				standin.convertToRealVM();
			}
			
			// set the new default VM install. This will fire a 'defaultVMChanged',
			// if it in fact changed
			String newDefaultId = newResults.getDefaultVMInstallCompositeID();
			if (newDefaultId != null) {
				IVMInstall newDefaultVM = JavaRuntime.getVMFromCompositeId(newDefaultId);
				if (newDefaultVM != null) {
					try {
						JavaRuntime.setDefaultVMInstall(newDefaultVM, null, false);
					} catch (CoreException ce) {
						log(ce);
					}			
				}
			}
			
		} finally {
			// stop batch changes
			fBatchingChanges = false;	
			if (vmChanges != null) {
				JavaRuntime.removeVMInstallChangedListener(vmChanges);
				try {
					vmChanges.process();
				} catch (CoreException e) {
					log(e);
				}	
			}
		}

	}
	
	/**
	 * Parse the given xml into a VM definitions container, returning an empty
	 * container if an exception occurrs.
	 * 
	 * @param xml
	 * @return VMDefinitionsContainer
	 */
	private VMDefinitionsContainer getVMDefinitions(String xml) {
		byte[] bytes = xml.getBytes();
		if (bytes.length > 0) {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			try {
				return VMDefinitionsContainer.parseXMLIntoContainer(stream);
			} catch (IOException e) {
				LaunchingPlugin.log(e);
			}
		}
		return new VMDefinitionsContainer(); 
	}
			
	/**
	 * Build the Java projects in the specified Set.  Because we're in a non-UI plugin,
	 * we can't directly put up a progress monitor.  Finesse this by using a status 
	 * handler to do the build and put up a progress monitor if the status handler is
	 * available (if UI is loaded), or just use a workspace runnable to do the build otherwise.	 */
	private void buildProjects(final Set projects) throws CoreException {
		
		// Workspace runnable that builds the specified projects
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(EMPTY_STRING, projects.size() * 100);
				Iterator iter = projects.iterator();
				while (iter.hasNext()) {
					IJavaProject jp = (IJavaProject)iter.next();
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
					IProject pro = jp.getProject();
					pro.build(IncrementalProjectBuilder.FULL_BUILD, subMonitor);
					subMonitor.done();
				}
				monitor.done();				
			}
		};
		
		// Try to retrieve a status handler.  If found, have it run the build, otherwise
		// just do it with a null progress monitor
//		IStatus status = new Status(IStatus.INFO, getUniqueIdentifier(), WORKSPACE_RUNNABLE_STATUS, LaunchingMessages.getString("LaunchingPlugin.Build_in_progress_1"), null); //$NON-NLS-1$
//		IStatusHandler handler= DebugPlugin.getDefault().getStatusHandler(status);
//		if (handler != null) {			
//			try {
//				handler.handleStatus(status, runnable);
//			} catch (CoreException ce) {
//				log(ce);
//			}
//		} else {
			IProgressMonitor monitor = JavaRuntime.getProgressMonitor();
			if (monitor == null) {
				monitor = new NullProgressMonitor(); 
			}
			ResourcesPlugin.getWorkspace().run(runnable, monitor);
//		}
	}	
			
	/**
	 * @see IVMInstallChangedListener#defaultVMInstallChanged(IVMInstall, IVMInstall)
	 */
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		if (!fBatchingChanges) {
			try {
				VMChanges changes = new VMChanges();
				changes.defaultVMInstallChanged(previous, current);
				changes.process();
			} catch (CoreException e) {
				log(e);
			}
		}
	}

	/**
	 * @see IVMInstallChangedListener#vmAdded(IVMInstall)
	 */
	public void vmAdded(IVMInstall vm) {
	}

	/**
	 * @see IVMInstallChangedListener#vmChanged(PropertyChangeEvent)
	 */
	public void vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent event) {
		if (!fBatchingChanges) {
			try {
				VMChanges changes = new VMChanges();
				changes.vmChanged(event);
				changes.process();
			} catch (CoreException e) {
				log(e);
			}
		}		
	}

	/**
	 * @see IVMInstallChangedListener#vmRemoved(IVMInstall)
	 */
	public void vmRemoved(IVMInstall vm) {
		if (!fBatchingChanges) {
			try {
				VMChanges changes = new VMChanges();
				changes.vmRemoved(vm);
				changes.process();
			} catch (CoreException e) {
				log(e);
			}
		}
	}

	/**
	 * Clear the archive cache when a project is about to be deleted.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		ArchiveSourceLocation.closeArchives();
	}

	public void setIgnoreVMDefPropertyChangeEvents(boolean ignore) {
		fIgnoreVMDefPropertyChangeEvents = ignore;
	}

	public boolean isIgnoreVMDefPropertyChangeEvents() {
		return fIgnoreVMDefPropertyChangeEvents;
	}

	/**
	 * Convenience method to get the java model.
	 */
	private static IJavaModel getJavaModel() {
		return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Return the VM definitions contained in this object as a String of XML.  The String
	 * is suitable for storing in the workbench preferences.
	 * <p>
	 * The resulting XML is compatible with the static method <code>parseXMLIntoContainer</code>.
	 * </p>
	 * @return String the results of flattening this object into XML
	 * @throws IOException if this method fails. Reasons include:<ul>
	 * <li>serialization of the XML document failed</li>
	 * </ul>
	 */
	private static String getLibraryInfoAsXML() throws IOException{
		
		// Create the Document and the top-level node
		Document doc = new DocumentImpl();
		Element config = doc.createElement("libraryInfos");    //$NON-NLS-1$
		doc.appendChild(config);
						
		// Create a node for each info in the table
		Iterator locations = fgLibraryInfoMap.keySet().iterator();
		while (locations.hasNext()) {
			String home = (String)locations.next();
			LibraryInfo info = (LibraryInfo) fgLibraryInfoMap.get(home);
			Element locationElemnet = infoAsElement(doc, info);
			locationElemnet.setAttribute("home", home); //$NON-NLS-1$
			config.appendChild(locationElemnet);
		}
		
		// Serialize the Document and return the resulting String
		return JavaLaunchConfigurationUtils.serializeDocument(doc);
	}	
	
	/**
	 * Creates an XML element for the given info.
	 * 
	 * @param doc
	 * @param info
	 * @return Element
	 */
	private static Element infoAsElement(Document doc, LibraryInfo info) {
		Element libraryElement = doc.createElement("libraryInfo"); //$NON-NLS-1$
		libraryElement.setAttribute("version", info.getVersion()); //$NON-NLS-1$
		appendPathElements(doc, "bootpath", libraryElement, info.getBootpath()); //$NON-NLS-1$
		appendPathElements(doc, "extensionDirs", libraryElement, info.getExtensionDirs()); //$NON-NLS-1$
		appendPathElements(doc, "endorsedDirs", libraryElement, info.getEndorsedDirs()); //$NON-NLS-1$
		return libraryElement;
	}
	
	/**
	 * Appends path elements to the given library element, rooted by an
	 * element of the given type.
	 * 
	 * @param doc
	 * @param elementType
	 * @param libraryElement
	 * @param paths
	 */
	private static void appendPathElements(Document doc, String elementType, Element libraryElement, String[] paths) {
		if (paths.length > 0) {
			Element child = doc.createElement(elementType);
			libraryElement.appendChild(child);
			for (int i = 0; i < paths.length; i++) {
				String path = paths[i];
				Element entry = doc.createElement("entry"); //$NON-NLS-1$
				child.appendChild(entry);
				entry.setAttribute("path", path); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Saves the library info in a local workspace state location 
	 */
	private static void saveLibraryInfo() {
		try {
			String xml = getLibraryInfoAsXML();
			IPath libPath = getDefault().getStateLocation();
			libPath = libPath.append("libraryInfos.xml"); //$NON-NLS-1$
			File file = libPath.toFile();
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
			stream.close();
		} catch (IOException e) {
			log(e);
		}	
	}
	
	/**
	 * Restores library information for VMs
	 */
	private static void restoreLibraryInfo() {
		fgLibraryInfoMap = new HashMap(10);
		IPath libPath = getDefault().getStateLocation();
		libPath = libPath.append("libraryInfos.xml"); //$NON-NLS-1$
		File file = libPath.toFile();
		if (file.exists()) {
			try {
				InputStream stream = new FileInputStream(file);
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Element root = parser.parse(new InputSource(stream)).getDocumentElement();
				if(!root.getNodeName().equals("libraryInfos")) { //$NON-NLS-1$
					return;
				}
				
				NodeList list = root.getChildNodes();
				int length = list.getLength();
				for (int i = 0; i < length; ++i) {
					Node node = list.item(i);
					short type = node.getNodeType();
					if (type == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						String nodeName = element.getNodeName();
						if (nodeName.equalsIgnoreCase("libraryInfo")) { //$NON-NLS-1$
							String version = element.getAttribute("version"); //$NON-NLS-1$
							String location = element.getAttribute("home"); //$NON-NLS-1$
							String[] bootpath = getPathsFromXML(element, "bootpath"); //$NON-NLS-1$
							String[] extDirs = getPathsFromXML(element, "extensionDirs"); //$NON-NLS-1$
							String[] endDirs = getPathsFromXML(element, "endorsedDirs"); //$NON-NLS-1$
							if (location != null) {
								LibraryInfo info = new LibraryInfo(version, bootpath, extDirs, endDirs);
								fgLibraryInfoMap.put(location, info);										
							}
						}
					}
				}				
			} catch (IOException e) {
				log(e);
			} catch (ParserConfigurationException e) {
				log(e);
			} catch (SAXException e) {
				log(e);
			}
		}
	}
	
	private static String[] getPathsFromXML(Element lib, String pathType) {
		List paths = new ArrayList();
		NodeList list = lib.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String nodeName = element.getNodeName();
				if (nodeName.equalsIgnoreCase(pathType)) {
					NodeList entries = element.getChildNodes();
					int numEntries = entries.getLength();
					for (int j = 0; j < numEntries; j++) {
						Node n = entries.item(j);
						short t = n.getNodeType();
						if (t == Node.ELEMENT_NODE) {
							Element entryElement = (Element)n;
							String name = entryElement.getNodeName();
							if (name.equals("entry")) { //$NON-NLS-1$
								String path = entryElement.getAttribute("path"); //$NON-NLS-1$
								if (path != null && path.length() > 0) {
									paths.add(path);
								}
							}
						}
					}
				}
			}
		}			
		return (String[])paths.toArray(new String[paths.size()]);		
	}
	
}

 