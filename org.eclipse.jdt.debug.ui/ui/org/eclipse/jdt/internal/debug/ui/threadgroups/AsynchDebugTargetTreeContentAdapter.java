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
import org.eclipse.debug.internal.ui.elements.adapters.DebugTargetTreeContentAdapter;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

/**
 * Content adapter for debug target to show thread groups.
 * 
 * @since 3.2
 */
public class AsynchDebugTargetTreeContentAdapter extends DebugTargetTreeContentAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousTreeContentAdapter#getChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
		String id = context.getPart().getSite().getId();
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(id)) {
			if (isShowThreadGroups()) {
				if (parent instanceof IJavaDebugTarget) {
					IJavaDebugTarget target = (IJavaDebugTarget) parent;
					return target.getRootThreadGroups();
				}
			}
		}
		return super.getChildren(parent, context);
	}
	
	/**
	 * Returns whether thread groups are being displayed.
	 * 
	 * @return whether thread groups are being displayed
	 */
	protected static boolean isShowThreadGroups() {
		return JDIDebugUIPlugin.getDefault().getPluginPreferences().getBoolean(IJDIPreferencesConstants.PREF_SHOW_THREAD_GROUPS);
	}

}
