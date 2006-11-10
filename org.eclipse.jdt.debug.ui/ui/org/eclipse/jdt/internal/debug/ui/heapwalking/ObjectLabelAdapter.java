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
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousLabelAdapter;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Label adapter for object references.
 * 
 * @since 3.3
 */
public class ObjectLabelAdapter extends AsynchronousLabelAdapter {

	private static JDIModelPresentation fgPresentation = new JDIModelPresentation();

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousLabelAdapter#getBackgrounds(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected RGB[] getBackgrounds(Object element, IPresentationContext context)
			throws CoreException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousLabelAdapter#getFontDatas(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected FontData[] getFontDatas(Object element,
			IPresentationContext context) throws CoreException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousLabelAdapter#getForegrounds(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected RGB[] getForegrounds(Object element, IPresentationContext context)
			throws CoreException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousLabelAdapter#getImageDescriptors(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected ImageDescriptor[] getImageDescriptors(Object element,
			IPresentationContext context) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousLabelAdapter#getLabels(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected String[] getLabels(Object element, IPresentationContext context)
			throws CoreException {
		return new String[]{fgPresentation.getValueText((IJavaValue) element)};
	}

}
