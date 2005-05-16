/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Resolves for JRELIB_VARIABLE and JRE_CONTAINER
 */
public class JRERuntimeClasspathEntryResolver implements IRuntimeClasspathEntryResolver {

	/**
	 * @see IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(IRuntimeClasspathEntry, ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		IVMInstall jre = null;
		if (entry.getType() == IRuntimeClasspathEntry.CONTAINER && entry.getPath().segmentCount() > 1) {
			// a specific VM
			jre = JREContainerInitializer.resolveVM(entry.getPath()); 
		} else {
			// default VM for config
			jre = JavaRuntime.computeVMInstall(configuration);
		}
		if (jre == null) {
			// cannot resolve JRE
			return new IRuntimeClasspathEntry[0];
		}
		return resolveLibraryLocations(jre, entry.getClasspathProperty());
	}
	
	/**
	 * @see IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(IRuntimeClasspathEntry, IJavaProject)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		IVMInstall jre = null;
		if (entry.getType() == IRuntimeClasspathEntry.CONTAINER && entry.getPath().segmentCount() > 1) {
			// a specific VM
			jre = JREContainerInitializer.resolveVM(entry.getPath()); 
		} else {
			// default VM for project
			jre = JavaRuntime.getVMInstall(project);
		}
		if (jre == null) {
			// cannot resolve JRE
			return new IRuntimeClasspathEntry[0];
		}		
		return resolveLibraryLocations(jre, entry.getClasspathProperty());
	}

	/**
	 * Resolves libray locations for the given VM install
	 */
	protected IRuntimeClasspathEntry[] resolveLibraryLocations(IVMInstall vm, int kind) {
		if (kind == IRuntimeClasspathEntry.BOOTSTRAP_CLASSES) {
			File vmInstallLocation= vm.getInstallLocation();
			if (vmInstallLocation != null) {
				LibraryInfo libraryInfo= LaunchingPlugin.getLibraryInfo(vmInstallLocation.getAbsolutePath());
				if (libraryInfo != null) {
					// only return bootstrap classpath entries if we have the info
					String[] bootpath= libraryInfo.getBootpath();
					int length= bootpath.length;
					// use libs to set source attachment properly
					LibraryLocation[] libs = JavaRuntime.getLibraryLocations(vm);
					List resolvedEntries = new ArrayList(length);
					for (int i= 0; i < length; i++) {
						IPath libPath = new Path(bootpath[i]);
						if (libPath.toFile().exists()) {
							IRuntimeClasspathEntry resolved = JavaRuntime.newArchiveRuntimeClasspathEntry(libPath);
							resolved.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
							for (int j = 0; j < libs.length; j++) {
								String resolvedPath = resolved.getPath().toString();
								if (libs[j].getSystemLibraryPath().toString().equalsIgnoreCase(resolvedPath)) {
									IPath path = libs[j].getSystemLibrarySourcePath();
									if (path != null && !path.isEmpty()) {
										resolved.setSourceAttachmentPath(path);
										resolved.setSourceAttachmentRootPath(libs[j].getPackageRootPath());
									}
									break;
								}
							}
							resolvedEntries.add(resolved);
						}
					}
					return (IRuntimeClasspathEntry[]) resolvedEntries.toArray(new IRuntimeClasspathEntry[resolvedEntries.size()]);
				}
			}
		}
		LibraryLocation[] libs = vm.getLibraryLocations();
		LibraryLocation[] defaultLibs = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		if (libs == null) {
			// default system libs
			libs = defaultLibs;
		} else if (!isSameArchives(libs, defaultLibs)) {
			// determine if bootpath should be explicit
			kind = IRuntimeClasspathEntry.BOOTSTRAP_CLASSES;
		}
		List resolvedEntries = new ArrayList(libs.length);
		for (int i = 0; i < libs.length; i++) {
			IPath systemLibraryPath = libs[i].getSystemLibraryPath();
			if (systemLibraryPath.toFile().exists()) {
				IRuntimeClasspathEntry resolved = JavaRuntime.newArchiveRuntimeClasspathEntry(systemLibraryPath);
				IPath path = libs[i].getSystemLibrarySourcePath();
				if (path != null && !path.isEmpty()) {
					resolved.setSourceAttachmentPath(path);
					resolved.setSourceAttachmentRootPath(libs[i].getPackageRootPath());
				}
				resolved.setClasspathProperty(kind);
				resolvedEntries.add(resolved);
			}
		}
		return (IRuntimeClasspathEntry[]) resolvedEntries.toArray(new IRuntimeClasspathEntry[resolvedEntries.size()]);
	}
		
	/**
	 * Return whether the given list of libraries refer to the same archives in the same
	 * order. Only considers the binary archive (not source or javadoc locations). 
	 *  
	 * @param libs
	 * @param defaultLibs
	 * @return whether the given list of libraries refer to the same archives in the same
	 * order
	 */
	public static boolean isSameArchives(LibraryLocation[] libs, LibraryLocation[] defaultLibs) {
		if (libs.length != defaultLibs.length) {
			return false;
		}
		for (int i = 0; i < defaultLibs.length; i++) {
			LibraryLocation def = defaultLibs[i];
			LibraryLocation lib = libs[i];
			if (!def.getSystemLibraryPath().equals(lib.getSystemLibraryPath())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see IRuntimeClasspathEntryResolver#resolveVMInstall(IClasspathEntry)
	 */
	public IVMInstall resolveVMInstall(IClasspathEntry entry) {
		switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_VARIABLE:
				if (entry.getPath().segment(0).equals(JavaRuntime.JRELIB_VARIABLE)) {
					return JavaRuntime.getDefaultVMInstall();
				}
				break;
			case IClasspathEntry.CPE_CONTAINER:
				if (entry.getPath().segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
					return JREContainerInitializer.resolveVM(entry.getPath());
				}
				break;
			default:
				break;
		}
		return null;
	}

}
