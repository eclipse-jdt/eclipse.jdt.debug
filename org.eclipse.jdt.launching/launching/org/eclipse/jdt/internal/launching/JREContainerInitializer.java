package org.eclipse.jdt.internal.launching;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Resolves a container for a JRE classpath container entry.
 */
public class JREContainerInitializer extends ClasspathContainerInitializer {

	/**
	 * @see ClasspathContainerInitializer#initialize(IPath, IJavaProject)
	 */
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
				IVMInstall vm = resolveVM(containerPath);
				if (vm == null) {
					handleResolutionError(containerPath, project);
					return;
				}					
				if (vm != null) {
					JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {new JREContainer(vm, containerPath)}, null);
				}
			}
		}
	}
	
	protected void handleResolutionError(IPath containerPath, IJavaProject project) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), JavaRuntime.ERR_UNABLE_TO_RESOLVE_JRE, 
			MessageFormat.format(LaunchingMessages.getString("JREContainerInitializer.Unable_to_locate_JRE_named_{0}_to_build_project_{1}._1"), new String[] {containerPath.segment(2), project.getElementName()}), null); //$NON-NLS-1$
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
		IVMInstall vm = null;
		if (handler != null) {
			vm = (IVMInstall)handler.handleStatus(status, project);
		}
		
		if (vm == null) {
			throw new CoreException(status);
		} else {
			String vmTypeId = vm.getVMInstallType().getId();
			String vmName = vm.getName();
			String prevId = containerPath.segment(1);
			String prevName = containerPath.segment(2);
			if (!(prevId.equals(vmTypeId) && prevName.equals(vmName))) {
				// update classpath
				IPath newPath = new Path(JavaRuntime.JRE_CONTAINER);
				if (vmTypeId != null) {
					newPath = newPath.append(vmTypeId).append(vmName);
				}
				IClasspathEntry[] classpath = project.getRawClasspath();
				for (int i = 0; i < classpath.length; i++) {
					switch (classpath[i].getEntryKind()) {
						case IClasspathEntry.CPE_CONTAINER:
							if (classpath[i].getPath().equals(containerPath)) {
								classpath[i] = JavaCore.newContainerEntry(newPath, classpath[i].isExported());
							}
							break;
						default:
							break;
					}
				}
				project.setRawClasspath(classpath, null);
				containerPath = newPath;
			}
			JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {new JREContainer(vm, containerPath)}, null);
		}
	}

	/**
	 * Returns the VM install associated with the container path, or <code>null</code>
	 * if it does not exist.
	 */
	public static IVMInstall resolveVM(IPath containerPath) {
		IVMInstall vm = null;
		if (containerPath.segmentCount() > 1) {
			// specific JRE
			String vmTypeId = containerPath.segment(1);
			String vmName = containerPath.segment(2);
			IVMInstallType vmType = JavaRuntime.getVMInstallType(vmTypeId);
			if (vmType != null) {
				vm = vmType.findVMInstallByName(vmName);
			}
		} else {
			// workspace default JRE
			vm = JavaRuntime.getDefaultVMInstall();
		}		
		return vm;
	}
	
	/**
	 * Update conatiners that point to the default JRE, which has changed
	 */
	public void updateDefatultJREContainers(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}		
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IJavaModel model = JavaCore.create(ws.getRoot());
		boolean wasAutobuild= setAutobuild(ws, false);
		try {
			IJavaProject[] projects = model.getJavaProjects();
			List affectedProjects = new ArrayList(projects.length);
			for (int i = 0; i < projects.length; i++) {
				IClasspathEntry[] classpath = projects[i].getRawClasspath();
				for (int j = 0; j < classpath.length; j++) {
					IClasspathEntry entry = classpath[j];
					if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
						IPath path = entry.getPath();
						if (path.segmentCount() == 1 && path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
							// references default JRE
							affectedProjects.add(projects[i]);
						}
					}
				}
			}
			if (!affectedProjects.isEmpty()) {
				IJavaProject[] projArray = (IJavaProject[])affectedProjects.toArray(new IJavaProject[affectedProjects.size()]);
				IPath containerPath = new Path(JavaRuntime.JRE_CONTAINER);
				IVMInstall vm = JREContainerInitializer.resolveVM(containerPath);
				JREContainer container = new JREContainer(vm, containerPath);
				IClasspathContainer[] containers = new IClasspathContainer[projArray.length];
				Arrays.fill(containers, container);
				JavaCore.setClasspathContainer(containerPath, projArray, containers, monitor);
			}
		} finally {
			setAutobuild(ws, wasAutobuild);
		}	
	}
	
	/**
	 * Update conatiners that point to removed JRE explicitly. The containers
	 * are now unbound.
	 */
	public void updateRemovedVM(IVMInstall vm) throws CoreException {
		IProgressMonitor monitor= new NullProgressMonitor();
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IJavaModel model = JavaCore.create(ws.getRoot());
		boolean wasAutobuild= setAutobuild(ws, false);
		try {
			IJavaProject[] projects = model.getJavaProjects();
			List affectedProjects = new ArrayList(projects.length);
			for (int i = 0; i < projects.length; i++) {
				IClasspathEntry[] classpath = projects[i].getRawClasspath();
				for (int j = 0; j < classpath.length; j++) {
					IClasspathEntry entry = classpath[j];
					if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
						IPath path = entry.getPath();
						if (path.segmentCount() == 3 && path.segment(0).equals(JavaRuntime.JRE_CONTAINER)
						&& path.segment(1).equals(vm.getVMInstallType().getId())
						&& path.segment(2).equals(vm.getName())) {
							// references removed JRE
							affectedProjects.add(projects[i]);
						}
					}
				}
			}
			if (!affectedProjects.isEmpty()) {
				IJavaProject[] projArray = (IJavaProject[])affectedProjects.toArray(new IJavaProject[affectedProjects.size()]);
				IPath containerPath = new Path(JavaRuntime.JRE_CONTAINER);
				containerPath = containerPath.append(vm.getVMInstallType().getId());
				containerPath = containerPath.append(vm.getName());
				IClasspathContainer[] containers = new IClasspathContainer[projArray.length];
				Arrays.fill(containers, null);
				JavaCore.setClasspathContainer(containerPath, projArray, containers, monitor);
			}
		} finally {
			setAutobuild(ws, wasAutobuild);
		}	
	}	
	
	private boolean setAutobuild(IWorkspace ws, boolean newState) throws CoreException {
		IWorkspaceDescription wsDescription= ws.getDescription();
		boolean oldState= wsDescription.isAutoBuilding();
		if (oldState != newState) {
			wsDescription.setAutoBuilding(newState);
			ws.setDescription(wsDescription);
		}
		return oldState;
	}	
}
