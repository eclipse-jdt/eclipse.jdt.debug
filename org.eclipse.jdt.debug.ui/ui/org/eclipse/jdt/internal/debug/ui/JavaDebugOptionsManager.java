/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Jesper Steen Moller - Enhancement 254677 - filter getters/setters
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint.SuspendOnRecurrenceStrategy;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.logicalstructures.IJavaStructuresListener;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructures;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.actions.JavaBreakpointPropertiesAction;
import org.eclipse.jdt.internal.debug.ui.breakpoints.SuspendOnCompilationErrorListener;
import org.eclipse.jdt.internal.debug.ui.breakpoints.SuspendOnUncaughtExceptionListener;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

/**
 * Manages options for the Java Debugger:<ul>
 * <li>Suspend on compilation errors</li>
 * <li>Suspend on uncaught exceptions</li>
 * <li>Step filters</li>
 * <li>Sets a system property that the Java debugger is active if
 * there are launches that contain running debug targets. Used for Java
 * debug action visibility.
 * </ul>
 */
public class JavaDebugOptionsManager implements IDebugEventSetListener, IPropertyChangeListener, IJavaBreakpointListener, ILaunchListener, IBreakpointsListener, IJavaStructuresListener {

	/**
	 * Singleton options manager
	 */
	private static JavaDebugOptionsManager fgOptionsManager = null;

	/**
	 * Breakpoint used to suspend on uncaught exceptions
	 */
	private IJavaExceptionBreakpoint fSuspendOnExceptionBreakpoint = null;

	/**
	 * Breakpoint used to suspend on compilation errors
	 */
	private IJavaExceptionBreakpoint fSuspendOnErrorBreakpoint = null;

	/**
	 * A label provider
	 */
	private static ILabelProvider fLabelProvider= DebugUITools.newDebugModelPresentation();

	/**
	 * Constants indicating whether a breakpoint
	 * is added, removed, or changed.
	 */
	private static final int ADDED = 0;
	private static final int REMOVED = 1;
	private static final int CHANGED = 2;

	/**
	 * Local cache of active step filters.
	 */
	private String[] fActiveStepFilters = null;

	/**
	 * Preferences that affect variable display options.
	 *
	 * @since 3.3
	 */
	private static Set<String> fgDisplayOptions;

	static {
		fgDisplayOptions = new HashSet<>();
		fgDisplayOptions.add(IJDIPreferencesConstants.PREF_SHOW_CHAR);
		fgDisplayOptions.add(IJDIPreferencesConstants.PREF_SHOW_HEX);
		fgDisplayOptions.add(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED);
	}

	/**
	 * Whether the manager has been activated
	 */
	private boolean fActivated = false;

	class InitJob extends Job {

		public InitJob() {
			super(DebugUIMessages.JavaDebugOptionsManager_0);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			MultiStatus status = new MultiStatus(JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Java debug options failed to initialize", null);  //$NON-NLS-1$
			// compilation error breakpoint
			try {
				IJavaExceptionBreakpoint bp = JDIDebugModel.createExceptionBreakpoint(ResourcesPlugin.getWorkspace().getRoot(),"java.lang.Error", true, true, false, false, null); //$NON-NLS-1$
				bp.setPersisted(false);
				bp.addBreakpointListener(SuspendOnCompilationErrorListener.ID_COMPILATION_ERROR_LISTENER);
				setSuspendOnCompilationErrorsBreakpoint(bp);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}

			// uncaught exception breakpoint
			try {
				IJavaExceptionBreakpoint bp = JDIDebugModel.createExceptionBreakpoint(ResourcesPlugin.getWorkspace().getRoot(),"java.lang.Throwable", false, true, false, false, null); //$NON-NLS-1$
				((JavaExceptionBreakpoint)bp).setSuspendOnSubclasses(true);
				bp.setPersisted(false);
				bp.addBreakpointListener(SuspendOnUncaughtExceptionListener.ID_UNCAUGHT_EXCEPTION_LISTENER);
				setSuspendOnUncaughtExceptionBreakpoint(bp);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}

			if (status.getChildren().length == 0) {
				return Status.OK_STATUS;
			}
			return status;
		}
	}

	/**
	 * Not to be instantiated
	 *
	 * @see JavaDebugOptionsManager#getDefault();
	 */
	private JavaDebugOptionsManager() {
	}

	/**
	 * Return the default options manager
	 */
	public static JavaDebugOptionsManager getDefault() {
		if (fgOptionsManager == null) {
			fgOptionsManager = new JavaDebugOptionsManager();
		}
		return fgOptionsManager;
	}

	/**
	 * Called at startup by the Java debug ui plug-in
	 */
	public void startup() {
		// lazy initialization will occur on the first launch
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.getLaunchManager().addLaunchListener(this);
		debugPlugin.getBreakpointManager().addBreakpointListener(this);
		EvaluationContextManager.startup();
	}

	/**
	 * Called at shutdown by the Java debug ui plug-in
	 */
	public void shutdown() {
		Job.getJobManager().cancel(JavaDebugOptionsManager.class);
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.removeDebugEventListener(this);
		debugPlugin.getLaunchManager().removeLaunchListener(this);
		debugPlugin.getBreakpointManager().removeBreakpointListener(this);
		if (!JDIDebugUIPlugin.getDefault().isShuttingDown()) {
			//avert restoring the preference store at shutdown
			JDIDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		}
		JDIDebugModel.removeJavaBreakpointListener(this);
        JavaLogicalStructures.removeStructuresListener(this);
		System.getProperties().remove(JDIDebugUIPlugin.getUniqueIdentifier() + ".debuggerActive"); //$NON-NLS-1$
	}

	/**
	 * Initializes compilation error handling and suspending
	 * on uncaught exceptions.
	 */
	protected void initializeProblemHandling() {
		InitJob job = new InitJob();
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * Notifies java debug targets of the given breakpoint
	 * addition or removal.
	 *
	 * @param breakpoint a breakpoint
	 * @param kind ADDED, REMOVED, or CHANGED
	 */
	protected void notifyTargets(IBreakpoint breakpoint, int kind) {
		IDebugTarget[] targets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for (int i = 0; i < targets.length; i++) {
			if (targets[i] instanceof IJavaDebugTarget) {
				IJavaDebugTarget target = (IJavaDebugTarget)targets[i];
				notifyTarget(target, breakpoint, kind);
			}
		}
	}

	/**
	 * Notifies the give debug target of filter specifications
	 *
	 * @param target Java debug target
	 */
	protected void notifyTargetOfFilters(IJavaDebugTarget target) {

		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();

		target.setFilterConstructors(store.getBoolean(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS));
		target.setFilterStaticInitializers(store.getBoolean(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS));
		target.setFilterSynthetics(store.getBoolean(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS));
		target.setFilterGetters(store.getBoolean(IJDIPreferencesConstants.PREF_FILTER_GETTERS));
		target.setFilterSetters(store.getBoolean(IJDIPreferencesConstants.PREF_FILTER_SETTERS));
		target.setStepThruFilters(store.getBoolean(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS));
		target.setStepFilters(getActiveStepFilters());

	}

	/**
	 * Notifies all targets of current filter specifications.
	 */
	protected void notifyTargetsOfFilters() {
		IDebugTarget[] targets = DebugPlugin.getDefault().getLaunchManager().getDebugTargets();
		for (int i = 0; i < targets.length; i++) {
			if (targets[i] instanceof IJavaDebugTarget) {
				IJavaDebugTarget target = (IJavaDebugTarget)targets[i];
				notifyTargetOfFilters(target);
			}
		}
	}

	/**
	 * Notifies the given target of the given breakpoint
	 * addition or removal.
	 *
	 * @param target Java debug target
	 * @param breakpoint a breakpoint
	 * @param kind ADDED, REMOVED, or CHANGED
	 */
	protected void notifyTarget(IJavaDebugTarget target, IBreakpoint breakpoint, int kind) {
		switch (kind) {
			case ADDED:
				target.breakpointAdded(breakpoint);
				break;
			case REMOVED:
				target.breakpointRemoved(breakpoint,null);
				break;
			case CHANGED:
				target.breakpointChanged(breakpoint,null);
				break;
		}
	}

	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS)) {
			IBreakpoint breakpoint = getSuspendOnCompilationErrorBreakpoint();
			if (breakpoint != null) {
				int kind = REMOVED;
				if (isSuspendOnCompilationErrors()) {
					kind = ADDED;
				}
				notifyTargets(breakpoint, kind);
			}
		} else if (property.equals(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS)) {
			IBreakpoint breakpoint = getSuspendOnUncaughtExceptionBreakpoint();
			if (breakpoint != null) {
				int kind = REMOVED;
				if (isSuspendOnUncaughtExceptions()) {
					kind = ADDED;
				}
				notifyTargets(breakpoint, kind);
			}
		} else if (fgDisplayOptions.contains(property)) {
			variableViewSettingsChanged();
		} else if (isUseFilterProperty(property)) {
			notifyTargetsOfFilters();
		} else if (isFilterListProperty(property)) {
			updateActiveFilters();
		}
	}

	/**
	 * Returns whether the given property is a property that affects whether
	 * or not step filters are used.
	 */
	private boolean isUseFilterProperty(String property) {
		return property.equals(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS) ||
			property.equals(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS) ||
			property.equals(IJDIPreferencesConstants.PREF_FILTER_GETTERS) ||
			property.equals(IJDIPreferencesConstants.PREF_FILTER_SETTERS) ||
			property.equals(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS) ||
			property.equals(IJDIPreferencesConstants.PREF_STEP_THRU_FILTERS);
	}

	/**
	 * Returns whether the given property is a property that affects
	 * the list of active or inactive step filters.
	 */
	private boolean isFilterListProperty(String property) {
		return property.equals(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST) ||
			property.equals(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST);
	}

	/**
	 * Enable/Disable the given breakpoint and notify
	 * targets of the change.
	 *
	 * @param breakpoint a breakpoint
	 * @param enabled whether enabeld
	 */
	protected void setEnabled(IBreakpoint breakpoint, boolean enabled) {
		try {
			breakpoint.setEnabled(enabled);
			notifyTargets(breakpoint, CHANGED);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * Returns whether suspend on compilation errors is
	 * enabled.
	 *
	 * @return whether suspend on compilation errors is
	 * enabled
	 */
	public boolean isSuspendOnCompilationErrors() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS);
	}

	/**
	 * Returns whether suspend on uncaught exception is
	 * enabled
	 *
	 * @return whether suspend on uncaught exception is
	 * enabled
	 */
	protected boolean isSuspendOnUncaughtExceptions() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS);
	}

	/**
	 * Sets the breakpoint used to suspend on uncaught exceptions
	 *
	 * @param breakpoint exception breakpoint
	 */
	private void setSuspendOnUncaughtExceptionBreakpoint(IJavaExceptionBreakpoint breakpoint) {
		fSuspendOnExceptionBreakpoint = breakpoint;
	}

	/**
	 * Returns the breakpoint used to suspend on uncaught exceptions
	 *
	 * @return exception breakpoint
	 */
	protected IJavaExceptionBreakpoint getSuspendOnUncaughtExceptionBreakpoint() {
		return fSuspendOnExceptionBreakpoint;
	}

	/**
	 * Sets the breakpoint used to suspend on compilation
	 * errors.
	 *
	 * @param breakpoint exception breakpoint
	 */
	private void setSuspendOnCompilationErrorsBreakpoint(IJavaExceptionBreakpoint breakpoint) {
		fSuspendOnErrorBreakpoint = breakpoint;
	}

	/**
	 * Returns the breakpoint used to suspend on compilation
	 * errors
	 *
	 * @return exception breakpoint
	 */
	protected IJavaExceptionBreakpoint getSuspendOnCompilationErrorBreakpoint() {
		return fSuspendOnErrorBreakpoint;
	}

	/**
	 * Parses the comma separated string into an array of strings
	 *
	 * @return list
	 */
	public static String[] parseList(String listString) {
		List<String> list = new ArrayList<>(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Serializes the array of strings into one comma
	 * separated string.
	 *
	 * @param list array of strings
	 * @return a single string composed of the given list
	 */
	public static String serializeList(String[] list) {
		if (list == null) {
			return Util.ZERO_LENGTH_STRING;
		}
		return String.join(String.valueOf(','), list);
	}

	/**
	 * Returns the current list of active step filters.
	 *
	 * @return current list of active step filters
	 */
	protected String[] getActiveStepFilters() {
		if (fActiveStepFilters == null) {
			fActiveStepFilters= parseList(JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
			// After active filters are cached, register to hear about future changes
			JDIDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		}
		return fActiveStepFilters;
	}

	/**
	 * Updates local copy of active step filters and
	 * notifies targets.
	 */
	protected void updateActiveFilters() {
		fActiveStepFilters= parseList(JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
		notifyTargetsOfFilters();
	}

	/**
	 * When a Java debug target is created, install options in
	 * the target and set that the Java debugger is active.
	 * When all Java debug targets are terminated set that that Java debugger is
	 * no longer active.
	 *
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.CREATE) {
				Object source = event.getSource();
				if (source instanceof IJavaDebugTarget) {
					IJavaDebugTarget javaTarget = (IJavaDebugTarget)source;

					// compilation breakpoints
					if (isSuspendOnCompilationErrors()) {
						notifyTarget(javaTarget, getSuspendOnCompilationErrorBreakpoint(), ADDED);
					}

					// uncaught exception breakpoint
					if (isSuspendOnUncaughtExceptions()) {
						ILaunchConfiguration launchConfiguration = javaTarget.getLaunch().getLaunchConfiguration();
						boolean isSnippetEditor = false;

						try {
							isSnippetEditor = (launchConfiguration.getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH, (String)null) != null);
						} catch (CoreException e) {
						}

						if (!isSnippetEditor) {
							notifyTarget(javaTarget, getSuspendOnUncaughtExceptionBreakpoint(), ADDED);
						}
					}

					// step filters
					notifyTargetOfFilters(javaTarget);
				}
			}
		}
	}

	/**
	 * @see IJavaBreakpointListener#addingBreakpoint(IJavaDebugTarget, IJavaBreakpoint)
	 */
	@Override
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	/**
	 * @see IJavaBreakpointListener#installingBreakpoint(IJavaDebugTarget, IJavaBreakpoint, IJavaType)
	 */
	@Override
	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		return DONT_CARE;
	}

	/**
	 * @see IJavaBreakpointListener#breakpointHit(IJavaThread, IJavaBreakpoint)
	 */
	@Override
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		if (thread instanceof JDIThread && breakpoint instanceof IJavaExceptionBreakpoint) {
			try {
				String[] breakpointListeners = breakpoint.getBreakpointListeners();
				if (breakpointListeners.length == 1
						&& SuspendOnCompilationErrorListener.ID_COMPILATION_ERROR_LISTENER.equals(breakpointListeners[0])) {
					return DONT_CARE; // not a user breakpoint
				}
			} catch (CoreException e1) {
				// continue
			}
			if (shouldSkipSubsequentOccurrence((JDIThread) thread, (IJavaExceptionBreakpoint) breakpoint)) {
				return DONT_SUSPEND;
			}
		}
		return DONT_CARE;
	}

	/**
	 * Figure out whether suspending on an exceptionBreakpoint should be skipped due to the user's choice regarding
	 * {@link IJavaExceptionBreakpoint#setSuspendOnRecurrenceStrategy(SuspendOnRecurrenceStrategy)}.
	 * <p>
	 * If the current hit is indeed recurrence of an already-seen exception instance, and if the user has not yet made a choice for this breakpoint,
	 * then a question dialog will be opened to request the user's choice.
	 * </p>
	 *
	 * @param thread
	 *            the thread where an exception occurred
	 * @param exceptionBreakpoint
	 *            the exceptionBreakpoint that just fired
	 * @return {@code true} if the current breakpoint hit should be skipped.
	 */
	public boolean shouldSkipSubsequentOccurrence(JDIThread thread, IJavaExceptionBreakpoint exceptionBreakpoint) {
		if (exceptionBreakpoint == fSuspendOnExceptionBreakpoint) {
			// this breakpoint doesn't have the recurrence property, wait until we are called from SuspendOnUncaughtExceptionListener itself
			return false;
		}
		SuspendOnRecurrenceStrategy skip = thread.shouldSkipExceptionRecurrence(exceptionBreakpoint);
		if (skip == null) {
			return false; // not a recurrence
		}
		if (skip == SuspendOnRecurrenceStrategy.RECURRENCE_UNCONFIGURED) {
			skip = new AskRecurrenceDialog(JDIDebugUIPlugin.getShell()).getAnswer();
		}
		if (skip == null) {
			return false; // user didn't understand the question and cancelled dialog
		}
		switch (skip) {
			case SKIP_RECURRENCES:
				return true;
			default:
				return false;
		}
	}

	private static final class AskRecurrenceDialog extends MessageDialog {

		private boolean fRememberChoice;
		private SuspendOnRecurrenceStrategy fStrategy;

		private AskRecurrenceDialog(Shell parentShell) {
			super(parentShell, DebugUIMessages.JavaDebugOptionsManager_exceptionRecurrence_dialogTitle, null, //
					DebugUIMessages.JavaDebugOptionsManager_exceptionRecurrence_dialogMessage,
					MessageDialog.QUESTION, 0, //
					DebugUIMessages.JavaDebugOptionsManager_skip_buttonLabel, //
					DebugUIMessages.JavaDebugOptionsManager_suspend_buttonLabel);
			parentShell.getDisplay().syncExec(this::open);
		}

		@Override
		protected Control createCustomArea(Composite parent) {
			Composite panel = new Composite(parent, SWT.NONE);
			panel.setFont(parent.getFont());

			GridLayout layout = new GridLayout(1, false);
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			panel.setLayout(layout);

			GridData data = new GridData(GridData.FILL_BOTH);
			data.verticalAlignment = GridData.END;
			panel.setLayoutData(data);

			Button button = new Button(panel, SWT.CHECK);
			button.setText(DebugUIMessages.JavaDebugOptionsManager_exceptionRecurrence_remember_decision);
			button.setSelection(false);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fRememberChoice = !fRememberChoice;
				}
			});
			return panel;
		}

		@Override
		public int open() {
			int ret = super.open();
			switch (ret) {
				case 0:
					fStrategy = SuspendOnRecurrenceStrategy.SKIP_RECURRENCES;
					break;
				case 1:
					fStrategy = SuspendOnRecurrenceStrategy.SUSPEND_ALWAYS;
					break;
			}
			if (fRememberChoice) {
				IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
				if (prefs != null) {
					prefs.put(JDIDebugModel.PREF_SUSPEND_ON_RECURRENCE_STRATEGY, fStrategy.name());
				}
			}
			return ret;
		}

		public SuspendOnRecurrenceStrategy getAnswer() {
			return fStrategy;
		}
	}

	/**
	 * @see IJavaBreakpointListener#breakpointInstalled(IJavaDebugTarget, IJavaBreakpoint)
	 */
	@Override
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	/**
	 * @see IJavaBreakpointListener#breakpointRemoved(IJavaDebugTarget, IJavaBreakpoint)
	 */
	@Override
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	/**
	 * Returns any problem marker associated with the current location
	 * of the given stack frame, or <code>null</code> if none.
	 *
	 * @param frame stack frame
	 * @return marker representing compilation problem, or <code>null</code>
	 */
	public IMarker getProblem(IJavaStackFrame frame) {
		ILaunch launch = frame.getLaunch();
		if (launch != null) {
			ISourceLookupResult result = DebugUITools.lookupSource(frame, null);
			Object sourceElement = result.getSourceElement();
			if (sourceElement instanceof IResource) {
				try {
					IResource resource = (IResource) sourceElement;
					IMarker[] markers = resource.findMarkers("org.eclipse.jdt.core.problem", true, IResource.DEPTH_INFINITE); //$NON-NLS-1$
					int line = frame.getLineNumber();
					for (int i = 0; i < markers.length; i++) {
						IMarker marker = markers[i];
						if (marker.getAttribute(IMarker.LINE_NUMBER, -1) == line && marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
							return marker;
						}
					}
				} catch (CoreException e) {
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasRuntimeException(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.debug.core.DebugException)
	 */
	@Override
	public void breakpointHasRuntimeException(final IJavaLineBreakpoint breakpoint, final DebugException exception) {
		IStatus status;
		Throwable wrappedException= exception.getStatus().getException();
		if (wrappedException instanceof InvocationException) {
			InvocationException ie= (InvocationException) wrappedException;
			ObjectReference ref= ie.exception();
			status= new Status(IStatus.ERROR,JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, ref.referenceType().name(), null);
		} else {
			status= exception.getStatus();
		}
		openConditionErrorDialog(breakpoint, DebugUIMessages.JavaDebugOptionsManager_Conditional_breakpoint_encountered_runtime_exception__1, status);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHasCompilationErrors(org.eclipse.jdt.debug.core.IJavaLineBreakpoint, org.eclipse.jdt.core.dom.Message[])
	 */
	@Override
	public void breakpointHasCompilationErrors(final IJavaLineBreakpoint breakpoint, final Message[] errors) {
		StringBuilder message= new StringBuilder();
		Message error;
		for (int i=0, numErrors= errors.length; i < numErrors; i++) {
			error= errors[i];
			message.append(error.getMessage());
			message.append("\n "); //$NON-NLS-1$
		}
		IStatus status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, message.toString(), null);
		openConditionErrorDialog(breakpoint, DebugUIMessages.JavaDebugOptionsManager_Conditional_breakpoint_has_compilation_error_s___2, status);
	}

	private void openConditionErrorDialog(final IJavaLineBreakpoint breakpoint, final String errorMessage, final IStatus status) {
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		if (display.isDisposed()) {
			return;
		}
		final String message = NLS.bind(errorMessage, fLabelProvider.getText(breakpoint));
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (display.isDisposed()) {
					return;
				}
				Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
				ConditionalBreakpointErrorDialog dialog= new ConditionalBreakpointErrorDialog(shell, message, status);
				int result = dialog.open();
				if (result == Window.OK) {
					JavaBreakpointPropertiesAction action= new JavaBreakpointPropertiesAction();
					action.selectionChanged(null, new StructuredSelection(breakpoint));
					action.run(null);
				}
			}
		});
	}

	/**
	 * Activates this debug options manager. When active, this
	 * manager becomes a listener to many notifications and updates
	 * running debug targets based on these notifications.
	 *
	 * A debug options manager does not need to be activated until
	 * there is a running debug target.
	 */
	private void activate() {
		if (fActivated) {
			return;
		}
		fActivated = true;
		initializeProblemHandling();
		notifyTargetsOfFilters();
		DebugPlugin.getDefault().addDebugEventListener(this);
		JDIDebugModel.addJavaBreakpointListener(this);
        JavaLogicalStructures.addStructuresListener(this);
	}

	/**
	 * Startup problem handling on the first launch.
	 *
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	@Override
	public void launchAdded(ILaunch launch) {
		launchChanged(launch);
	}
	/**
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	@Override
	public void launchChanged(ILaunch launch) {
		activate();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
	}

	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	@Override
	public void launchRemoved(ILaunch launch) {
	}

	/**
	 * Adds message attributes to java breakpoints.
	 *
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	@Override
	public void breakpointsAdded(final IBreakpoint[] breakpoints) {
		// if a breakpoint is added, but already has a message, do not update it
		List<IBreakpoint> update = new ArrayList<>();
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			try {
				if (breakpoint instanceof IJavaBreakpoint && breakpoint.getMarker().getAttribute(IMarker.MESSAGE) == null) {
					update.add(breakpoint);
				}
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		if (!update.isEmpty()) {
			updateBreakpointMessages(update.toArray(new IBreakpoint[update.size()]));
		}
	}

	/**
	 * Updates message attributes on the given java breakpoints.
	 *
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	private void updateBreakpointMessages(final IBreakpoint[] breakpoints) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint = breakpoints[i];
					if (breakpoint instanceof IJavaBreakpoint) {
						String info = fLabelProvider.getText(breakpoint);
						String type = DebugUIMessages.JavaDebugOptionsManager_Breakpoint___1;
						if (breakpoint instanceof IJavaMethodBreakpoint || breakpoint instanceof IJavaMethodEntryBreakpoint) {
							type = DebugUIMessages.JavaDebugOptionsManager_Method_breakpoint___2;
						} else if (breakpoint instanceof IJavaWatchpoint) {
							type = DebugUIMessages.JavaDebugOptionsManager_Watchpoint___3;
						} else if (breakpoint instanceof IJavaLineBreakpoint) {
							type = DebugUIMessages.JavaDebugOptionsManager_Line_breakpoint___4;
						}
						IMarker marker = breakpoint.getMarker();
						if (marker.exists()) {
							marker.setAttribute(IMarker.MESSAGE, type + info);
						}
					}
				}
			}
		};

		final ISchedulingRule modifyWorkspaceRule = modifyWorkspaceRule();

		ISchedulingRule currentRule = Job.getJobManager().currentRule();
		if (currentRule != null && currentRule.contains(modifyWorkspaceRule)) {
			try {
				ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		} else {
			UpdateBreakpointMessagesJob updateBreakpointMessagesJob = new UpdateBreakpointMessagesJob(modifyWorkspaceRule, runnable);
			updateBreakpointMessagesJob.schedule();
		}
	}

	private static ISchedulingRule modifyWorkspaceRule() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IResourceRuleFactory rootFactory = workspace.getRuleFactory();
		ISchedulingRule modifyWorkspaceRule = rootFactory.modifyRule(workspaceRoot);
		return modifyWorkspaceRule;
	}

	private static class UpdateBreakpointMessagesJob extends Job {

		private final ISchedulingRule rule;
		private final ICoreRunnable runnable;

		UpdateBreakpointMessagesJob(ISchedulingRule rule, ICoreRunnable runnable) {
			super("Updating breakpoint messages"); //$NON-NLS-1$
			this.rule = rule;
			this.runnable = runnable;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			try {
				ResourcesPlugin.getWorkspace().run(runnable, rule, 0, monitor);
			} catch (CoreException e) {
				String errorMessage = "Failed to update breakpoint messages"; //$NON-NLS-1$
				IStatus errorStatus = new Status(IStatus.ERROR, IJavaDebugUIConstants.PLUGIN_ID, errorMessage, e);
				return errorStatus;
			}
			return Status.OK_STATUS;
		}

		@Override
		public boolean belongsTo(Object family) {
			return JavaDebugOptionsManager.class == family;
		}
	}

	/**
	 * Updates message attributes on java breakpoints.
	 *
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	@Override
	public void breakpointsChanged(
		IBreakpoint[] breakpoints,
		IMarkerDelta[] deltas) {
			updateBreakpointMessages(breakpoints);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	@Override
	public void breakpointsRemoved(
		IBreakpoint[] breakpoints,
		IMarkerDelta[] deltas) {
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.core.logicalstructures.IJavaStructuresListener#logicalStructuresChanged()
     */
    @Override
	public void logicalStructuresChanged() {
    	variableViewSettingsChanged();
    }

    /**
	 * Refreshes the variables view by firing a change event on a stack frame (active
	 * debug context).
	 */
    protected void variableViewSettingsChanged() {
        // If a Java stack frame is selected in the Debug view, fire a change event on
        // it so the variables view will update for any structure changes.
        IAdaptable selected = DebugUITools.getDebugContext();
        if (selected != null) {
			IJavaStackFrame frame = selected.getAdapter(IJavaStackFrame.class);
            if (frame != null) {
                DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {
                        new DebugEvent(frame, DebugEvent.CHANGE)
                });
            }
        }
    }

}
