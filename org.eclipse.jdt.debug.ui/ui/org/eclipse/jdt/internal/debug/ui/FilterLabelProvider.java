/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for Filter model objects
 */
public class FilterLabelProvider extends LabelProvider implements ITableLabelProvider {

	private static final Image IMG_CUNIT =
		JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	private static final Image IMG_PKG =
		JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);

	/**
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	public String getColumnText(Object object, int column) {
		if (column == 0) {
			return ((Filter) object).getName();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	@Override
	public String getText(Object element) {
		return ((Filter) element).getName();
	}

	/**
	 * @see ITableLabelProvider#getColumnImage(Object, int)
	 */
	public Image getColumnImage(Object object, int column) {
		String name = ((Filter) object).getName();
		if (name.endsWith("*") || name.equals("(default package)")) { //$NON-NLS-1$ //$NON-NLS-2$
			return IMG_PKG;
		}
		return IMG_CUNIT;
	}
}
