/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Frits Jalvingh - Contribution for Bug 459831 - [launching] Support attaching
 *     	external annotations to a JRE container
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIImageDescriptor;
import org.eclipse.jdt.internal.debug.ui.jres.LibraryContentProvider.SubElement;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for jre libraries.
 *
 * @since 3.2
 */
public class LibraryLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		String key = null;
		IStatus status = Status.OK_STATUS;
		if (element instanceof LibraryStandin) {
			LibraryStandin library= (LibraryStandin) element;
			IPath sourcePath= library.getSystemLibrarySourcePath();
			if (sourcePath != null && !Path.EMPTY.equals(sourcePath)) {
                key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE_WITH_SOURCE;
			} else {
				key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE;
			}
			status = library.validate();

		} else if (element instanceof SubElement) {
			switch(((SubElement)element).getType()) {
				case SubElement.SOURCE_PATH:
					key = ISharedImages.IMG_OBJS_JAR_WITH_SOURCE;
					break;

				case SubElement.EXTERNAL_ANNOTATIONS_PATH:
					key = ISharedImages.IMG_OBJS_EXTERNAL_ANNOTATIONS;
					break;

				default:
				case SubElement.JAVADOC_URL:
					key = ISharedImages.IMG_OBJS_JAVADOCTAG;
					break;
			}
		}
		if(key != null) {
			if (!status.isOK()) {
				ImageDescriptor base = JavaUI.getSharedImages().getImageDescriptor(key);
				JDIImageDescriptor descriptor= new JDIImageDescriptor(base, JDIImageDescriptor.IS_OUT_OF_SYNCH);
				return JDIDebugUIPlugin.getImageDescriptorRegistry().get(descriptor);
			}
			return JavaUI.getSharedImages().getImage(key);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof LibraryStandin) {
			return ((LibraryStandin)element).getSystemLibraryPath().toOSString();
		} else if (element instanceof SubElement) {
			SubElement subElement= (SubElement) element;
			StringBuilder text= new StringBuilder();
			if (subElement.getType() == SubElement.SOURCE_PATH) {
				text.append(JREMessages.VMLibraryBlock_0);
				IPath systemLibrarySourcePath= subElement.getParent().getSystemLibrarySourcePath();
				if (systemLibrarySourcePath != null && !Path.EMPTY.equals(systemLibrarySourcePath)) {
					text.append(systemLibrarySourcePath.toOSString());
				} else {
					text.append(JREMessages.VMLibraryBlock_1);
				}
			} else if(subElement.getType() == SubElement.JAVADOC_URL){
				text.append(JREMessages.LibraryLabelProvider_0);
				URL javadocLocation= subElement.getParent().getJavadocLocation();
				if (javadocLocation != null) {
					text.append(javadocLocation.toExternalForm());
				} else {
					text.append(JREMessages.VMLibraryBlock_1);
				}
			} else if (subElement.getType() == SubElement.EXTERNAL_ANNOTATIONS_PATH) {
				text.append(JREMessages.VMExternalAnnsBlock_1);
				IPath externalAnnotationsPath = subElement.getParent().getExternalAnnotationsPath();
				if (externalAnnotationsPath != null && !Path.EMPTY.equals(externalAnnotationsPath)) {
					text.append(externalAnnotationsPath.toOSString());
				} else {
					text.append(JREMessages.VMExternalAnnsBlock_2);
				}
			}
			return text.toString();
		}
		return null;
	}

}