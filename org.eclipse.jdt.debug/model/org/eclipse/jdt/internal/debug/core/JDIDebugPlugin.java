package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 * The plugin class for the JDI Debug Model plug-in.
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
	 * Instantiates and starts up the hot code replace
	 * manager.  Also initializes step filter information.
	 */
	public void startup() throws CoreException {
		fJavaHCRMgr= new JavaHotCodeReplaceManager();
		fJavaHCRMgr.startup();		

		JDIDebugModel.setupStepFilterState();
	}
	

	/**
	 * Shutdown the HCR mgr and the Java debug targets.
	 * Save the current in-memory step filter state.
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
		JDIDebugModel.saveStepFilterState();

		fgPlugin = null;
		super.shutdown();
	}
	
	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		Throwable t = e;
		if (getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			System.out.println("Internal error logged from JDI debug model: "); //$NON-NLS-1$
			if (e instanceof DebugException) {
				DebugException de = (DebugException)e;
				IStatus status = de.getStatus();
				if (status.getException() != null) {
					t = status.getException();
				}
			}
			t.printStackTrace();
			System.out.println();
		}
	}
}