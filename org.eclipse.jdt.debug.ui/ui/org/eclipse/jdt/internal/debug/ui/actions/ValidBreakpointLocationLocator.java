/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * This class remains for internal binary compatibility. The original class moved to a different
 * package. @see bug 297808.
 */
public class ValidBreakpointLocationLocator extends org.eclipse.jdt.internal.debug.core.breakpoints.ValidBreakpointLocationLocator {

	/**
	 * @param compilationUnit the JDOM CompilationUnit of the source code.
	 * @param lineNumber the line number in the source code where to put the breakpoint.
	 * @param bestMatch if <code>true</code> look for the best match, otherwise look only for a valid line
	 */
	public ValidBreakpointLocationLocator(CompilationUnit compilationUnit, int lineNumber, boolean bindingsResolved, boolean bestMatch) {
		super(compilationUnit, lineNumber, bindingsResolved, bestMatch);
	}
}
