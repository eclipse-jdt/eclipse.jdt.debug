package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Manages options for the Java Debugger:<ul>
 * <li>Suspend on compilation errors</li>
 * <li>Ssuspend on uncaught exceptions</li>
 * <li>Step filters</li>
 * </ul>
 */
public class JavaDebugOptionsManager implements IResourceChangeListener, IDebugEventListener, IPropertyChangeListener, IJavaBreakpointListener {
	
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
	 * Constants indicating whether a breakpoint
	 * is added, removed, or changed.
	 */
	private static final int ADDED = 0;
	private static final int REMOVED = 1;
	private static final int CHANGED = 2;
		
	/**
	 * Local cache of active step filters.
	 */
	private String[] fActiveStepFilters = new String[0];
	
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
	 * @see
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
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		JDIDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		JDIDebugModel.addJavaBreakpointListener(this);
		initialize();
	}
	
	/**
	 * Called at shutdown by the java debug ui plug-in
	 */
	public void shutdown() throws CoreException {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		JDIDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		JDIDebugModel.removeJavaBreakpointListener(this);
		fProblemMap.clear();
		fLocationMap.clear();
	}	
	
	/**
	 * Notes existing compilation problems, creates exception
	 * breakpoints for copmilation problems and uncaught exceptions.
	 * 
	 * @exception CoreException if unable to initialize
	 */
	protected void initialize() throws CoreException {
		// compilation error breakpoint
		IJavaExceptionBreakpoint bp = JDIDebugModel.createExceptionBreakpoint(ResourcesPlugin.getWorkspace().getRoot(),"java.lang.Error", true, true, false, false, null); //$NON-NLS-1$
		bp.setPersisted(false);
		bp.setRegistered(false);
		// disabled until there are errors
		bp.setEnabled(false);
		setSuspendOnCompilationErrorsBreakpoint(bp);
		
		// note compilation errors
		IMarker[] problems = ResourcesPlugin.getWorkspace().getRoot().findMarkers("org.eclipse.jdt.core.problem", true, IResource.DEPTH_INFINITE); //$NON-NLS-1$
		if (problems != null) {
			for (int i = 0; i < problems.length; i++) {
				problemAdded(problems[i]);
			}
		}
		
		// uncaught exception breakpoint
		bp = JDIDebugModel.createExceptionBreakpoint(ResourcesPlugin.getWorkspace().getRoot(),"java.lang.Throwable", false, true, false, false, null); //$NON-NLS-1$
		bp.setPersisted(false);
		bp.setRegistered(false);
		bp.setEnabled(isSuspendOnUncaughtExceptions());
		setSuspendOnUncaughtExceptionBreakpoint(bp);
		
		// step filters
		updateActiveFilters();
	}
	
	/**
	 * The given problem has been added. Cross
	 * reference the problem with its location.
	 * Enable the error breakpoint if the suspend
	 * option is on.
	 */
	protected void problemAdded(IMarker problem) {
		if (problem.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR) {
			IResource res = problem.getResource();
			IJavaElement cu = JavaCore.create(res);
			if (cu != null && cu instanceof ICompilationUnit) {
				int line = problem.getAttribute(IMarker.LINE_NUMBER, -1);
				String name = cu.getElementName();
				Location l = new Location(cu.getParent().getElementName(), name, line);
				fLocationMap.put(l, problem);
				fProblemMap.put(problem, l);
				try {
					getSuspendOnCompilationErrorBreakpoint().setEnabled(isSuspendOnCompilationErrors());
				} catch (CoreException e) {
					JDIDebugPlugin.logError(e);
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
		Object location = fLocationMap.remove(problem);
		if (location != null) {
			fProblemMap.remove(problem);
		}
		if (fProblemMap.isEmpty()) {
			try {
				getSuspendOnCompilationErrorBreakpoint().setEnabled(false);
			} catch (CoreException e) {
				JDIDebugPlugin.logError(e);
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
		target.setStepFiltersEnabled(store.getBoolean(IJDIPreferencesConstants.PREF_USE_FILTERS));
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
			setSuspendOnCompilationErrors(((Boolean)event.getNewValue()).booleanValue());
		} else if (event.getProperty().equals(IJDIPreferencesConstants.SUSPEND_ON_UNCAUGHT_EXCEPTIONS)) {
			setSuspendOnUncaughtExceptions(((Boolean)event.getNewValue()).booleanValue());
		} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_FILTER_CONSTRUCTORS)) {
			notifyTargetsOfFilters();
		} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_FILTER_STATIC_INITIALIZERS)) {
			notifyTargetsOfFilters();
		} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_FILTER_SYNTHETICS)) {
			notifyTargetsOfFilters();
		} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_USE_FILTERS)) {
			notifyTargetsOfFilters();
		} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST)) {
			updateActiveFilters();
		} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST)) {
			updateActiveFilters();
		}
	}
	
	/**
	 * Sets whether or not to suspend on compilation errors
	 * 
	 * @param enabled whether to suspend on compilation errors
	 */
	protected void setSuspendOnCompilationErrors(boolean enabled) {
		IBreakpoint breakpoint = getSuspendOnCompilationErrorBreakpoint();
		setEnabled(breakpoint, enabled);
	}
	
	/**
	 * Sets whether or not to suspend on uncaught exceptions
	 * 
	 * @param enabled whether or not to suspend on uncaught exceptions
	 */
	protected void setSuspendOnUncaughtExceptions(boolean enabled) {
		IBreakpoint breakpoint = getSuspendOnUncaughtExceptionBreakpoint();
		setEnabled(breakpoint, enabled);
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
	 * Returns whether suspend on comiplation errors is
	 * enabled.
	 * 
	 * @return whether suspend on comiplation errors is
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
		return JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.SUSPEND_ON_UNCAUGHT_EXCEPTIONS);
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
	protected static String[] parseList(String listString) {
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
	protected static String serializeList(String[] list) {
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
	 * Sets the current list of active step filters
	 * 
	 * @param filters the current list of active step filters
	 */
	private void setActiveStepFilters(String[] filters) {
		fActiveStepFilters = filters;
	}
	
	/**
	 * Returns the current list of active step filters
	 * 
	 * @return current list of active step filters
	 */
	protected String[] getActiveStepFilters() {
		return fActiveStepFilters;
	}
	
	/**
	 * Updates local copy of active step filters and
	 * notifies targets.
	 */
	protected void updateActiveFilters() {
		String[] filters = parseList(JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
		setActiveStepFilters(filters);
		notifyTargetsOfFilters();
	}
	
	/**
	 * When a Java debug target is created, install options in
	 * the target.
	 * 
	 * @see IDebugEventListener#handleDebugEvent(DebugEvent)
	 */
	public void handleDebugEvent(DebugEvent event) {
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

	/*
	 * @see IJavaBreakpointListener#breakpointAdded(IJavaDebugTarget, IJavaBreakpoint)
	 */
	public void breakpointAdded(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
	}

	/*
	 * @see IJavaBreakpointListener#breakpointHit(IJavaThread, IJavaBreakpoint)
	 */
	public boolean breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		if (breakpoint == getSuspendOnCompilationErrorBreakpoint()) {
			try {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				if (frame != null) {
					return  getProblem(frame) != null;
				}
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
			}
			
		}
		return true;
	}

	/*
	 * @see IJavaBreakpointListener#breakpointInstalled(IJavaDebugTarget, IJavaBreakpoint)
	 */
	public void breakpointInstalled(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
	}

	/*
	 * @see IJavaBreakpointListener#breakpointRemoved(IJavaDebugTarget, IJavaBreakpoint)
	 */
	public void breakpointRemoved(
		IJavaDebugTarget target,
		IJavaBreakpoint breakpoint) {
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
			String packageName = frame.getDeclaringType().getName();
			int index = packageName.lastIndexOf('.');
			if (index == -1) {
				packageName = "";
			} else {
				packageName = packageName.substring(0,index);
			}
			String name = frame.getSourceName();
			int line = frame.getLineNumber();
			Location l = new Location(packageName, name, line);
			return  (IMarker)fLocationMap.get(l);		
		} catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}

}
