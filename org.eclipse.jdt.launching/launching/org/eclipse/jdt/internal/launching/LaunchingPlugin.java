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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
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
		
		// Obtain lists of VMStandins corresponding to the VMs specified in the 
		// old preference value
		VMDefinitionsContainer oldResults = null;
		List oldList = null;
		byte[] oldByteArray = oldPrefString.getBytes();
		if (oldByteArray.length > 0) {
			ByteArrayInputStream oldStream = new ByteArrayInputStream(oldByteArray);
			try {
				oldResults = VMDefinitionsContainer.parseXMLIntoContainer(oldStream);
			} catch (IOException e) {
				LaunchingPlugin.log(e);
			}
			if (oldResults != null) {
				oldList = oldResults.getVMList();
			} else {
				oldList = new ArrayList(1);
			}
		} else {
			oldList = new ArrayList(1);
		}
		
		// Obtain lists of VMStandins corresponding to the VMs specified in the 
		// new preference value
		VMDefinitionsContainer newResults = null;
		List newList = null;
		byte[] newByteArray = newPrefString.getBytes();
		if (newByteArray.length > 0) {
			ByteArrayInputStream newStream = new ByteArrayInputStream(newPrefString.getBytes());
			try {
				newResults = VMDefinitionsContainer.parseXMLIntoContainer(newStream);
			} catch (IOException e) {
				LaunchingPlugin.log(e);
			}
			if (newResults != null) {
				newList = newResults.getVMList();
			} else {
				newList = new ArrayList(1);
			}
		} else {
			newList = new ArrayList(1);
		}
		
		// Determine the adds, deletes and changes for the old and new lists
		List adds = new ArrayList(newList.size());
		List deletes = new ArrayList(oldList.size());
		List changes = new ArrayList(oldList.size());
		determineVMListDifferences(oldList, newList, adds, deletes, changes);
		
		// Take action based on the added, changed & deleted VMs and update a set
		// of affected projects
		processVMAddsChangesDeletes(adds, changes, deletes, oldResults, newResults);
	}
	
	/**
	 * Added VMs need to have their underlying VM objects created.  Deleted VMs need to be
	 * disposed.  Projects that referenced deleted & changed VMs as well as projects that
	 * referenced a previous default VM need to be rebuilt.
	 */	private void processVMAddsChangesDeletes(List adds, List changes, List deletes, 
											  VMDefinitionsContainer oldResults, 
											  VMDefinitionsContainer newResults) {
		
		// The data structure that tracks projects that will need to be rebuilt as a
		// result of deletions, changes or a change in default VM
		Set affectedProjects = new HashSet(changes.size() + deletes.size());
		
		// In order to track affected projects, we need a mapping of VMs to the projects
		// that reference them.  Create this mapping only when we need it.
		Map vmsToProjectsMap = null;
		if (deletes.size() > 0) {
			vmsToProjectsMap = getCompositeVMIdsToProjectsMap();
		}
		
		// For all deleted VMs, retrieve the underlying 'real' VM (which we know must exist),
		// update the set of affected projects with all projects that referenced the VM, and
		// dispose of the VM.  The 'disposeVMInstall' method fires notification of the 
		// deletion.
		Iterator deletedIterator = deletes.iterator();
		while (deletedIterator.hasNext()) {
			VMStandin deletedVMStandin = (VMStandin) deletedIterator.next();
			String deletedVMStandinId = deletedVMStandin.getId();
			IVMInstall orginal = deletedVMStandin.getVMInstallType().findVMInstall(deletedVMStandinId);
			List list = (List)vmsToProjectsMap.get(orginal);
			if (list != null) {
				affectedProjects.addAll(list);
			}
			deletedVMStandin.getVMInstallType().disposeVMInstall(deletedVMStandin.getId());			
		}
		
		// For all added VMs, create the corresponding 'real' VM, and fire a VM added 
		// notification
		Iterator addedIterator = adds.iterator();
		while (addedIterator.hasNext()) {
			VMStandin addedVMStandin = (VMStandin) addedIterator.next();
			IVMInstall realAddedVM = addedVMStandin.convertToRealVM();
			JavaRuntime.fireVMAdded(realAddedVM);
		}
				
		// Get the default VM Ids
		boolean oldDefaultVMChanged = false;
		String oldDefaultVMCompositeId = EMPTY_STRING;
		String newDefaultVMCompositeId = EMPTY_STRING;
		if (oldResults != null) {
			oldDefaultVMCompositeId = oldResults.getDefaultVMInstallCompositeID();
		}
		if (newResults != null) {
			newDefaultVMCompositeId = newResults.getDefaultVMInstallCompositeID();
		}		

		// For all changed VMs, commit the changes.  The 'convertToRealVM' method 
		// fires notification of the VM change  
		Iterator changedIterator = changes.iterator();
		while (changedIterator.hasNext()) {
			VMStandin changedVMStandin = (VMStandin) changedIterator.next();
			IVMInstall realChangedVM = changedVMStandin.convertToRealVM();
			
			// Determine if the default VM changed
			String changedVMCompositeId = JavaRuntime.getCompositeIdFromVM(realChangedVM);
			if (changedVMCompositeId.equals(oldDefaultVMCompositeId)) {
				oldDefaultVMChanged = true;
			}
		}
		
		// If a different VM is now the default, or if the old default VM was changed
		// in some way, add all referencing projects to the set of projects to rebuild
		if (!oldDefaultVMCompositeId.equals(newDefaultVMCompositeId) || oldDefaultVMChanged) {
			
			// If there was no previous default, then ALL projects must be rebuilt
			if (oldDefaultVMCompositeId == EMPTY_STRING) {
				try {
					IJavaProject[] proj = getJavaModel().getJavaProjects();
					for (int i = 0; i < proj.length; i++) {
						affectedProjects.add(proj[i]);
					}
				} catch (JavaModelException jme) {
					LaunchingPlugin.log(jme);
				}
			} else {
				if (vmsToProjectsMap == null) {
					vmsToProjectsMap = getCompositeVMIdsToProjectsMap();
				}					
				List list = (List)vmsToProjectsMap.get(oldDefaultVMCompositeId);
				if (list != null) {
					affectedProjects.addAll(list);
				}		 	
			}
			IVMInstall newDefaultVM = JavaRuntime.getVMFromCompositeId(newDefaultVMCompositeId);
			try {
				JavaRuntime.setDefaultVMInstall(newDefaultVM, null, false);
			} catch (CoreException ce) {
				log(ce);
			}
		}

		// For all projects that were affected, build them (if autobuild is turned on)
		if (affectedProjects.size() > 0) {
			if (ResourcesPlugin.getWorkspace().isAutoBuilding()) {
				buildProjects(affectedProjects);
			}
		}
	}
	
	/**
	 * Compare the lists of VMStandins in the first two List arguments and put all
	 * elements present in the 2nd, but not in the 1st into 'added', put all elements
	 * present in the 1st but not in the 2nd into 'deleted' and put all elements present
	 * in both but not identical into 'changed'.
	 */
	private void determineVMListDifferences(List first, List second, List added, List deleted, List changed) {		
		// Scan the first list for deletions & changes
		Iterator firstIterator = first.iterator();
		while (firstIterator.hasNext()) {
			VMStandin firstElement = (VMStandin) firstIterator.next();
			
			// If a VM is present in both lists but different, add it to the changed list
			int secondIndex = second.indexOf(firstElement);
			if (secondIndex > -1) {
				VMStandin secondElement = (VMStandin) second.get(secondIndex);
				if (firstElement.different(secondElement)) {
					changed.add(secondElement);
				}				
			// If a VM is only present in first list, this is a deletion
			} else {
				deleted.add(firstElement);
			}
		}
		
		// Scan the second list for additions
		Iterator secondIterator = second.iterator();
		while (secondIterator.hasNext()) {
			VMStandin secondElement = (VMStandin) secondIterator.next();
			
			// If a VM is only present in second list, this is an addition
			int firstIndex = first.indexOf(secondElement);
			if (firstIndex == -1) {
				added.add(secondElement);
			}
		}
	}	

	/**
	 * Returns a map of composite VM install Ids to projects that reference those VMs.
	 */
	private static Map getCompositeVMIdsToProjectsMap() {
		HashMap map = new HashMap();
		try {
			IJavaProject[] projects = getJavaModel().getJavaProjects();
			for (int i = 0; i < projects.length; i++) {
				IJavaProject jp = projects[i];
				if (jp.getProject().isOpen()) {
					IVMInstall vm = JavaRuntime.getVMInstall(jp);
					if (vm != null) {
						String compositeVMId = JavaRuntime.getCompositeIdFromVM(vm);
						List list = (List)map.get(compositeVMId);
						if (list == null) {
							list = new ArrayList(2);
							map.put(compositeVMId, list);
						}
						list.add(jp);
					}
				}
			}
		} catch (CoreException ce) {
			LaunchingPlugin.log(ce);
		}
		
		return map;
	}
	
	/**
	 * Build the Java projects in the specified Set.  Because we're in a non-UI plugin,
	 * we can't directly put up a progress monitor.  Finesse this by using a status 
	 * handler to do the build and put up a progress monitor if the status handler is
	 * available (if UI is loaded), or just use a workspace runnable to do the build otherwise.	 */
	private void buildProjects(final Set projects) {
		
		// Workspace runnable that builds the specified projects
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				Iterator iter = projects.iterator();
				monitor.beginTask(EMPTY_STRING, projects.size() * 100);
				while (iter.hasNext()) {
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
					IProject pro = ((IJavaProject)iter.next()).getProject();
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
			try {
				ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());
			} catch (CoreException ce) {
				log(ce);
			}
//		}
	}	
			
	/**
	 * @see IVMInstallChangedListener#defaultVMInstallChanged(IVMInstall, IVMInstall)
	 */
	public void defaultVMInstallChanged(
		IVMInstall previous,
		IVMInstall current) {
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
	}

	/**
	 * @see IVMInstallChangedListener#vmRemoved(IVMInstall)
	 */
	public void vmRemoved(IVMInstall vm) {
		JREContainerInitializer init = new JREContainerInitializer();
		try {
			init.updateRemovedVM(vm);
		} catch (CoreException ce) {
			log(ce);
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
		appendPathElements(doc, "bootpath", libraryElement, info.getBootpath()); //$NON-NLS-1$
		appendPathElements(doc, "extensionDirs", libraryElement, info.getExtensionDirs()); //$NON-NLS-1$
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
							String location = element.getAttribute("home"); //$NON-NLS-1$
							String[] bootpath = getPathsFromXML(element, "bootpath"); //$NON-NLS-1$
							String[] extDirs = getPathsFromXML(element, "extensionDirs"); //$NON-NLS-1$
							if (location != null) {
								LibraryInfo info = new LibraryInfo(bootpath, extDirs);
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

 