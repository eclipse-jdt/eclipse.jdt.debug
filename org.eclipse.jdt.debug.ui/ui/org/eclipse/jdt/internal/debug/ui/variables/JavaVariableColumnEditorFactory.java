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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * @since 3.2
 *
 */
public class JavaVariableColumnEditorFactory implements
		IColumnEditorFactoryAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter#createColumnEditor(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	public IColumnEditor createColumnEditor(IPresentationContext context, Object element) {
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId())) {
			if (element instanceof IJavaVariable) {
				return new JavaVariableColumnEditor();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter#getColumnEditorId(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	public String getColumnEditorId(IPresentationContext context, Object element) {
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId())) {
			if (element instanceof IJavaVariable) {
				return JavaVariableColumnEditor.JAVA_VARIABLE_COLUMN_EDITOR;
			}
		}
		return null;
	}

}
