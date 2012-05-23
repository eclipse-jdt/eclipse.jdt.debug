/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.osgi.util.NLS;

/**
 * @since 3.3
 */
public class JavaThreadGroupLabelProvider extends ElementLabelProvider {
	
	private static ImageDescriptor fgImage = JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_OBJS_THREAD_GROUP);

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#getLabel(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	@Override
	protected String getLabel(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		IJavaThreadGroup group = (IJavaThreadGroup) elementPath.getLastSegment();
		return NLS.bind(ThreadGroupMessages.AsyncThreadGroupLabelAdapter_0, new String[]{group.getName()});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#getImageDescriptor(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	@Override
	protected ImageDescriptor getImageDescriptor(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		return fgImage;
	}

}
