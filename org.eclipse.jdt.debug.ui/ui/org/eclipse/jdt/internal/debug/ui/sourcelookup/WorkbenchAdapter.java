/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.launching.sourcelookup.containers.ClasspathContainerSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.ClasspathVariableSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaProjectSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.PackageFragmentRootSourceContainer;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Workbench adapter for Java source containers and source container
 * types.
 * 
 * @since 3.0
 */
public class WorkbenchAdapter implements IWorkbenchAdapter {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (object instanceof PackageFragmentRootSourceContainer) {
			PackageFragmentRootSourceContainer container = (PackageFragmentRootSourceContainer) object;
			IPackageFragmentRoot fragmentRoot = container.getPackageFragmentRoot();
			return getImageDescriptor(fragmentRoot);
		}
		if (object instanceof JavaProjectSourceContainer) {
			JavaProjectSourceContainer container = (JavaProjectSourceContainer) object;
			IJavaProject javaProject = container.getJavaProject();
			return getImageDescriptor(javaProject);
		}		
		if (object instanceof ClasspathVariableSourceContainer) {
			return DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_ENV_VAR);
		}
		if (object instanceof ClasspathContainerSourceContainer) {
            return JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_LIBRARY);
		}
		return null;
	}
	
	/**
	 * Returns an image descriptor for a java element, or <code>null</code>
	 * if none.
	 * 
	 * @param element java element
	 * @return an image descriptor for a java element, or <code>null</code>
	 * if none
	 */
	protected ImageDescriptor getImageDescriptor(IJavaElement element) {
		IWorkbenchAdapter adapter = (IWorkbenchAdapter)element.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			return adapter.getImageDescriptor(element);
		}	
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		if (o instanceof PackageFragmentRootSourceContainer) {
			PackageFragmentRootSourceContainer container = (PackageFragmentRootSourceContainer) o;
			IPackageFragmentRoot fragmentRoot = container.getPackageFragmentRoot();
			IPath path = fragmentRoot.getPath();
			if (path.segmentCount() > 0) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(path.lastSegment());
				if (path.segmentCount() > 1) {
					buffer.append(" - "); //$NON-NLS-1$
					if (path.getDevice() != null) {
						buffer.append(path.getDevice());
					}
					String[] segments = path.segments();
					for (int i = 0; i < segments.length - 1; i++) {
						buffer.append(File.separatorChar);
						buffer.append(segments[i]);
					}
				}
				return buffer.toString();
			}
		}
		return ""; //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return null;
	}
}
