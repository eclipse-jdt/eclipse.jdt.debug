/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
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
import org.eclipse.jdt.internal.debug.core.breakpoints.BreakpointListenerManager;
import org.eclipse.jdt.internal.debug.core.hcr.JavaHotCodeReplaceManager;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.sun.jdi.VirtualMachineManager;

/**
 * The plug-in class for the JDI Debug Model plug-in.
 */
public class JDIDebugPlugin extends Plugin implements IEclipsePreferences.IPreferenceChangeListener {

	/**
	 * integer preference controlling if we should, by default, suspend the VM
	 * instead of the thread
	 * 
	 * @since 3.2
	 */
	public static final String PREF_DEFAULT_BREAKPOINT_SUSPEND_POLICY = JDIDebugPlugin
			.getUniqueIdentifier() + ".default_breakpoint_suspend_policy"; //$NON-NLS-1$

	/**
	 * integer preference controlling which default suspend option to set on new
	 * watchpoints
	 * 
	 * @since 3.3.1
	 */
	public static final String PREF_DEFAULT_WATCHPOINT_SUSPEND_POLICY = JDIDebugPlugin
			.getUniqueIdentifier() + "default_watchpoint_suspend_policy"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling if references should be displayed as
	 * variables in the variables and expressions view
	 * 
	 * @since 3.3
	 */
	public static final String PREF_SHOW_REFERENCES_IN_VAR_VIEW = JDIDebugPlugin
			.getUniqueIdentifier() + ".show_references_in_var_view"; //$NON-NLS-1$

	/**
	 * Integer preference determining the maximum number of references that
	 * should be returned to the user when displaying reference information
	 * 
	 * @since 3.3
	 */
	public static final String PREF_ALL_REFERENCES_MAX_COUNT = JDIDebugPlugin
			.getUniqueIdentifier() + "._all_references_max_count"; //$NON-NLS-1$

	/**
	 * Integer preference determining the maximum number of instances that
	 * should be returned to the user when displaying instance information
	 * 
	 * @since 3.3
	 */
	public static final String PREF_ALL_INSTANCES_MAX_COUNT = JDIDebugPlugin
			.getUniqueIdentifier() + ".all_instances_max_count"; //$NON-NLS-1$

	/**
	 * Extension point for java logical structures.
	 * 
	 * @since 3.1
	 */
	public static final String EXTENSION_POINT_JAVA_LOGICAL_STRUCTURES = "javaLogicalStructures"; //$NON-NLS-1$

	/**
	 * Extension point for java breakpoint action delegates.
	 * 
	 * @since 3.5
	 */
	public static final String EXTENSION_POINT_JAVA_BREAKPOINT_LISTENERS = "breakpointListeners"; //$NON-NLS-1$

	/**
	 * Status code indicating an unexpected error.
	 */
	public static final int ERROR = 120;

	/**
	 * Status code indicating an unexpected internal error. Internal errors
	 * should never be displayed to the user in dialogs or status text. Internal
	 * error messages are not translated.
	 */
	public static final int INTERNAL_ERROR = 125;

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
	 * Whether this plug-in is in trace mode. Extra messages are logged in trace
	 * mode.
	 */
	private boolean fTrace = false;

	/**
	 * Detected (speculated) JDI interface version
	 */
	private static int[] fJDIVersion = null;

	/**
	 * Status code used by the debug model to retrieve a thread to use for
	 * evaluations, via a status handler. A status handler is contributed by the
	 * Java debug UI. When not present, the debug model uses any suspended
	 * thread.
	 * 
	 * @since 3.0
	 */
	public static final int INFO_EVALUATION_THREAD = 110;

	/**
	 * Associated status to retrieve a thread.
	 */
	public static final IStatus STATUS_GET_EVALUATION_THREAD = new Status(
			IStatus.INFO, getUniqueIdentifier(), INFO_EVALUATION_THREAD,
			"Provides thread context for an evaluation", null); //$NON-NLS-1$	

	/**
	 * Status code used by the debug model to retrieve a frame to use for
	 * evaluations, via a status handler. A status handler is contributed by the
	 * Java debug UI. When not present, the debug model uses any suspended
	 * thread.
	 * 
	 * @since 3.0
	 */
	public static final int INFO_EVALUATION_STACK_FRAME = 111;

	/**
	 * Associated status to retrieve a stack frame.
	 */
	public static IStatus STATUS_GET_EVALUATION_FRAME = new Status(
			IStatus.INFO, getUniqueIdentifier(), INFO_EVALUATION_STACK_FRAME,
			"Provides thread context for an evaluation", null); //$NON-NLS-1$

	/**
	 * Manages breakpoint listener extensions
	 */
	private BreakpointListenerManager fJavaBreakpointManager;

	/**
	 * Returns whether the debug UI plug-in is in trace mode.
	 * 
	 * @return whether the debug UI plug-in is in trace mode
	 */
	public boolean isTraceMode() {
		return fTrace;
	}

	/**
	 * Logs the given message if in trace mode.
	 * 
	 * @param message the string to log
	 */
	public static void logTraceMessage(String message) {
		if (getDefault().isTraceMode()) {
			IStatus s = new Status(IStatus.WARNING,
					JDIDebugPlugin.getUniqueIdentifier(), INTERNAL_ERROR,
					message, null);
			getDefault().getLog().log(s);
		}
	}

	/**
	 * Return the singleton instance of the JDI Debug Model plug-in.
	 * 
	 * @return the singleton instance of JDIDebugPlugin
	 */
	public static JDIDebugPlugin getDefault() {
		return fgPlugin;
	}

	/**
	 * Convenience method which returns the unique identifier of this plug-in.
	 * <br><br>
	 * Value is: <code>org.eclipse.jdt.debug</code>
	 * @return the unique id of the JDT debug core plug-in
	 */
	public static String getUniqueIdentifier() {
		return "org.eclipse.jdt.debug"; //$NON-NLS-1$
	}

	/**
	 * Returns the detected version of JDI support. This is intended to
	 * distinguish between clients that support JDI 1.4 methods like hot code
	 * replace.
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
	 * Returns if the JDI version being used is greater than or equal to the
	 * given version (major, minor).
	 * 
	 * @param version the array of version number identifiers to compare
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

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		new JDIDebugOptions(context);
		ResourcesPlugin.getWorkspace().addSaveParticipant(getUniqueIdentifier(),
				new ISaveParticipant() {
					public void doneSaving(ISaveContext c) {
					}

					public void prepareToSave(ISaveContext c)
							throws CoreException {
					}

					public void rollback(ISaveContext c) {
					}

					public void saving(ISaveContext c) throws CoreException {
						IEclipsePreferences node = InstanceScope.INSTANCE.getNode(getUniqueIdentifier());
						if(node != null) {
							try {
								node.flush();
							} catch (BackingStoreException bse) {
								log(bse);
							}
						}
					}
				});
		JavaHotCodeReplaceManager.getDefault().startup();
		fBreakpointListeners = new ListenerList();
		fJavaBreakpointManager = new BreakpointListenerManager();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(getUniqueIdentifier());
		if(node != null) {
			node.addPreferenceChangeListener(this);
		}
	}

	/**
	 * Adds the given hot code replace listener to the collection of listeners
	 * that will be notified by the hot code replace manager in this plug-in.
	 * @param listener the listener to add
	 */
	public void addHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		JavaHotCodeReplaceManager.getDefault().addHotCodeReplaceListener(listener);
	}

	/**
	 * Removes the given hot code replace listener from the collection of
	 * listeners that will be notified by the hot code replace manager in this
	 * plug-in.
	 * @param listener the listener to remove
	 */
	public void removeHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		JavaHotCodeReplaceManager.getDefault().removeHotCodeReplaceListener(listener);
	}

	/**
	 * Shutdown the HCR manager and the Java debug targets.
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			IEclipsePreferences node = InstanceScope.INSTANCE.getNode(getUniqueIdentifier());
			if(node != null) {
				node.removePreferenceChangeListener(this);
			}
			JavaHotCodeReplaceManager.getDefault().shutdown();
			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			IDebugTarget[] targets = launchManager.getDebugTargets();
			for (IDebugTarget target : targets) {
				if (target instanceof JDIDebugTarget) {
					((JDIDebugTarget) target).shutdown();
				}
			}
			fBreakpointListeners = null;
			ResourcesPlugin.getWorkspace().removeSaveParticipant(getUniqueIdentifier());
		} finally {
			fgPlugin = null;
			super.stop(context);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(JDIDebugModel.PREF_REQUEST_TIMEOUT)) {
			int value = Platform.getPreferencesService().getInt(
					JDIDebugPlugin.getUniqueIdentifier(), 
					JDIDebugModel.PREF_REQUEST_TIMEOUT, 
					JDIDebugModel.DEF_REQUEST_TIMEOUT, 
					null);
			IDebugTarget[] targets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
			for (IDebugTarget target : targets) {
				if (target instanceof IJavaDebugTarget) {
					((IJavaDebugTarget) target).setRequestTimeout(value);
				}
			}
		}
	}
	
	/**
	 * Logs the specified {@link Throwable} with this plug-in's log.
	 * 
	 * @param t {@link Throwable} to log
	 */
	public static void log(Throwable t) {
		Throwable top = t;
		if (t instanceof CoreException) {
			CoreException de = (CoreException) t;
			IStatus status = de.getStatus();
			if (status.getException() != null) {
				top = status.getException();
			}
		}
		// this message is intentionally not internationalized, as an exception
		// may be due to the resource bundle itself
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR,
				"Internal error logged from JDI Debug: ", top)); //$NON-NLS-1$		
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * @param breakpoint the breakpoint with problems
	 * @param errors the error set to notify about
	 * @see IJavaBreakpointListener#breakpointHasRuntimeException(IJavaLineBreakpoint,
	 *      DebugException)
	 */
	public void fireBreakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
		getBreakpointNotifier().notify(null, breakpoint, COMPILATION_ERRORS, errors, null);
	}

	/**
	 * @param breakpoint the breakpoint with problems
	 * @param exception the exception to notify about
	 * @see IJavaBreakpointListener#breakpointHasCompilationErrors(IJavaLineBreakpoint, Message[])
	 */
	public void fireBreakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
		getBreakpointNotifier().notify(null, breakpoint, RUNTIME_EXCEPTION, null, exception);
	}

	/**
	 * Adds the given breakpoint listener to the JDI debug model.
	 * 
	 * @param listener
	 *            breakpoint listener
	 */
	public void addJavaBreakpointListener(IJavaBreakpointListener listener) {
		fBreakpointListeners.add(listener);
	}

	/**
	 * Removes the given breakpoint listener from the JDI debug model.
	 * 
	 * @param listener
	 *            breakpoint listener
	 */
	public void removeJavaBreakpointListener(IJavaBreakpointListener listener) {
		fBreakpointListeners.remove(listener);
	}

	/**
	 * Notifies listeners that the given breakpoint is about to be added.
	 * 
	 * @param target
	 *            Java debug target
	 * @param breakpoint
	 *            Java breakpoint
	 */
	public void fireBreakpointAdding(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, ADDING, null, null);
	}

	/**
	 * Notifies listeners that the given breakpoint has been installed.
	 * 
	 * @param target
	 *            Java debug target
	 * @param breakpoint
	 *            Java breakpoint
	 */
	public void fireBreakpointInstalled(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, INSTALLED, null,
				null);
	}

	/**
	 * Notifies listeners that the given breakpoint has been removed.
	 * 
	 * @param target
	 *            Java debug target
	 * @param breakpoint
	 *            Java breakpoint
	 */
	public void fireBreakpointRemoved(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
		getBreakpointNotifier().notify(target, breakpoint, REMOVED, null, null);
	}

	/**
	 * Notifies listeners that the given breakpoint has been hit. Returns
	 * whether the thread should suspend.
	 * 
	 * @param thread the current thread context
	 * @param breakpoint Java breakpoint
	 * @return if the thread should suspend
	 */
	public boolean fireBreakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		return getHitNotifier().notifyHit(thread, breakpoint);
	}

	/**
	 * Notifies listeners that the given breakpoint is about to be installed in
	 * the given type. Returns whether the breakpoint should be installed.
	 * 
	 * @param target
	 *            Java debug target
	 * @param breakpoint
	 *            Java breakpoint
	 * @param type
	 *            the type the breakpoint is about to be installed in
	 * @return whether the breakpoint should be installed
	 */
	public boolean fireInstalling(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint, IJavaType type) {
		return getInstallingNotifier().notifyInstalling(target, breakpoint,
				type);
	}

	private BreakpointNotifier getBreakpointNotifier() {
		return new BreakpointNotifier();
	}

	abstract class AbstractNotifier implements ISafeRunnable {

		private IJavaBreakpoint fBreakpoint;
		private IJavaBreakpointListener fListener;

		/**
		 * Iterates through listeners calling this notifier's safe runnable.
		 * @param breakpoint the breakpoint to notify about
		 */
		protected void notifyListeners(IJavaBreakpoint breakpoint) {
			fBreakpoint = breakpoint;
			String[] ids = null;
			try {
				ids = breakpoint.getBreakpointListeners();
			} catch (CoreException e) {
				JDIDebugPlugin.log(e);
			}
			// breakpoint specific listener extensions
			if (ids != null && ids.length > 0) {
				for (String id : ids) {
					fListener = fJavaBreakpointManager
							.getBreakpointListener(id);
					if (fListener != null) {
						SafeRunner.run(this);
					}
				}
			}
			// global listener extensions
			IJavaBreakpointListener[] global = fJavaBreakpointManager
					.getGlobalListeners();
			if (global.length > 0) {
				for (IJavaBreakpointListener element : global) {
					fListener = element;
					SafeRunner.run(this);
				}
			}
			// programmatic global listeners
			Object[] listeners = fBreakpointListeners.getListeners();
			for (Object listener : listeners) {
				fListener = (IJavaBreakpointListener) listener;
				SafeRunner.run(this);
			}
			fBreakpoint = null;
			fListener = null;
		}

		/**
		 * Returns the breakpoint for which notification is proceeding or
		 * <code>null</code> if not in notification.
		 * 
		 * @return breakpoint or <code>null</code>
		 */
		protected IJavaBreakpoint getBreakpoint() {
			return fBreakpoint;
		}

		/**
		 * Returns the listener for which notification is proceeding or
		 * <code>null</code> if not in notification loop.
		 * 
		 * @return breakpoint listener or <code>null</code>
		 */
		protected IJavaBreakpointListener getListener() {
			return fListener;
		}
	}

	class BreakpointNotifier extends AbstractNotifier {

		private IJavaDebugTarget fTarget;
		private int fKind;
		private Message[] fErrors;
		private DebugException fException;

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
				getListener().addingBreakpoint(fTarget, getBreakpoint());
				break;
			case INSTALLED:
				getListener().breakpointInstalled(fTarget, getBreakpoint());
				break;
			case REMOVED:
				getListener().breakpointRemoved(fTarget, getBreakpoint());
				break;
			case COMPILATION_ERRORS:
				getListener().breakpointHasCompilationErrors(
						(IJavaLineBreakpoint) getBreakpoint(), fErrors);
				break;
			case RUNTIME_EXCEPTION:
				getListener().breakpointHasRuntimeException(
						(IJavaLineBreakpoint) getBreakpoint(), fException);
				break;
			}
		}

		/**
		 * Notifies listeners of the given addition, install, or remove.
		 * 
		 * @param target
		 *            debug target
		 * @param breakpoint
		 *            the associated breakpoint
		 * @param kind
		 *            one of ADDED, REMOVED, INSTALLED
		 * @param errors
		 *            associated errors, or <code>null</code> if none
		 * @param exception
		 *            associated exception, or <code>null</code> if none
		 */
		public void notify(IJavaDebugTarget target, IJavaBreakpoint breakpoint,
				int kind, Message[] errors, DebugException exception) {
			fTarget = target;
			fKind = kind;
			fErrors = errors;
			fException = exception;
			notifyListeners(breakpoint);
			fTarget = null;
			fErrors = null;
			fException = null;
		}
	}

	private InstallingNotifier getInstallingNotifier() {
		return new InstallingNotifier();
	}

	class InstallingNotifier extends AbstractNotifier {

		private IJavaDebugTarget fTarget;
		private IJavaType fType;
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
			fInstall = fInstall
					| getListener().installingBreakpoint(fTarget,
							getBreakpoint(), fType);
		}

		private void dispose() {
			fTarget = null;
			fType = null;
		}

		/**
		 * Notifies listeners that the given breakpoint is about to be installed
		 * in the given type. Returns whether the breakpoint should be
		 * installed.
		 * 
		 * @param target
		 *            Java debug target
		 * @param breakpoint
		 *            Java breakpoint
		 * @param type
		 *            the type the breakpoint is about to be installed in
		 * @return whether the breakpoint should be installed
		 */
		public boolean notifyInstalling(IJavaDebugTarget target,
				IJavaBreakpoint breakpoint, IJavaType type) {
			fTarget = target;
			fType = type;
			fInstall = IJavaBreakpointListener.DONT_CARE;
			notifyListeners(breakpoint);
			dispose();
			// install if any listener voted to install, or if no one voted to
			// not install
			return (fInstall & IJavaBreakpointListener.INSTALL) > 0
					|| (fInstall & IJavaBreakpointListener.DONT_INSTALL) == 0;
		}
	}

	private HitNotifier getHitNotifier() {
		return new HitNotifier();
	}

	class HitNotifier extends AbstractNotifier {

		private IJavaThread fThread;
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
			if (fThread instanceof JDIThread) {
				if (((JDIThread) fThread).hasClientRequestedSuspend()) {
					// abort notification to breakpoint listeners if a client
					// suspend
					// request is received
					fSuspend = fSuspend | IJavaBreakpointListener.SUSPEND;
					return;
				}
			}
			fSuspend = fSuspend
					| getListener().breakpointHit(fThread, getBreakpoint());
		}

		/**
		 * Notifies listeners that the given breakpoint has been hit. Returns
		 * whether the thread should suspend.
		 * 
		 * @param thread
		 *            thread in which the breakpoint was hit
		 * @param breakpoint
		 *            Java breakpoint
		 * @return whether the thread should suspend
		 */
		public boolean notifyHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			fThread = thread;
			fSuspend = IJavaBreakpointListener.DONT_CARE;
			notifyListeners(breakpoint);
			fThread = null;
			// Suspend if any listener voted to suspend or no one voted
			// "don't suspend"
			return (fSuspend & IJavaBreakpointListener.SUSPEND) > 0
					|| (fSuspend & IJavaBreakpointListener.DONT_SUSPEND) == 0;
		}
	}

	/**
	 * Returns an evaluation engine for the given project in the given debug
	 * target or <code>null</code> if target does not have a IJavaDebugTarget
	 * that is a JDIDebugTarget implementation.
	 * 
	 * @param project java project
	 * @param target the debug target
	 * @return evaluation engine or <code>null</code>
	 */
	public IAstEvaluationEngine getEvaluationEngine(IJavaProject project,
			IJavaDebugTarget target) {
		// get adapter for those that wrapper us
		IJavaDebugTarget javaTarget = (IJavaDebugTarget) target
				.getAdapter(IJavaDebugTarget.class);
		if (javaTarget instanceof JDIDebugTarget) {
			return ((JDIDebugTarget) javaTarget).getEvaluationEngine(project);
		}
		return null;
	}
}
