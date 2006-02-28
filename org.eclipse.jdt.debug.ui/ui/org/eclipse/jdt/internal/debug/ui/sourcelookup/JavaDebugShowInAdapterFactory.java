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
package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;

/**
 * @since 3.2
 *
 */
public class JavaDebugShowInAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IShowInSource.class) {
			if (adaptableObject instanceof IJavaStackFrame) {
				IJavaStackFrame frame = (IJavaStackFrame) adaptableObject;
				return new StackFrameShowInSourceAdapter(frame);
			}
		}
		if (adapterType == IShowInTargetList.class) {
			if (adaptableObject instanceof IJavaStackFrame) {
				return new StackFrameShowInTargetListAdapter();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{IShowInSource.class, IShowInTargetList.class};
	}

}
