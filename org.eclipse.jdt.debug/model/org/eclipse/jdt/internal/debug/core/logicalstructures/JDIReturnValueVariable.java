/*******************************************************************************
 * Copyright (c) 2016 Till Brychcy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
