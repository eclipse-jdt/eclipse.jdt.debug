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
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == IShowInSource.class) {
			if (adaptableObject instanceof IJavaStackFrame) {
				IJavaStackFrame frame = (IJavaStackFrame) adaptableObject;
				return (T) new StackFrameShowInSourceAdapter(frame);
			}
		}
		if (adapterType == IShowInTargetList.class) {
			if (adaptableObject instanceof IJavaStackFrame) {
				return (T) new StackFrameShowInTargetListAdapter();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return new Class[]{IShowInSource.class, IShowInTargetList.class};
	}

}
