/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousLabelAdapter;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Label adapter for thread group.
 * 
 * @since 3.2
 */
public class JavaThreadGroupLabelAdapter extends AsynchronousLabelAdapter {
	
	private static ImageDescriptor[] image = new ImageDescriptor[]{JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_OBJS_THREAD_GROUP)};

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousLabelAdapter#getLabels(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	protected String[] getLabels(Object element, IPresentationContext context) throws CoreException {
		if (element instanceof IJavaThreadGroup) {
			IJavaThreadGroup group = (IJavaThreadGroup) element;
			return new String[]{MessageFormat.format(ThreadGroupMessages.AsyncThreadGroupLabelAdapter_0, new String[]{group.getName()})};
		}
		return new String[]{""}; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousLabelAdapter#getImageDescriptors(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	protected ImageDescriptor[] getImageDescriptors(Object element, IPresentationContext context) throws CoreException {
		return image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousLabelAdapter#getFontDatas(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	protected FontData[] getFontDatas(Object element, IPresentationContext context) throws CoreException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousLabelAdapter#getForegrounds(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	protected RGB[] getForegrounds(Object element, IPresentationContext context) throws CoreException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousLabelAdapter#getBackgrounds(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	protected RGB[] getBackgrounds(Object element, IPresentationContext context) throws CoreException {
		return null;
	}

}
