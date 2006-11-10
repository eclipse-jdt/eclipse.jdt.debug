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
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

/**
 * Content adapter for object references.
 * 
 * @since 3.3
 */
public class ObjectReferencesContentAdapter extends AsynchronousContentAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter#getChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
		if (parent instanceof IJavaObject) {
			IJavaObject object = (IJavaObject) parent;
			long count = JDIDebugUIPlugin.getDefault().getPreferenceStore().getLong(IJavaDebugUIConstants.PREF_ALLREFERENCES_MAX_COUNT);
			return object.getReferringObjects(count);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		if (element instanceof IJavaObject) {
			IJavaObject object = (IJavaObject) element;
			IJavaObject[] referringObjects = object.getReferringObjects(1);
			return referringObjects.length > 0;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter#supportsPartId(java.lang.String)
	 */
	protected boolean supportsPartId(String id) {
		return ReferencesViewer.VIEWER_ID.equals(id);
	}

}
