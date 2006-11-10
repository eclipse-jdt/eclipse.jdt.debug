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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;

/**
 * @since 3.3
 */
public class ThreadGroupAdapterFactory implements IAdapterFactory{
	
	
	private static IElementContentProvider fgCPThreadGroup = new JavaThreadGroupContentProvider();
	private static IElementLabelProvider fgLPThreadGroup = new JavaThreadGroupLabelProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {

		if (adapterType.equals(IElementContentProvider.class)) {
			if (adaptableObject instanceof IJavaThreadGroup) {
				return fgCPThreadGroup;
			}
		}
		if (adapterType.equals(IElementLabelProvider.class)) {
			if (adaptableObject instanceof IJavaThreadGroup) {
				return fgLPThreadGroup;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{
				IElementContentProvider.class,
				IElementLabelProvider.class};
	}

}
