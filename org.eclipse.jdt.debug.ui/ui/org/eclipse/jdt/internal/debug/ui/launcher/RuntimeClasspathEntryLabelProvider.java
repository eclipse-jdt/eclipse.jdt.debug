package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.JREContainer;
import org.eclipse.jdt.internal.launching.JREContainerInitializer;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.LabelProvider;
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
		private ILaunchConfiguration fLaunchConfuration;
		
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
					res = entry.getResource();
					if (res instanceof IContainer) {
						return lp.getImage(res);
					}
					boolean external = res == null;
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
					// XXX: illegal internal access
					return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_ENV_VAR);				
				case IRuntimeClasspathEntry.CONTAINER:
					// XXX: illegal internal access
					return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LIBRARY);
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
					if (path.equals(new Path(JavaRuntime.JRELIB_VARIABLE)) && fLaunchConfuration != null) {
						try {
							IVMInstall vm = JavaRuntime.computeVMInstall(fLaunchConfuration);					
							buf.append(" - "); //$NON-NLS-1$
							buf.append(vm.getName());
						} catch (CoreException e) {
							JDIDebugUIPlugin.log(e);
						}
					}
					return buf.toString();
				case IRuntimeClasspathEntry.CONTAINER:
					path = entry.getPath();
					if (fLaunchConfuration != null) {
						try {
							if (path.equals(new Path(JavaRuntime.JRE_CONTAINER))) {
								// default JRE - resolve the name for the launch config, rather than using the "workspace" default description
								IVMInstall vm = JavaRuntime.computeVMInstall(fLaunchConfuration);
								return MessageFormat.format(LauncherMessages.getString("RuntimeClasspathEntryLabelProvider.JRE_System_Library_[{0}]_2"), new String[]{vm.getName()}); //$NON-NLS-1$
							}
							IJavaProject project = JavaRuntime.getJavaProject(fLaunchConfuration);
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
							JDIDebugUIPlugin.log(e);
						}
					}
					return entry.getPath().toString();
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
		
		/**
		 * Sets the launch configuration context for this label provider
		 */
		public void setLaunchConfiguration(ILaunchConfiguration configuration) {
			fLaunchConfuration = configuration;
		}

}

