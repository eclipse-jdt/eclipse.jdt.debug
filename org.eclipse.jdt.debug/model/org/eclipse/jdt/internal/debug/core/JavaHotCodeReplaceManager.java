package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 * The hot code replace manager listens for changes to
 * class files and notifies running debug targets of the changes.
 * <p>
 * Currently, replacing .jar files has no effect on running targets.
 */
public class JavaHotCodeReplaceManager implements IResourceChangeListener, ILaunchListener {

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
	}

	/**
	 * Deregisters this HCR manager as a resource change listener. Removes all hot
	 * code replace listeners. This method* is called by the JDI debug model plugin
	 * on shutdown.
	 */
	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
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
			JDIDebugTarget target= (JDIDebugTarget) iter.next();
			if (target.isTerminated() || target.isDisconnected()) {
				target.typesFailedReload(resources, qualifiedNames);
			}
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
				JDIDebugPlugin.logError(de);
				fireHCRFailed(de);
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
	private void fireHCRFailed(DebugException exception) {
		Object[] listeners= fHotCodeReplaceListeners.getListeners();
		for (int i=0; i<listeners.length; i++) {
			((IJavaHotCodeReplaceListener)listeners[i]).hotCodeReplaceFailed(exception);
		}
	}

	/**
	 * Looks for the deepest effected stack frame in the stack
	 * and forces a drop to frame.  Does this for all of the active
	 * stack frames in the target.
	 */
	protected void attemptDropToFrame(IDebugTarget target, List replacedClassNames) throws DebugException {
		IThread[] threads= target.getThreads();
		List dropFrames= new ArrayList(1);
		for (int i = 0; i < threads.length; i++) {
			IThread thread= (IThread) threads[i];
			if (thread.isSuspended()) {
				IStackFrame[] frames= thread.getStackFrames();
				IJavaStackFrame dropFrame= null;
				for (int j= frames.length - 1; j >= 0; j--) {
					IJavaStackFrame f= (IJavaStackFrame) frames[j];
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
					throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
						DebugException.NOT_SUPPORTED, JDIDebugModelMessages.getString("JDIStackFrame.Drop_to_frame_not_supported"), null)); //$NON-NLS-1$
				}
			}
		}
		
		// All threads that want to drop to frame are able. Proceed with the drop
		Iterator iter= dropFrames.iterator();
		while (iter.hasNext()) {
			try {
				((IJavaStackFrame)iter.next()).dropToFrame();
			} catch (DebugException de) {
				continue;
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
						if (CLASS_FILE_EXTENSION.equals(resource.getFullPath().getFileExtension()))
							fFiles.add(resource);
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
	 * @see ILaunchListener#launchDeregistered(ILaunch)
	 * 
	 * When a launch is deregistered, check if there are any
	 * other launches registered. If not, stop listening
	 * to resource changes.
	 */
	public void launchDeregistered(ILaunch launch) {
		if (!(launch instanceof JDIDebugTarget)) {
			return;
		}
		JDIDebugTarget target= (JDIDebugTarget) launch.getDebugTarget();
		ILaunch[] launches= DebugPlugin.getDefault().getLaunchManager().getLaunches();
		// Remove the target from its hot swap target cache.
		if (!fHotSwapTargets.remove(target)) {
			fNoHotSwapTargets.remove(target);
		}
		// If there are no more JDIDebugTargets, stop
		// listening to resource changes.
		for (int i= 0; i < launches.length; i++) {
			if (launches[i] instanceof JDIDebugTarget) {
				return;
			}
		}
		// To get here, there must be no JDIDebugTargets
		getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * @see ILaunchListener#launchRegistered(ILaunch)
	 * 
	 * Begin listening for resource changes when a launch is
	 * registered.
	 */
	public void launchRegistered(ILaunch launch) {
		IDebugTarget debugTarget= launch.getDebugTarget();
		if (!(debugTarget instanceof JDIDebugTarget)) {
			return;
		}
		JDIDebugTarget target= (JDIDebugTarget) debugTarget;
		if (target.supportsHotCodeReplace()) {
			fHotSwapTargets.add(target);
		} else {
			fNoHotSwapTargets.add(target);
		}
		getWorkspace().addResourceChangeListener(this);
	}

}

