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

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

class ZipLabelProvider extends LabelProvider {
	
	private final Image IMG_JAR= JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR);
	private final Image IMG_FOLDER= PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	
	public Image getImage(Object element) {
		if (element == null || !(element instanceof ZipTreeNode))
			return super.getImage(element);
		if (((ZipTreeNode)element).representsZipFile())
			return IMG_JAR;
		else 
			return IMG_FOLDER;
	}

	public String getText(Object element) {
		if (element == null || !(element instanceof ZipTreeNode))
			return super.getText(element);
		return ((ZipTreeNode) element).getName();
	}
}


