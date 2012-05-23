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
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaProjectSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.PackageFragmentRootSourceContainer;

/**
 * Private source lookup utilities. Translates runtime classpath entries
 * to source containers.
 * 
 * @since 3.0
 */
public class JavaSourceLookupUtil {

	/**
	 * Translates the given runtime classpath entries into associated source
	 * containers.
	 * 
	 * @param entries entries to translate
	 * @return source containers corresponding to the given runtime classpath entries
	 */
	public static ISourceContainer[] translate(IRuntimeClasspathEntry[] entries) {
		List<ISourceContainer> containers = new ArrayList<ISourceContainer>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			switch (entry.getType()) {
				case IRuntimeClasspathEntry.ARCHIVE:
					IPackageFragmentRoot root = getPackageFragmentRoot(entry);
					if (root == null) {
						String path = entry.getSourceAttachmentLocation();
						ISourceContainer container = null;
						if (path == null) {
							// use the archive itself
							path = entry.getLocation();
						}
						if (path != null) {
							// check if file or folder
							File file = new File(path);
							if (file.isDirectory()) {
								IResource resource = entry.getResource();
								if (resource instanceof IContainer) {
									container = new FolderSourceContainer((IContainer)resource, false);
								} else {
									container = new DirectorySourceContainer(file, false);
								}
							} else {
								container = new ExternalArchiveSourceContainer(path, true);
							}
							if (!containers.contains(container)) {
								containers.add(container);
							}
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
					if (resource != null && resource.getType() == IResource.PROJECT) {
						IJavaProject javaProject = JavaCore.create((IProject)resource);
						ISourceContainer container = null;
						if (javaProject.exists()) {
							container = new JavaProjectSourceContainer(javaProject);
						} else if (resource.exists()) {
							container = new ProjectSourceContainer((IProject)resource, false);
						}
						if (container != null && !containers.contains(container)) {
							containers.add(container);
						}
					}
					break;
				default:
					// no other classpath types are valid in a resolved path
					break;
			}
		}
		return containers.toArray(new ISourceContainer[containers.size()]);
	}
	
	/**
	 * Returns whether the source attachments of the given package fragment
	 * root and runtime classpath entry are equal.
	 * <p>
	 * NOTE: If the runtime classpath entry's source attachment is not specified,
	 * then it is considered equal. This way, the corresponding package fragment
	 * root is used for source lookup if it has a source attachment or not.
	 * </p>
	 * 
	 * @param root package fragment root
	 * @param entry runtime classpath entry
	 * @return whether the source attachments of the given package fragment
	 * root and runtime classpath entry are equal
	 * @throws JavaModelException if there is a problem accessing the given {@link IPackageFragmentRoot}
	 */
	private static boolean isSourceAttachmentEqual(IPackageFragmentRoot root, IRuntimeClasspathEntry entry) throws JavaModelException {
		IPath entryPath = entry.getSourceAttachmentPath();
		if (entryPath == null) {
			return true;
		}
		IPath rootPath = root.getSourceAttachmentPath();
		if (rootPath == null) {
			// entry has a source attachment that the package root does not
			return false;
		}
		return rootPath.equals(entryPath);
		
	}
	
	/**
	 * Determines if the given archive runtime classpath entry exists
	 * in the workspace as a package fragment root. Returns the associated
	 * package fragment root or <code>null</code> if none.
	 *  
	 * @param entry archive runtime classpath entry
	 * @return package fragment root or <code>null</code>
	 */
	private static IPackageFragmentRoot getPackageFragmentRoot(IRuntimeClasspathEntry entry) {
		IResource resource = entry.getResource();
		if (resource != null) {
			// find package fragment associated with the resource
			IProject project = resource.getProject();
			IJavaProject jp = JavaCore.create(project);
			try {
				if (project.isOpen() && jp.exists()) {
					IPackageFragmentRoot root = jp.findPackageFragmentRoot(resource.getFullPath());
					if (root != null && isSourceAttachmentEqual(root, entry)) {
						// use package fragment root
						return root;
					}
				}
			} catch (JavaModelException e) {
				LaunchingPlugin.log(e);
			}
		}
		// Check all package fragment roots for case of external archive.
		// External jars are shared, so it does not matter which project it
		// originates from
		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		IPath entryPath = entry.getPath();
		try {
			IJavaProject[] jps = model.getJavaProjects();
			for (int i = 0; i < jps.length; i++) {
				IJavaProject jp = jps[i];
				IProject p =  jp.getProject();
				if (p.isOpen()) {
					IPackageFragmentRoot[] allRoots = jp.getPackageFragmentRoots();
					for (int j = 0; j < allRoots.length; j++) {
						IPackageFragmentRoot root = allRoots[j];
						if (root.getPath().equals(entryPath) && isSourceAttachmentEqual(root, entry)) {
							// use package fragment root
							return root;
						}
					}
				}
			}
		} catch (JavaModelException e) {
			LaunchingPlugin.log(e);
		}
		return null;
	}	
}
