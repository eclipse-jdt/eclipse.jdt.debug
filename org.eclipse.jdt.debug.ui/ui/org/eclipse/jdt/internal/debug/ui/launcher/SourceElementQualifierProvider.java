/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.io.File;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.LocalFileStorage;
import org.eclipse.jdt.launching.sourcelookup.ZipEntryStorage;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A label provider for source element qaulifiers found with a JavaSourceLocator
 */
public class SourceElementQualifierProvider extends LabelProvider implements ILabelProvider {

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof IJavaElement) {
			IJavaProject project = ((IJavaElement)element).getJavaProject();
			return project.getElementName();
		} else if (element instanceof ZipEntryStorage) {
			ZipEntryStorage storage = (ZipEntryStorage)element;
			ZipFile zipFile = storage.getArchive();
			IPath path = new Path(zipFile.getName());
			IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
			IResource res = entry.getResource();
			if (res == null) {
				// external
				return zipFile.getName();
			} else {
				// intenral
				return res.getName();
			}
		} else if (element instanceof LocalFileStorage) {
			LocalFileStorage storage = (LocalFileStorage)element;
			File extFile = storage.getFile();
			IPath path = new Path(extFile.getAbsolutePath());
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			if (file == null) {
				// external
				return extFile.getParent();
			} else {
				// internal
				return file.getProject().getName();
			}
		}
		return super.getText(element);
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof IJavaElement) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_PROJECT);
		} else if (element instanceof ZipEntryStorage) {
			return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_JAR_WSRC);
		} else if (element instanceof LocalFileStorage) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		return super.getImage(element);
	}

}
