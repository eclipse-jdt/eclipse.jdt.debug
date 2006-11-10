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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;

/**
 * Content adapter for thread groups.
 * 
 * @since 3.2
 */
public class JavaThreadGroupContentAdapter extends AsynchronousContentAdapter {

    protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
		if (parent instanceof IJavaThreadGroup) {
			IJavaThreadGroup group = (IJavaThreadGroup) parent;
			IJavaThreadGroup[] threadGroups = group.getThreadGroups();
			IJavaThread[] threads = group.getThreads();
			Object[] kids = new Object[threadGroups.length + threads.length];
			int index = 0;
			for (int i = 0; i < threads.length; i++) {
				kids[index]= threads[i];
				index++;
			}
			for (int i = 0; i < threadGroups.length; i++) {
				kids[index] = threadGroups[i];
				index++;
			}
			return kids;
		}
		return EMPTY;
	}

	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		if (element instanceof IJavaThreadGroup) {
			IJavaThreadGroup group = (IJavaThreadGroup) element;
			return group.hasThreads() || group.hasThreadGroups();
		}
		return false;
	}

	protected boolean supportsPartId(String id) {
		return IDebugUIConstants.ID_DEBUG_VIEW.equals(id);
	}

}
