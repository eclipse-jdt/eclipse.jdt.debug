package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.hcr.JavaHotCodeReplaceManager;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

/**
 * The plugin class for the JDI Debug Model plug-in.
 */

public class JDIDebugPlugin extends Plugin {
	
	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 120;
	
	private static JDIDebugPlugin fgPlugin;
	
	private JavaHotCodeReplaceManager fJavaHCRMgr;
	
	/**
	 * Breakpoint listener list.
	 */
	private ListenerList fBreakpointListeners = null;
	
	/**
	 * Breakpoint notification types
	 */
	private static final int ADDED = 1;
	private static final int INSTALLED = 2;
	private static final int REMOVED = 3;
	
	/**
	 * Whether this plug-in is in trace mode.
	 * Extra messages are logged in trace mode.
	 */
	private boolean fTrace = false;
	
	/**
	 * Returns whether the debug UI plug-in is in trace
	 * mode.
	 * 
	 * @return whether the debug UI plug-in is in trace
	 *  mode
	 */
	public boolean isTraceMode() {
		return fTrace;
	}
	
	/**
	 * Logs the given message if in trace mode.
	 * 
	 * @param String message to log
	 */
	public static void logTraceMessage(String message) {
		if (getDefault().isTraceMode()) {
			IStatus s = new Status(IStatus.WARNING, JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), INTERNAL_ERROR, message, null);
			getDefault().getLog().log(s);
		}
	}	
	
	/**
	 * Return the singleton instance of the JDI Debug Model plug-in.  
	 * @return the singleton instance of JDIDebugPlugin
	 */
	public static JDIDebugPlugin getDefault() {
		return fgPlugin;
	}
		
	public JDIDebugPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgPlugin = this;
	}
	
	/**
	 * Instantiates and starts up the hot code replace
	 * manager.
	 */
	public void startup() throws CoreException {
		fJavaHCRMgr= JavaHotCodeReplaceManager.getDefault();
		fBreakpointListeners = new ListenerList(5);
	}
	
	public void addHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		fJavaHCRMgr.addHotCodeReplaceListener(listener);
	}
	
	public void removeHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		fJavaHCRMgr.removeHotCodeReplaceListener(listener);
	}

	/**
	 * Shutdown the HCR mgr and the Java debug targets.
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
		fBreakpointListeners = null;

		fgPlugin = null;
		super.shutdown();
	}
	
	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		if (getDefault().isDebugging()) {
			Throwable t = e;
			if (e instanceof DebugException) {
				DebugException de = (DebugException)e;
				IStatus status = de.getStatus();
				if (status.getException() != null) {
					t = status.getException();
				}
			}
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			log(new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), INTERNAL_ERROR, "Internal error logged from JDI Debug: ", t));  //$NON-NLS-1$		
		}
	}
	
	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Adds the given breakpoint listener to the JDI debug model.
	 * 
	 * @param listener breakpoint listener
	 */
	public void addJavaBreakpointListener(IJavaBreakpointListener listener) {
		fBreakpointListeners.add(listener);
	}	

	/**
	 * Removes the given breakpoint listener from the JDI debug model.
	 * 
	 * @param listener breakpoint listener
	 */
	public void removeJavaBreakpointListener(IJavaBreakpointListener listener) {
		fBreakpointListeners.remove(listener);
	}		
	
	/**
	 * Notifies listeners that the given breakpoint has been added.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void fireBreakpointAdded(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		notify(target, breakpoint, ADDED);
	}
	
	/**
	 * Notifies listeners that the given breakpoint has been installed.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void fireBreakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		notify(target, breakpoint, INSTALLED);
	}	
	
	/**
	 * Notifies listeners that the given breakpoint has been removed.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void fireBreakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		notify(target, breakpoint, REMOVED);
	}
		
	/**
	 * Notifies listeners of the given addition, install, or
	 * remove.
	 * 
	 * @param target debug target
	 * @param breakpoint the associated breakpoint
	 * @param kind one of ADDED, REMOVED, INSTALLED
	 */
	protected void notify(IJavaDebugTarget target, IJavaBreakpoint breakpoint, int kind) {
		Object[] listeners = fBreakpointListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IJavaBreakpointListener jbpl = (IJavaBreakpointListener)listeners[i];
			switch (kind) {
				case ADDED:
					jbpl.breakpointAdded(target, breakpoint);
					break;
				case INSTALLED:
					jbpl.breakpointInstalled(target, breakpoint);
					break;
				case REMOVED:
					jbpl.breakpointRemoved(target, breakpoint);
					break;					
			}
		}
	}
	
	/**
	 * Notifies listeners that the given breakpoint has been hit.
	 * Returns whether the thread should suspend.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public boolean fireBreakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		Object[] listeners = fBreakpointListeners.getListeners();
		boolean suspend = listeners.length == 0;
		for (int i = 0; i < listeners.length; i++) {
			IJavaBreakpointListener jbpl = (IJavaBreakpointListener)listeners[i];
			suspend = suspend | jbpl.breakpointHit(thread, breakpoint);
		}	
		return suspend;
	}
	
}