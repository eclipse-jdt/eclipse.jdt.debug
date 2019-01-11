/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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
 *     Paul Pazderski - Bug 542989 - inherit from {@link JDIPlaceholderVariable} to prevent NPEs
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * Represents the closure context inside a lambda expression.
 */
public class JDILambdaVariable extends JDIPlaceholderVariable {

	public JDILambdaVariable(IJavaValue value) {
		super("Lambda", value); //$NON-NLS-1$
	}

	@Override
	public boolean isSynthetic() {
		return true;
	}
}
