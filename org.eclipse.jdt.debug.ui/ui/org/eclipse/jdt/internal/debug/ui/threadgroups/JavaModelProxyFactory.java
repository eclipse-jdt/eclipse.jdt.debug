/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;

/**
 * @since 3.2
 *
 */
public class JavaModelProxyFactory implements IModelProxyFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelProxyFactory#createModelProxy(java.lang.Object, org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public IModelProxy createModelProxy(Object element, IPresentationContext context) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {
			if (element instanceof IJavaDebugTarget) {
				return new JavaDebugTargetProxy((IDebugTarget) element);
			}
		}
		return null;
	}

}
