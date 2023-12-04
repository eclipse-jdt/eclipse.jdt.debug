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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * @since 3.2
 */
public class JavaVariableColumnPresentationFactory implements IColumnPresentationFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentationFactoryAdapter#createColumnPresentation(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	@Override
	public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
		if (isApplicable(context, element)) {
			return new JavaVariableColumnPresentation();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentationFactoryAdapter#getColumnPresentationId(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.Object)
	 */
	@Override
	public String getColumnPresentationId(IPresentationContext context, Object element) {
		if (isApplicable(context, element)) {
			return JavaVariableColumnPresentation.JAVA_VARIABLE_COLUMN_PRESENTATION;
		}
		return null;
	}

	private boolean isApplicable(IPresentationContext context, Object element) {
		IJavaStackFrame frame = null;
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId())) {
			if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable)element;
				frame = adaptable.getAdapter(IJavaStackFrame.class);
			}
		}
		return frame != null;
	}

}
