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


import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A label provider for source elements found with a JavaSourceLocator
 */
public class SourceElementLabelProvider extends LabelProvider implements ILabelProvider {

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IJavaElement) {
			return ((IJavaElement)element).getElementName();
		} else if (element instanceof ZipEntryStorage) {
			ZipEntryStorage storage = (ZipEntryStorage)element;
			return storage.getZipEntry().getName();
		} else if (element instanceof LocalFileStorage) {
			LocalFileStorage storage = (LocalFileStorage)element;
			return storage.getName();
		}
		return super.getText(element);
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof ICompilationUnit) {
			return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CUNIT);
		} else if (element instanceof IClassFile) {
			return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CFILE);
		} else if (element instanceof ZipEntryStorage) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		} else if (element instanceof LocalFileStorage) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		}
		return super.getImage(element);
	}

}
