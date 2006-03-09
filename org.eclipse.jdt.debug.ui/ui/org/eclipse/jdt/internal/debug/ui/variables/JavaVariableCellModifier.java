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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.elements.adapters.DefaultVariableCellModifier;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * @since 3.2
 *
 */
public class JavaVariableCellModifier extends DefaultVariableCellModifier {

	/**
	 * Constructs a new cell modifier for Java variables.
	 * 
	 * @param context
	 */
	public JavaVariableCellModifier(IPresentationContext context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.DefaultVariableCellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		if (VariableColumnPresentation.COLUMN_VARIABLE_VALUE.equals(property)) {
			if (element instanceof IJavaVariable) {
				IJavaVariable var = (IJavaVariable) element;
				try {
					String signature = var.getSignature();
					if (signature.length() == 1) {
						// primitive
						return true;
					}
					return signature.equals("Ljava/lang/String;"); //$NON-NLS-1$
				} catch (DebugException e) {
				}
			}
		}
		return false;
	}



}
