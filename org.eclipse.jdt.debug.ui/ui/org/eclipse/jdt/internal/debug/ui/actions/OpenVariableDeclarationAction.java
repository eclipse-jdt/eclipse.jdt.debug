/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.core.model.JDIFieldVariable;
import org.eclipse.jdt.ui.JavaUI;

/**
 * Open the source code, where the variable is declared.
 */
public class OpenVariableDeclarationAction extends OpenVariableConcreteTypeAction {

	@Override
	protected IJavaType getTypeToOpen(IDebugElement element) throws CoreException {
		if (element instanceof IJavaFieldVariable) {
			var variable = (IJavaFieldVariable) element;
			return variable.getDeclaringType();
		}
		return null;
	}

	@Override
	protected void openInEditor(Object element, IType sourceElement) throws CoreException {
		if (element instanceof JDIFieldVariable) {
			var field = (JDIFieldVariable) element;
			var fieldElement = sourceElement.getField(field.getName());
			var editor = JavaUI.openInEditor(fieldElement);
			if (editor != null) {
				return;
			}
		}
		super.openInEditor(element, sourceElement);
	}
}
