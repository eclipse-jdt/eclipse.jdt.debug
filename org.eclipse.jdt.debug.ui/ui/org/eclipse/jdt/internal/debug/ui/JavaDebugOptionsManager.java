package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.actions.JavaBreakpointPropertiesDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
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
public class JavaDebugOptionsManager implements IResourceChangeListener, IDebugEventSetListener, IPropertyChangeListener, IJavaBreakpointListener, ILaunchListener, IBreakpointsListener {
	
	/**
	 * Singleton options manager
	 */
	private static JavaDebugOptionsManager fgOptionsManager = null;
	
	/**
	 * Map of problems to locations
	 * (<code>IMarker</code> -> <code>Location</code>)
	 */
	private HashMap fProblemMap = new HashMap(10);
	
	/**
	 * Map of locations to problems.
	 * (<code>Location</code> -> <code>IMarker</code>)
	 */
	private HashMap fLocationMap = new HashMap(10);
	
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
	 * Helper class that describes a location in a stack
	 * frame. A location consists of a package name, source
	 * file name, and a line number.
	 */
	class Location {
		private String fPackageName;
		private String fSourceName;
		private int fLineNumber;
		
		public Location(String packageName, String sourceName, int lineNumber) {
			fPackageName = packageName;
			fSourceName = sourceName;
			fLineNumber = lineNumber;
		}
		
		public boolean equals(Object o) {
			if (o instanceof Location) {
				Location l = (Location)o;
				return l.fPackageName.equals(fPackageName) && l.fSourceName.equals(fSourceName) && l.fLineNumber == fLineNumber;
				
			}
			return false;
		}
		
		public int hashCode() {
			return fPackageName.hashCode() + fSourceName.hashCode() + fLineNumber;
		}
	}

	/**
	 * Update cache of problems as they are added/removed.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IMarkerDelta[] deltas = event.findMarkerDeltas("org.eclipse.jdt.core.problem", true); //$NON-NLS-1$
		if (deltas != null) {
			for (int i = 0; i < deltas.length; i++) {
				IMarkerDelta delta = deltas[i];
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						problemAdded(delta.getMarker());
						break;
					case IResourceDelta.REMOVED:
						problemRemoved(delta.getMarker());
						break;
				}
			}
		}
	}
	
	/**
	 * Not to be instantiated
	 * 
	 * @see JavaDebugOptionsManager.getDefault();
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
	 * Called at startup by the java debug ui plug-in
	 */
	public void startup() throws CoreException {
		// lazy initialization will occur on the first launch
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.getLaunchManager().addLaunchListener(this);
		debugPlugin.getBreakpointManager().addBreakpointListener(this);
		EvaluationContextManager.startup();
	}
	
	/**
	 * Called at shutdown by the Java debug ui plug-in
	 */
	public void shutdown() throws CoreException {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.removeDebugEventListener(this);
		debugPlugin.getLaunchManager().removeLaunchListener(this);
		debugPlugin.getBreakpointManager().removeBreakpointListener(this);
		if (!JDIDebugUIPlugin.getDefault().isShuttingDown()) {
			//avert restoring the preference store at shutdown
			JDIDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		}
		JDIDebugModel.removeJavaBreakpointListener(this);
		fProblemMap.clear();
		fLocationMap.clear();
		System.getProperties().remove(JDIDebugUIPlugin.getUniqueIdentifier() + ".debuggerActive"); //$NON-NLS-1$
	}	

	/**
	 * Initializes compilation error handling and suspending
	 * on uncaught exceptions.
	 */
	protected void initializeProblemHandling() {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// compilation error breakpoint
				IJavaExceptionBreakpoint bp = JDIDebugModel.createExceptionBreakpoint(ResourcesPlugin.getWorkspace().getRoot(),"java.lang.Error", true, true, false, false, null); //$NON-NLS-1$
				bp.setPersisted(false);
				bp.setRegistered(false);
				// disabled until there are errors
				bp.setEnabled(false);
				setSuspendOnCompilationErrorsBreakpoint(bp);
				
				// uncaught exception breakpoint
				bp = JDIDebugModel.createExceptionBreakpoint(ResourcesPlugin.getWorkspace().getRoot(),"java.lang.Throwable", false, true, false, false, null); //$NON-NLS-1$
				bp.setPersisted(false);
				bp.setRegistered(false);
				bp.setEnabled(isSuspendOnUncaughtExceptions());
				setSuspendOnUncaughtExceptionBreakpoint(bp);
				
				// note existing compilation errors
				IMarker[] problems = ResourcesPlugin.getWorkspace().getRoot().findMarkers("org.eclipse.jdt.core.problem", true, IResource.DEPTH_INFINITE); //$NON-NLS-1$
				if (problems != null) {
					for (int i = 0; i < problems.length; i++) {
						problemAdded(problems[i]);
					}
				}				
			}
		};
		
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}
		
	/**
	 * The given problem has been added. Cross
	 * reference the problem with its location.
	 * Enable the error breakpoint if the suspend
	 * option is on, and this is the first problem
	 * being added.
	 */
	protected void problemAdded(IMarker problem) {
		if (problem.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR) {
			IResource res = problem.getResource();
			IJavaElement cu = JavaCore.create(res);
			if (cu != null && cu instanceof ICompilationUnit) {
				// auto-enable the exception breakpoint if this is the first problem added
				// and the preference is turned on.
				boolean autoEnable = fProblemMap.isEmpty();
				int line = problem.getAttribute(IMarker.LINE_NUMBER, -1);
				String name = cu.getElementName();
				Location l = new Location(cu.getParent().getElementName(), name, line);
				fLocationMap.put(l, problem);
				fProblemMap.put(problem, l);
				if (autoEnable) {
					try {
						getSuspendOnCompilationErrorBreakpoint().setEnabled(isSuspendOnCompilationErrors());
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				}
			}
		}
	}
	
	/**
	 * The given problem has been removed. Remove
	 * cross reference of problem and location.
	 * Disable the breakpoint if there are no errors.
	 */
	protected void problemRemoved(IMarker problem) {
		Object location = fProblemMap.remove(problem);
		if (location != null) {
			fLocationMap.remove(location);
		}
		if (fProblemMap.isEmpty()) {
			try {
				getSuspendOnCompilationErrorBreakpoint().setEnabled(false);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
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
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS)) {
			IBreakpoint breakpoint = getSuspendOnCompilationErrorBreakpoint();
			if (breakpoint != null) {
				setEnabled(breakpoint, JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS));
			}
		} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS)) {
			IBreakpoint breakpoint = getSuspendOnUncaughtExceptionBreakpoint();
			if (breakpoint != null) {
				setEnabled(breakpoint, JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS));
			}
		} else if (isUseFilterProperty(event.getProperty())) {
			notifyTargetsOfFilters();
		} else if (isFilterListProperty(event.getProperty())) {
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
			property.equals(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS);
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
	protected boolean isSuspendOnCompilationErrors() {
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
		List list = new ArrayList(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return (String[])list.toArray(new String[list.size()]);
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
			return ""; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < list.length; i++) {
			if (i > 0) {
				buffer.append(',');
			}
			buffer.append(list[i]);
		}
		return buffer.toString();
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
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.CREATE) {
				Object source = event.getSource();
				if (source instanceof IJavaDebugTarget) {
					IJavaDebugTarget javaTarget = (IJavaDebugTarget)source;
					
					// compilation breakpoints				
					notifyTarget(javaTarget, getSuspendOnCompilationErrorBreakpoint(), ADDED);
					
					// uncaught exception breakpoint
					notifyTarget(javaTarget, getSuspendOnUncaughtExceptionBreakpoint(), ADDED);
					
					// step filters
					notifyTargetOfFilters(javaTarget);
				}
			}
		}
	}

	/**
	 * @see IJavaBreakpointListener#addingBreakpoint(IJavaDebugTarget, IJavaBreakpoint)
	 */
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	/**
	 * @see IJavaBreakpointListener#installingBreakpoint(IJavaDebugTarget, IJavaBreakpoint, IJavaType)
	 */
	public boolean installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		return true;
	}
	
	/**
	 * @see IJavaBreakpointListener#breakpointHit(IJavaThread, IJavaBreakpoint)
	 */
	public boolean breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		if (breakpoint == getSuspendOnCompilationErrorBreakpoint()) {
			return  getProblem(thread) != null;
		}
		if (breakpoint == getSuspendOnUncaughtExceptionBreakpoint()) {
			// the "uncaught" exceptions breakpoint subsumes the "compilation error" breakpoint
			// since "Throwable" is a supertype of "Error". Thus, if there is actually a compilation
			// error here, but the option to suspend on compilation errors is off, we should
			// resume (i.e. do not suspend)
			if (!isSuspendOnCompilationErrors()) {
				// only suspend if there is no compilation error
				return getProblem(thread) == null;
			}
		}
		return true;
	}
	
	private IMarker getProblem(IJavaThread thread) {
		try {
			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			if (frame != null) {
				return  getProblem(frame);
			}
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
		}	
		return null;	
	}

	/**
	 * @see IJavaBreakpointListener#breakpointInstalled(IJavaDebugTarget, IJavaBreakpoint)
	 */
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	/**
	 * @see IJavaBreakpointListener#breakpointRemoved(IJavaDebugTarget, IJavaBreakpoint)
	 */
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}
	
	/**
	 * Returns any problem marker associated with the current location
	 * of the given stack frame, or <code>null</code> if none.
	 * 
	 * @param frame stack frame
	 * @return marker representing compilation problem, or <code>null</code>
	 */
	protected IMarker getProblem(IJavaStackFrame frame) {
		try {
			String name = frame.getSourceName();
			String packageName = frame.getDeclaringTypeName();
			int index = packageName.lastIndexOf('.');
			if (index == -1) {
				if (name == null) {
					// guess at source name if no debug attribute
					name = packageName;
					int dollar = name.indexOf('$');
					if (dollar >= 0) {
						name = name.substring(0, dollar);
					}
					name+= ".java"; //$NON-NLS-1$
				}
				packageName = ""; //$NON-NLS-1$
			} else {
				if (name == null) {
					name = packageName.substring(index + 1);
					int dollar = name.indexOf('$');
					if (dollar >= 0) {
						name = name.substring(0, dollar);
					}
					name += ".java"; //$NON-NLS-1$
				}
				packageName = packageName.substring(0,index);
			}
			int line = frame.getLineNumber();
			Location l = new Location(packageName, name, line);
			return  (IMarker)fLocationMap.get(l);		
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}	
	
	/**
	 * @see IJavaConditionalBreakpointListener#breakpointHasRuntimeException(IJavaLineBreakpoint, Throwable)
	 */
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
		openConditionErrorDialog(breakpoint, DebugUIMessages.getString("JavaDebugOptionsManager.Conditional_breakpoint_encountered_runtime_exception._1"), status); //$NON-NLS-1$
	}

	/**
	 * @see IJavaConditionalBreakpointListener#breakpointHasCompilationErrors(IJavaLineBreakpoint, Message[])
	 */
	public void breakpointHasCompilationErrors(final IJavaLineBreakpoint breakpoint, final Message[] errors) {
		StringBuffer message= new StringBuffer();
		Message error;
		for (int i=0, numErrors= errors.length; i < numErrors; i++) {
			error= errors[i];
			message.append(error.getMessage());
			message.append("\n "); //$NON-NLS-1$
		}
		IStatus status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, message.toString(), null);
		openConditionErrorDialog(breakpoint, DebugUIMessages.getString("JavaDebugOptionsManager.Conditional_breakpoint_has_compilation_error(s)._2"), status); //$NON-NLS-1$
	}
	
	private void openConditionErrorDialog(final IJavaLineBreakpoint breakpoint, final String errorMessage, final IStatus status) {
		final Display display= JDIDebugUIPlugin.getStandardDisplay();
		if (display.isDisposed()) {
			return;
		}
		final String message= MessageFormat.format(errorMessage, new String[] {fLabelProvider.getText(breakpoint)});
		display.asyncExec(new Runnable() {
			public void run() {
				if (display.isDisposed()) {
					return;
				}
				Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
				ConditionalBreakpointErrorDialog dialog= new ConditionalBreakpointErrorDialog(shell, message, status);
				int result = dialog.open();
				if (result == Dialog.OK) {
					JavaBreakpointPropertiesDialog breakpointPropertiesDialog = new JavaBreakpointPropertiesDialog(shell, breakpoint);
					breakpointPropertiesDialog.open();
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
		initializeProblemHandling();
		notifyTargetsOfFilters();
		DebugPlugin.getDefault().addDebugEventListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_AUTO_BUILD);
		JDIDebugModel.addJavaBreakpointListener(this);
	}	

	/**
	 * Startup problem handling on the first launch.
	 * 
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		launchChanged(launch);
	}
	/**
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
		activate();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);		
	}

	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}
	
	/**
	 * Adds message attributes to java breakpoints.
	 * 
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded(final IBreakpoint[] breakpoints) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint = breakpoints[i];
					if (breakpoint instanceof IJavaBreakpoint) {
						String info = fLabelProvider.getText(breakpoint);
						String type = DebugUIMessages.getString("JavaDebugOptionsManager.Breakpoint___1"); //$NON-NLS-1$
						if (breakpoint instanceof IJavaMethodBreakpoint || breakpoint instanceof IJavaMethodEntryBreakpoint) {
							type = DebugUIMessages.getString("JavaDebugOptionsManager.Method_breakpoint___2"); //$NON-NLS-1$
						} else if (breakpoint instanceof IJavaWatchpoint) {
							type = DebugUIMessages.getString("JavaDebugOptionsManager.Watchpoint___3");  //$NON-NLS-1$
						} else if (breakpoint instanceof IJavaLineBreakpoint) {
							type = DebugUIMessages.getString("JavaDebugOptionsManager.Line_breakpoint___4"); //$NON-NLS-1$
						}
						breakpoint.getMarker().setAttribute(IMarker.MESSAGE, type + info);
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * Updates message attributes on java breakpoints.
	 * 
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged(
		IBreakpoint[] breakpoints,
		IMarkerDelta[] deltas) {
			breakpointsAdded(breakpoints);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved(
		IBreakpoint[] breakpoints,
		IMarkerDelta[] deltas) {
	}

}
