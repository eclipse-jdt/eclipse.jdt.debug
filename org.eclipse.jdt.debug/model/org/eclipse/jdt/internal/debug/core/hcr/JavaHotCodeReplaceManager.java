package org.eclipse.jdt.internal.debug.core.hcr;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

/**
 * The hot code replace manager listens for changes to
 * class files and notifies running debug targets of the changes.
 * <p>
 * Currently, replacing .jar files has no effect on running targets.
 */
public class JavaHotCodeReplaceManager implements IResourceChangeListener, ILaunchListener, IDebugEventSetListener {
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
	 * A mapping of the last time projects were built.
	 * <ol>
	 * <li>key: project (IProject)</li>
	 * <li>value: build date (ProjectBuildTime)</li>
	 * </ol>
	 */
	private Map fProjectBuildTimes= new HashMap();
	private static Date fStartupDate= new Date();
	/**
	 * Utility object used for tracking build times of projects.
	 * The HCR manager receives notification of builds AFTER
	 * the build has occurred but BEFORE the classfile
	 * resource changed deltas are fired. Thus, when the
	 * current build time is set, we need to hang onto
	 * the last build time so that we can use the last build
	 * time for comparing changes to compilation units (for smart
	 * drop to frame).
	 */
	class ProjectBuildTime {
		private Date fCurrentDate= new Date();
		private Date fPreviousDate= new Date();
		
		public void setCurrentBuildDate(Date date) {
			fPreviousDate= fCurrentDate;
			fCurrentDate= date;
		}
		
		public void setLastBuildDate(Date date) {
			fPreviousDate= date;
			if (fPreviousDate.getTime() > fCurrentDate.getTime()) {
				// If the previous date is set later than the current
				// date, move the current date up to the previous.
				fCurrentDate= fPreviousDate;
			}
		}
		
		/**
		 * Returns the last build time
		 */
		public Date getLastBuildDate() {
			return fPreviousDate;
		}
	}
	
	protected BuiltProjectVisitor fProjectVisitor= new BuiltProjectVisitor();
	
	/**
	 * Visitor for resource deltas.
	 */
	protected ChangedClassFilesVisitor fClassfileVisitor = new ChangedClassFilesVisitor();
	
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
	}
	
	/**
	 * Deregisters this HCR manager as a resource change listener. Removes all hot
	 * code replace listeners. This method is called by the JDI debug model plugin
	 * on shutdown.
	 */
	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		getWorkspace().removeResourceChangeListener(this);
		fHotCodeReplaceListeners.removeAll();
		fHotSwapTargets= null;
		fNoHotSwapTargets= null;
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
		List projects= getBuiltProjects(event);
		if (!projects.isEmpty()) {
			updateProjectBuildTime(projects);
		}
		List resources= getChangedClassFiles(event);
		if (!resources.isEmpty()) {
			notifyTargets(resources);
		}
	}
	
	/**
	 * Returns all projects which this event says may have been built.
	 */
	protected List getBuiltProjects(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (event.getType() != IResourceChangeEvent.POST_AUTO_BUILD || delta == null) {
			return Collections.EMPTY_LIST;
		}
		fProjectVisitor.reset();
		try {
			delta.accept(fProjectVisitor);
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
			return Collections.EMPTY_LIST;
		}
		return fProjectVisitor.getBuiltProjects();
	}
	
	/**
	 * If the given event contains a build notification, update the
	 * last build time of the corresponding project
	 */
	private void updateProjectBuildTime(List projects) {
		Iterator iter= projects.iterator();
		IProject project= null;
		Date currentDate= new Date();
		ProjectBuildTime buildTime= null;
		while (iter.hasNext()) {
			project= (IProject) iter.next();
			buildTime= (ProjectBuildTime)fProjectBuildTimes.get(project);
			if (buildTime == null) {
				buildTime= new ProjectBuildTime();
				fProjectBuildTimes.put(project, buildTime);
			}
			buildTime.setCurrentBuildDate(currentDate);
		}
	}
	
	/**
	 * Returns the last known build time for the given project.
	 * If no build time is known for the given project, the 
	 * last known build time for the project is set to the 
	 * hot code replace manager's startup time.
	 */
	protected long getLastProjectBuildTime(IProject project) {
		ProjectBuildTime time= (ProjectBuildTime)fProjectBuildTimes.get(project);
		if (time == null) {
			time= new ProjectBuildTime();
			time.setLastBuildDate(fStartupDate);
			fProjectBuildTimes.put(project, time);
		}
		return time.getLastBuildDate().getTime();
	}
	
	/**
	 * Notifies the targets of the changed types
	 */
	private void notifyTargets(final List resources) {
		final List hotSwapTargets= getHotSwapTargets();
		final List noHotSwapTargets= getNoHotSwapTargets();
		final List qualifiedNames= getQualifiedNames(resources);
		if (!hotSwapTargets.isEmpty()) {
			IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					doHotCodeReplace(hotSwapTargets, resources, qualifiedNames);
				}
			};
			fork(wRunnable);	
		}
		if (!noHotSwapTargets.isEmpty()) {
			IWorkspaceRunnable wRunnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					notifyUnsupportedHCR(noHotSwapTargets, resources, qualifiedNames);
				}
			};
			fork(wRunnable);
		}
	}
	
	/**
	 * Notify the given targets that HCR failed for classes
	 * with the given fully qualified names.
	 */
	protected void notifyUnsupportedHCR(List targets, List resources, List qualifiedNames) {
		Iterator iter= targets.iterator();
		JDIDebugTarget target= null;
		while (iter.hasNext()) {	
			target= (JDIDebugTarget) iter.next();	
			fireHCRFailed(target, null);
			notifyFailedHCR(target, resources, qualifiedNames);
		}
	}
	
	protected void notifyFailedHCR(JDIDebugTarget target, List resources, List qualifiedNames) {
		if (target.isAvailable()) {
			target.typesFailedHCR(qualifiedNames);
			target.fireChangeEvent(DebugEvent.STATE);
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
	
	/**
	 * Perform a hot code replace with the given resources.
	 * For a JDK 1.4 compliant VM this involves:
	 * <ol>
	 * <li>Popping all frames from all thread stacks which will be affected by reloading the given resources</li>
	 * <li>Telling the VirtualMachine to redefine the affected classes</li>
	 * <li>Performing a step-into operation on all threads which were affected by the class redefinition.
	 *     This returns execution to the first (deepest) affected method on the stack</li>
	 * </ol>
	 * For a J9 compliant VM this involves:
	 * <ol>
	 * <li>Telling the VirtualMachine to redefine the affected classes</li>
	 * <li>Popping all frames from all thread stacks which were affected by reloading the given resources and then
	 *     performing a step-into operation on all threads which were affected by the class redefinition.</li>
	 * </ol>
	 * 
	 * @param targets the targets in which to perform HCR
	 * @param resources the resources which correspond to the changed classes
	 */
	private void doHotCodeReplace(List targets, List resources, List qualifiedNames) {
		MultiStatus ms= new MultiStatus(JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(), DebugException.TARGET_REQUEST_FAILED, JDIDebugHCRMessages.getString("JavaHotCodeReplaceManager.drop_to_frame_failed"), null); //$NON-NLS-1$
		Iterator iter= targets.iterator();
		while (iter.hasNext()) {
			JDIDebugTarget target= (JDIDebugTarget) iter.next();
			List poppedThreads= new ArrayList();
			try {
				if (!target.isAvailable()) {
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
						attemptPopFrames(target, resources, qualifiedNames, poppedThreads);
						framesPopped= true; // No exception occurred
					} catch (DebugException de) {
						ms.merge(de.getStatus());
					}
				}
				target.typesHaveChanged(resources, qualifiedNames);
				if (containsObsoleteMethods(target)) {
					fireObsoleteMethods(target);
				}
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
					attemptDropToFrame(target, resources, qualifiedNames);
				}
				fireHCRSucceeded(target);
			} catch (DebugException de) {
				// target update failed
				fireHCRFailed(target, de);
			}
			target.fireChangeEvent(DebugEvent.CONTENT);
		}
		if (!ms.isOK()) {
			JDIDebugPlugin.log(ms);
		}
	}
	
	/**
	 * Notifies listeners that a hot code replace attempt succeeded
	 */
	private void fireHCRSucceeded(IJavaDebugTarget target) {
		Object[] listeners= fHotCodeReplaceListeners.getListeners();
		for (int i=0; i<listeners.length; i++) {
			((IJavaHotCodeReplaceListener)listeners[i]).hotCodeReplaceSucceeded(target);
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
	 * Notifies listeners that obsolete methods remain on the stack
	 */
	private void fireObsoleteMethods(JDIDebugTarget target) {
		Object[] listeners= fHotCodeReplaceListeners.getListeners();
		for (int i=0; i<listeners.length; i++) {
			((IJavaHotCodeReplaceListener)listeners[i]).obsoleteMethods(target);
		}
	}
	
	/**
	 * Looks for the deepest effected stack frame in the stack
	 * and forces a drop to frame.  Does this for all of the active
	 * stack frames in the target.
	 * 
	 * @param target the debug target in which frames are to be dropped
	 * @param replacedClassNames the classes that have been redefined
	 */
	protected void attemptDropToFrame(JDIDebugTarget target, List resources, List replacedClassNames) throws DebugException {
		List dropFrames= getAffectedFrames(target.getThreads(), resources, replacedClassNames);

		// All threads that want to drop to frame are able. Proceed with the drop
		JDIStackFrame dropFrame= null;
		Iterator iter= dropFrames.iterator();
		while (iter.hasNext()) {
			try {
				dropFrame= ((JDIStackFrame)iter.next());
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
	protected void attemptPopFrames(JDIDebugTarget target, List resources, List replacedClassNames, List poppedThreads) throws DebugException {
		List popFrames= getAffectedFrames(target.getThreads(), resources, replacedClassNames);

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
	 * Returns whether or not the given target contains stack frames with obsolete
	 * methods.
	 */
	protected boolean containsObsoleteMethods(JDIDebugTarget target) throws DebugException {
		IThread[] threads=target.getThreads();
		List frames= null;
		Iterator iter= null;
		for (int i= 0, numThreads= threads.length; i < numThreads; i++) {
			frames= ((JDIThread)threads[i]).computeNewStackFrames();
			iter= frames.iterator();
			while (iter.hasNext()) {
				if (((JDIStackFrame)iter.next()).isObsolete()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns a list of frames which should be popped in the given threads.
	 */
	protected List getAffectedFrames(IThread[] threads, List resourceList, List replacedClassNames) throws DebugException {
		JDIThread thread= null;
		JDIStackFrame affectedFrame= null;
		List popFrames= new ArrayList();
		int numThreads= threads.length;
		IResource[] resources= new IResource[resourceList.size()];
		resourceList.toArray(resources);
		for (int i = 0; i < numThreads; i++) {
			thread= (JDIThread) threads[i];
			if (thread.isSuspended()) {
				affectedFrame= getAffectedFrame(thread, resources, replacedClassNames);
				if (affectedFrame == null) {
					// No frame to drop to in this thread
					continue;
				}
				if (affectedFrame.supportsDropToFrame()) {
					popFrames.add(affectedFrame);
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
	protected JDIStackFrame getAffectedFrame(JDIThread thread, IResource[] resources, List replacedClassNames) throws DebugException {
		List frames= thread.computeStackFrames();
		JDIStackFrame affectedFrame= null;
		JDIStackFrame frame= null;
		ICompilationUnit compilationUnit= null;
		IMethod method= null;
		CompilationUnitDelta delta= null;
		IProject project= null;
		for (int j= frames.size() - 1; j >= 0; j--) {
			frame= (JDIStackFrame) frames.get(j);
			if (containsChangedType(frame, replacedClassNames)) {
				// smart drop to frame support
				compilationUnit= getCompilationUnit(frame);
				try {
					project= compilationUnit.getCorrespondingResource().getProject();
					method= getMethod(frame, resources);
					if (method != null) {
						delta= new CompilationUnitDelta(compilationUnit, getLastProjectBuildTime(project));
						if (!delta.hasChanged(method)) {
							continue;
						}
					}
				} catch (CoreException exception) {
					// If smart drop to frame fails, just do type-based drop	
				}

				if (frame.supportsDropToFrame()) {
					affectedFrame= frame;
					break;
				} else {
					// The frame we wanted to drop to cannot be popped.
					// Set the affected frame to the next lowest poppable
					// frame on the stack.
					while (j > 0) {
						j--;
						frame= (JDIStackFrame) frames.get(j);
						if (frame.supportsDropToFrame()) {
							affectedFrame= frame;
							break;
						}
					}
					break;
				}
			}
		}
		return affectedFrame;
	}
	
	/**
	 * Returns whether the given frame's declaring type was changed
	 * based on the given list of changed class names.
	 */
	protected boolean containsChangedType(JDIStackFrame frame, List replacedClassNames) throws DebugException {
		String declaringTypeName= frame.getDeclaringTypeName();
		// Check if the frame's declaring type was changed
		if (replacedClassNames.contains(declaringTypeName)) {
			return true;
		}
		// Check if one of the frame's declaring type's inner classes have changed
		Iterator iter= replacedClassNames.iterator();
		int index;
		String className= null;
		while (iter.hasNext()) {
			className= (String) iter.next();
			index= className.indexOf('$');
			if (index > -1 && declaringTypeName.equals(className.substring(0, index))) {
				return true;
			}
		}
		return false;
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
	 * suspended or <code>null</code> if none can be found
	 */
	public IMethod getMethod(JDIStackFrame frame, IResource[] resources) throws CoreException {
		String declaringTypeName= frame.getDeclaringTypeName();
		String methodName= frame.getMethodName();
		String[] arguments= null;
		try {
			arguments= Signature.getParameterTypes(frame.getSignature());
		} catch (IllegalArgumentException exception) {
			// If Signature can't parse the signature, we can't
			// create the method
			return null;
		}
		ICompilationUnit compilationUnit= getCompilationUnit(frame);
		IType type= compilationUnit.getType(getUnqualifiedName(declaringTypeName));;
		if (type != null) {
			return type.getMethod(methodName, arguments);
		}
		return null;
	}
	
	/**
	 * Given a fully qualified name, return the unqualified name.
	 */
	protected String getUnqualifiedName(String qualifiedName) {
		int index= qualifiedName.lastIndexOf('.');
		return qualifiedName.substring(index + 1);
	}
	
	/**
	 * Notify the given frames that a drop to frame has failed after
	 * an HCR with the given class names.
	 */
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
	protected List getChangedClassFiles(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (event.getType() != IResourceChangeEvent.POST_CHANGE || delta == null) {
			return Collections.EMPTY_LIST;
		}
		fClassfileVisitor.reset();
		try {
			delta.accept(fClassfileVisitor);
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
			return Collections.EMPTY_LIST; // quiet failure
		}
		return fClassfileVisitor.getChangedClassFiles();
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
							} catch (CoreException e) {
								JDIDebugPlugin.log(e);
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
	
	class BuiltProjectVisitor implements IResourceDeltaVisitor {
		/**
		 * The collection of built projects
		 */
		protected List fProjects= new ArrayList();
		/**
		 * Answers whether children should be visited.
		 * <p>
		 * If the associated resource is a project which 
		 * has been built, record it.
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null || 0 == (delta.getKind() & IResourceDelta.CHANGED)) {
				return false;
			}
			IResource resource= delta.getResource();
			if (resource != null && resource.getType() == IResource.PROJECT) {
				fProjects.add(resource);
				return false;
			}
			return true;
		}
		/**
		 * Resets the project collection to empty
		 */
		public void reset() {
			fProjects = new ArrayList();
		}
		
		/**
		 * Returns the collection of built projects
		 */
		public List getBuiltProjects() {
			return fProjects;
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
					JDIDebugPlugin.log(ce);
				}
			}
		};
		new Thread(runnable).start();
	}
	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
		IDebugTarget[] debugTargets= launch.getDebugTargets();
		for (int i = 0; i < debugTargets.length; i++) {
			if (debugTargets[i] instanceof JDIDebugTarget) {
				deregisterTarget((JDIDebugTarget)debugTargets[i]);		
			}
		}
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
			getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.POST_AUTO_BUILD);
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
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource() instanceof JDIDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
				deregisterTarget((JDIDebugTarget) event.getSource());
			}	
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
				if (((JDIDebugTarget) launches[i].getDebugTarget()).isAvailable()) {
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
	
	/**
	 * Returns a collection of <code>String</code>s representing
	 * the qualified type names of the given resources. The qualified
	 * names are returned dot separated.
	 * <p>
	 * This method takes into account the output directory of 
	 * Java projects.
	 */
	public static List getQualifiedNames(List resources) {
		List qualifiedNames= new ArrayList(resources.size());
		Iterator itr= resources.iterator();
		IProject project = null;
		IPath outputPath = null;
		IJavaProject javaProject = null;
		while (itr.hasNext()) {
			IResource resource= (IResource) itr.next();
			if (project == null || !resource.getProject().equals(project)) {
				project= resource.getProject();
				javaProject= JavaCore.create(project);
				try {
					outputPath= javaProject.getOutputLocation();
				} catch (JavaModelException e) {
					JDIDebugPlugin.log(e);
					project = null;
					continue;
				}
			}
			IPath resourcePath= resource.getFullPath();
			int count= resourcePath.matchingFirstSegments(outputPath);
			resourcePath= resourcePath.removeFirstSegments(count);
			String pathString= resourcePath.toString();
			// get rid of ".class" then switch to dot separated
			pathString= pathString.substring(0, pathString.length() - 6).replace(IPath.SEPARATOR, '.');
			qualifiedNames.add(pathString);
		}
		return qualifiedNames;
	}

}

