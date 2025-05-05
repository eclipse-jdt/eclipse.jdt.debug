/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.sourcelookup.advanced.AdvancedSourceLookupSupport;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.util.tracker.ServiceTracker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("deprecation")
public class LaunchingPlugin extends Plugin implements DebugOptionsListener, IEclipsePreferences.IPreferenceChangeListener, IVMInstallChangedListener, IResourceChangeListener, ILaunchesListener, IDebugEventSetListener {

	/**
	 * Whether debug options are turned on for this plug-in.
	 */
	public static boolean DEBUG = false;
	public static boolean DEBUG_JRE_CONTAINER = false;

	public static final String DEBUG_JRE_CONTAINER_FLAG = "org.eclipse.jdt.launching/debug/classpath/jreContainer"; //$NON-NLS-1$
	public static final String DEBUG_FLAG = "org.eclipse.jdt.launching/debug"; //$NON-NLS-1$

	/**
	 * list of temp files for the launch (separated by the path separator char). Files must start with {@link #LAUNCH_TEMP_FILE_PREFIX} and will be
	 * deleted once the process is terminated
	 */
	public static final String ATTR_LAUNCH_TEMP_FILES = "tempFiles"; //$NON-NLS-1$

	/**
	 * prefix for temp files
	 */
	public static final String LAUNCH_TEMP_FILE_PREFIX = ".temp-"; //$NON-NLS-1$

	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 * @since 3.8
	 */
	private static DebugTrace fgDebugTrace;

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

	private static LaunchingPlugin fgLaunchingPlugin;

	private final ConcurrentHashMap<String, IVMConnector> fVMConnectors = new ConcurrentHashMap<>();

	/**
	 * Runtime classpath extensions
	 */
	private final ConcurrentHashMap<String, IConfigurationElement> fClasspathEntryExtensions = new ConcurrentHashMap<>();

	private volatile String fOldVMPrefString = EMPTY_STRING;

	private volatile boolean fIgnoreVMDefPropertyChangeEvents;

	private static final String EMPTY_STRING = "";    //$NON-NLS-1$
	public static final String PREF_DETECT_VMS_AT_STARTUP = "detectVMsAtStartup"; //$NON-NLS-1$

	/**
	 * Mapping of top-level VM installation directories to library info for that
	 * VM.
	 */
	private static final ConcurrentHashMap<String, LibraryInfo> fgLibraryInfoMap = new ConcurrentHashMap<>(10);

	/**
	 * Mapping of the last time the directory of a given SDK was modified.
	 * <br><br>
	 * Mapping: <code>Map&lt;String,Long&gt;</code>
	 * @since 3.7
	 */
	private static final ConcurrentHashMap<String, Long> fgInstallTimeMap = new ConcurrentHashMap<>();
	/**
	 * List of install locations that have been detected to have changed
	 *
	 * @since 3.7
	 */
	private static final Set<String> fgHasChanged = ConcurrentHashMap.newKeySet();
	/**
	 * Mutex for checking the time stamp of an install location
	 *
	 * @since 3.7
	 */
	private static final Object installLock = new Object();

	/**
	 * Whether changes in VM preferences are being batched. When being batched
	 * the plug-in can ignore processing and changes.
	 */
	private volatile boolean fBatchingChanges;

	/**
	 * Shared XML parser
	 */
	private static volatile DocumentBuilder fgXMLParser;

	/**
	 * Service tracker for the workspace service
	 */
	private ServiceTracker<IWorkspace, IWorkspace> fWorkspaceServiceTracker;

	/**
	 * Stores VM changes resulting from a JRE preference change.
	 */
	class VMChanges implements IVMInstallChangedListener {

		// true if the default VM changes
		private volatile boolean fDefaultChanged;

		// old container ids to new
		private final ConcurrentHashMap<IPath, IPath> fRenamedContainerIds = new ConcurrentHashMap<>();

		/**
		 * Returns the JRE container id that the given VM would map to, or
		 * <code>null</code> if none.
		 *
		 * @param vm the new path id of the {@link IVMInstall}
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

		@Override
		public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
			fDefaultChanged = true;
		}

		@Override
		public void vmAdded(IVMInstall vm) {
		}

		@Override
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

		@Override
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
				@Override
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
		 * @param monitor a progress monitor or <code>null</code>
		 * @param projects the list of {@link IJavaProject}s to re-bind the VM to
		 * @throws CoreException if an exception is thrown
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
			int length = projects.length;
			Map<IPath, List<IJavaProject>> projectsMap = new HashMap<>();
			for (int i = 0; i < length; i++) {
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
									IPath renamed = fRenamedContainerIds.get(reference);
									if (renamed != null) {
										// The JRE was re-named. This changes the identifier of
										// the container entry.
										newBinding = renamed;
									}
								}
								if (newBinding == null){
									// re-bind old path
									// @see bug 310789 - batch updates by common container paths
									List<IJavaProject> projectsList = projectsMap.get(reference);
									if (projectsList == null) {
										projectsMap.put(reference, projectsList = new ArrayList<>(length));
									}
									projectsList.add(project);
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
			Iterator<IPath> references = projectsMap.keySet().iterator();
			while (references.hasNext()) {
				IPath reference = references.next();
				List<IJavaProject> projectsList = projectsMap.get(reference);
				IJavaProject[] referenceProjects = new IJavaProject[projectsList.size()];
				projectsList.toArray(referenceProjects);
				// re-bind old path
				JREContainerInitializer initializer = new JREContainerInitializer();
				initializer.initialize(reference, referenceProjects);
			}
		}

	}

	class JREUpdateJob extends Job {
		private final VMChanges fChanges;

		public JREUpdateJob(VMChanges changes) {
			super(LaunchingMessages.LaunchingPlugin_1);
			fChanges = changes;
			setSystem(true);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
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
	 * @param javaInstallPath the absolute path to the java executable
	 * @return the library info that corresponds to the specified JRE install
	 * path, or <code>null</code> if none
	 */
	public static LibraryInfo getLibraryInfo(String javaInstallPath) {
		if (fgLibraryInfoMap.isEmpty()) {
			restoreLibraryInfo();
		}
		return fgLibraryInfoMap.get(javaInstallPath);
	}

	/**
	 * Sets the library info that corresponds to the specified JRE install
	 * path.
	 *
	 * @param javaInstallPath home location for a JRE
	 * @param info the library information, or <code>null</code> to remove
	 */
	public static void setLibraryInfo(String javaInstallPath, LibraryInfo info) {
		if (isVMLogging()) {
			LaunchingPlugin.log(LaunchingMessages.VMLogging_2 + javaInstallPath);
		}
		if (fgLibraryInfoMap.isEmpty()) {
			restoreLibraryInfo();
		}
		if (info == null) {
			fgLibraryInfoMap.remove(javaInstallPath);
			if (!fgInstallTimeMap.isEmpty()) {
				fgInstallTimeMap.remove(javaInstallPath);
				writeInstallInfo();
			}

		} else {
			fgLibraryInfoMap.put(javaInstallPath, info);
		}
		//once the library info has been set we can forget it has changed
		fgHasChanged.remove(javaInstallPath);
		saveLibraryInfo();
	}

	public static boolean isVMLogging() {
		String vmLogging = System.getProperty("jdt.debug.launching.vmLogging"); //$NON-NLS-1$
		return "true".equalsIgnoreCase(vmLogging); //$NON-NLS-1$
	}

	/**
	 * Return a <code>java.io.File</code> object that corresponds to the specified
	 * <code>IPath</code> in the plug-in directory.
	 *
	 * @param path the path to look for in the launching bundle
	 * @return the {@link File} from the bundle or <code>null</code>
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
	 * Convenience method which returns the unique identifier of this plug-in.
	 *
	 * @return the id of the {@link LaunchingPlugin}
	 */
	public static String getUniqueIdentifier() {
		return ID_PLUGIN;
	}

	/**
	 * Returns the singleton instance of <code>LaunchingPlugin</code>
	 * @return the singleton instance of <code>LaunchingPlugin</code>
	 */
	public static LaunchingPlugin getDefault() {
		return fgLaunchingPlugin;
	}

	/**
	 * Logs the specified status
	 * @param status the status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs the specified message, by creating a new <code>Status</code>
	 * @param message the message to log as an error status
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs the specified exception by creating a new <code>Status</code>
	 * @param e the {@link Throwable} to log as an error
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	/**
	 * Clears zip file cache.
	 * Shutdown the launch configuration helper.
	 *
	 * @see Plugin#stop(BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			AdvancedSourceLookupSupport.stop();

			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
			DebugPlugin.getDefault().removeDebugEventListener(this);
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
			ArchiveSourceLocation.closeArchives();
			InstanceScope.INSTANCE.getNode(ID_PLUGIN).removePreferenceChangeListener(this);
			JavaRuntime.removeVMInstallChangedListener(this);
			JavaRuntime.saveVMConfiguration();
			fgXMLParser = null;
			fWorkspaceServiceTracker.close();
		} finally {
			super.stop(context);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Hashtable<String, String> props = new Hashtable<>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
		fWorkspaceServiceTracker = new ServiceTracker<>(context, IWorkspace.class, null) {

			@Override
			public IWorkspace addingService(ServiceReference<IWorkspace> reference) {
				IWorkspace workspace = context.getService(reference);
				if (workspace == null) {
					log("Could not add save participant as IWorkspace service is unavailable"); //$NON-NLS-1$
					return null;
				}
				try {
					workspace.addSaveParticipant(ID_PLUGIN, new ISaveParticipant() {
						@Override
						public void doneSaving(ISaveContext context1) {}
						@Override
						public void prepareToSave(ISaveContext context1)	throws CoreException {}
						@Override
						public void rollback(ISaveContext context1) {}
						@Override
						public void saving(ISaveContext context1) throws CoreException {
							try {
								InstanceScope.INSTANCE.getNode(ID_PLUGIN).flush();
							} catch (BackingStoreException e) {
								log(e);
							}
							//catch in case any install times are still cached for removed JREs
							writeInstallInfo();
						}
					});
				} catch (CoreException e) {
					log(e.getStatus());
					context.ungetService(reference);
					return null;
				}
				return super.addingService(reference);
			}

			@Override
			public void removedService(ServiceReference<IWorkspace> reference, IWorkspace service) {
				service.removeSaveParticipant(ID_PLUGIN);
				context.ungetService(reference);
			}

		};
		fWorkspaceServiceTracker.open();

		InstanceScope.INSTANCE.getNode(ID_PLUGIN).addPreferenceChangeListener(this);
		JavaRuntime.addVMInstallChangedListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		AdvancedSourceLookupSupport.start();
	}

	/**
	 * Returns the VM connector with the specified id, or <code>null</code>
	 * if none.
	 *
	 * @param id connector identifier
	 * @return VM connector
	 */
	public IVMConnector getVMConnector(String id) {
		if (fVMConnectors.isEmpty()) {
			initializeVMConnectors();
		}
		return fVMConnectors.get(id);
	}

	/**
	 * Returns all VM connector extensions.
	 *
	 * @return VM connectors
	 */
	public IVMConnector[] getVMConnectors() {
		if (fVMConnectors.isEmpty()) {
			initializeVMConnectors();
		}
		return fVMConnectors.values().toArray(new IVMConnector[0]);
	}

	/**
	 * Loads VM connector extensions
	 */
	private void initializeVMConnectors() {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(ID_PLUGIN, ID_EXTENSION_POINT_VM_CONNECTORS);
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements();
		MultiStatus status= new MultiStatus(getUniqueIdentifier(), IStatus.OK, "Exception occurred reading vmConnectors extensions.", null);  //$NON-NLS-1$
		fVMConnectors.clear();
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
		if (fClasspathEntryExtensions.isEmpty()) {
			initializeRuntimeClasspathExtensions();
		}
		IConfigurationElement config = fClasspathEntryExtensions.get(id);
		if (config != null) {
			return (IRuntimeClasspathEntry2) config.createExecutableExtension("class"); //$NON-NLS-1$
		}
		abort(NLS.bind(LaunchingMessages.LaunchingPlugin_32, id), null);
		return null;
	}

	/**
	 * Loads runtime classpath extensions
	 */
	private void initializeRuntimeClasspathExtensions() {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(LaunchingPlugin.ID_PLUGIN, ID_EXTENSION_POINT_RUNTIME_CLASSPATH_ENTRIES);
		IConfigurationElement[] configs= extensionPoint.getConfigurationElements();
		fClasspathEntryExtensions.clear();
		for (int i= 0; i < configs.length; i++) {
			fClasspathEntryExtensions.put(configs[i].getAttribute("id"), configs[i]); //$NON-NLS-1$
		}
	}

	/**
	 * Check for differences between the old and new sets of installed JREs. Differences may include additions, deletions and changes. Take
	 * appropriate action for each type of difference.
	 *
	 * When importing preferences, TWO propertyChange events are fired. The first has an old value but an empty new value. The second has a new value,
	 * but an empty old value. Normal user changes to the preferences result in a single propertyChange event, with both old and new values populated.
	 * This method handles both types of notification.
	 *
	 * @param oldValue
	 *            the old preference value
	 * @param newValue
	 *            the new preference value
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
			// sequence.  Now that we have both old & new preferences, we can parse and compare them.
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
			List<IVMInstall> deleted = oldResults.getVMList();
			List<IVMInstall> current = newResults.getValidVMList();
			deleted.removeAll(current);

			// Dispose deleted VMs.  The 'disposeVMInstall' method fires notification of the
			// deletion.
			Iterator<IVMInstall> deletedIterator = deleted.iterator();
			while (deletedIterator.hasNext()) {
				VMStandin deletedVMStandin = (VMStandin) deletedIterator.next();
				deletedVMStandin.getVMInstallType().disposeVMInstall(deletedVMStandin.getId());
			}

			// Fire change notification for added and changed VMs. The 'convertToRealVM'
			// fires the appropriate notification.
			Iterator<IVMInstall> iter = current.iterator();
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
	 * Parse the given XML into a VM definitions container, returning an empty
	 * container if an exception occurs.
	 *
	 * @param xml the XML to parse for VM descriptions
	 * @return VMDefinitionsContainer
	 */
	private VMDefinitionsContainer getVMDefinitions(String xml) {
		if (xml.length() > 0) {
			try {
				ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
				return VMDefinitionsContainer.parseXMLIntoContainer(stream);
			} catch (IOException e) {
				LaunchingPlugin.log(e);
			}
		}
		return new VMDefinitionsContainer();
	}

	@Override
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		if (!fBatchingChanges) {
			VMChanges changes = new VMChanges();
			changes.defaultVMInstallChanged(previous, current);
			changes.process();
		}
	}

	@Override
	public void vmAdded(IVMInstall vm) {
	}

	@Override
	public void vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent event) {
		if (!fBatchingChanges) {
			VMChanges changes = new VMChanges();
			changes.vmChanged(event);
			changes.process();
		}
	}

	@Override
	public void vmRemoved(IVMInstall vm) {
		if (!fBatchingChanges) {
			VMChanges changes = new VMChanges();
			changes.vmRemoved(vm);
			changes.process();
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		ArchiveSourceLocation.closeArchives();
	}

	/**
	 * Allows VM property change events to be ignored
	 * @param ignore if we should ignore VM property changed events or not
	 */
	public void setIgnoreVMDefPropertyChangeEvents(boolean ignore) {
		fIgnoreVMDefPropertyChangeEvents = ignore;
	}

	/**
	 * Returns if VM property changed event should be ignored or not
	 * @return if VM property changed event should be ignored or not
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
	 * @throws CoreException if this method fails. Reasons include:<ul>
	 * <li>serialization of the XML document failed</li>
	 * </ul>
	 */
	private static String getLibraryInfoAsXML() throws CoreException {

		Document doc = DebugPlugin.newDocument();
		Element config = doc.createElement("libraryInfos");    //$NON-NLS-1$
		doc.appendChild(config);

		// Create a node for each info in the table
		Iterator<String> locations = fgLibraryInfoMap.keySet().iterator();
		while (locations.hasNext()) {
			String home = locations.next();
			LibraryInfo info = fgLibraryInfoMap.get(home);
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
	 * @param doc the backing {@link Document}
	 * @param info the {@link LibraryInfo} to add to the {@link Document}
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
	 * @param doc the backing {@link Document}
	 * @param elementType the kind of {@link Element} to create
	 * @param libraryElement the {@link Element} describing a given {@link LibraryInfo} object
	 * @param paths the paths to add
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
			try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
				stream.write(xml.getBytes(StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			log(e);
		}  catch (CoreException e) {
			log(e);
		}
	}

	/**
	 * Restores library information for VMs
	 */
	private static void restoreLibraryInfo() {
		fgLibraryInfoMap.clear();
		IPath libPath = getDefault().getStateLocation();
		libPath = libPath.append("libraryInfos.xml"); //$NON-NLS-1$
		File file = libPath.toFile();
		if (file.exists()) {
			try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
				@SuppressWarnings("restriction")
				DocumentBuilder parser = org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE();
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
								if (isVMLogging()) {
									LaunchingPlugin.log(LaunchingMessages.VMLogging_1 + location);
								}
								LibraryInfo info = new LibraryInfo(version, bootpath, extDirs, endDirs);
								fgLibraryInfoMap.put(location, info);
							}
						}
					}
				}
			} catch (IOException | ParserConfigurationException | SAXException e) {
				log(e);
			}
		}
	}

	/**
	 * Checks to see if the time stamp of the file describe by the given location string
	 * has been modified since the last recorded time stamp. If there is no last recorded
	 * time stamp we assume it has changed. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information
	 *
	 * @param location the location of the SDK we want to check the time stamp for
	 * @return <code>true</code> if the time stamp has changed compared to the cached one or if there is
	 * no recorded time stamp, <code>false</code> otherwise.
	 *
	 * @since 3.7
	 */
	public static boolean timeStampChanged(String location) {
		synchronized (installLock) {
			if(fgHasChanged.contains(location)) {
				return true;
			}
			File file = new File(location);
			if(file.exists()) {
				if (fgInstallTimeMap.isEmpty()) {
					readInstallInfo();
				}
				Long stamp = fgInstallTimeMap.get(location);
				long fstamp = file.lastModified();
				if(stamp != null) {
					if(stamp.longValue() == fstamp) {
						return false;
					}
				}
				//if there is no recorded stamp we have to assume it is new
				stamp = Long.valueOf(fstamp);
				fgInstallTimeMap.put(location, stamp);
				writeInstallInfo();
				fgHasChanged.add(location);
				return true;
			}
		}
		return false;
	}

	/**
	 * Reads the file of saved time stamps and populates the {@link #fgInstallTimeMap}.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information
	 *
	 * @since 3.7
	 */
	private static void readInstallInfo() {
		fgInstallTimeMap.clear();
		IPath libPath = getDefault().getStateLocation();
		libPath = libPath.append(".install.xml"); //$NON-NLS-1$
		File file = libPath.toFile();
		if (file.exists()) {
			try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
				@SuppressWarnings("restriction")
				DocumentBuilder parser = org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE();
				parser.setErrorHandler(new DefaultHandler());
				Element root = parser.parse(new InputSource(stream)).getDocumentElement();
				if(root.getNodeName().equalsIgnoreCase("dirs")) { //$NON-NLS-1$
					NodeList nodes = root.getChildNodes();
					Node node = null;
					Element element = null;
					for (int i = 0; i < nodes.getLength(); i++) {
						node = nodes.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE) {
							element = (Element) node;
							if(element.getNodeName().equalsIgnoreCase("entry")) { //$NON-NLS-1$
								String loc = element.getAttribute("loc"); //$NON-NLS-1$
								String stamp = element.getAttribute("stamp"); //$NON-NLS-1$
								try {
									Long l = Long.valueOf(stamp);
									fgInstallTimeMap.put(loc, l);
								}
								catch(NumberFormatException nfe) {
								//do nothing
								}
							}
						}
					}
				}
			} catch (IOException | ParserConfigurationException | SAXException e) {
				log(e);
			}
		}
	}

	/**
	 * Writes out the mappings of SDK install time stamps to disk. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information.
	 *
	 * @since 3.7
	 */
	private static void writeInstallInfo() {
		if (!fgInstallTimeMap.isEmpty()) {
			try {
				Document doc = DebugPlugin.newDocument();
				Element root = doc.createElement("dirs");    //$NON-NLS-1$
				doc.appendChild(root);
				Entry<String, Long> entry = null;
				Element e = null;
				String key = null;
				for(Iterator<Entry<String, Long>> i = fgInstallTimeMap.entrySet().iterator(); i.hasNext();) {
					entry = i.next();
					key = entry.getKey();
					if (fgLibraryInfoMap.isEmpty() || fgLibraryInfoMap.containsKey(key)) {
						//only persist the info if the library map also has info OR is null - prevent persisting deleted JRE information
						e = doc.createElement("entry"); //$NON-NLS-1$
						root.appendChild(e);
						e.setAttribute("loc", key); //$NON-NLS-1$
						e.setAttribute("stamp", entry.getValue().toString()); //$NON-NLS-1$
					}
				}
				String xml = DebugPlugin.serializeDocument(doc);
				IPath libPath = getDefault().getStateLocation();
				libPath = libPath.append(".install.xml"); //$NON-NLS-1$
				File file = libPath.toFile();
				if (!file.exists()) {
					file.createNewFile();
				}
				try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
					stream.write(xml.getBytes(StandardCharsets.UTF_8));
				}
			} catch (IOException | CoreException e) {
				log(e);
			}
		}
	}

	/**
	 * Returns paths stored in XML
	 * @param lib the library path in {@link Element} form
	 * @param pathType the type of the path
	 * @return paths stored in XML
	 */
	private static String[] getPathsFromXML(Element lib, String pathType) {
		List<String> paths = new ArrayList<>();
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
		return paths.toArray(new String[paths.size()]);
	}

	@Override
	public void launchesRemoved(ILaunch[] launches) {
		ArchiveSourceLocation.closeArchives();
	}

	@Override
	public void launchesAdded(ILaunch[] launches) {
	}

	@Override
	public void launchesChanged(ILaunch[] launches) {
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.TERMINATE) {
				Object source = event.getSource();
				if (source instanceof IDebugTarget || source instanceof IProcess) {
					ArchiveSourceLocation.closeArchives();
					IProcess process;
					if (source instanceof IProcess) {
						process = (IProcess) source;
					} else {
						process = ((IDebugTarget) source).getProcess();
					}
					if (process != null) {
						deleteProcessTempFiles(process);
					}
				}
			}
		}
	}

	private void deleteProcessTempFiles(IProcess process) {
		String tempFiles = process.getAttribute(ATTR_LAUNCH_TEMP_FILES);
		if (tempFiles == null) {
			return;
		}
		// we only delete files starting with LAUNCH_TEMP_FILE_PREFIX²
		Arrays.stream(tempFiles.split(File.pathSeparator)).map(path -> new File(path)).filter(file -> isValidProcessTempFile(file)).forEach(file -> file.delete());
	}

	private boolean isValidProcessTempFile(File file) {
		return file.getName().startsWith(LAUNCH_TEMP_FILE_PREFIX);
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
				@SuppressWarnings("restriction")
				DocumentBuilder p = org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE();
				p.setErrorHandler(new DefaultHandler());
				fgXMLParser = p;
			} catch (ParserConfigurationException | FactoryConfigurationError e) {
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
	 * @throws CoreException if an exception occurs
	 */
	protected static void abort(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, message, exception);
		throw new CoreException(status);
	}

	/**
	 * Compares two URL for equality, but do not connect to do DNS resolution
	 *
	 * @param url1 a given URL
	 * @param url2 another given URL to compare to url1
	 *
	 * @since 3.5
	 * @return <code>true</code> if the URLs are equal, <code>false</code> otherwise
	 */
	public static boolean sameURL(URL url1, URL url2) {
		if (url1 == url2) {
			return true;
		}
		if (url1 == null ^ url2 == null) {
			return false;
		}
		// check if URL are file: URL as we may have two URL pointing to the same doc location
		// but with different representation - (i.e. file:/C;/ and file:C:/)
		final boolean isFile1 = "file".equalsIgnoreCase(url1.getProtocol());//$NON-NLS-1$
		final boolean isFile2 = "file".equalsIgnoreCase(url2.getProtocol());//$NON-NLS-1$
		if (isFile1 && isFile2) {
			File file1 = new File(url1.getFile());
			File file2 = new File(url2.getFile());
			return file1.equals(file2);
		}
		// URL1 XOR URL2 is a file, return false. (They either both need to be files, or neither)
		if (isFile1 ^ isFile2) {
			return false;
		}
		return getExternalForm(url1).equals(getExternalForm(url2));
	}

	/**
	 * Gets the external form of this URL. In particular, it trims any white space,
	 * removes a trailing slash and creates a lower case string.
	 * @param url the URL to get the {@link String} value of
	 * @return the lower-case {@link String} form of the given URL
	 */
	private static String getExternalForm(URL url) {
		String externalForm = url.toExternalForm();
		if (externalForm == null)
		 {
			return ""; //$NON-NLS-1$
		}
		externalForm = externalForm.trim();
		if (externalForm.endsWith("/")) { //$NON-NLS-1$
			// Remove the trailing slash
			externalForm = externalForm.substring(0, externalForm.length() - 1);
		}
		return externalForm.toLowerCase();

	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		String property = event.getKey();
		if (property.equals(JavaRuntime.PREF_VM_XML)) {
			if (!isIgnoreVMDefPropertyChangeEvents()) {
				processVMPrefsChanged((String)event.getOldValue(), (String)event.getNewValue());
			}
		}
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_JRE_CONTAINER = DEBUG && options.getBooleanOption(DEBUG_JRE_CONTAINER_FLAG, false);
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 * @since 3.8
	 */
	public static void trace(String option, String message, Throwable throwable) {
		System.out.println(message);
		if(fgDebugTrace != null) {
			fgDebugTrace.trace(option, message, throwable);
		}
	}

	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 *
	 * @param message the message or <code>null</code>
	 * @since 3.8
	 */
	public static void trace(String message) {
		trace(null, message, null);
	}

}