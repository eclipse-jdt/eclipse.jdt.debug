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

import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnEditor;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.ICellModifier;

/**
 * Editor for Java variable columns. Restricts edits to primitives and strings.
 * 
 * @since 3.2
 *
 */
public class JavaVariableColumnEditor extends VariableColumnEditor {

	public static final String JAVA_VARIABLE_COLUMN_EDITOR = JDIDebugUIPlugin.getUniqueIdentifier() + ".JAVA_VARIABLE_COLUMN_EDITOR"; //$NON-NLS-1$

	private ICellModifier fModifier;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor#getCellModifier()
	 */
	public ICellModifier getCellModifier() {
		if (fModifier == null) {
			fModifier = new JavaVariableCellModifier(getPresentationContext());
		}
		return fModifier;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor#getId()
	 */
	public String getId() {
		return JAVA_VARIABLE_COLUMN_EDITOR;
	}

}
