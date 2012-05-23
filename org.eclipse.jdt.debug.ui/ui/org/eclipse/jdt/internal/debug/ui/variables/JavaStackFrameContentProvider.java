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
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.model.elements.StackFrameContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * @since 3.3
 */
public class JavaStackFrameContentProvider extends StackFrameContentProvider {

	/* (non-Javadoc)
	 * 
	 * Cancels updates when thread is resumed.
	 * 
	 * @see org.eclipse.debug.internal.ui.model.elements.StackFrameContentProvider#getAllChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Object[] getAllChildren(Object parent, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		try {
			Object[] children = super.getAllChildren(parent, context, monitor);
			return JavaContentProviderFilter.filterVariables(children, context);
		} catch (CoreException e) {
			if (e.getStatus().getCode() == IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
				monitor.cancel();
				return EMPTY;
			}
			throw e;
		}
	}
	

}
