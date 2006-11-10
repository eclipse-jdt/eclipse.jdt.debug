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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
import org.eclipse.jdt.internal.debug.ui.heapwalking.ObjectLabelAdapter;
import org.eclipse.jdt.internal.debug.ui.heapwalking.ObjectReferencesContentAdapter;

/**
 * @since 3.2
 *
 */
public class JavaDebugElementLabelAdapterFactory implements IAdapterFactory{
	
	private static IAsynchronousLabelAdapter fgThreadGroupLabelAdapter = new JavaThreadGroupLabelAdapter();
	private static IAsynchronousContentAdapter fgThreadGroupTreeAdapter = new JavaThreadGroupContentAdapter();
	private static IAsynchronousContentAdapter fgTargetTreeAdapter = new JavaDebugTargetContentAdapter();
	private static IModelProxyFactoryAdapter fgJavaModelProxyFactory = new JavaModelProxyFactory();
	private static IAsynchronousLabelAdapter fgObjectLabelAdapter = new ObjectLabelAdapter();
	private static IAsynchronousContentAdapter fgObjectContentAdapter = new ObjectReferencesContentAdapter();
	
	private static IElementContentProvider fgTargetPresentation = new JavaDebugTargetContentProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IAsynchronousLabelAdapter.class)) {
			if (adaptableObject instanceof IJavaThreadGroup) {
				return fgThreadGroupLabelAdapter;
			}
			if (adaptableObject instanceof IJavaObject) {
				return fgObjectLabelAdapter;
			}
		}
		if (adapterType.equals(IAsynchronousContentAdapter.class)) {
			if (adaptableObject instanceof IJavaThreadGroup) {
				return fgThreadGroupTreeAdapter;
			}
			if (adaptableObject instanceof IJavaDebugTarget) {
				return fgTargetTreeAdapter;
			}
			if (adaptableObject instanceof IJavaObject) {
				return fgObjectContentAdapter;
			}
		}
		if (adapterType.equals(IModelProxyFactoryAdapter.class)) {
			if (adaptableObject instanceof IJavaDebugTarget) {
				return fgJavaModelProxyFactory;
			}
		}
		if (adapterType.equals(IElementContentProvider.class)) {
			if (adaptableObject instanceof IJavaDebugTarget) {
				return fgTargetPresentation;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{IAsynchronousLabelAdapter.class, IAsynchronousContentAdapter.class, IModelProxyFactoryAdapter.class,
				IElementContentProvider.class};
	}

}
