package org.eclipse.jdt.internal.debug.core.hcr;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.JDIDebugModel;
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
	 * The time that the last build occurred.
	 * Calculated by tracking the time between classfile changes.
	 */
	private long fLastBuildTime= new Date().getTime();
	/**
	 * The time that the last classfile change notification was received
	 */
	private long fLastChangeTime= new Date().getTime();
	
	/**
	 * Map of projects to their builders
	 */
	private Map fProjectToBuilder= new HashMap();
	
	/**
	 * Visitor for resource deltas.
	 */
	protected ChangedClassFilesVisitor fVisitor = new ChangedClassFilesVisitor();
	
	/**
	 * Registers a build watcher for the given project
	 */
	public void registerBuilder(ProjectBuildWatcher watcher, IProject project) {
		fProjectToBuilder.put(project, watcher);
	}
	
	/**
	 * Creates a new HCR manager
	 */
	private JavaHotCodeReplaceManager() {
	}
	/**
	 * Returns the singleton HCR manager
	 */
	public static JavaHotCodeReplaceManager getDefault() {
		if (fgInstance == null) {
			fgInstance= new JavaHotCodeReplaceManager();
			fgInstance.startup();
		}
		return fgInstance;
	}
	/**
	 * Registers this HCR manager as a resource change listener. This method
	 * is called by the JDI debug model plugin on startup.
	 */
	public void startup() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		// Register build watchers with each project
		try {
			IProject[] projects= getWorkspace().getRoot().getProjects();
			int numProjects= projects.length;
			IProject project= null;
			for (int i= 0;  i < numProjects; i++) {
				project= projects[i];
				IProjectDescription description = project.getDescription();
				ICommand buildWatcherCommand = getBuildWatcherCommand(description);
	
				if (buildWatcherCommand == null) {
		
					// Add a Java command to the build spec
					ICommand command = description.newCommand();
					command.setBuilderName(ProjectBuildWatcher.BUILDER_ID);
					setBuildWatcherCommand(project, command);
				}
			}
		} catch (CoreException exception) {
		}
	}
	
	/**
	 * Find the specific build watcher command amongst the build spec of a given description
	 */
	private ICommand getBuildWatcherCommand(IProjectDescription description) throws CoreException {

		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(ProjectBuildWatcher.BUILDER_ID)) {
				return commands[i];
			}
		}
		return null;
	}
	
	/**
	 * Update the build watcher command in the build spec (replace existing one if present,
	 * add one first if none).
	 */
	private void setBuildWatcherCommand(IProject project, ICommand newCommand) throws CoreException {

		IProjectDescription description= project.getDescription();
		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldBuildWatcherCommand = getBuildWatcherCommand(description);
		ICommand[] newCommands;

		if (oldBuildWatcherCommand == null) {
			// Add a build watcher spec to the project
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
			newCommands[oldCommands.length] = newCommand;
		} else {
			for (int i = 0, max = oldCommands.length; i < max; i++) {
				if (oldCommands[i] == oldBuildWatcherCommand) {
					oldCommands[i] = newCommand;
					break;
				}
			}
			newCommands = oldCommands;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		project.setDescription(description, null);
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
		MultiStatus ms= new MultiStatus(JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), DebugException.TARGET_REQUEST_FAILED, JDIDebugHCRMessages.getString("JavaHotCodeReplaceManager.drop_to_frame_failed"), null); //$NON-NLS-1$
		Iterator iter= targets.iterator();
		while (iter.hasNext()) {
			JDIDebugTarget target= (JDIDebugTarget) iter.next();
			List poppedThreads= new ArrayList();
			try {
				if (target.isTerminated() || target.isDisconnected()) {
					continue;
				}
				boolean framesPopped= false;
				if (target.getVM().canPopFrames()) {
					// JDK 1.4 drop to frame support:
					// JDK 1.4 spec is faulty around methods that have
					// been rendered obsolete after class redefinition.
					// Thus, pop the frames that contain affected methods
					// *before* the class redefinition to avoid problems.
					try {
						attemptPopFrames(target, qualifiedNames, poppedThreads);
						framesPopped= true; // No exception occurred
					} catch (DebugException de) {
						ms.merge(de.getStatus());
					}
				}
				target.typesHaveChanged(resources, qualifiedNames);
				if (target.getVM().canPopFrames() && framesPopped) {
					// Second half of JDK 1.4 drop to frame support:
					// All affected frames have been popped and the classes
					// have been reloaded. Step into the first changed
					// frame of each affected thread.
					try {
						attemptStepIn(poppedThreads);
					} catch (DebugException de) {
						ms.merge(de.getStatus());
					}
				} else {
					// J9 drop to frame support:
					// After redefining classes, drop to frame
					attemptDropToFrame(target, qualifiedNames);
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
	 * 
	 * @param target the debug target in which frames are to be dropped
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
					JDIStackFrame frame= (JDIStackFrame) frames.get(j);
					if (replacedClassNames.contains(frame.getDeclaringTypeName())) {
						// smart drop to frame support
						ICompilationUnit compilationUnit= getCompilationUnit(frame);
						try {
							IMethod method= getMethod(frame);
							if (method != null) {
								CompilationUnitDelta delta= new CompilationUnitDelta(compilationUnit, fLastBuildTime);
								if (!delta.hasChanged(method)) {
									continue;
								}
							}
						} catch (CoreException exception) {
							// If smart drop to frame fails, just do type-based drop	
						}				
						dropFrame = frame;
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
						DebugException.NOT_SUPPORTED, JDIDebugHCRMessages.getString("JDIStackFrame.Drop_to_frame_not_supported"), null)); //$NON-NLS-1$
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
	
	/**
	 * Looks for the deepest effected stack frame in the stack
	 * and forces a drop to frame.  Does this for all of the active
	 * stack frames in the target.
	 * 
	 * @param target the debug target in which frames are to be dropped
	 * @param replacedClassNames the classes that have been redefined
	 * @param poppedThreads a list of the threads in which frames
	 *        were popped.This parameter may have entries added by this method
	 */
	protected void attemptPopFrames(JDIDebugTarget target, List replacedClassNames, List poppedThreads) throws DebugException {
		List popFrames= getFramesToPop(target.getThreads(), replacedClassNames);

		// All threads that want to drop to frame are able. Proceed with the drop
		JDIStackFrame popFrame= null;
		Iterator iter= popFrames.iterator();
		while (iter.hasNext()) {
			try {
				popFrame= ((JDIStackFrame)iter.next());
				popFrame.popFrame();
				poppedThreads.add(popFrame.getThread());
			} catch (DebugException de) {
				poppedThreads.remove(popFrame.getThread());
				notifyFailedDrop(((JDIThread)popFrame.getThread()).computeStackFrames(), replacedClassNames);
			}
		}
	}
	
	/**
	 * Returns a list of frames which should be popped in the given threads.
	 */
	protected List getFramesToPop(IThread[] threads, List replacedClassNames) throws DebugException {
		JDIThread thread= null;
		JDIStackFrame dropFrame= null;
		List popFrames= new ArrayList();
		int numThreads= threads.length;
		for (int i = 0; i < numThreads; i++) {
			thread= (JDIThread) threads[i];
			if (thread.isSuspended()) {
				dropFrame= getDropFrame(thread, replacedClassNames);
				if (dropFrame == null) {
					// No frame to drop to in this thread
					continue;
				}
				if (dropFrame.supportsDropToFrame()) {
					popFrames.add(dropFrame);
				} else {
					// if any thread that should drop does not support the drop,
					// do not drop in any threads.
					for (int j= 0; j < numThreads; j++) {
						notifyFailedDrop(((JDIThread)threads[i]).computeStackFrames(), replacedClassNames);
					}
					throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
						DebugException.NOT_SUPPORTED, JDIDebugHCRMessages.getString("JDIStackFrame.Drop_to_frame_not_supported"), null)); //$NON-NLS-1$
				}
			}
		}
		return popFrames;
	}
	
	/**
	 * Returns the stack frame that should be dropped to in the
	 * given thread after a hot code replace.
	 * This is calculated by determining if the threads contain stack frames
	 * that reside in one of the given replaced class names. If possible, only
	 * stack frames whose methods were directly affected (and not simply all frames
	 * in affected types) will be returned.
	 */
	protected JDIStackFrame getDropFrame(JDIThread thread, List replacedClassNames) throws DebugException {
		List frames= thread.computeStackFrames();
		JDIStackFrame dropFrame= null;
		JDIStackFrame frame= null;
		ICompilationUnit compilationUnit= null;
		IMethod method= null;
		CompilationUnitDelta delta= null;
		IProject project= null;
		ProjectBuildWatcher builder= null;
		for (int j= frames.size() - 1; j >= 0; j--) {
			frame= (JDIStackFrame) frames.get(j);
			if (replacedClassNames.contains(frame.getDeclaringTypeName())) {
				// smart drop to frame support
				compilationUnit= getCompilationUnit(frame);
				try {
					project= compilationUnit.getCorrespondingResource().getProject();
					method= getMethod(frame);
					builder= (ProjectBuildWatcher)fProjectToBuilder.get(project);
					if (method != null && builder != null) {
						delta= new CompilationUnitDelta(compilationUnit, builder.getLastBuildTime());
						if (!delta.hasChanged(method)) {
							continue;
						}
					}
				} catch (CoreException exception) {
					// If smart drop to frame fails, just do type-based drop	
				}
				if (frame.supportsDropToFrame()) {
					dropFrame= frame;
				} else {
					// The frame we wanted to drop to cannot be popped.
					// Set the drop frame to the next lowest (poppable)
					// frame on the stack.
					while (j > 0) {
						j--;
						frame= (JDIStackFrame) frames.get(j);
						if (frame.supportsDropToFrame()) {
							dropFrame= frame;
							break;
						}
					}
					break;
				}
			}
		}
		return dropFrame;
	}
	
	/**
	 * Performs a "step into" operation on the given threads.
	 */
	protected void attemptStepIn(List threads) throws DebugException {
		Iterator iter= threads.iterator();
		while (iter.hasNext()) {
			((JDIThread) iter.next()).stepInto();;
		}
	}
	
	/**
	 * Returns the compilation unit associated with this
	 * Java stack frame. Returns <code>null</code> for a binary
	 * stack frame.
	 */
	protected ICompilationUnit getCompilationUnit(IJavaStackFrame frame) {
		ILaunch launch= frame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null) {
			return null;
		}
		Object sourceElement= locator.getSourceElement(frame);
		if (sourceElement instanceof IType) {
			return (ICompilationUnit)((IType)sourceElement).getCompilationUnit();
		}
		if (sourceElement instanceof ICompilationUnit) {
			return (ICompilationUnit)sourceElement;
		}
		return null;
	}
	
	/**
	 * Returns the method in which this stack frame is
	 * suspended
	 */
	public IMethod getMethod(JDIStackFrame frame) throws CoreException {
		StringBuffer methodName= new StringBuffer(frame.getDeclaringTypeName());
		methodName.append('.');
		methodName.append(frame.getMethodName());
		return new MethodFinder().getMethod(methodName.toString());
	}
	
	/**
	 * Utility class which searches for an IMethod with a
	 * search engine by collecting search results from
	 * the engine
	 */
	class MethodFinder implements IJavaSearchResultCollector {
		/**
		 * The method caught by the search result collector
		 */
		IMethod fMethod= null;
		public IMethod getMethod(String methodName) {
			SearchEngine engine= new SearchEngine();
			IWorkspace workspace= getWorkspace();
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
			try {
				engine.search(workspace, methodName, IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope, this);
			} catch (JavaModelException exception) {
			}
			return fMethod;
		}
		
		public void accept(IResource resource, int start, int end, IJavaElement enclosingElement, int accuracy)	throws CoreException {
				if (accuracy != IJavaSearchResultCollector.EXACT_MATCH) {
					return;
				}
				if (enclosingElement instanceof IMethod) {
					fMethod= (IMethod) enclosingElement;
				}
			}
			
			public void aboutToStart() {}
			public void done() {}
			public IProgressMonitor getProgressMonitor() {
				return null;
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
							boolean hasCompileErrors= false;
							try {
								// Get the source file associated with the class file
								// and query it for compilation errors
								IResource sourceFile= getSourceFile(resource);
								if (sourceFile != null) {
									problemMarkers= sourceFile.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
									for (int i= 0; i < problemMarkers.length; i++) {
										if (problemMarkers[i].getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR)
											hasCompileErrors= true;
									}
								}
							} catch (CoreException exception) {
							}
							if (!hasCompileErrors) {
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

