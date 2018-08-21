/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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
import org.eclipse.jdt.core.IJavaElement;

/**
 * Adapter factory for duplicate source lookup elements.
 *
 * @since 3.7
 */
public class SourceElementLabelProviderAdapterFactory implements IAdapterFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "restriction" })
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.equals(org.eclipse.debug.internal.ui.sourcelookup.SourceElementLabelProvider.class)
				&& adaptableObject instanceof IJavaElement) {
			return (T) new SourceElementLabelProviderAdapter();
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@SuppressWarnings("restriction")
	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { org.eclipse.debug.internal.ui.sourcelookup.SourceElementLabelProvider.class };
	}
}
