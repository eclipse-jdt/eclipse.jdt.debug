/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.internal.debug.core.hcr.JavaHotCodeReplaceManager;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.eval.JavaEvaluationEngineManager;
import org.osgi.framework.BundleContext;

import com.sun.jdi.VirtualMachineManager;

/**
 * The plugin class for the JDI Debug Model plug-in.
 */

public class JDIDebugPlugin extends Plugin implements Preferences.IPropertyChangeListener {
	
	public static final String EXTENSION_POINT_JAVA_LOGICAL_STRUCTURES= "javaLogicalStructures"; //$NON-NLS-1$

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 120;
	
	private static JDIDebugPlugin fgPlugin;
	
	/**
	 * Breakpoint listener list.
	 */
	private ListenerList fBreakpointListeners = null;
	
	/**
	 * Breakpoint notification types
	 */
	private static final int ADDING = 1;
	private static final int INSTALLED = 2;
	private static final int REMOVED = 3;
	private static final int COMPILATION_ERRORS = 4;
	private static final int RUNTIME_EXCEPTION = 5;
	
	/**
	 * Whether this plug-in is in trace mode.
	 * Extra messages are logged in trace mode.
	 */
	private boolean fTrace = false;
	
	/**
	 * Detected (speculated) JDI interface version
	 */
	private static int[] fJDIVersion = null;
	
	private JavaEvaluationEngineManager fEvaluationEngineManager;

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
			IStatus s = new Status(IStatus.WARNING, JDIDebugPlugin.getUniqueIdentifier(), INTERNAL_ERROR, message, null);
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
	
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		// TODO review this change.  Unclear how the plugin id could ever be different
		// should likely just be a constant reference.
		return "org.eclipse.jdt.debug"; //$NON-NLS-1$
	}
	
	/**
	 * Returns the detected version of JDI support. This
	 * is intended to distinguish between clients that support
	 * JDI 1.4 methods like hot code replace.
	 * 
	 * @return an array of version numbers, major followed by minor
	 * @since 2.1
	 */
	public static int[] getJDIVersion() {
		if (fJDIVersion == null) {
			fJDIVersion = new int[2];
			VirtualMachineManager mgr = Bootstrap.virtualMachineManager();
			fJDIVersion[0] = mgr.majorInterfaceVersion();
			fJDIVersion[1] = mgr.minorInterfaceVersion();
		}
		return fJDIVersion;
	}
	
	/**
	 * Reutrns if the JDI version being used is greater than or equal to the
	 * given version (major, minor).
	 * 
	 * @param version
	 * @return boolean
	 */
	public static boolean isJdiVersionGreaterThanOrEqual(int[] version) {
		int[] runningVersion = getJDIVersion();
		return runningVersion[0] > version[0] || (runningVersion[0] == version[0] && runningVersion[1] >= version[1]);
	}
		
	public JDIDebugPlugin() {
		super();	
		fgPlugin = this;
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		JavaHotCodeReplaceManager.getDefault().startup();
		fBreakpointListeners = new ListenerList(5);
		fEvaluationEngineManager= new JavaEvaluationEngineManager();
	}
	
	/**
	 * Adds the given hot code replace listener to the collection of listeners
	 * that will be notified by the hot code replace manager in this plugin.
	 */
	public void addHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		JavaHotCodeReplaceManager.getDefault().addHotCodeReplaceListener(listener);
	}

	/**
	 * Removes the given hot code replace listener from the collection of listeners
	 * that will be notified by the hot code replace manager in this plugin.
	 */	
	public void removeHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		JavaHotCodeReplaceManager.getDefault().removeHotCodeReplaceListener(listener);
	}

	/**
	 * Shutdown the HCR mgr and the Java debug targets.
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)@see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			getPluginPreferences().removePropertyChangeListener(this); //added in the preference initializer
			savePluginPreferences();
			JavaHotCodeReplaceManager.getDefault().shutdown();
			fEvaluationEngineManager.dispose();
			ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
			IDebugTarget[] targets= launchManager.getDebugTargets();
			for (int i= 0 ; i < targets.length; i++) {
				IDebugTarget target= targets[i];
				if (target instanceof JDIDebugTarget) {
					((JDIDebugTarget)target).shutdown();
				}
			}
			fBreakpointListeners = null;
		} finally {
			fgPlugin = null;
			super.stop(context);
		}
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log(Throwable t) {
		Throwable top= t;
		if (t instanceof DebugException) {
			DebugException de = (DebugException)t;
			IStatus status = de.getStatus();
			if (status.getException() != null) {
				top = status.getException();
			}
		} 
		// this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR, "Internal error logged from JDI Debug: ", top));  //$NON-NLS-1$		
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
	 * @see IJavaBreakpointListener#breakpointHasRuntimeException(IJavaLineBreakpoint, DebugException)
	 */
	public void fireBreakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
		getBreakpointNotifier().notify(null, breakpoint, COMPILATION_ERRORS, errors, null);
	}
	
	/**
	 * @see IJavaBreakpointListener#breakpointHasCompilationErrors(IJavaLineBreakpoint, Message[])
	 */
	public void fireBreakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
		getBreakpointNotifier().notify(null, breakpoint, RUNTIME_EXCEPTION, null, exception);
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
	 * Notifies listeners that the given breakpoint is about to be
	 * added.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void fireBreakpointAdding(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, ADDING, null, null);
	}
	
	/**
	 * Notifies listeners that the given breakpoint has been installed.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void fireBreakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, INSTALLED, null, null);
	}	
	
	/**
	 * Notifies listeners that the given breakpoint has been removed.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public void fireBreakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, REMOVED, null, null);
	}
	
	/**
	 * Notifies listeners that the given breakpoint has been hit.
	 * Returns whether the thread should suspend.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 */
	public boolean fireBreakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		return getHitNotifier().notifyHit(thread, breakpoint);
	}
	
	/**
	 * Notifies listeners that the given breakpoint is about to be installed
	 * in the given type. Returns whether the breakpoint should be
	 * installed.
	 * 
	 * @param target Java debug target
	 * @param breakpoint Java breakpoint
	 * @param type the type the breakpoint is about to be installed in
	 * @return whether the breakpoint should be installed
	 */
	public boolean fireInstalling(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		return getInstallingNotifier().notifyInstalling(target, breakpoint, type);
	}	
	
	/**
	 * Save preferences and update all debug targets when the timeout changes.
	 * 
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(JDIDebugModel.PREF_REQUEST_TIMEOUT)) {
			savePluginPreferences();
			int value = getPluginPreferences().getInt(JDIDebugModel.PREF_REQUEST_TIMEOUT);
			IDebugTarget[] targets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
			for (int i = 0; i < targets.length; i++) {
				if (targets[i] instanceof IJavaDebugTarget) {
					((IJavaDebugTarget)targets[i]).setRequestTimeout(value);
				}
			}
		}
	}
	
	private BreakpointNotifier getBreakpointNotifier() {
		return new BreakpointNotifier();
	}

	class BreakpointNotifier implements ISafeRunnable {
		
		private IJavaDebugTarget fTarget;
		private IJavaBreakpoint fBreakpoint;
		private int fKind;
		private Message[] fErrors;
		private DebugException fException;
		private IJavaBreakpointListener fListener;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fKind) {
				case ADDING:
					fListener.addingBreakpoint(fTarget, fBreakpoint);
					break;
				case INSTALLED:
					fListener.breakpointInstalled(fTarget, fBreakpoint);
					break;
				case REMOVED:
					fListener.breakpointRemoved(fTarget, fBreakpoint);
					break;		
				case COMPILATION_ERRORS:
					fListener.breakpointHasCompilationErrors((IJavaLineBreakpoint)fBreakpoint, fErrors);
					break;
				case RUNTIME_EXCEPTION:
					fListener.breakpointHasRuntimeException((IJavaLineBreakpoint)fBreakpoint, fException);
					break;	
			}			
		}

		/**
		 * Notifies listeners of the given addition, install, or
		 * remove.
		 * 
		 * @param target debug target
		 * @param breakpoint the associated breakpoint
		 * @param kind one of ADDED, REMOVED, INSTALLED
		 * @param errors associated errors, or <code>null</code> if none
		 * @param exception associated exception, or <code>null</code> if none
		 */
		public void notify(IJavaDebugTarget target, IJavaBreakpoint breakpoint, int kind, Message[] errors, DebugException exception) {
			fTarget = target;
			fBreakpoint = breakpoint;
			fKind = kind;
			fErrors = errors;
			fException = exception;
			Object[] listeners = fBreakpointListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				fListener = (IJavaBreakpointListener)listeners[i];
				Platform.run(this);
			}
			fTarget = null;
			fBreakpoint = null;
			fErrors = null;
			fException = null;
			fListener = null;
		}
	}
	
	private InstallingNotifier getInstallingNotifier() {
		return new InstallingNotifier();
	}
		
	class InstallingNotifier implements ISafeRunnable {
		
		private IJavaDebugTarget fTarget;
		private IJavaBreakpoint fBreakpoint;
		private IJavaType fType;
		private IJavaBreakpointListener fListener;
		private int fInstall;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			fInstall = fInstall | fListener.installingBreakpoint(fTarget, fBreakpoint, fType);		
		}
		
		private void dispose() {
			fTarget = null;
			fBreakpoint = null;
			fType = null;
			fListener = null;
		}

		/**
		 * Notifies listeners that the given breakpoint is about to be installed
		 * in the given type. Returns whether the breakpoint should be
		 * installed.
		 * 
		 * @param target Java debug target
		 * @param breakpoint Java breakpoint
		 * @param type the type the breakpoint is about to be installed in
		 * @return whether the breakpoint should be installed
		 */
		public boolean notifyInstalling(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
			fTarget = target;
			fBreakpoint = breakpoint;
			fType = type;
			fInstall = IJavaBreakpointListener.DONT_CARE;
			Object[] listeners = fBreakpointListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				fListener = (IJavaBreakpointListener)listeners[i];
				Platform.run(this);
			}
			dispose();
			// install if any listener voted to install, or if no one voted to not install
			return (fInstall & IJavaBreakpointListener.INSTALL) > 0 ||
				(fInstall & IJavaBreakpointListener.DONT_INSTALL) == 0;
		}
	}	
	
	private HitNotifier getHitNotifier() {
		return new HitNotifier();
	}
		
	class HitNotifier implements ISafeRunnable {
		
		private IJavaThread fThread;
		private IJavaBreakpoint fBreakpoint;
		private IJavaBreakpointListener fListener;
		private int fSuspend;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			fSuspend = fSuspend | fListener.breakpointHit(fThread, fBreakpoint);
		}

		/**
		 * Notifies listeners that the given breakpoint has been hit.
		 * Returns whether the thread should suspend.
		 * 
		 * @param thread thread in which the breakpoint was hit
		 * @param breakpoint Java breakpoint
		 * @return whether the thread should suspend
		 */
		public boolean notifyHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			fThread = thread;
			fBreakpoint = breakpoint;
			Object[] listeners = fBreakpointListeners.getListeners();
			fSuspend = IJavaBreakpointListener.DONT_CARE;
			for (int i = 0; i < listeners.length; i++) {
				fListener = (IJavaBreakpointListener)listeners[i];
				Platform.run(this);
			}
			fThread = null;
			fBreakpoint = null;
			fListener = null;
			// Suspend if any listener voted to suspend or no one voted "don't suspend"
			return (fSuspend & IJavaBreakpointListener.SUSPEND) > 0 ||
					(fSuspend & IJavaBreakpointListener.DONT_SUSPEND) == 0;
		}
	}
	
	/**
	 * Returns an evaluation engine for the given project in the given debug target.
	 * 
	 * @see JavaEvaluationEngineManager#getEvaluationEngine(IJavaProject, IJavaDebugTarget)
	 * 
	 * @param project java project
	 * @param target the debug target
	 * @return evalaution engine
	 */
	public IAstEvaluationEngine getEvaluationEngine(IJavaProject project, IJavaDebugTarget target) {
		return fEvaluationEngineManager.getEvaluationEngine(project, target);
	}
	
}
