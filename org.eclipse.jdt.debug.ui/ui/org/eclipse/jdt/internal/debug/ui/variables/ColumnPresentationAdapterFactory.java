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
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Adapter factory.
 *
 * @since 3.2
 */
public class ColumnPresentationAdapterFactory implements IAdapterFactory {

	private static final IColumnPresentationFactory fgColumnPresentation = new JavaVariableColumnPresentationFactory();
	private static final IElementEditor fgEEJavaVariable = new JavaVariableEditor();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IJavaVariable) {
			if (IElementEditor.class.equals(adapterType)) {
				return (T) fgEEJavaVariable;
			}
		}
		if (adaptableObject instanceof IJavaStackFrame) {
			if (IColumnPresentationFactory.class.equals(adapterType)) {
				return (T) fgColumnPresentation;
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
				IColumnPresentationFactory.class,
				IElementEditor.class};
	}

}
