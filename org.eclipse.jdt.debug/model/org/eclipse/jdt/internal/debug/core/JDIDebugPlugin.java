package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;

/**
 * The plugin class for the JDI Debug Model plugin.
 */

public class JDIDebugPlugin extends Plugin {
	
	protected static JDIDebugPlugin fgPlugin;
	
	protected JavaHotCodeReplaceManager fJavaHCRMgr;
	
	public static JDIDebugPlugin getDefault() {
		return fgPlugin;
	}
		
	public JDIDebugPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgPlugin = this;
	}

	/**
	 * Resets the install count attribute on all breakpoint markers
	 * to "0".  Resets the expired attribute on all breakpoint markers to false.
	 * If a workbench crashes, the attributes could have been persisted
	 * in an incorrect state. Instantiates and starts up the hot code replace
	 * manager.
	 */
	public void startup() throws CoreException {
		
		fJavaHCRMgr= new JavaHotCodeReplaceManager();
		fJavaHCRMgr.startup();		

		IMarker[] breakpoints= null;
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		try {
			breakpoints= root.findMarkers(IJavaDebugConstants.JAVA_LINE_BREAKPOINT, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			DebugJavaUtils.logError(e);
			return;
		}
		
		if (breakpoints == null) {
			return;
		}
		
		for (int i = 0; i < breakpoints.length; i++) {
			IMarker breakpoint = breakpoints[i];
			DebugJavaUtils.configureBreakpointAtStartup(breakpoint);
		}
	}

	/**
	 * Shutdown the HCR mgr and the debug targets.
	 */
	public void shutdown() throws CoreException {
		fJavaHCRMgr.shutdown();
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		IDebugTarget[] targets= launchManager.getDebugTargets();
		for (int i= 0 ; i < targets.length; i++) {
			IDebugTarget target= targets[i];
			if (target instanceof JDIDebugTarget) {
				((JDIDebugTarget)target).shutdown();
			}
		}
		fgPlugin = null;
		super.shutdown();
	}
}