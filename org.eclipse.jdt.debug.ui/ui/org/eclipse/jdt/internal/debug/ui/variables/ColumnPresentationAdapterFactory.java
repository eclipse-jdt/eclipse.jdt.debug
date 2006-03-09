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
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Adapter factory.
 * 
 * @since 3.2
 */
public class ColumnPresentationAdapterFactory implements IAdapterFactory {
	
	private static final IAsynchronousLabelAdapter fgLabel = new JavaVariableLabelAdapter();
	private static final IColumnEditorFactoryAdapter fgColumnEditor = new JavaVariableColumnEditorFactory();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IJavaVariable) {
			if (IAsynchronousLabelAdapter.class.equals(adapterType)) {
				return fgLabel;
			}
			if (IColumnEditorFactoryAdapter.class.equals(adapterType)) {
				return fgColumnEditor;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{IAsynchronousLabelAdapter.class, IColumnEditorFactoryAdapter.class};
	}

}
