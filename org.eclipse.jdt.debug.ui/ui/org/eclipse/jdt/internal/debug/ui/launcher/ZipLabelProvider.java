/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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


