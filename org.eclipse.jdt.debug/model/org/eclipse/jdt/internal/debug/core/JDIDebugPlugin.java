package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IDebugTarget;
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

		IMarker[] markers= null;
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		try {
			markers= root.findMarkers(IJavaDebugConstants.JAVA_LINE_BREAKPOINT, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			DebugJavaUtils.logError(e);
			return;
		}
		if (markers == null) {
			return;
		}

		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		for (int i= 0; i < markers.length; i++) {
			JavaBreakpoint breakpoint= (JavaBreakpoint)manager.loadMarker(markers[i]);
			if (breakpoint != null) {
				breakpoint.configureBreakpointAtStartup();
			}
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