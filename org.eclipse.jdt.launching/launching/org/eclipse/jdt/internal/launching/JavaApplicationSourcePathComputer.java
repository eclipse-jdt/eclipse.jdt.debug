/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.internal.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Computes a default source lookup path for a local Java application.
 * 
 * @since 3.0
 */
public class JavaApplicationSourcePathComputer implements ISourcePathComputerDelegate {
	
	/**
	 * Unique identifier for the local Java application source path computer
	 * (value <code>org.eclipse.jdt.launching.sourceLookup.javaApplicationSourcePathComputer</code>).
	 */
	public static final String ID = "org.eclipse.jdt.launching.sourceLookup.javaApplicationSourcePathComputer"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputer#getId()
	 */
	public String getId() {
		return ID;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedSourceLookupPath(configuration);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, configuration);
		return translate(resolved, true);
	}
	
	/**
	 * Translates the given runtime classpath entries into associated source
	 * containers.
	 * 
	 * @param entries entries to translate
	 * @param considerSourceAttachments whether to consider source attachments
	 *  when comparing against existing packagr fragment roots
	 * @exception CoreException if unable to expand the path
	 */
	protected static ISourceContainer[] translate(IRuntimeClasspathEntry[] entries, boolean considerSourceAttachments) throws CoreException {
		List containers = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			switch (entry.getType()) {
				case IRuntimeClasspathEntry.ARCHIVE:
					IPackageFragmentRoot root = getPackageFragmentRoot(entry, considerSourceAttachments);
					if (root == null) {
						String path = entry.getSourceAttachmentLocation();
						ISourceContainer container = null;
						if (path == null) {
							// use the archive itself
							container = new ArchiveSourceContainer(entry.getLocation(), true);
						} else {
							container = new ArchiveSourceContainer(path, true);

						}
						if (!containers.contains(container)) {
							containers.add(container);
						}
					} else {
						ISourceContainer container = new PackageFragmentRootSourceContainer(root);
						if (!containers.contains(container)) {
							containers.add(container);
						}
					}
					break;
				case IRuntimeClasspathEntry.PROJECT:
					IResource resource = entry.getResource();
					if (resource.getType() == IResource.PROJECT) {
						ISourceContainer container = new JavaProjectSourceContainer(JavaCore.create((IProject)resource));
						if (!containers.contains(container)) {
							containers.add(container);
						}
					}
					break;
				default:
					// no other classpath types are valid in a resolved path
					break;
			}
		}
		return (ISourceContainer[]) containers.toArray(new ISourceContainer[containers.size()]);
	}
	
	/**
	 * Returns whether the given objects are equal, allowing
	 * for <code>null</code>.
	 * 
	 * @param a
	 * @param b
	 * @return whether the given objects are equal, allowing
	 *   for <code>null</code>
	 */
	private static boolean equalOrNull(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		return a.equals(b);
	}
	
	/**
	 * Returns whether the source attachments of the given package fragment
	 * root and runtime classpath entry are equal.
	 * 
	 * @param root package fragment root
	 * @param entry runtime classpath entry
	 * @return whether the source attachments of the given package fragment
	 * root and runtime classpath entry are equal
	 * @throws JavaModelException 
	 */
	private static boolean isSourceAttachmentEqual(IPackageFragmentRoot root, IRuntimeClasspathEntry entry) throws JavaModelException {
		return equalOrNull(root.getSourceAttachmentPath(), entry.getSourceAttachmentPath());
	}
	
	/**
	 * Determines if the given archive runtime classpath entry exists
	 * in the workspace as a package fragment root. Returns the associated
	 * package fragment root possible, otherwise
	 * <code>null</code>.
	 *  
	 * @param entry archive runtime classpath entry
	 * @param considerSourceAttachment whether the source attachments should be
	 *  considered comparing against package fragment roots
	 * @return package fragment root or <code>null</code>
	 */
	private static IPackageFragmentRoot getPackageFragmentRoot(IRuntimeClasspathEntry entry, boolean considerSourceAttachment) {
		IResource resource = entry.getResource();
		if (resource == null) { 
			// Check all package fragment roots for case of external archive.
			// External jars are shared, so it does not matter which project it
			// originates from
			IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
			try {
				IJavaProject[] jps = model.getJavaProjects();
				for (int i = 0; i < jps.length; i++) {
					IPackageFragmentRoot[] allRoots = jps[i].getPackageFragmentRoots();
					for (int j = 0; j < allRoots.length; j++) {
						IPackageFragmentRoot root = allRoots[j];
						if (root.isExternal() && root.getPath().equals(new Path(entry.getLocation()))) {
							if (!considerSourceAttachment || isSourceAttachmentEqual(root, entry)) {
								// use package fragment root
								return root;
							}							
						}
					}
				}
			} catch (JavaModelException e) {
				LaunchingPlugin.log(e);
			}
		} else {
			// check if the archive is a package fragment root
			IProject project = resource.getProject();
			IJavaProject jp = JavaCore.create(project);
			try {
				if (jp.exists()) {
					IPackageFragmentRoot root = jp.getPackageFragmentRoot(resource);
					IPackageFragmentRoot[] allRoots = jp.getPackageFragmentRoots();
					for (int j = 0; j < allRoots.length; j++) {
						if (allRoots[j].equals(root)) {
							// ensure source attachment paths match
							if (!considerSourceAttachment || isSourceAttachmentEqual(root, entry)) {
								// use package fragment root
								return root;
							}
						}
					}

				}
				// check all other java projects to see if another project references
				// the archive
				IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
				IJavaProject[] jps = model.getJavaProjects();
				for (int i = 0; i < jps.length; i++) {
					IPackageFragmentRoot[] allRoots = jps[i].getPackageFragmentRoots();
					for (int j = 0; j < allRoots.length; j++) {
						IPackageFragmentRoot root = allRoots[j];
						if (!root.isExternal() && root.getPath().equals(entry.getPath())) {
							if (!considerSourceAttachment || isSourceAttachmentEqual(root, entry)) {
								// use package fragment root
								return root;
							}							
						}
					}
				}
			} catch (JavaModelException e) {
				LaunchingPlugin.log(e);
			}		
		}		
		return null;
	}	
}
