/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.model.elements.DebugTargetContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaElementContentProvider;

/**
 * @since 3.3
 */
public class JavaDebugTargetContentProvider extends DebugTargetContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	@Override
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (JavaElementContentProvider.isDisplayThreadGroups()) {
				IJavaDebugTarget debugTarget = (IJavaDebugTarget)element;
				if (debugTarget.isDisconnected() || debugTarget.isTerminated()) {
					return 0;
				}
				return debugTarget.getRootThreadGroups().length;
			}
		}
		return super.getChildCount(element, context, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	@Override
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (JavaElementContentProvider.isDisplayThreadGroups()) {
				return getElements(((IJavaDebugTarget)parent).getRootThreadGroups(), index, length);
			}
		}
		return super.getChildren(parent, index, length, context, monitor);
	}

}
