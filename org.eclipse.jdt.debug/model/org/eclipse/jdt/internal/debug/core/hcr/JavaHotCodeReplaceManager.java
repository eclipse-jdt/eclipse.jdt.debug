package org.eclipse.jdt.internal.debug.core.hcr;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugModelMessages;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JDIDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

/**
 * The hot code replace manager listens for changes to
 * class files and notifies running debug targets of the changes.
 * <p>
 * Currently, replacing .jar files has no effect on running targets.
 */
public class JavaHotCodeReplaceManager implements IResourceChangeListener, ILaunchListener, IDebugEventListener {
	/**
	 * Singleton 
	 */
	private static JavaHotCodeReplaceManager fgInstance= null;
	/**
	 * The class file extension
	 */
	private static final String CLASS_FILE_EXTENSION= "class"; //$NON-NLS-1$
	
	/**
	 * The list of <code>IJavaHotCodeReplaceListeners</code> which this hot code replace 
	 * manager will notify about hot code replace attempts.
	 */
	private ListenerList fHotCodeReplaceListeners= new ListenerList(1);
	
	/**
	 * The lists of hot swap targets which support HCR and those which don't
	 */
	private List fHotSwapTargets= new ArrayList(1);
	private List fNoHotSwapTargets= new ArrayList(1);
	/**
	 * Visitor for resource deltas.
	 */
	protected ChangedClassFilesVisitor fVisitor = new ChangedClassFilesVisitor();
	
	/**
	 * Creates a new HCR manager
	 */
	public JavaHotCodeReplaceManager() {
		fgInstance= this;
	}
	/**
	 * Returns the singleton HCR manager
	 */
	public static JavaHotCodeReplaceManager getDefault() {
		return fgInstance;
	}
	/**
	 * Registers this HCR manager as a resource change listener. This method
	 * is called by the JDI debug model plugin on startup.
	 */
	public void startup() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	/**
	 * Deregisters this HCR manager as a resource change listener. Removes all hot
	 * code replace listeners. This method* is called by the JDI debug model plugin
	 * on shutdown.
	 */
	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		getWorkspace().removeResourceChangeListener(this);
		fHotCodeReplaceListeners.removeAll();
		clearHotSwapTargets();
	}
	/**
	 * Returns the workspace.
	 */
	protected IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * Returns the launch manager.
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	/**
	 * @see IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		final List resources= getChangedClassFiles(event.getDelta());
		if (resources.isEmpty()) {
			return;
		}	
		final List hotSwapTargets= getHotSwapTargets();
		final List noHotSwapTargets= getNoHotSwapTargets();
		final List qualifiedNames= JDIDebugUtils.getQualifiedNames(resources);
		if (!hotSwapTargets.isEmpty()) {
			IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					notify(hotSwapTargets, resources, qualifiedNames);
				}
			};
			fork(wRunnable);	
		}
		if (!noHotSwapTargets.isEmpty()) {
			IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					notifyFailedHCR(noHotSwapTargets, resources, qualifiedNames);
				}
			};
			fork(wRunnable);
		}
	}
	
	/**
	 * Notify the given targets that HCR failed for classes
	 * with the given fully qualified names.
	 */
	protected void notifyFailedHCR(List targets, List resources, List qualifiedNames) {
		Iterator iter= targets.iterator();
		while (iter.hasNext()) {
			notifyFailedHCR((JDIDebugTarget) iter.next(), resources, qualifiedNames);
		}
	}
	
	protected void notifyFailedHCR(JDIDebugTarget target, List resources, List qualifiedNames) {
		if (!target.isTerminated() && !target.isDisconnected()) {
				fireHCRFailed(target, null);
				target.typesFailedReload(resources, qualifiedNames);
		}
	}	
	
	/**
	 * Returns the currently registered debug targets that support
	 * hot code replace.
	 */
	protected List getHotSwapTargets() {
		return fHotSwapTargets;
	}
	
	/**
	 * Returns the currently registered debug targets that do
	 * not support hot code replace.
	 */
	protected List getNoHotSwapTargets() {
		return fNoHotSwapTargets;
	}
	
	protected void clearHotSwapTargets() {
		fHotSwapTargets= null;
		fNoHotSwapTargets= null;
	}
	
	/**
	 * Notifies the targets of the changed types
	 * 
	 * @param targets the targets to notify
	 * @param resources the resources which correspond to the changed classes
	 */
	private void notify(List targets, List resources, List qualifiedNames) {
		MultiStatus ms= new MultiStatus(JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), DebugException.TARGET_REQUEST_FAILED, JDIDebugModelMessages.getString("JavaHotCodeReplaceManager.drop_to_frame_failed"), null); //$NON-NLS-1$
		Iterator iter= targets.iterator();
		while (iter.hasNext()) {
			JDIDebugTarget target= (JDIDebugTarget) iter.next();
			try {
				if (target.isTerminated() || target.isDisconnected()) {
					continue;
				}
				target.typesHaveChanged(resources, qualifiedNames);
				try {
					attemptDropToFrame(target, qualifiedNames);
				} catch (DebugException de) {
					ms.merge(de.getStatus());
				}
				fireHCRSucceeded();
			} catch (DebugException de) {
				// target update failed
				fireHCRFailed(target, de);
				notifyFailedHCR(target, resources, qualifiedNames);
			}
		}
		if (!ms.isOK()) {
			JDIDebugPlugin.logError(new DebugException(ms));
		}
	}
	
	/**
	 * Notifies listeners that a hot code replace attempt succeeded
	 */
	private void fireHCRSucceeded() {
		Object[] listeners= fHotCodeReplaceListeners.getListeners();
		for (int i=0; i<listeners.length; i++) {
			((IJavaHotCodeReplaceListener)listeners[i]).hotCodeReplaceSucceeded();
		}		
	}
	
	/**
	 * Notifies listeners that a hot code replace attempt failed with the given exception
	 */
	private void fireHCRFailed(JDIDebugTarget target, DebugException exception) {
		Object[] listeners= fHotCodeReplaceListeners.getListeners();
		for (int i=0; i<listeners.length; i++) {
			((IJavaHotCodeReplaceListener)listeners[i]).hotCodeReplaceFailed(target, exception);
		}
	}
	/**
	 * Looks for the deepest effected stack frame in the stack
	 * and forces a drop to frame.  Does this for all of the active
	 * stack frames in the target.
	 */
	protected void attemptDropToFrame(JDIDebugTarget target, List replacedClassNames) throws DebugException {
		IThread[] threads= target.getThreads();
		List dropFrames= new ArrayList(1);
		int numThreads= threads.length;
		for (int i = 0; i < numThreads; i++) {
			JDIThread thread= (JDIThread) threads[i];
			if (thread.isSuspended()) {
				List frames= thread.computeStackFrames();
				JDIStackFrame dropFrame= null;
				for (int j= frames.size() - 1; j >= 0; j--) {
					JDIStackFrame f= (JDIStackFrame) frames.get(j);
					if (replacedClassNames.contains(f.getDeclaringTypeName())) {
						dropFrame = f;
						break;
					}
				}
				if (dropFrame == null) {
					// No frame to drop to in this thread
					continue;
				}
				if (dropFrame.supportsDropToFrame()) {
					dropFrames.add(dropFrame);
				} else {
					// if any thread that should drop does not support the drop,
					// do not drop in any threads.
					for (int j= 0; j < numThreads; j++) {
						notifyFailedDrop(((JDIThread)threads[i]).computeStackFrames(), replacedClassNames);
					}
					throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
						DebugException.NOT_SUPPORTED, JDIDebugModelMessages.getString("JDIStackFrame.Drop_to_frame_not_supported"), null)); //$NON-NLS-1$
				}
			}
		}
		
		// All threads that want to drop to frame are able. Proceed with the drop
		Iterator iter= dropFrames.iterator();
		IJavaStackFrame dropFrame= null;
		while (iter.hasNext()) {
			try {
				dropFrame= ((IJavaStackFrame)iter.next());
				dropFrame.dropToFrame();
			} catch (DebugException de) {
				notifyFailedDrop(((JDIThread)dropFrame.getThread()).computeStackFrames(), replacedClassNames);
			}
		}
	}
	
	private void notifyFailedDrop(List frames, List replacedClassNames) throws DebugException {
		JDIStackFrame frame;
		Iterator iter= frames.iterator();
		while (iter.hasNext()) {
			frame= (JDIStackFrame) iter.next();
			if (replacedClassNames.contains(frame.getDeclaringTypeName())) {
				frame.setOutOfSynch(true);
			}
		}
	}
	/**
	 * Returns the changed class files in the delta or <code>null</code> if none.
	 */
	protected List getChangedClassFiles(IResourceDelta delta) {
		if (delta == null) {
			return new ArrayList(0);
		}
		fVisitor.reset();
		try {
			delta.accept(fVisitor);
		} catch (CoreException e) {
			JDIDebugPlugin.logError(e);
			return new ArrayList(0); // quiet failure
		}
		return fVisitor.getChangedClassFiles();
	}
	/**
	 * A visitor which collects changed class files.
	 */
	class ChangedClassFilesVisitor implements IResourceDeltaVisitor {
		/**
		 * The collection of changed class files.
		 */
		protected List fFiles= null;
		/**
		 * Answers whether children should be visited.
		 * <p>
		 * If the associated resource is a class file which 
		 * has been changed, record it.
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null || 0 == (delta.getKind() & IResourceDelta.CHANGED)) {
				return false;
			}
			IResource resource= delta.getResource();
			if (resource != null) {
				switch (resource.getType()) {
					case IResource.FILE :
						if (0 == (delta.getFlags() & IResourceDelta.CONTENT))
							return false;
						if (CLASS_FILE_EXTENSION.equals(resource.getFullPath().getFileExtension())) {
							IMarker[] problemMarkers= null;
							boolean hasCompilerErrors= false;
							try {
								// Get the source file associated with the class file
								// and query it for compilation errors
								IResource sourceFile= getSourceFile(resource);
								if (sourceFile != null) {
									problemMarkers= sourceFile.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
									for (int i= 0; i < problemMarkers.length; i++) {
										if (problemMarkers[i].getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR)
											hasCompilerErrors= true;
									}
								}
							} catch (CoreException exception) {
							}
							if (hasCompilerErrors) {
								// Only return class files that have no compilation errors.
								fFiles.add(resource);
							}
						}
						return false;
						
					default :
						return true;
				}
			}
			return true;
		}
		/**
		 * Resets the file collection to empty
		 */
		public void reset() {
			fFiles = new ArrayList();
		}
		
		/**
		 * Answers a collection of changed class files or <code>null</code>
		 */
		public List getChangedClassFiles() {
			return fFiles;
		}
		
		/**
		 * Returns the source file associated with the given class file
		 * 
		 * XXX: Not yet implemented
		 */
		private IResource getSourceFile(IResource classFile) {
			return null;
		}
	}
	
	/**
	 * Adds the given listener to the collection of hot code replace listeners.
	 * Listeners are notified when hot code replace attempts succeed or fail.
	 */
	public void addHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		fHotCodeReplaceListeners.add(listener);
	}
	
	/**
	 * Removes the given listener from the collection of hot code replace listeners.
	 * Once a listener is removed, it will no longer be notified of hot code replace
	 * attempt successes or failures.
	 */
	public void removeHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		fHotCodeReplaceListeners.remove(listener);
	}
	
	protected void fork(final IWorkspaceRunnable wRunnable) {
		Runnable runnable= new Runnable() {
			public void run() {
				try {
					getWorkspace().run(wRunnable, null);
				} catch (CoreException ce) {
					JDIDebugPlugin.logError(ce);
				}
			}
		};
		new Thread(runnable).start();
	}
	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}
	/**
	 * Begin listening for resource changes when a launch is
	 * registered with a hot swapable target.
	 * 
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		IDebugTarget[] debugTargets= launch.getDebugTargets();
		for (int i = 0; i < debugTargets.length; i++) {
			if (debugTargets[i] instanceof JDIDebugTarget) {
				JDIDebugTarget target = (JDIDebugTarget)debugTargets[i];
				if (target.supportsHotCodeReplace()) {
					addHotSwapTarget(target);
				} else {
					addNonHotSwapTarget(target);
				}				
			}
		}
		if (!fHotSwapTargets.isEmpty() || !fNoHotSwapTargets.isEmpty()) {
			getWorkspace().addResourceChangeListener(this);
		}
	}
	
	/**
	 * Begin listening for resource changes when a launch is
	 * registered with a hot swapable target.
	 * 
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
		launchAdded(launch);
	}	
	
	public void handleDebugEvent(DebugEvent event) {
		if (event.getSource() instanceof JDIDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
			deregisterTarget((JDIDebugTarget) event.getSource());
		}	
	}
	
	protected void deregisterTarget(JDIDebugTarget target) {
		// Remove the target from its hot swap target cache.
		if (!fHotSwapTargets.remove(target)) {
			fNoHotSwapTargets.remove(target);
		}
		ILaunch[] launches= DebugPlugin.getDefault().getLaunchManager().getLaunches();		
		// If there are no more active JDIDebugTargets, stop
		// listening to resource changes.
		for (int i= 0; i < launches.length; i++) {
			if (launches[i].getDebugTarget() instanceof JDIDebugTarget) {
				JDIDebugTarget launchTarget= (JDIDebugTarget) launches[i].getDebugTarget();
				if (!launchTarget.isDisconnected() && !launchTarget.isTerminated()) {
					return;
				}
			}
		}
		// To get here, there must be no JDIDebugTargets
		getWorkspace().removeResourceChangeListener(this);
	}
	
	/**
	 * Adds the given target to the list of hot-swappable targets.
	 * Has no effect if the target is alread registered.
	 * 
	 * @param target a target that supports hot swap
	 */
	protected void addHotSwapTarget(JDIDebugTarget target) {
		if (!fHotSwapTargets.contains(target)) {
			fHotSwapTargets.add(target);
		}
	}
	
	/**
	 * Adds the given target to the list of non hot-swappable targets.
	 * Has no effect if the target is alread registered.
	 * 
	 * @param target a target that does not support hot swap
	 */
	protected void addNonHotSwapTarget(JDIDebugTarget target) {
		if (!fNoHotSwapTargets.contains(target)) {
			fNoHotSwapTargets.add(target);
		}
	}	
}

