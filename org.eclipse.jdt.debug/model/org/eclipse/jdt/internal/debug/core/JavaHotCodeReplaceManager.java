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
		final List j9HotSwapTargets= getJ9HotSwapTargets();
		final List jdkHotSwapTargets= getJDKHotSwapTargets();
		if (j9HotSwapTargets.isEmpty() && jdkHotSwapTargets.isEmpty()) {
			return;
		}
		final List typeNames= JDTDebugUtils.getQualifiedNames(resources);
		if (!j9HotSwapTargets.isEmpty()) {
			IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					notifyJ9(j9HotSwapTargets, resources, typeNames);
				}
			};
			fork(wRunnable);	
		}
		if (!jdkHotSwapTargets.isEmpty()) {
			IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					notifyJDK(jdkHotSwapTargets, resources, typeNames);
				}
			};
			fork(wRunnable);	
		}
	}
	
	/**
	 * Returns the currently registered debug targets that support
	 * J9 hot code replace, or <code>null</code> if none.
	 */
	protected List getJ9HotSwapTargets() {
		return getHotSwapTargets(false);
	}
	
	/**
	 * Returns the currently registered debug targets that support
	 * JDK 1.4 hot code replace, or <code>null</code> if none.
	 */	
	protected List getJDKHotSwapTargets() {
		return getHotSwapTargets(true);
	}

	/**
	 * Returns the currently registered debug targets that support
	 * hot code replace, or <code>null</code> if none.
	 * 
	 * @param jdk <code>true</code> if this method will return targets
	 *  which support JDK 1.4 hot code replace, <code>false</code> if
	 *  this method will return targets which support J9 hot code replace.
	 */
	protected List getHotSwapTargets(boolean jdk) {
		List hotSwapTargets = new ArrayList(0);
		DebugPlugin plugin= DebugPlugin.getDefault();
		IDebugTarget[] allTargets= plugin.getLaunchManager().getDebugTargets();
		boolean supports;
		for (int i= 0; i < allTargets.length; i++) {
			supports= false;
			IDebugTarget target= allTargets[i];
			if (target instanceof JDIDebugTarget) {
				JDIDebugTarget javaTarget= (JDIDebugTarget) target;
				if ((jdk && javaTarget.supportsJDKHotCodeReplace()) || 
					(!jdk && javaTarget.supportsJ9HotCodeReplace())) {
						supports= true;
				}
				if (supports) {
					hotSwapTargets.add(target);
				}
			}
		}
		return hotSwapTargets;
	}

	/**
	 * Notifies the J9 targets of the changed types.
	 */
	protected void notifyJ9(List targets, List resources, List typeNames) {
		notify(targets, resources, typeNames, false);
	}
	
	/**
	 * Notifies the JDK targets of the changed types
	 */	
	protected void notifyJDK(List target, List resources, List typeNames) {
		notify(target, resources, typeNames, true);
	}
	
	/**
	 * Notifies the targets of the changed types
	 * 
	 * @param jdk <code>true</code> if this method will use JDK-style
	 * 	HCR notification, <code>false</code> if this method will use
	 *  J9-style notification.
	 */
	private void notify(List targets, List resources, List typeNames, boolean jdk) {
		String[] qNames = (String[]) typeNames.toArray(new String[typeNames.size()]);		
		Iterator iter= targets.iterator();
		while (iter.hasNext()) {
			JDIDebugTarget target= (JDIDebugTarget) iter.next();
			try {
				if (jdk) {
					// JDK 1.4 support
					target.typesHaveChanged(resources);
				} else {
					// J9 HCR support
					target.typesHaveChanged(qNames);
				}
				attemptDropToFrame(target, typeNames);
				fireHCRSucceeded();
			} catch (DebugException de) {
				// target update failed
				JDIDebugPlugin.logError(de);
				fireHCRFailed(de);
			}
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
				if (null != dropFrame && dropFrame.supportsDropToFrame()) {
					dropFrame.dropToFrame();
				}
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
			if (0 == (delta.getKind() & IResourceDelta.CHANGED))
				return false;
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
	 * When a launch is deregistered, check if there are any
	 * other launches registered. If not, stop listening
	 * to resource changes.
	 */
	public void launchDeregistered(ILaunch launch) {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= manager.getLaunches();
		if (launches.length < 1) {
			getWorkspace().removeResourceChangeListener(this);
		}
	}

	/**
	 * Begin listening for resource changes when a launch is
	 * registered.
	 */
	public void launchRegistered(ILaunch launch) {
		getWorkspace().addResourceChangeListener(this);
	}

}

