package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
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
		IVMInstall configJRE = JavaRuntime.computeVMInstall(configuration);
		return resolveLibraryLocations(configJRE);
	}
	
	/**
	 * @see IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(IRuntimeClasspathEntry, IJavaProject)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		IVMInstall projectJRE = JavaRuntime.getVMInstall(project);
		return resolveLibraryLocations(projectJRE);
	}

	/**
	 * Resolves libray locations for the given VM install
	 */
	protected IRuntimeClasspathEntry[] resolveLibraryLocations(IVMInstall vm) {
		IRuntimeClasspathEntry[] resolved = null;
		int kind = IRuntimeClasspathEntry.STANDARD_CLASSES;
		LibraryLocation[] libs = vm.getLibraryLocations();
		if (libs == null) {
			// default system libs
			libs = vm.getVMInstallType().getDefaultLibraryLocations(vm.getInstallLocation());
		} else {
			// custom system libs - place on bootpath explicitly
			kind = IRuntimeClasspathEntry.BOOTSTRAP_CLASSES;
		}
		resolved = new IRuntimeClasspathEntry[libs.length];
		for (int i = 0; i < libs.length; i++) {
			resolved[i] = JavaRuntime.newArchiveRuntimeClasspathEntry(libs[i].getSystemLibraryPath());
			resolved[i].setSourceAttachmentPath(libs[i].getSystemLibrarySourcePath());
			resolved[i].setSourceAttachmentRootPath(libs[i].getPackageRootPath());
			resolved[i].setClasspathProperty(kind);
		}
		return resolved;
	}
		
	/**
	 * @see IRuntimeClasspathEntryResolver#resolveVMInstall(IClasspathEntry)
	 */
	public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException {
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