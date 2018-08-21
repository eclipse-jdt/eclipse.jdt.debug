/*******************************************************************************
 * Copyright (c) 2016 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * Represents the return value after a "step-return".
 */
public class JDIReturnValueVariable extends JDIPlaceholderVariable {
	public final boolean hasResult;

	public JDIReturnValueVariable(String name, IJavaValue value, boolean hasResult) {
		super(name, value);
		this.hasResult = hasResult;
	}
}
