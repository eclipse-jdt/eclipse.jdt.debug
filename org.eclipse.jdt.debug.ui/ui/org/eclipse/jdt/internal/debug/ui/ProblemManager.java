package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Creates breakpoints corresponding to compilation errors
 */
public class ProblemManager implements IResourceChangeListener, ILaunchListener, IPropertyChangeListener {
	
	/**
	 * Singleton problem manager
	 */
	private static ProblemManager fgProblemManager = null;
	
	/**
	 * Map of problems to associated breakpoints
	 */
	private HashMap fProblemMap = new HashMap(10);
	
	/**
	 * Constants indicating whether a notification
	 * is added or removed.
	 */
	private static final int ADDED = 0;
	private static final int REMOVED = 1;
	private static final int CHANGED = 2;
	
	/**
	 * Marker attribute denoting a problem breakpoint
	 */
	public static final String ATTR_PROBLEM_BREAKPOINT = "org.eclipse.jdt.debug.ui.problemBreakpoint";
	
	/**
	 * Marker attribute problem message
	 */
	public static final String ATTR_PROBLEM_MESSAGE = "org.eclipse.jdt.debug.ui.problemBreakpoint.message";

	/*
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		
		final IMarkerDelta[] deltas = event.findMarkerDeltas("org.eclipse.jdt.core.problem", true);
		if (deltas != null) {
			IWorkspaceRunnable wr = new IWorkspaceRunnable() {
				public void run(IProgressMonitor m) throws CoreException {
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
			};
			fork(wr);
		}
	}
	
	/**
	 * Not to be instantiated
	 * 
	 * @see
	 */
	private ProblemManager() {
	}
	
	/**
	 * Return the default problem manager
	 */
	public static ProblemManager getDefault() {
		if (fgProblemManager == null) {
			fgProblemManager = new ProblemManager();
		}
		return fgProblemManager;
	}
	
	/**
	 * Called at startup by the java debug ui plug-in
	 */
	public void startup() throws CoreException {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		JDIDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		initialize();
	}
	
	/**
	 * Called at shutdown by the java debug ui plug-in
	 */
	public void shutdown() throws CoreException {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		JDIDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		fProblemMap.clear();
	}	
	
	/**
	 * Creates breakpoints for existing problems
	 * 
	 * @exception CoreException if unable to initialize
	 */
	protected void initialize() throws CoreException {
		IMarker[] problems = ResourcesPlugin.getWorkspace().getRoot().findMarkers("org.eclipse.jdt.core.problem", true, IResource.DEPTH_INFINITE);
		if (problems != null) {
			for (int i = 0; i < problems.length; i++) {
				problemAdded(problems[i]);
			}
		}
	}

	/**
	 * Creates and returns a breakpoint for the given
	 * compilation problem, or <code>null</code> if the
	 * given problem does not require a breakpoint.
	 * 
	 * @param problem marker of type 'org.eclipse.jdt.core.problem'
	 * @return breakpoint for the given compilation problem,
	 *  or <code>null</code>
	 * @exception CoreException if an exception occurrs accessing
	 *  marker properties
	 */
	protected IBreakpoint createBreakpoint(IMarker problem) throws CoreException {
		IBreakpoint breakpoint = null;
		if (problem.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR) {
			IResource res = problem.getResource();
			IJavaElement cu = JavaCore.create(res);
			if (cu != null && cu instanceof ICompilationUnit) {
				int start = problem.getAttribute(IMarker.CHAR_START, -1);
				int end = problem.getAttribute(IMarker.CHAR_END, -1);
				int line = problem.getAttribute(IMarker.LINE_NUMBER, -1);
				int pos = start;
				if (pos == -1) {
					pos = line;
				}
				IJavaElement je = ((ICompilationUnit)cu).getElementAt(pos);
				if (je != null) {
					Map map = new HashMap(10);
					map.put(ATTR_PROBLEM_BREAKPOINT, Boolean.TRUE);
					String message = problem.getAttribute(IMarker.MESSAGE, null);
					if (message != null) {
						map.put(ATTR_PROBLEM_MESSAGE, message);
					}					
					if (je instanceof IMethod) {
						IMethod method = (IMethod)je;
						breakpoint = JDIDebugModel.createMethodBreakpoint(res, method.getDeclaringType().getFullyQualifiedName(), method.getElementName(), null, true, false, false, line, start, end, 0, false, map);
					} else if (je instanceof IMember) {
						IMember member = (IMember)je;
						String name = null;
						if (member instanceof IType) {
							name = ((IType)member).getFullyQualifiedName();
						} else {
							name = member.getDeclaringType().getFullyQualifiedName();
						}
						breakpoint = JDIDebugModel.createMethodBreakpoint(res, name, null, null, true, false, false, line, start, end, 0, false, map);	
					}	
					if (breakpoint != null) {
						breakpoint.setPersisted(false);
						breakpoint.setEnabled(isEnabled());
					}				
				}
			}
		}
		return breakpoint;
	}
	
	/**
	 * The given problem has been added. Create a breakpoint
	 * for the problem.
	 */
	protected void problemAdded(IMarker problem) {
		try {
			IBreakpoint breakpoint = createBreakpoint(problem);
			if (breakpoint != null) {
				setBreakpoint(problem, breakpoint);
				notifyTargets(breakpoint, ADDED);
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.logError(e);
		}
	}
	
	/**
	 * The given problem has been removed. Remove any breakpoint
	 * associated with the problem.
	 */
	protected void problemRemoved(IMarker problem){
		IBreakpoint breakpoint = getBreakpoint(problem);
		if (breakpoint != null) {
			setBreakpoint(problem, null);
			notifyTargets(breakpoint, REMOVED);
			try {
				breakpoint.delete();
			} catch (CoreException e) {
				JDIDebugUIPlugin.logError(e);
			}
		}		
	}
	
	/**
	 * Sets the breakpoint associated with the given problem.
	 * 
	 * @param problem problem marker
	 * @param breakpoint, or <code>null</code>
	 */
	protected void setBreakpoint(IMarker problem, IBreakpoint breakpoint) {
		if (breakpoint == null) {
			fProblemMap.remove(problem);
		} else {
			fProblemMap.put(problem, breakpoint);
		}
	}
	
	/**
	 * Returns the breakpoint associated with the given problem.
	 * 
	 * @param problem problem marker
	 * @return breakpoint, or <code>null</code>
	 */
	protected IBreakpoint getBreakpoint(IMarker problem) {
		return (IBreakpoint)fProblemMap.get(problem);
	}	
	
	/**
	 * Returns the current set of problem breakpoints
	 */
	protected IBreakpoint[] getBreakpoints() {
		Collection collection = fProblemMap.values();
		return (IBreakpoint[])collection.toArray(new IBreakpoint[collection.size()]);
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
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		IDebugTarget target = launch.getDebugTarget();
		if (target instanceof IJavaDebugTarget) { 
			IBreakpoint[] breakpoints = getBreakpoints();
			for (int i = 0; i < breakpoints.length; i++) {
				notifyTarget((IJavaDebugTarget)target, breakpoints[i], ADDED);
			}
		}
	}

	/*
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}

	/*
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}

	/**
	 * Forks the runnable in a new thread
	 */
	protected void fork(final IWorkspaceRunnable wRunnable) {
		Runnable runnable= new Runnable() {
			public void run() {
				try {
					ResourcesPlugin.getWorkspace().run(wRunnable, null);
				} catch (CoreException ce) {
					JDIDebugUIPlugin.log(ce);
				}
			}
		};
		new Thread(runnable).start();
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS)) {
			setEnabled(((Boolean)event.getNewValue()).booleanValue());
		}
	}
	
	/**
	 * Sets whether or not to suspend on compilation errors
	 * 
	 * @param enabled whether to suspend on compilation errors
	 */
	protected void setEnabled(boolean enabled) {
		IBreakpoint[] breakpoints = getBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			try {
				breakpoints[i].setEnabled(enabled);
				notifyTargets(breakpoints[i], CHANGED);
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
	}
	
	/**
	 * Returns whether suspend on comiplation errors is
	 * enabled.
	 * 
	 * @return whether suspend on comiplation errors is
	 * enabled
	 */
	protected boolean isEnabled() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS);
	}

}
