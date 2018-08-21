/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;

/**
 * Adapter factory for Java debug target.
 *
 * @since 3.3
 */
public class TargetAdapterFactory implements IAdapterFactory{

	private static IModelProxyFactory fgJavaModelProxyFactory = new JavaModelProxyFactory();
	private static IElementContentProvider fgCPTarget = new JavaDebugTargetContentProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.equals(IModelProxyFactory.class)) {
			if (adaptableObject instanceof IJavaDebugTarget) {
				return (T) fgJavaModelProxyFactory;
			}
		}
		if (adapterType.equals(IElementContentProvider.class)) {
			if (adaptableObject instanceof IJavaDebugTarget) {
				return (T) fgCPTarget;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return new Class[]{
				IModelProxyFactory.class,
				IElementContentProvider.class};
	}

}
