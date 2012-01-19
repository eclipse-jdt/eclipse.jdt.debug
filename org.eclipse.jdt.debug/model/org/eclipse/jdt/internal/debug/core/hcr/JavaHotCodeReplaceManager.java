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
package org.eclipse.jdt.internal.debug.core.hcr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ISourceAttribute;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;

/**
 * The hot code replace manager listens for changes to class files and notifies
 * running debug targets of the changes.
 * <p>
 * Currently, replacing .jar files has no effect on running targets.
 */
public class JavaHotCodeReplaceManager implements IResourceChangeListener,
		ILaunchListener, IDebugEventSetListener {
	/**
	 * Singleton
	 */
	private static JavaHotCodeReplaceManager fgInstance = null;
	/**
	 * The class file extension
	 */
	private static final String CLASS_FILE_EXTENSION = "class"; //$NON-NLS-1$

	/**
	 * The list of <code>IJavaHotCodeReplaceListeners</code> which this hot code
	 * replace manager will notify about hot code replace attempts.
	 */
	private ListenerList fHotCodeReplaceListeners = new ListenerList();

	/**
	 * The lists of hot swap targets which support HCR and those which don't
	 */
	private ArrayList<JDIDebugTarget> fHotSwapTargets = new ArrayList<JDIDebugTarget>(1);
	private ArrayList<JDIDebugTarget> fNoHotSwapTargets = new ArrayList<JDIDebugTarget>(1);

	/**
	 * A mapping of the last time projects were built.
	 * <ol>
	 * <li>key: project (IProject)</li>
	 * <li>value: build date (ProjectBuildTime)</li>
	 * </ol>
	 */
	private Map<IProject, ProjectBuildTime> fProjectBuildTimes = new HashMap<IProject, ProjectBuildTime>();
	private static Date fStartupDate = new Date();

	/**
	 * Cache of compilation unit deltas renewed on each HCR attempt.
	 */
	private Map<ICompilationUnit, CompilationUnitDelta> fDeltaCache = new HashMap<ICompilationUnit, CompilationUnitDelta>();

	/**
	 * Utility object used for tracking build times of projects. The HCR manager
	 * receives notification of builds AFTER the build has occurred but BEFORE
	 * the classfile resource changed deltas are fired. Thus, when the current
	 * build time is set, we need to hang onto the last build time so that we
	 * can use the last build time for comparing changes to compilation units
	 * (for smart drop to frame).
	 */
	class ProjectBuildTime {
		private Date fCurrentDate = new Date();
		private Date fPreviousDate = new Date();

		public void setCurrentBuildDate(Date date) {
			fPreviousDate = fCurrentDate;
			fCurrentDate = date;
		}

		public void setLastBuildDate(Date date) {
			fPreviousDate = date;
			if (fPreviousDate.getTime() > fCurrentDate.getTime()) {
				// If the previous date is set later than the current
				// date, move the current date up to the previous.
				fCurrentDate = fPreviousDate;
			}
		}

		/**
		 * Returns the last build time
		 */
		public Date getLastBuildDate() {
			return fPreviousDate;
		}
	}

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
	public static synchronized JavaHotCodeReplaceManager getDefault() {
		if (fgInstance == null) {
			fgInstance = new JavaHotCodeReplaceManager();
		}
		return fgInstance;
	}

	/**
	 * Registers this HCR manager as a resource change listener. This method is
	 * called by the JDI debug model plug-in on startup.
	 */
	public void startup() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * unregisters this HCR manager as a resource change listener. Removes all
	 * hot code replace listeners. This method is called by the JDI debug model
	 * plug-in on shutdown.
	 */
	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		getWorkspace().removeResourceChangeListener(this);
		fHotCodeReplaceListeners = new ListenerList();
		synchronized (this) {
			fHotSwapTargets.clear();
			fNoHotSwapTargets.clear();
		}
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
		List<IProject> projects = getBuiltProjects(event);
		if (!projects.isEmpty()) {
			updateProjectBuildTime(projects);
		}
		synchronized (this) {
			if (fHotSwapTargets.isEmpty() && fNoHotSwapTargets.isEmpty()) {
				// If there are no targets to notify, only update the build
				// times.
				return;
			}
		}
		ChangedClassFilesVisitor visitor = getChangedClassFiles(event);
		if (visitor != null) {
			List<IResource> resources = visitor.getChangedClassFiles();
			List<String> names = visitor.getQualifiedNamesList();
			if (!resources.isEmpty()) {
				notifyTargets(resources, names);
			}
		}
	}

	/**
	 * Returns all projects which this event says may have been built.
	 */
	protected List<IProject> getBuiltProjects(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (event.getType() != IResourceChangeEvent.POST_BUILD || delta == null
				|| event.getBuildKind() == 0) {
			return Collections.EMPTY_LIST;
		}
		if (event.getBuildKind() == IncrementalProjectBuilder.AUTO_BUILD
				&& !ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			// If this is an auto build and the workspace is not autobuilding,
			// no projects will actually be compiled.
			return Collections.EMPTY_LIST;
		}
		Object source = event.getSource();
		if (source instanceof IProject) {
			List<IProject> list = new ArrayList<IProject>();
			list.add((IProject) source);
			return list;
		} else if (source instanceof IWorkspace) {
			IProject[] allProjects = ((IWorkspace) source).getRoot()
					.getProjects();
			return Arrays.asList(allProjects);
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * If the given event contains a build notification, update the last build
	 * time of the corresponding project
	 */
	private void updateProjectBuildTime(List<IProject> projects) {
		Date currentDate = new Date();
		ProjectBuildTime buildTime = null;
		for(IProject project : projects) {
			buildTime = fProjectBuildTimes.get(project);
			if (buildTime == null) {
				buildTime = new ProjectBuildTime();
				fProjectBuildTimes.put(project, buildTime);
			}
			buildTime.setCurrentBuildDate(currentDate);
		}
	}

	/**
	 * Returns the last known build time for the given project. If no build time
	 * is known for the given project, the last known build time for the project
	 * is set to the hot code replace manager's startup time.
	 */
	protected long getLastProjectBuildTime(IProject project) {
		ProjectBuildTime time = fProjectBuildTimes
				.get(project);
		if (time == null) {
			time = new ProjectBuildTime();
			time.setLastBuildDate(fStartupDate);
			fProjectBuildTimes.put(project, time);
		}
		return time.getLastBuildDate().getTime();
	}

	/**
	 * Notifies the targets of the changed types
	 */
	private void notifyTargets(final List<IResource> resources, final List<String> qualifiedNames) {
		final List<JDIDebugTarget> hotSwapTargets = getHotSwapTargets();
		final List<JDIDebugTarget> noHotSwapTargets = getNoHotSwapTargets();
		if (!hotSwapTargets.isEmpty()) {
			Runnable runnable = new Runnable() {
				public void run() {
					doHotCodeReplace(hotSwapTargets, resources, qualifiedNames);
				}
			};
			DebugPlugin.getDefault().asyncExec(runnable);
		}
		if (!noHotSwapTargets.isEmpty()) {
			Runnable runnable = new Runnable() {
				public void run() {
					notifyUnsupportedHCR(noHotSwapTargets, resources,
							qualifiedNames);
				}
			};
			DebugPlugin.getDefault().asyncExec(runnable);
		}
	}

	/**
	 * Filters elements out of the given collections of resources and qualified
	 * names if there is no type corresponding type loaded in the given debug
	 * target. This method allows us to avoid bogus HCR attempts and
	 * "HCR failed" notifications.
	 * 
	 * @param target
	 *            the debug target
	 * @param resources
	 *            the list of resources to filter
	 * @param qualifiedNames
	 *            the list of qualified names to filter, which corresponds to
	 *            the list of resources on a one-to-one-basis
	 */
	private void filterUnloadedTypes(JDIDebugTarget target, List<IResource> resources,
			List<String> qualifiedNames) {
		for (int i = 0, numElements = qualifiedNames.size(); i < numElements; i++) {
			String name = qualifiedNames.get(i);
			List<ReferenceType> list = target.jdiClassesByName(name);
			if (list.isEmpty()) {
				// If no classes with the given name are loaded in the VM, don't
				// waste
				// cycles trying to replace.
				qualifiedNames.remove(i);
				resources.remove(i);
				// Decrement the index and number of elements to compensate for
				// item removal
				i--;
				numElements--;
			}
		}
	}

	/**
	 * Notify the given targets that HCR failed for classes with the given fully
	 * qualified names.
	 */
	protected void notifyUnsupportedHCR(List<JDIDebugTarget> targets, List<IResource> resources,
			List<String> qualifiedNames) {
		Iterator<JDIDebugTarget> iter = targets.iterator();
		JDIDebugTarget target = null;
		while (iter.hasNext()) {
			target = iter.next();
			if (target.isAvailable()) {
				// Make a local copy of the resources/names to swap so we can
				// filter
				// unloaded types on a per-target basis.
				List<IResource> resourcesToReplace = new ArrayList<IResource>(resources);
				List<String> qualifiedNamesToReplace = new ArrayList<String>(qualifiedNames);
				filterUnloadedTypes(target, resourcesToReplace,
						qualifiedNamesToReplace);

				if (!qualifiedNamesToReplace.isEmpty()) {
					// Don't notify if the changed types aren't loaded.
					fireHCRFailed(target, null);
					notifyFailedHCR(target, qualifiedNamesToReplace);
				}
			} else {
				// Targets should be unregistered when they terminate,
				// but this is a fall-back.
				deregisterTarget(target);
			}
		}
	}

	protected void notifyFailedHCR(JDIDebugTarget target, List<String> qualifiedNames) {
		if (target.isAvailable()) {
			target.addOutOfSynchTypes(qualifiedNames);
			target.fireChangeEvent(DebugEvent.STATE);
		}
	}

	/**
	 * Returns the currently registered debug targets that support hot code
	 * replace.
	 */
	protected synchronized List<JDIDebugTarget> getHotSwapTargets() {
		return (List<JDIDebugTarget>) fHotSwapTargets.clone();
	}

	/**
	 * Returns the currently registered debug targets that do not support hot
	 * code replace.
	 */
	protected synchronized List<JDIDebugTarget> getNoHotSwapTargets() {
		return (List<JDIDebugTarget>) fNoHotSwapTargets.clone();
	}

	/**
	 * Perform a hot code replace with the given resources. For a JDK 1.4
	 * compliant VM this involves:
	 * <ol>
	 * <li>Popping all frames from all thread stacks which will be affected by
	 * reloading the given resources</li>
	 * <li>Telling the VirtualMachine to redefine the affected classes</li>
	 * <li>Performing a step-into operation on all threads which were affected
	 * by the class redefinition. This returns execution to the first (deepest)
	 * affected method on the stack</li>
	 * </ol>
	 * For a J9 compliant VM this involves:
	 * <ol>
	 * <li>Telling the VirtualMachine to redefine the affected classes</li>
	 * <li>Popping all frames from all thread stacks which were affected by
	 * reloading the given resources and then performing a step-into operation
	 * on all threads which were affected by the class redefinition.</li>
	 * </ol>
	 * 
	 * @param targets
	 *            the targets in which to perform HCR
	 * @param resources
	 *            the resources which correspond to the changed classes
	 */
	private void doHotCodeReplace(List<JDIDebugTarget> targets, List<IResource> resources,
			List<String> qualifiedNames) {
		MultiStatus ms = new MultiStatus(
				JDIDebugPlugin.getUniqueIdentifier(),
				DebugException.TARGET_REQUEST_FAILED,
				"At least one target failed to drop to frame after successful hot code replace.", null); //$NON-NLS-1$
		Iterator<JDIDebugTarget> iter = targets.iterator();
		while (iter.hasNext()) {
			JDIDebugTarget target = iter.next();
			if (!target.isAvailable()) {
				deregisterTarget(target);
				continue;
			}
			// Make a local copy of the resources/names to swap so we can filter
			// unloaded types on a per-target basis.
			List<IResource> resourcesToReplace = new ArrayList<IResource>(resources);
			List<String> qualifiedNamesToReplace = new ArrayList<String>(qualifiedNames);
			filterUnloadedTypes(target, resourcesToReplace,
					qualifiedNamesToReplace);
			if (qualifiedNamesToReplace.isEmpty()) {
				// If none of the changed types are loaded, do nothing.
				continue;
			}

			List<IThread> poppedThreads = new ArrayList<IThread>();
			target.setIsPerformingHotCodeReplace(true);
			try {
				boolean framesPopped = false;
				if (target.canPopFrames()) {
					// JDK 1.4 drop to frame support:
					// JDK 1.4 spec is faulty around methods that have
					// been rendered obsolete after class redefinition.
					// Thus, pop the frames that contain affected methods
					// *before* the class redefinition to avoid problems.
					try {
						attemptPopFrames(target, resourcesToReplace,
								qualifiedNamesToReplace, poppedThreads);
						framesPopped = true; // No exception occurred
					} catch (DebugException de) {
						if (shouldLogHCRException(de)) {
							ms.merge(de.getStatus());
						}
					}
				}
				target.removeOutOfSynchTypes(qualifiedNamesToReplace);
				if (target.supportsJDKHotCodeReplace()) {
					redefineTypesJDK(target, resourcesToReplace,
							qualifiedNamesToReplace);
				} else if (target.supportsJ9HotCodeReplace()) {
					redefineTypesJ9(target, qualifiedNamesToReplace);
				}
				if (containsObsoleteMethods(target)) {
					fireObsoleteMethods(target);
				}
				try {
					if (target.canPopFrames() && framesPopped) {
						// Second half of JDK 1.4 drop to frame support:
						// All affected frames have been popped and the classes
						// have been reloaded. Step into the first changed
						// frame of each affected thread.
						// must re-set 'is doing HCR' to be able to step
						target.setIsPerformingHotCodeReplace(false);
						attemptStepIn(poppedThreads);
					} else {
						// J9 drop to frame support:
						// After redefining classes, drop to frame
						attemptDropToFrame(target, resourcesToReplace,
								qualifiedNamesToReplace);
					}
				} catch (DebugException de) {
					if (shouldLogHCRException(de)) {
						ms.merge(de.getStatus());
					}
				}
				fireHCRSucceeded(target);
			} catch (DebugException de) {
				// target update failed
				fireHCRFailed(target, de);
			}
			// also re-set 'is doing HCR' here in case HCR failed
			target.setIsPerformingHotCodeReplace(false);
			target.fireChangeEvent(DebugEvent.CONTENT);
		}
		if (!ms.isOK()) {
			JDIDebugPlugin.log(ms);
		}
		fDeltaCache.clear();
	}

	/**
	 * Returns whether the given exception, which occurred during HCR, should be
	 * logged. We anticipate that we can get IncompatibleThreadStateExceptions
	 * if the user happens to resume a thread at just the right moment. Since
	 * this has no ill effects for HCR, we don't log these exceptions.
	 */
	private boolean shouldLogHCRException(DebugException exception) {
		return !(exception.getStatus().getException() instanceof IncompatibleThreadStateException
				|| exception.getStatus().getCode() == IJavaThread.ERR_INCOMPATIBLE_THREAD_STATE || exception
				.getStatus().getCode() == IJavaThread.ERR_THREAD_NOT_SUSPENDED);
	}

	/**
	 * Replaces the given types in the given J9 debug target. A fully qualified
	 * name of each type must be supplied.
	 * 
	 * Breakpoints are reinstalled automatically when the new types are loaded.
	 * 
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                <li>The target VM was unable to reload a type due to a
	 *                shape change</li>
	 *                </ul>
	 */
	private void redefineTypesJ9(JDIDebugTarget target, List<String> qualifiedNames)
			throws DebugException {
		String[] typeNames = qualifiedNames
				.toArray(new String[qualifiedNames.size()]);
		if (target.supportsJ9HotCodeReplace()) {
			target.setHCROccurred(true);
			org.eclipse.jdi.hcr.VirtualMachine vm = (org.eclipse.jdi.hcr.VirtualMachine) target
					.getVM();
			if (vm == null) {
				target.requestFailed(
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_Hot_code_replace_failed___VM_disconnected__1,
						null);
			}
			int result = org.eclipse.jdi.hcr.VirtualMachine.RELOAD_FAILURE;
			try {
				result = vm.classesHaveChanged(typeNames);
			} catch (RuntimeException e) {
				target.targetRequestFailed(
						MessageFormat.format(JDIDebugHCRMessages.JavaHotCodeReplaceManager_exception_replacing_types, e.toString()), 
						e);
			}
			switch (result) {
			case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_SUCCESS:
				break;
			case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_IGNORED:
				target.targetRequestFailed(
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_ignored,
						null);
				break;
			case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_FAILURE:
				target.targetRequestFailed(
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_failed,
						null);
				target.addOutOfSynchTypes(qualifiedNames);
				break;
			}
		} else {
			target.notSupported(JDIDebugHCRMessages.JavaHotCodeReplaceManager_does_not_support_hcr);
			target.addOutOfSynchTypes(qualifiedNames);
		}
	}

	/**
	 * Replaces the given types in the given JDK-compliant debug target.
	 * 
	 * This method is to be used for JDK hot code replace.
	 */
	private void redefineTypesJDK(JDIDebugTarget target, List<IResource> resources,
			List<String> qualifiedNames) throws DebugException {
		if (target.supportsJDKHotCodeReplace()) {
			target.setHCROccurred(true);
			Map<ReferenceType, byte[]> typesToBytes = getTypesToBytes(target, resources,
					qualifiedNames);
			try {
				VirtualMachine vm = target.getVM();
				if (vm == null) {
					target.requestFailed(
							JDIDebugHCRMessages.JavaHotCodeReplaceManager_Hot_code_replace_failed___VM_disconnected__2,
							null);
				}
				vm.redefineClasses(typesToBytes);
			} catch (UnsupportedOperationException exception) {
				String detail = exception.getMessage();
				if (detail != null) {
					redefineTypesFailedJDK(
							target,
							qualifiedNames,
							MessageFormat.format(
									JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_unsupported_operation,
									detail), 
							exception);
				} else {
					redefineTypesFailedJDK(
							target,
							qualifiedNames,
							JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_unsupported_redefinition,
							exception);
				}
			} catch (NoClassDefFoundError exception) {
				redefineTypesFailedJDK(
						target,
						qualifiedNames,
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_bad_bytes,
						exception);
			} catch (VerifyError exception) {
				redefineTypesFailedJDK(
						target,
						qualifiedNames,
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_verify_error,
						exception);
			} catch (UnsupportedClassVersionError exception) {
				redefineTypesFailedJDK(
						target,
						qualifiedNames,
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_unsupported_class_version,
						exception);
			} catch (ClassFormatError exception) {
				redefineTypesFailedJDK(
						target,
						qualifiedNames,
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_class_format_error,
						exception);
			} catch (ClassCircularityError exception) {
				redefineTypesFailedJDK(
						target,
						qualifiedNames,
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_class_circularity_error,
						exception);
			} catch (RuntimeException exception) {
				redefineTypesFailedJDK(
						target,
						qualifiedNames,
						JDIDebugHCRMessages.JavaHotCodeReplaceManager_hcr_failed,
						exception);
			}
			target.reinstallBreakpointsIn(resources, qualifiedNames);
		} else {
			target.notSupported(JDIDebugHCRMessages.JavaHotCodeReplaceManager_does_not_support_hcr);
		}
	}

	/**
	 * Error handling for JDK hot code replace.
	 * 
	 * The given exception occurred when redefinition was attempted for the
	 * given types.
	 */
	private void redefineTypesFailedJDK(JDIDebugTarget target,
			List<String> qualifiedNames, String message, Throwable exception)
			throws DebugException {
		target.addOutOfSynchTypes(qualifiedNames);
		target.jdiRequestFailed(message, exception);
	}

	/**
	 * Returns a mapping of class files to the bytes that make up those class
	 * files.
	 * 
	 * @param target
	 *            the debug target to query
	 * @param resources
	 *            the classfiles
	 * @param qualifiedNames
	 *            the fully qualified type names corresponding to the
	 *            classfiles. The typeNames correspond to the resources on a
	 *            one-to-one basis.
	 * @return a mapping of class files to bytes key: class file value: the
	 *         bytes which make up that classfile
	 */
	private Map<ReferenceType, byte[]> getTypesToBytes(JDIDebugTarget target, List<IResource> resources,
			List<String> qualifiedNames) {
		Map<ReferenceType, byte[]> typesToBytes = new HashMap<ReferenceType, byte[]>(resources.size());
		Iterator<IResource> resourceIter = resources.iterator();
		Iterator<String> nameIter = qualifiedNames.iterator();
		IResource resource;
		String name;
		while (resourceIter.hasNext()) {
			resource = resourceIter.next();
			name = nameIter.next();
			List<ReferenceType> classes = target.jdiClassesByName(name);
			byte[] bytes = null;
			try {
				bytes = Util.getResourceContentsAsByteArray((IFile) resource);
			} catch (JavaModelException jme) {
				continue;
			}
			for(ReferenceType type : classes) {
				typesToBytes.put(type, bytes);
			}
		}
		return typesToBytes;
	}

	/**
	 * Return the listeners to notify for the given target. Target specific
	 * listeners take precedence over generic listeners registered with the
	 * debug model plug-in.
	 * 
	 * @param target
	 *            Java debug target
	 * @return hot code replace listeners
	 */
	private Object[] getHotCodeReplaceListeners(IJavaDebugTarget target) {
		Object[] listeners = null;
		if (target instanceof JDIDebugTarget) {
			listeners = ((JDIDebugTarget) target).getHotCodeReplaceListeners();
		}
		if (listeners == null || listeners.length == 0) {
			listeners = fHotCodeReplaceListeners.getListeners();
		}
		return listeners;
	}

	/**
	 * Notifies listeners that a hot code replace attempt succeeded
	 */
	private void fireHCRSucceeded(IJavaDebugTarget target) {
		Object[] listeners = getHotCodeReplaceListeners(target);
		for (Object listener : listeners) {
			((IJavaHotCodeReplaceListener) listener)
					.hotCodeReplaceSucceeded(target);
		}
	}

	/**
	 * Notifies listeners that a hot code replace attempt failed with the given
	 * exception
	 */
	private void fireHCRFailed(JDIDebugTarget target, DebugException exception) {
		Object[] listeners = getHotCodeReplaceListeners(target);
		for (Object listener : listeners) {
			((IJavaHotCodeReplaceListener) listener).hotCodeReplaceFailed(
					target, exception);
		}
	}

	/**
	 * Notifies listeners that obsolete methods remain on the stack
	 */
	private void fireObsoleteMethods(JDIDebugTarget target) {
		Object[] listeners = getHotCodeReplaceListeners(target);
		for (Object listener : listeners) {
			((IJavaHotCodeReplaceListener) listener).obsoleteMethods(target);
		}
	}

	/**
	 * Looks for the deepest affected stack frame in the stack and forces a drop
	 * to frame. Does this for all of the active stack frames in the target.
	 * 
	 * @param target
	 *            the debug target in which frames are to be dropped
	 * @param replacedClassNames
	 *            the classes that have been redefined
	 */
	protected void attemptDropToFrame(JDIDebugTarget target, List<IResource> resources,
			List<String> replacedClassNames) throws DebugException {
		List<JDIStackFrame> dropFrames = getAffectedFrames(target.getThreads(), resources,
				replacedClassNames);

		// All threads that want to drop to frame are able. Proceed with the
		// drop
		JDIStackFrame dropFrame = null;
		Iterator<JDIStackFrame> iter = dropFrames.iterator();
		while (iter.hasNext()) {
			try {
				dropFrame = iter.next();
				dropFrame.dropToFrame();
			} catch (DebugException de) {
				notifyFailedDrop(
						((JDIThread) dropFrame.getThread())
								.computeStackFrames(),
						replacedClassNames);
			}
		}
	}

	/**
	 * Looks for the deepest affected stack frame in the stack and forces a drop
	 * to frame. Does this for all of the active stack frames in the target.
	 * 
	 * @param target
	 *            the debug target in which frames are to be dropped
	 * @param replacedClassNames
	 *            the classes that have been redefined
	 * @param poppedThreads
	 *            a list of the threads in which frames were popped.This
	 *            parameter may have entries added by this method
	 */
	protected void attemptPopFrames(JDIDebugTarget target, List<IResource> resources,
			List<String> replacedClassNames, List<IThread> poppedThreads) throws DebugException {
		List<JDIStackFrame> popFrames = getAffectedFrames(target.getThreads(), resources,
				replacedClassNames);

		// All threads that want to drop to frame are able. Proceed with the
		// drop
		JDIStackFrame popFrame = null;
		Iterator<JDIStackFrame> iter = popFrames.iterator();
		while (iter.hasNext()) {
			try {
				popFrame = iter.next();
				popFrame.popFrame();
				poppedThreads.add(popFrame.getThread());
			} catch (DebugException de) {
				poppedThreads.remove(popFrame.getThread());
				notifyFailedDrop(
						((JDIThread) popFrame.getThread()).computeStackFrames(),
						replacedClassNames);
			}
		}
	}

	/**
	 * Returns whether or not the given target contains stack frames with
	 * obsolete methods.
	 */
	protected boolean containsObsoleteMethods(JDIDebugTarget target)
			throws DebugException {
		IThread[] threads = target.getThreads();
		List<IJavaStackFrame> frames = null;
		for (IThread thread : threads) {
			frames = ((JDIThread) thread).computeNewStackFrames();
			for(IJavaStackFrame frame : frames) {
				if(frame.isObsolete()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a list of frames which should be popped in the given threads.
	 */
	protected List<JDIStackFrame> getAffectedFrames(IThread[] threads, List<IResource> resourceList,
			List<String> replacedClassNames) throws DebugException {
		JDIThread thread = null;
		JDIStackFrame affectedFrame = null;
		List<JDIStackFrame> popFrames = new ArrayList<JDIStackFrame>();
		int numThreads = threads.length;
		IResource[] resources = new IResource[resourceList.size()];
		resourceList.toArray(resources);
		for (int i = 0; i < numThreads; i++) {
			thread = (JDIThread) threads[i];
			if (thread.isSuspended()) {
				affectedFrame = getAffectedFrame(thread, replacedClassNames);
				if (affectedFrame == null) {
					// No frame to drop to in this thread
					continue;
				}
				if (affectedFrame.supportsDropToFrame()) {
					popFrames.add(affectedFrame);
				} else {
					// if any thread that should drop does not support the drop,
					// do not drop in any threads.
					for (int j = 0; j < numThreads; j++) {
						notifyFailedDrop(
								((JDIThread) threads[i]).computeStackFrames(),
								replacedClassNames);
					}
					throw new DebugException(
							new Status(
									IStatus.ERROR,
									JDIDebugModel.getPluginIdentifier(),
									DebugException.NOT_SUPPORTED,
									JDIDebugHCRMessages.JavaHotCodeReplaceManager_Drop_to_frame_not_supported,
									null));
				}
			}
		}
		return popFrames;
	}

	/**
	 * Returns the stack frame that should be dropped to in the given thread
	 * after a hot code replace. This is calculated by determining if the
	 * threads contain stack frames that reside in one of the given replaced
	 * class names. If possible, only stack frames whose methods were directly
	 * affected (and not simply all frames in affected types) will be returned.
	 */
	protected JDIStackFrame getAffectedFrame(JDIThread thread,
			List<String> replacedClassNames) throws DebugException {
		List<IJavaStackFrame> frames = thread.computeStackFrames();
		JDIStackFrame affectedFrame = null;
		JDIStackFrame frame = null;
		ICompilationUnit compilationUnit = null;
		CompilationUnitDelta delta = null;
		IProject project = null;
		for (int j = frames.size() - 1; j >= 0; j--) {
			frame = (JDIStackFrame) frames.get(j);
			if (containsChangedType(frame, replacedClassNames)) {
				// smart drop to frame support
				compilationUnit = getCompilationUnit(frame);
				// if we can't find the source, then do type-based drop
				if (compilationUnit != null) {
					try {
						project = compilationUnit.getCorrespondingResource()
								.getProject();
						delta = getDelta(compilationUnit,
								getLastProjectBuildTime(project));
						if (!delta.hasChanged(frame.getName(),
								frame.getSignature())) {
							continue;
						}
					} catch (CoreException exception) {
						// If smart drop to frame fails, just do type-based drop
					}
				}

				if (frame.supportsDropToFrame()) {
					affectedFrame = frame;
					break;
				}
				// The frame we wanted to drop to cannot be popped.
				// Set the affected frame to the next lowest pop-able
				// frame on the stack.
				while (j > 0) {
					j--;
					frame = (JDIStackFrame) frames.get(j);
					if (frame.supportsDropToFrame()) {
						affectedFrame = frame;
						break;
					}
				}
				break;
			}
		}
		return affectedFrame;
	}

	/**
	 * Returns the delta object for the given compilation unit
	 * 
	 * @param cu
	 *            compilation unit
	 * @param time
	 *            time to compare to (i.e. compare to first version before this
	 *            time)
	 * @return delta object
	 */
	private CompilationUnitDelta getDelta(ICompilationUnit cu, long time)
			throws CoreException {
		CompilationUnitDelta delta = fDeltaCache.get(cu);
		if (delta == null) {
			delta = new CompilationUnitDelta(cu, time);
			fDeltaCache.put(cu, delta);
		}
		return delta;
	}

	/**
	 * Returns whether the given frame's declaring type was changed based on the
	 * given list of changed class names.
	 */
	protected boolean containsChangedType(JDIStackFrame frame,
			List<String> replacedClassNames) throws DebugException {
		String declaringTypeName = frame.getDeclaringTypeName();
		// Check if the frame's declaring type was changed
		if (replacedClassNames.contains(declaringTypeName)) {
			return true;
		}
		// Check if one of the frame's declaring type's inner classes have
		// changed
		Iterator<String> iter = replacedClassNames.iterator();
		int index;
		String className = null;
		while (iter.hasNext()) {
			className = iter.next();
			index = className.indexOf('$');
			if (index > -1
					&& declaringTypeName.equals(className.substring(0, index))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Performs a "step into" operation on the given threads.
	 */
	protected void attemptStepIn(List<IThread> threads) throws DebugException {
		Iterator<IThread> iter = threads.iterator();
		while (iter.hasNext()) {
			((JDIThread) iter.next()).stepInto();
		}
	}

	/**
	 * Returns the compilation unit associated with this Java stack frame.
	 * Returns <code>null</code> for a binary stack frame.
	 */
	protected ICompilationUnit getCompilationUnit(IJavaStackFrame frame) {
		ILaunch launch = frame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator = launch.getSourceLocator();
		if (locator == null) {
			return null;
		}
		IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
		String def = target.getDefaultStratum();
		target.setDefaultStratum("Java"); //$NON-NLS-1$
		Object sourceElement = locator.getSourceElement(frame);
		target.setDefaultStratum(def);
		if (!(sourceElement instanceof IJavaElement)
				&& sourceElement instanceof IAdaptable) {
			sourceElement = ((IAdaptable) sourceElement)
					.getAdapter(IJavaElement.class);
		}
		if (sourceElement instanceof IType) {
			return ((IType) sourceElement).getCompilationUnit();
		}
		if (sourceElement instanceof ICompilationUnit) {
			return (ICompilationUnit) sourceElement;
		}
		return null;
	}

	/**
	 * Returns the method in which this stack frame is suspended or
	 * <code>null</code> if none can be found
	 */
	public IMethod getMethod(JDIStackFrame frame, ICompilationUnit unit)
			throws CoreException {
		String declaringTypeName = frame.getDeclaringTypeName();
		String methodName = frame.getMethodName();
		String[] arguments = null;
		try {
			arguments = Signature.getParameterTypes(frame.getSignature());
		} catch (IllegalArgumentException exception) {
			// If Signature can't parse the signature, we can't
			// create the method
			return null;
		}
		String typeName = getUnqualifiedName(declaringTypeName);
		int index = typeName.indexOf('$');
		IType type = null;
		if (index > 0) {
			String remaining = typeName.substring(index + 1);
			typeName = typeName.substring(0, index);
			type = unit.getType(typeName);
			while (remaining != null) {
				index = remaining.indexOf('$');
				if (index > 0) {
					typeName = remaining.substring(0, index);
					remaining = remaining.substring(index + 1);
				} else {
					typeName = remaining;
					remaining = null;
				}
				type = type.getType(typeName);
			}
		} else {
			type = unit.getType(typeName);
		}
		if (type != null) {
			return type.getMethod(methodName, arguments);
		}
		return null;
	}

	/**
	 * Given a fully qualified name, return the unqualified name.
	 */
	protected String getUnqualifiedName(String qualifiedName) {
		int index = qualifiedName.lastIndexOf('.');
		return qualifiedName.substring(index + 1);
	}

	/**
	 * Notify the given frames that a drop to frame has failed after an HCR with
	 * the given class names.
	 */
	private void notifyFailedDrop(List<IJavaStackFrame> frames, List<String> replacedClassNames)
			throws DebugException {
		for(IJavaStackFrame frame : frames) {
			if (replacedClassNames.contains(frame.getDeclaringTypeName())) {
				((JDIStackFrame)frame).setOutOfSynch(true);
			}
		}
	}

	/**
	 * Returns the class file visitor after visiting the resource change. The
	 * visitor contains the changed class files and qualified type names.
	 * Returns <code>null</code> if the visitor encounters an exception, or the
	 * delta is not a POST_BUILD.
	 */
	protected ChangedClassFilesVisitor getChangedClassFiles(
			IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (event.getType() != IResourceChangeEvent.POST_BUILD || delta == null) {
			return null;
		}
		fClassfileVisitor.reset();
		try {
			delta.accept(fClassfileVisitor);
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
			return null; // quiet failure
		}
		return fClassfileVisitor;
	}

	/**
	 * A visitor which collects changed class files.
	 */
	class ChangedClassFilesVisitor implements IResourceDeltaVisitor {
		/**
		 * The collection of changed class files.
		 */
		protected List<IResource> fFiles = null;

		/**
		 * Collection of qualified type names, corresponding to class files.
		 */
		protected List<String> fNames = null;

		/**
		 * Answers whether children should be visited.
		 * <p>
		 * If the associated resource is a class file which has been changed,
		 * record it.
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null
					|| 0 == (delta.getKind() & IResourceDelta.CHANGED)) {
				return false;
			}
			IResource resource = delta.getResource();
			if (resource != null) {
				switch (resource.getType()) {
				case IResource.FILE:
					if (0 == (delta.getFlags() & IResourceDelta.CONTENT))
						return false;
					if (CLASS_FILE_EXTENSION.equals(resource.getFullPath()
							.getFileExtension())) {
						IPath localLocation = resource.getLocation();
						if (localLocation != null) {
							String path = localLocation.toOSString();
							IClassFileReader reader = ToolFactory
									.createDefaultClassFileReader(
											path,
											IClassFileReader.CLASSFILE_ATTRIBUTES);
							if (reader != null) {
								// this name is slash-delimited
								String qualifiedName = new String(
										reader.getClassName());
								boolean hasBlockingErrors = false;
								try {
									if (!Platform.getPreferencesService().getBoolean(
											JDIDebugPlugin.getUniqueIdentifier(), 
											JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS, 
											true, 
											null)) {
										// If the user doesn't want to replace
										// classfiles containing
										// compilation errors, get the source
										// file associated with
										// the class file and query it for
										// compilation errors
										IJavaProject pro = JavaCore
												.create(resource.getProject());
										ISourceAttribute sourceAttribute = reader
												.getSourceFileAttribute();
										String sourceName = null;
										if (sourceAttribute != null) {
											sourceName = new String(
													sourceAttribute
															.getSourceFileName());
										}
										IResource sourceFile = getSourceFile(
												pro, qualifiedName, sourceName);
										if (sourceFile != null) {
											IMarker[] problemMarkers = null;
											problemMarkers = sourceFile
													.findMarkers(
															IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
															true,
															IResource.DEPTH_INFINITE);
											for (IMarker problemMarker : problemMarkers) {
												if (problemMarker.getAttribute(
														IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
													hasBlockingErrors = true;
													break;
												}
											}
										}
									}
								} catch (CoreException e) {
									JDIDebugPlugin.log(e);
								}
								if (!hasBlockingErrors) {
									fFiles.add(resource);
									// dot-delimit the name
									fNames.add(qualifiedName.replace('/', '.'));
								}
							}
						}
					}
					return false;

				default:
					return true;
				}
			}
			return true;
		}

		/**
		 * Resets the file collection to empty
		 */
		public void reset() {
			fFiles = new ArrayList<IResource>();
			fNames = new ArrayList<String>();
		}

		/**
		 * Answers a collection of changed class files or <code>null</code>
		 */
		public List<IResource> getChangedClassFiles() {
			return fFiles;
		}

		/**
		 * Returns a collection of qualified type names corresponding to the
		 * changed class files.
		 * 
		 * @return List
		 */
		public List<String> getQualifiedNamesList() {
			return fNames;
		}

		/**
		 * Returns the source file associated with the given type, or
		 * <code>null</code> if no source file could be found.
		 * 
		 * @param project
		 *            the java project containing the classfile
		 * @param qualifiedName
		 *            fully qualified name of the type, slash delimited
		 * @param sourceAttribute
		 *            debug source attribute, or <code>null</code> if none
		 */
		private IResource getSourceFile(IJavaProject project,
				String qualifiedName, String sourceAttribute) {
			String name = null;
			IJavaElement element = null;
			try {
				if (sourceAttribute == null) {
					element = JavaDebugUtils
							.findElement(qualifiedName, project);
				} else {
					int i = qualifiedName.lastIndexOf('/');
					if (i > 0) {
						name = qualifiedName.substring(0, i + 1);
						name = name + sourceAttribute;
					} else {
						name = sourceAttribute;
					}
					element = project.findElement(new Path(name));
				}
				if (element instanceof ICompilationUnit) {
					ICompilationUnit cu = (ICompilationUnit) element;
					return cu.getCorrespondingResource();
				}
			} catch (CoreException e) {
			}
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
	 * Removes the given listener from the collection of hot code replace
	 * listeners. Once a listener is removed, it will no longer be notified of
	 * hot code replace attempt successes or failures.
	 */
	public void removeHotCodeReplaceListener(
			IJavaHotCodeReplaceListener listener) {
		fHotCodeReplaceListeners.remove(listener);
	}

	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
		IDebugTarget[] debugTargets = launch.getDebugTargets();
		for (IDebugTarget debugTarget : debugTargets) {
			IJavaDebugTarget jt = (IJavaDebugTarget) debugTarget
					.getAdapter(IJavaDebugTarget.class);
			if (jt != null) {
				deregisterTarget((JDIDebugTarget) jt);
			}
		}
	}

	/**
	 * Begin listening for resource changes when a launch is registered with a
	 * hot swap-able target.
	 * 
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		IDebugTarget[] debugTargets = launch.getDebugTargets();
		for (IDebugTarget debugTarget : debugTargets) {
			IJavaDebugTarget jt = (IJavaDebugTarget) debugTarget
					.getAdapter(IJavaDebugTarget.class);
			if (jt != null) {
				JDIDebugTarget target = (JDIDebugTarget) jt;
				if (target.supportsHotCodeReplace()) {
					addHotSwapTarget(target);
				} else if (target.isAvailable()) {
					addNonHotSwapTarget(target);
				}
			}
		}
		synchronized (this) {
			if (!fHotSwapTargets.isEmpty() || !fNoHotSwapTargets.isEmpty()) {
				getWorkspace().addResourceChangeListener(this,
						IResourceChangeEvent.POST_BUILD);
			}
		}
	}

	/**
	 * Begin listening for resource changes when a launch is registered with a
	 * hot swap-able target.
	 * 
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
		launchAdded(launch);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse
	 * .debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			if (event.getKind() == DebugEvent.TERMINATE) {
				Object source = event.getSource();
				if (source instanceof IAdaptable
						&& source instanceof IDebugTarget) {
					IJavaDebugTarget jt = (IJavaDebugTarget) ((IAdaptable) source)
							.getAdapter(IJavaDebugTarget.class);
					if (jt != null) {
						deregisterTarget((JDIDebugTarget) jt);
					}
				}
			}
		}
	}

	protected void deregisterTarget(JDIDebugTarget target) {
		// Remove the target from its hot swap target cache.
		if (!fHotSwapTargets.remove(target)) {
			fNoHotSwapTargets.remove(target);
		}
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager()
				.getLaunches();
		// If there are no more active JDIDebugTargets, stop
		// listening to resource changes.
		for (ILaunch launche : launches) {
			IDebugTarget[] targets = launche.getDebugTargets();
			for (IDebugTarget debugTarget : targets) {
				IJavaDebugTarget jt = (IJavaDebugTarget) debugTarget
						.getAdapter(IJavaDebugTarget.class);
				if (jt != null) {
					if (((JDIDebugTarget) jt).isAvailable()) {
						return;
					}
				}
			}
		}
	}

	/**
	 * Adds the given target to the list of hot-swap-able targets. Has no effect
	 * if the target is already registered.
	 * 
	 * @param target
	 *            a target that supports hot swap
	 */
	protected synchronized void addHotSwapTarget(JDIDebugTarget target) {
		if (!fHotSwapTargets.contains(target)) {
			fHotSwapTargets.add(target);
		}
	}

	/**
	 * Adds the given target to the list of non hot-swap-able targets. Has no
	 * effect if the target is already registered.
	 * 
	 * @param target
	 *            a target that does not support hot swap
	 */
	protected synchronized void addNonHotSwapTarget(JDIDebugTarget target) {
		if (!fNoHotSwapTargets.contains(target)) {
			fNoHotSwapTargets.add(target);
		}
	}

}
