/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Class provides the JavaElement labels for WorkbenchAdapater Objects while debugging
 */
public class JavaDebugWorkbenchAdapter implements IWorkbenchAdapter {
	private JavaElementImageProvider fImageProvider;

	public JavaDebugWorkbenchAdapter() {
		fImageProvider = new JavaElementImageProvider();
	}

	/*
	 *  Append Root path to identify full path for duplicate Java elements in source lookup dialog
	 */

	public String getLabel(Object element) {
		return JavaElementLabels.getTextLabel(getJavaElement(element), JavaElementLabels.ALL_DEFAULT | JavaElementLabels.APPEND_ROOT_PATH);
	}

	private IJavaElement getJavaElement(Object element) {
		if (element instanceof IJavaElement) {
			return (IJavaElement) element;
		}
		return null;
	}

	public Object[] getChildren(Object element) {
		IJavaElement je = getJavaElement(element);
		if (je instanceof IParent) {
			try {
				return ((IParent) je).getChildren();
			}
			catch (JavaModelException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return new Object[0];
	}

	public ImageDescriptor getImageDescriptor(Object element) {
		IJavaElement je = getJavaElement(element);
		if (je != null) {
			return fImageProvider.getJavaImageDescriptor(je, JavaElementImageProvider.OVERLAY_ICONS | JavaElementImageProvider.SMALL_ICONS);
		}

		return null;

	}

	public Object getParent(Object element) {
		IJavaElement je = getJavaElement(element);
		return je != null ? je.getParent() : null;
	}
}
