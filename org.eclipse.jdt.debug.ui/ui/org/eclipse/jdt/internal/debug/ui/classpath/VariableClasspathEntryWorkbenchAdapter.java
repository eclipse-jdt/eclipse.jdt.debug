/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.classpath;

import org.eclipse.jdt.internal.launching.VariableClasspathEntry;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Workbench adapter for a string substitution runtime classpath entry.
 * 
 * @since 3.0
 */
public class VariableClasspathEntryWorkbenchAdapter implements IWorkbenchAdapter {
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
		if (object instanceof VariableClasspathEntry) {
			// TODO: illegal reference
			return JavaPluginImages.DESC_OBJS_JAR;
		}
		return null;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		if (o instanceof VariableClasspathEntry) {
			return ((VariableClasspathEntry)o).getName();
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
