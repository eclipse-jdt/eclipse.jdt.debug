/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.launching;

import java.util.HashMap;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;

public class LaunchingPlugin extends Plugin {
	
	public static final String PLUGIN_ID= "org.eclipse.jdt.launching"; //$NON-NLS-1$
	
	/**
	 * Identifier for 'vmConnectors' extension point
	 */
	public static final String ID_EXTENSION_POINT_VM_CONNECTORS = PLUGIN_ID + ".vmConnectors"; //$NON-NLS-1$
	
	private static LaunchingPlugin fgLaunchingPlugin;
	
	private HashMap fVMConnectors = null;
	
	public LaunchingPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgLaunchingPlugin= this;
	}

	public static LaunchingPlugin getPlugin() {
		return fgLaunchingPlugin;
	}
	
	public static void log(IStatus status) {
		getPlugin().getLog().log(status);
	}
	
	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}	
		
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
	}	
	
	/**
	 * Clears zip file cache.
	 * Shutdown the launch config helper.
	 * 
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {
		ArchiveSourceLocation.shutdown();
	}
		
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		
		//exclude launch configurations from being copied to the output directory
		Hashtable optionsMap = JavaCore.getOptions();
		String filters= (String)optionsMap.get("org.eclipse.jdt.core.builder.resourceCopyExclusionFilters"); //$NON-NLS-1$
		filters= filters + ",*." + ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION; //$NON-NLS-1$
		optionsMap.put("org.eclipse.jdt.core.builder.resourceCopyExclusionFilters", filters);  //$NON-NLS-1$
		JavaCore.setOptions(optionsMap);
	}
	
	/**
	 * Returns the VM connetor with the specified id, or <code>null</code>
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
		MultiStatus status= new MultiStatus(PLUGIN_ID, IStatus.OK, LaunchingMessages.getString("LaunchingPlugin.Exception_occurred_reading_vmConnectors_extensions_1"), null); //$NON-NLS-1$
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
}

