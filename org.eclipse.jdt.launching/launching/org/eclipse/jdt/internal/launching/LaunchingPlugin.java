/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.launching;

import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;

public class LaunchingPlugin extends Plugin {
	
	public static final String PLUGIN_ID= "org.eclipse.jdt.launching"; //$NON-NLS-1$
	
	private static LaunchingPlugin fgLaunchingPlugin;
	
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
}