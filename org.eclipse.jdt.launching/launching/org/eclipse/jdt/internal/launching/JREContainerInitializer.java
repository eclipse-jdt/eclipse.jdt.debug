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

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;

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
			
		// if there are no JREs to choose from there is no point in consulting the status handler
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		if (types.length == 0) {
			throw new CoreException(status);
		}
		int count = 0;
		for (int i = 0; i < types.length; i++) {
			IVMInstallType type = types[i];
			IVMInstall[] installs = type.getVMInstalls();
			count += installs.length;
		}
		if (count == 0) {
			throw new CoreException(status);
		}
		
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
			String prevId = getVMTypeId(containerPath);
			String prevName = getVMName(containerPath);
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
			String vmTypeId = getVMTypeId(containerPath);
			String vmName = getVMName(containerPath);
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
	 * Returns the VM type identifier from the given container ID path.
	 * 
	 * @return the VM type identifier from the given container ID path
	 */
	public static String getVMTypeId(IPath path) {
		return path.segment(1);
	}
	
	/**
	 * Returns the VM name from the given container ID path.
	 * 
	 * @return the VM name from the given container ID path
	 */
	public static String getVMName(IPath path) {
		return path.removeFirstSegments(2).toString();
	}	
	
	/**
	 * The container can be updated if it refers to an existing VM.
	 * 
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#canUpdateClasspathContainer(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
		if (containerPath != null && containerPath.segmentCount() > 0) {
			if (JavaRuntime.JRE_CONTAINER.equals(containerPath.segment(0))) {
				return resolveVM(containerPath) != null;
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#requestClasspathContainerUpdate(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.IClasspathContainer)
	 */
	public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) throws CoreException {
		IVMInstall vm = resolveVM(containerPath);
		if (vm == null) { 
			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST, MessageFormat.format("JRE referenced by classpath container {0} does not exist.", new String[]{containerPath.toString()}), null);
			throw new CoreException(status);
		} else {
			// update of the vm with new library locations
			
			IClasspathEntry[] entries = containerSuggestion.getClasspathEntries();
			LibraryLocation[] libs = new LibraryLocation[entries.length];
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IPath path = entry.getPath();
					File lib = path.toFile();
					if (lib.exists() && lib.isFile()) {
						IPath srcPath = entry.getSourceAttachmentPath();
						if (srcPath == null) {
							srcPath = Path.EMPTY;
						}
						IPath rootPath = entry.getSourceAttachmentRootPath();
						if (rootPath == null) {
							rootPath = Path.EMPTY;
						}
						libs[i] = new LibraryLocation(path, srcPath, rootPath);
					} else {
						IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, MessageFormat.format("Classpath entry {0} does not refer to an existing library.", new String[]{entry.getPath().toString()}), null);
						throw new CoreException(status);
					}
				} else {
					IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, MessageFormat.format("Classpath entry {0} does not refer to a library.", new String[]{entry.getPath().toString()}), null);
					throw new CoreException(status);
				}
			}
			VMStandin standin = new VMStandin(vm);
			standin.setLibraryLocations(libs);
			standin.convertToRealVM();
			JavaRuntime.saveVMConfiguration();
		}
	}

}
