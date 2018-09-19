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
package org.eclipse.jdt.internal.debug.ui.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.jdt.core.JavaCore;

/**
 * Resolves to Java-like file extensions for hyperlink matching.
 *
 * @since 3.2
 */
public class JavaLikeExtensionsResolver implements IDynamicVariableResolver {

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		String[] javaLikeExtensions = JavaCore.getJavaLikeExtensions();
		StringBuilder buffer = new StringBuilder();
		if (javaLikeExtensions.length > 1) {
			buffer.append("("); //$NON-NLS-1$
		}
		for (int i = 0; i < javaLikeExtensions.length; i++) {
			String ext = javaLikeExtensions[i];
			buffer.append("\\."); //$NON-NLS-1$
			buffer.append(ext);
			buffer.append(":"); //$NON-NLS-1$
			if (i < (javaLikeExtensions.length - 1)) {
				buffer.append("|"); //$NON-NLS-1$
			}
		}
		if (javaLikeExtensions.length > 1) {
			buffer.append(")"); //$NON-NLS-1$
		}
		return buffer.toString();
	}

}
