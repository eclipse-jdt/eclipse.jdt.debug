/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import com.ibm.icu.text.MessageFormat;

import org.osgi.framework.BundleContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;

public class LaunchingPlugin extends Plugin implements Preferences.IPropertyChangeListener, IVMInstallChangedListener, IResourceChangeListener, ILaunchesListener, IDebugEventSetListener {
	
	/**
	 * The id of the JDT launching plug-in (value <code>"org.eclipse.jdt.launching"</code>).
	 */
	public static final String ID_PLUGIN= "org.eclipse.jdt.launching"; //$NON-NLS-1$
	
	/**
	 * Identifier for 'vmConnectors' extension point
	 */
	public static final String ID_EXTENSION_POINT_VM_CONNECTORS = "vmConnectors"; //$NON-NLS-1$
	
	/**
	 * Identifier for 'runtimeClasspathEntries' extension point
	 */
	public static final String ID_EXTENSION_POINT_RUNTIME_CLASSPATH_ENTRIES = "runtimeClasspathEntries"; //$NON-NLS-1$
	
	/**
	 * Marker type for JRE container problems.
	 * 
	 * @since 3.2
	 */
	public static final String ID_JRE_CONTAINER_MARKER = ID_PLUGIN + ".jreContainerMarker"; //$NON-NLS-1$
	
	private static LaunchingPlugin fgLaunchingPlugin;
	
	private HashMap fVMConnectors = null;
	
	/**
	 * Runtime classpath extensions
	 */
	private HashMap fClasspathEntryExtensions = null;

	private String fOldVMPrefString = EMPTY_STRING;
	
	private boolean fIgnoreVMDefPropertyChangeEvents = false;
		
	private static final String EMPTY_STRING = "";    //$NON-NLS-1$
			
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
	 * Shared XML parser
	 */
	private static DocumentBuilder fgXMLParser = null;
	
	/**
	 * Whether debug options are turned on for this plug-in.
	 */
	public static boolean DEBUG = false;
	
	/**
	 * Stores VM changes resulting from a JRE preference change.
	 */
	class VMChanges implements IVMInstallChangedListener {
		
		// true if the default VM changes
		private boolean fDefaultChanged = false;
		
		// old container ids to new
		private HashMap fRenamedContainerIds = new HashMap();
		
		/**
		 * Returns the JRE container id that the given VM would map to, or
		 * <code>null</code> if none.
		 * 
		 * @param vm
		 * @return container id or <code>null</code>
		 */
		private IPath getContainerId(IVMInstall vm) {
			if (vm != null) {
				String name = vm.getName();
				if (name != null) {
					IPath path = new Path(JavaRuntime.JRE_CONTAINER);
					path = path.append(new Path(vm.getVMInstallType().getId()));
					path = path.append(new Path(name));
					return path;
				}
			}
			return null;
		}
		
		/**
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
		 */
		public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
			fDefaultChanged = true;
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
			if (property.equals(IVMInstallChangedListener.PROPERTY_NAME)) {
				IPath newId = getContainerId(vm);
				IPath oldId = new Path(JavaRuntime.JRE_CONTAINER);
				oldId = oldId.append(vm.getVMInstallType().getId());
				String oldName = (String)event.getOldValue();
				// bug 33746 - if there is no old name, then this is not a re-name.
				if (oldName != null) {
					oldId = oldId.append(oldName);
					fRenamedContainerIds.put(oldId, newId);
					//bug 39222 update launch configurations that ref old name
					try {
						ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
						String container = null;
						ILaunchConfigurationWorkingCopy wc = null;
						IPath cpath = null;
						for(int i = 0; i < configs.length; i++) {
							container = configs[i].getAttribute(JavaRuntime.JRE_CONTAINER, (String)null);
							if(container != null) {
								cpath = new Path(container);
								if(cpath.lastSegment().equals(oldName)) {
									cpath = cpath.removeLastSegments(1).append(newId.lastSegment()).addTrailingSeparator();
									wc = configs[i].getWorkingCopy();
									wc.setAttribute(JavaRuntime.JRE_CONTAINER, cpath.toString());
									wc.doSave();
								}
							}
						}
					} catch (CoreException e) {}
				}
			}
		}

		/**
		 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
		 */
		public void vmRemoved(IVMInstall vm) {
		}
	
		/**
		 * Re-bind classpath variables and containers affected by the JRE
		 * changes.
		 */
		public void process() {
			JREUpdateJob job = new JREUpdateJob(this);
			job.schedule();
		}
		
		protected void doit(IProgressMonitor monitor) throws CoreException {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor1) throws CoreException {
					IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
					monitor1.beginTask(LaunchingMessages.LaunchingPlugin_0, projects.length + 1);
					rebind(monitor1, projects);
					monitor1.done();
				}
			};
			JavaCore.run(runnable, null, monitor);
		}
				
		/**
		 * Re-bind classpath variables and containers affected by the JRE
		 * changes.
		 * @param monitor
		 */
		private void rebind(IProgressMonitor monitor, IJavaProject[] projects) throws CoreException {
			 
			if (fDefaultChanged) {
				// re-bind JRELIB if the default VM changed
				JavaClasspathVariablesInitializer initializer = new JavaClasspathVariablesInitializer();
				initializer.initialize(JavaRuntime.JRELIB_VARIABLE);
				initializer.initialize(JavaRuntime.JRESRC_VARIABLE);
				initializer.initialize(JavaRuntime.JRESRCROOT_VARIABLE);
			}
			monitor.worked(1);
														
			// re-bind all container entries
			for (int i = 0; i < projects.length; i++) {
				IJavaProject project = projects[i];
				IClasspathEntry[] entries = project.getRawClasspath();
				boolean replace = false;
				for (int j = 0; j < entries.length; j++) {
					IClasspathEntry entry = entries[j];
					switch (entry.getEntryKind()) {
						case IClasspathEntry.CPE_CONTAINER:
							IPath reference = entry.getPath();
							IPath newBinding = null;
							String firstSegment = reference.segment(0);
							if (JavaRuntime.JRE_CONTAINER.equals(firstSegment)) {
								if (reference.segmentCount() > 1) {
									IPath renamed = (IPath)fRenamedContainerIds.get(reference);
									if (renamed != null) {
										// The JRE was re-named. This changes the identifier of
										// the container entry.
										newBinding = renamed;
									}
								}
								JREContainerInitializer initializer = new JREContainerInitializer();
								if (newBinding == null){
									// re-bind old path
									initializer.initialize(reference, project);
								} else {
									// replace old class path entry with a new one
									IClasspathEntry newEntry = JavaCore.newContainerEntry(newBinding, entry.isExported());
									entries[j] = newEntry;
									replace = true;
								}
							}
							break;
						default:
							break;
					}
				}
				if (replace) {
					project.setRawClasspath(entries, null);
				}
				monitor.worked(1);
			}

		}

	}
	
	class JREUpdateJob extends Job {
		private VMChanges fChanges;
		
		public JREUpdateJob(VMChanges changes) {
			super(LaunchingMessages.LaunchingPlugin_1);
			fChanges = changes;
			setSystem(true);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			try {
				fChanges.doit(monitor);
			} catch (CoreException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}
		
	}
	
	/**
	 * Constructor
	 */
	public LaunchingPlugin() {
		super();
		fgLaunchingPlugin = this;
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
	 * @param info the library information, or <code>null</code> to remove
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
	 * <code>IPath</code> in the plugin directory.
	 */
	public static File getFileInPlugin(IPath path) {
		try {
			URL installURL =
				new URL(getDefault().getBundle().getEntry("/"), path.toString()); //$NON-NLS-1$
			URL localURL = FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException ioe) {
			return null;
		}
	}
		
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		return ID_PLUGIN;
	}

	/**
	 * Returns the singleton instance of <code>LaunchingPlugin</code>
	 * @return the singleton instance of <code>LaunchingPlugin</code>
	 */
	public static LaunchingPlugin getDefault() {
		if(fgLaunchingPlugin == null) {
			fgLaunchingPlugin = new LaunchingPlugin();
		}
		return fgLaunchingPlugin;
	}
	
	/**
	 * Logs the specified status
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Logs the specified message, by creating a new <code>Status</code>
	 * @param message
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}
		
	/**
	 * Logs the specified exception by creating a new <code>Status</code>
	 * @param e
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}
	
	/**
	 * Clears zip file cache.
	 * Shutdown the launch config helper.
	 * 
	 * @see Plugin#stop(BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
			DebugPlugin.getDefault().removeDebugEventListener(this);
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
			ArchiveSourceLocation.closeArchives();
			getPluginPreferences().removePropertyChangeListener(this);
			JavaRuntime.removeVMInstallChangedListener(this);
			JavaRuntime.saveVMConfiguration();
			fgXMLParser = null;
			ResourcesPlugin.getWorkspace().removeSaveParticipant(this);
		} finally {
			super.stop(context);
		}
	}
		
	/**
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.jdt.launching/debug"));  //$NON-NLS-1$//$NON-NLS-2$
		ResourcesPlugin.getWorkspace().addSaveParticipant(this, new ISaveParticipant() {
			public void doneSaving(ISaveContext context1) {}
			public void prepareToSave(ISaveContext context1)	throws CoreException {}
			public void rollback(ISaveContext context1) {}
			public void saving(ISaveContext context1) throws CoreException {
				savePluginPreferences();
			}
			
		});
		// Exclude launch configurations from being copied to the output directory
		String launchFilter = "*." + ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION; //$NON-NLS-1$
		Hashtable optionsMap = JavaCore.getOptions();
		String filters= (String)optionsMap.get(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);
		boolean modified = false;
		if (filters == null || filters.length() == 0) {
			filters= launchFilter;
			modified = true;
		} else if (filters.indexOf(launchFilter) == -1) {
			filters= filters + ',' + launchFilter;
			modified = true;
		}

		if (modified) {
			optionsMap.put(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, filters);
			JavaCore.setOptions(optionsMap);
		}

		// set default preference values
		getPluginPreferences().setDefault(JavaRuntime.PREF_CONNECT_TIMEOUT, JavaRuntime.DEF_CONNECT_TIMEOUT);
		getPluginPreferences().addPropertyChangeListener(this);

		JavaRuntime.addVMInstallChangedListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_BUILD);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
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
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(ID_PLUGIN, ID_EXTENSION_POINT_VM_CONNECTORS);
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements();
		MultiStatus status= new MultiStatus(getUniqueIdentifier(), IStatus.OK, "Exception occurred reading vmConnectors extensions.", null);  //$NON-NLS-1$
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
	 * Returns a new runtime classpath entry of the specified type.
	 * 
	 * @param id extension type id
	 * @return new uninitialized runtime classpath entry
	 * @throws CoreException if unable to create an entry
	 */
	public IRuntimeClasspathEntry2 newRuntimeClasspathEntry(String id) throws CoreException {
		if (fClasspathEntryExtensions == null) {
			initializeRuntimeClasspathExtensions();
		}
		IConfigurationElement config = (IConfigurationElement) fClasspathEntryExtensions.get(id);
		if (config == null) {
			abort(MessageFormat.format(LaunchingMessages.LaunchingPlugin_32, new String[]{id}), null);
		}
		return (IRuntimeClasspathEntry2) config.createExecutableExtension("class"); //$NON-NLS-1$
	}
	
	/**
	 * Loads runtime classpath extensions
	 */
	private void initializeRuntimeClasspathExtensions() {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, ID_EXTENSION_POINT_RUNTIME_CLASSPATH_ENTRIES);
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements();
		fClasspathEntryExtensions = new HashMap(configs.length);
		for (int i= 0; i < configs.length; i++) {
			fClasspathEntryExtensions.put(configs[i].getAttribute("id"), configs[i]); //$NON-NLS-1$
		}
	}
	
	/**
	 * Save preferences whenever the connect timeout changes.
	 * Process changes to the list of installed JREs.
	 * 
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(JavaRuntime.PREF_VM_XML)) {
			if (!isIgnoreVMDefPropertyChangeEvents()) {
				processVMPrefsChanged((String)event.getOldValue(), (String)event.getNewValue());
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
			
			// Determine the deleted VMs
			List deleted = oldResults.getVMList();
			List current = newResults.getValidVMList();
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
				vmChanges.process();
			}
		}

	}
	
	/**
	 * Parse the given xml into a VM definitions container, returning an empty
	 * container if an exception occurs.
	 * 
	 * @param xml
	 * @return VMDefinitionsContainer
	 */
	private VMDefinitionsContainer getVMDefinitions(String xml) {
		if (xml.length() > 0) {
			try {
				ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes("UTF8")); //$NON-NLS-1$
				return VMDefinitionsContainer.parseXMLIntoContainer(stream);
			} catch (IOException e) {
				LaunchingPlugin.log(e);
			}
		}
		return new VMDefinitionsContainer();
	}
						
	/**
	 * @see IVMInstallChangedListener#defaultVMInstallChanged(IVMInstall, IVMInstall)
	 */
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		if (!fBatchingChanges) {
			VMChanges changes = new VMChanges();
			changes.defaultVMInstallChanged(previous, current);
			changes.process();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
	 */
	public void vmAdded(IVMInstall vm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
	 */
	public void vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent event) {
		if (!fBatchingChanges) {
			VMChanges changes = new VMChanges();
			changes.vmChanged(event);
			changes.process();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
	 */
	public void vmRemoved(IVMInstall vm) {
		if (!fBatchingChanges) {
			VMChanges changes = new VMChanges();
			changes.vmRemoved(vm);
			changes.process();
		}
	}

	/**
	 * Clear the archive cache when a project is about to be deleted.
	 * Warn when a build path changes and references an execution environment
	 * that does not have a perfect match.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		ArchiveSourceLocation.closeArchives();
		if (event.getType() == IResourceChangeEvent.PRE_BUILD) {
			IResourceDelta delta = event.getDelta();
			IResourceDelta[] projectDeltas = delta.getAffectedChildren();
			for (int i = 0, length = projectDeltas.length; i < length; i++) {
				IResourceDelta projectDelta = projectDeltas[i];
				IResourceDelta classpathDelta = projectDelta.findMember(new Path(".classpath")); //$NON-NLS-1$
				if (classpathDelta != null || (projectDelta.getFlags() & IResourceDelta.DESCRIPTION) > 0) {
					IJavaProject project = (IJavaProject) JavaCore.create(projectDelta.getResource());
					if (project != null && project.exists()) {
						try {
							IClasspathEntry[] rawClasspath = project.getRawClasspath();
							for (int j = 0; j < rawClasspath.length; j++) {
								IClasspathEntry entry = rawClasspath[j];
								if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
									IPath path = entry.getPath();
									if (JavaRuntime.JRE_CONTAINER.equals(path.segment(0))) {
										IVMInstall vm = JREContainerInitializer.resolveVM(path);
										validateEnvironment(path, project, vm);
									}
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

	/**
	 * Allows vm property change events to be ignored
	 * @param ignore
	 */
	public void setIgnoreVMDefPropertyChangeEvents(boolean ignore) {
		fIgnoreVMDefPropertyChangeEvents = ignore;
	}

	/**
	 * Returns if vm property changed event should be ignored or not
	 * @return if vm property changed event should be ignored or not
	 */
	public boolean isIgnoreVMDefPropertyChangeEvents() {
		return fIgnoreVMDefPropertyChangeEvents;
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
	private static String getLibraryInfoAsXML() throws CoreException {
		
		Document doc = DebugPlugin.newDocument();
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
		return DebugPlugin.serializeDocument(doc);
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
		OutputStream stream= null;
		try {
			String xml = getLibraryInfoAsXML();
			IPath libPath = getDefault().getStateLocation();
			libPath = libPath.append("libraryInfos.xml"); //$NON-NLS-1$
			File file = libPath.toFile();
			if (!file.exists()) {
				file.createNewFile();
			}
			stream = new BufferedOutputStream(new FileOutputStream(file));
			stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
		} catch (IOException e) {
			log(e);
		}  catch (CoreException e) {
			log(e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e1) {
				}
			}
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
				InputStream stream = new BufferedInputStream(new FileInputStream(file));
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				parser.setErrorHandler(new DefaultHandler());
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
	
	/**
	 * Returns paths stored in XML
	 * @param lib
	 * @param pathType
	 * @return paths stored in XML
	 */
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
	
	/**
	 * When a launch is removed, close all source archives. Prevents file
	 * sharing violations.
	 * 
	 * @see ILaunchesListener#launchesRemoved(ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
		ArchiveSourceLocation.closeArchives();
	}
	
	/**
	 * @see ILaunchesListener#launchesAdded(ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
	}
	
	/**
	 * @see ILaunchesListener#launchesChanged(ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
	}
	
	/**
	 * When a debug target or process terminates, close source archives.
	 * Prevents file sharing violations.
	 * 
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.TERMINATE) {
				Object source = event.getSource();
				if (source instanceof IDebugTarget || source instanceof IProcess) {
					ArchiveSourceLocation.closeArchives();
				}
			}
		}
	}
	
	/**
	 * Returns a shared XML parser.
	 * 
	 * @return an XML parser
	 * @throws CoreException if unable to create a parser
	 * @since 3.0
	 */
	public static DocumentBuilder getParser() throws CoreException {
		if (fgXMLParser == null) {
			try {
				fgXMLParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				fgXMLParser.setErrorHandler(new DefaultHandler());
			} catch (ParserConfigurationException e) {
				abort(LaunchingMessages.LaunchingPlugin_33, e);
			} catch (FactoryConfigurationError e) {
				abort(LaunchingMessages.LaunchingPlugin_34, e);
			}
		}
		return fgXMLParser;
	}
	
	/**
	 * Throws an exception with the given message and underlying exception.
	 * 
	 * @param message error message
	 * @param exception underlying exception or <code>null</code> if none
	 * @throws CoreException
	 */
	protected static void abort(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, message, exception);
		throw new CoreException(status);
	}
	
	/**
	 * Validates the environment
	 * @param containerPath
	 * @param project
	 * @param vm
	 */
	private void validateEnvironment(IPath containerPath, final IJavaProject project, IVMInstall vm) {
		try {
			project.getProject().deleteMarkers(ID_JRE_CONTAINER_MARKER, false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			LaunchingPlugin.log(e);
		}
		String id = JREContainerInitializer.getExecutionEnvironmentId(containerPath);
		if (id != null) {
			IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
			final IExecutionEnvironment environment = manager.getEnvironment(id);
			if (environment != null) {
				if (vm == null) {
					String message = MessageFormat.format(
							LaunchingMessages.LaunchingPlugin_38,
							new String[]{environment.getId()});
					createJREContainerProblem(project, message, IMarker.SEVERITY_ERROR);
				} else if (!environment.isStrictlyCompatible(vm)) {
					// warn that VM does not match EE
					// first determine if there is a strictly compatible JRE available
					IVMInstall[] compatibleVMs = environment.getCompatibleVMs();
					int exact = 0;
					for (int i = 0; i < compatibleVMs.length; i++) {
						if (environment.isStrictlyCompatible(compatibleVMs[i])) {
							exact++;
						}
					}
					String message = null;
					if (exact == 0) {
						message = MessageFormat.format(
							LaunchingMessages.LaunchingPlugin_35,
							new String[]{environment.getId()});
					} else {
						message = MessageFormat.format(
								LaunchingMessages.LaunchingPlugin_36,
								new String[]{environment.getId()});
					}
					createJREContainerProblem(project, message, IMarker.SEVERITY_WARNING);
				}
			}
		}
	}
	
	/**
	 * creates a problem marker for a jre container problem
	 * @param javaProject
	 * @param message
	 * @param severity
	 */
	private void createJREContainerProblem(IJavaProject javaProject, String message, int severity) {
		try {
			IMarker marker = javaProject.getProject().createMarker(ID_JRE_CONTAINER_MARKER);
			marker.setAttributes(
				new String[] {
						IMarker.MESSAGE,
						IMarker.SEVERITY,
						IMarker.LOCATION},
					new Object[] {
						message,
						new Integer(severity),
						LaunchingMessages.LaunchingPlugin_37
					});
		} catch (CoreException e) {
			return;
		}
	}
}

 
