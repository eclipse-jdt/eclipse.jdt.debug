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
import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.internal.launching.JREContainer;
import org.eclipse.jdt.internal.launching.JREContainerInitializer;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for runtime classpath entries.
 */
public class RuntimeClasspathEntryLabelProvider extends LabelProvider {
		
	private WorkbenchLabelProvider lp = new WorkbenchLabelProvider();
	
	/**
	 * Context in which to render containers, or <code>null</code>
	 */
	private ILaunchConfiguration fLaunchConfiguration;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)element;
		IResource resource = entry.getResource();
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.PROJECT:
				//TODO what if project not loaded?
				IJavaElement proj = JavaCore.create(resource);
				return lp.getImage(proj);
			case IRuntimeClasspathEntry.ARCHIVE:
				//TODO illegal access to images
				if (resource instanceof IContainer) {
					return lp.getImage(resource);
				}
				boolean external = resource == null;
				boolean source = (entry.getSourceAttachmentPath() != null && !Path.EMPTY.equals(entry.getSourceAttachmentPath()));
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
				//TODO: illegal internal access
				return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_ENV_VAR);				
			case IRuntimeClasspathEntry.CONTAINER:
				// TODO: illegal internal access
				return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LIBRARY);
			case IRuntimeClasspathEntry.OTHER:
				if (resource == null) {
					return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LIBRARY);
				} else {
					return lp.getImage(resource);
				}
		}	
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
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
					return entry.getPath().toOSString();
				} else {
					String[] segments = entry.getPath().segments();
					StringBuffer displayPath = new StringBuffer();
					for (int i = 0; i < segments.length-1; i++) 
						displayPath.append(segments[i] + File.separator);					
					return lp.getText(res) + " - " + displayPath.toString(); //$NON-NLS-1$
				}
			case IRuntimeClasspathEntry.VARIABLE:
				IPath path = entry.getPath();
				IPath srcPath = entry.getSourceAttachmentPath();
				StringBuffer buf = new StringBuffer(path.toString());
				if (srcPath != null) {
					buf.append(" ["); //$NON-NLS-1$
					buf.append(srcPath.toString());
					IPath rootPath = entry.getSourceAttachmentRootPath();
					if (rootPath != null) {
						buf.append(IPath.SEPARATOR);
						buf.append(rootPath.toString());
					}
					buf.append(']'); //$NON-NLS-1$
				}
				// append JRE name if we can compute it
				if (path.equals(new Path(JavaRuntime.JRELIB_VARIABLE)) && fLaunchConfiguration != null) {
					try {
						IVMInstall vm = JavaRuntime.computeVMInstall(fLaunchConfiguration);					
						buf.append(" - "); //$NON-NLS-1$
						buf.append(vm.getName());
					} catch (CoreException e) {
					}
				}
				return buf.toString();
			case IRuntimeClasspathEntry.CONTAINER:
				path = entry.getPath();
				if (fLaunchConfiguration != null) {
					try {
						if (path.equals(new Path(JavaRuntime.JRE_CONTAINER))) {
							// default JRE - resolve the name for the launch config, rather than using the "workspace" default description
							IVMInstall vm = JavaRuntime.computeVMInstall(fLaunchConfiguration);
							return MessageFormat.format(LauncherMessages.getString("RuntimeClasspathEntryLabelProvider.JRE_System_Library_[{0}]_2"), new String[]{vm.getName()}); //$NON-NLS-1$
						}
						IJavaProject project = null;
						try {
							project = JavaRuntime.getJavaProject(fLaunchConfiguration);
						} catch (CoreException e) {
						}
						if (project == null) {
							if (path.segmentCount() > 0 && path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
								IVMInstall vm = JREContainerInitializer.resolveVM(path);
								if (vm != null) {
									JREContainer container = new JREContainer(vm, path);
									return container.getDescription();
								}
							}
						} else {
							IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
							if (container != null) {
								return container.getDescription();
							}
						}
					} catch (CoreException e) {
					}
				}
				return entry.getPath().toString();
			case IRuntimeClasspathEntry.OTHER:
				return ((IRuntimeClasspathEntry2)entry).getName();	
		}	
		return ""; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		lp.dispose();
	}
	
	/**
	 * Sets the launch configuration context for this label provider
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		fLaunchConfiguration = configuration;
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}
}