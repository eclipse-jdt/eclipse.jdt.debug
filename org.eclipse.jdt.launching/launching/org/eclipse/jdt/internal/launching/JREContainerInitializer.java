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
	
	// avoid stack overflow
	private static IPath fPath;
	private static IJavaProject fProject;

	/**
	 * @see ClasspathContainerInitializer#initialize(IPath, IJavaProject)
	 */
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		if (fPath != null) {
			if (fPath.equals(containerPath) && fProject.equals(project)) {
				return; 
			}
		}
		fPath = containerPath;
		fProject = project;
		
		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
				IVMInstall vm = resolveVM(containerPath);
				JREContainer container = null;
				if (vm != null) {
					container = new JREContainer(vm, containerPath);
				}
				JavaCore.setClasspathContainer(containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
			}
		}
		fPath = null;
		fProject = null;
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
			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST, MessageFormat.format(LaunchingMessages.getString("JREContainerInitializer.JRE_referenced_by_classpath_container_{0}_does_not_exist._1"), new String[]{containerPath.toString()}), null); //$NON-NLS-1$
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
						IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, MessageFormat.format(LaunchingMessages.getString("JREContainerInitializer.Classpath_entry_{0}_does_not_refer_to_an_existing_library._2"), new String[]{entry.getPath().toString()}), null); //$NON-NLS-1$
						throw new CoreException(status);
					}
				} else {
					IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, MessageFormat.format(LaunchingMessages.getString("JREContainerInitializer.Classpath_entry_{0}_does_not_refer_to_a_library._3"), new String[]{entry.getPath().toString()}), null); //$NON-NLS-1$
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
