package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for runtime classpath entries.
 */
public class RuntimeClasspathEntryLabelProvider extends LabelProvider {
		
		WorkbenchLabelProvider lp = new WorkbenchLabelProvider();
		
		/**
		 * @see ILabelProvider#getImage(Object)
		 */
		public Image getImage(Object element) {
			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)element;
			switch (entry.getType()) {
				case IRuntimeClasspathEntry.PROJECT:
					// XXX: what if project not loaded?
					IResource res = entry.getResource();
					IJavaElement proj = JavaCore.create(res);
					return lp.getImage(proj);
				case IRuntimeClasspathEntry.ARCHIVE:
					// XXX: illegal access to images
					boolean external = entry.getResource() == null;
					boolean source = entry.getSourceAttachmentPaths() != null;
					String key = null;
					if (external) {
						if (source) {
							key = JavaPluginImages.IMG_OBJS_EXTJAR_WSRC;
						} else {
							key = JavaPluginImages.IMG_OBJS_EXTJAR;
						}	
					} else {
						if (source) {
							key = JavaPluginImages.IMG_OBJS_JAR_WSRC;
						} else {
							key = JavaPluginImages.IMG_OBJS_JAR;
						}
					}
					return JavaPluginImages.get(key);
				case IRuntimeClasspathEntry.VARIABLE:
				case IRuntimeClasspathEntry.LIBRARY:
					// XXX: illegal internal access
					return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_ENV_VAR);
			}	
			return null;
		}

		/**
		 * @see ILabelProvider#getText(Object)
		 */
		public String getText(Object element) {
			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)element;
			switch (entry.getType()) {
				case IRuntimeClasspathEntry.PROJECT:
					IResource res = entry.getResource();
					IJavaElement proj = JavaCore.create(res);
					return lp.getText(proj);
				case IRuntimeClasspathEntry.ARCHIVE:
					res = entry.getResource();
					if (res == null) {
						return entry.getPath().toString();
					} else {
						return lp.getText(res);				
					}
				case IRuntimeClasspathEntry.VARIABLE:
					IPath path = entry.getPath();
					return path.segment(0);
				case IRuntimeClasspathEntry.LIBRARY:
					break;
			}	
			return ""; //$NON-NLS-1$
		}
		
		/**
		 * @see IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			super.dispose();
			lp.dispose();
		}

}

