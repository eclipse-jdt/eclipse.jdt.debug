/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.io.File;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A label provider for source element qualifiers found with a JavaSourceLocator
 */
public class SourceElementQualifierProvider extends LabelProvider implements ILabelProvider {
	
	private JavaElementLabelProvider fJavaLabels;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IJavaElement) {
			IJavaElement parent = ((IJavaElement)element).getParent();
			return fJavaLabels.getText(parent);
		} else if (element instanceof ZipEntryStorage) {
			ZipEntryStorage storage = (ZipEntryStorage)element;
			ZipFile zipFile = storage.getArchive();
			IPath path = new Path(zipFile.getName());
			IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
			IResource res = entry.getResource();
			if (res == null) {
				// external
				return zipFile.getName();
			}
			// internal
			return res.getName();
		} else if (element instanceof LocalFileStorage) {
			LocalFileStorage storage = (LocalFileStorage)element;
			File extFile = storage.getFile();
			return extFile.getParent();
		}
		return super.getText(element);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof IJavaElement) {
			IJavaElement parent = ((IJavaElement)element).getParent();
			return fJavaLabels.getImage(parent);
		} else if (element instanceof ZipEntryStorage) {
            return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR_WITH_SOURCE);
		} else if (element instanceof LocalFileStorage) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		return super.getImage(element);
	}

	/**
	 * Constructs a new label provider
	 */
	public SourceElementQualifierProvider() {
		super();
		fJavaLabels = new JavaElementLabelProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		fJavaLabels.dispose();
		fJavaLabels = null;
	}
}
