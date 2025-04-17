/*******************************************************************************
 * Copyright (c) 2020, Andrey Loskutov <loskutov@gmx.de> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

public class RulerToggleTracepointActionDelegate extends AbstractRulerToggleBreakpointActionDelegate {

	@Override
	protected boolean doWork(ITextEditor editor, ITextSelection selection) {
		BreakpointToggleUtils.setUnsetTracepoints(true);
		return true;
	}
}
