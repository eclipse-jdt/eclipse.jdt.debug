/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
 *     BEA - Daniel R Somerfield - Bug 88939
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntry;
import org.eclipse.jdt.internal.launching.JREContainer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for runtime classpath entries.
 */
public class RuntimeClasspathEntryLabelProvider extends LabelProvider {

	private final WorkbenchLabelProvider lp = new WorkbenchLabelProvider();

	/**
	 * Context in which to render containers, or <code>null</code>
	 */
	private ILaunchConfiguration fLaunchConfiguration;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)element;
		IResource resource = entry.getResource();
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.PROJECT:
				IJavaElement proj = JavaCore.create(resource);
				if(proj == null) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT_CLOSED);
				}
				return lp.getImage(proj);
			case IRuntimeClasspathEntry.ARCHIVE:
				if (resource instanceof IContainer) {
					return lp.getImage(resource);
				}
				boolean external = resource == null;
				boolean source = (entry.getSourceAttachmentPath() != null && !Path.EMPTY.equals(entry.getSourceAttachmentPath()));
				String key = null;
				if (external) {
					IPath path = entry.getPath();
					if (path != null)
					{
						//TODO: check for invalid paths and change image
						File file = path.toFile();
						if (file.exists() && file.isDirectory()) {
							key = ISharedImages.IMG_OBJS_PACKFRAG_ROOT;
						} else if (source) {
	                        key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE_WITH_SOURCE;
						} else {
							key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE;
						}
					}

				} else {
					if (source) {
						key = ISharedImages.IMG_OBJS_JAR_WITH_SOURCE;
					} else {
						key = ISharedImages.IMG_OBJS_JAR;
					}
				}
				return JavaUI.getSharedImages().getImage(key);
			case IRuntimeClasspathEntry.VARIABLE:
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_ENV_VAR);
			case IRuntimeClasspathEntry.CONTAINER:
                return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
			case IRuntimeClasspathEntry.OTHER:
				IRuntimeClasspathEntry delegate = entry;
				if (entry instanceof ClasspathEntry) {
					delegate = ((ClasspathEntry)entry).getDelegate();
				}
				Image image = lp.getImage(delegate);
				if (image != null) {
					return image;
				}
				if (resource == null) {
                    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
				}
				return lp.getImage(resource);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)element;
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.PROJECT:
				IResource res = entry.getResource();
				IJavaElement proj = JavaCore.create(res);
				if(proj == null) {
					return entry.getPath().lastSegment();
				}
				return lp.getText(proj);
			case IRuntimeClasspathEntry.ARCHIVE:
				IPath path = entry.getPath();
				if (path == null) {
                    return NLS.bind(LauncherMessages.RuntimeClasspathEntryLabelProvider_Invalid_path, new String[]{"null"}); //$NON-NLS-1$
                }
                if (!path.isAbsolute() || !path.isValidPath(path.toString())) {
					return NLS.bind(LauncherMessages.RuntimeClasspathEntryLabelProvider_Invalid_path, new String[]{path.toOSString()});
				}
				String[] segments = path.segments();
				StringBuilder displayPath = new StringBuilder();
				if (segments.length > 0) {
					displayPath.append(segments[segments.length - 1]);
					displayPath.append(" - "); //$NON-NLS-1$
					String device = path.getDevice();
					if (device != null) {
						displayPath.append(device);
					}
					displayPath.append(File.separator);
					for (int i = 0; i < segments.length - 1; i++) {
						displayPath.append(segments[i]).append(File.separator);
					}
				} else {
					displayPath.append(path.toOSString());
				}
				return displayPath.toString();
			case IRuntimeClasspathEntry.VARIABLE:
				path = entry.getPath();
				IPath srcPath = entry.getSourceAttachmentPath();
				StringBuilder buf = new StringBuilder(path.toString());
				if (srcPath != null) {
					buf.append(" ["); //$NON-NLS-1$
					buf.append(srcPath.toString());
					IPath rootPath = entry.getSourceAttachmentRootPath();
					if (rootPath != null) {
						buf.append(IPath.SEPARATOR);
						buf.append(rootPath.toString());
					}
					buf.append(']');
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
						IJavaProject project = null;
						try {
							project = JavaRuntime.getJavaProject(fLaunchConfiguration);
						} catch (CoreException e) {
						}
						if (project == null) {
							if (path.segmentCount() > 0 && path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
								IVMInstall vm = JavaRuntime.getVMInstall(path);
								if (vm != null) {
									JREContainer container = new JREContainer(vm, path, project);
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
				IRuntimeClasspathEntry delegate = entry;
				if (entry instanceof ClasspathEntry) {
					delegate = ((ClasspathEntry)entry).getDelegate();
				}
				String name = lp.getText(delegate);
				if (name == null || name.length() == 0) {
					return ((IRuntimeClasspathEntry2)delegate).getName();
				}
				return name;
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
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
