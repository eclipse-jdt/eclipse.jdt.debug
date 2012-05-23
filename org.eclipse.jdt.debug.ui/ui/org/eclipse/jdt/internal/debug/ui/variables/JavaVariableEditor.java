/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.debug.internal.ui.model.elements.VariableEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Editor for Java variable columns. Restricts edits to primitives and strings.
 * 
 * @since 3.2
 *
 */
public class JavaVariableEditor extends VariableEditor {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellModifier(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
	 */
	@Override
	public ICellModifier getCellModifier(IPresentationContext context, Object element) {
		return new JavaVariableCellModifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellEditor(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String, java.lang.Object, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
		if (element instanceof IJavaVariable) {
			IJavaVariable var = (IJavaVariable) element;
			if (JavaVariableCellModifier.isBoolean(var)) {
				return new ComboBoxCellEditor(parent, new String[]{Boolean.toString(true), Boolean.toString(false)}, SWT.READ_ONLY);
			}
		}
		return super.getCellEditor(context, columnId, element, parent);
	}

}
