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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

public class RulerToggleLambdaEntryBreakpointActionDelegate extends AbstractRulerToggleBreakpointActionDelegate {

	@Override
	protected boolean doWork(ITextEditor editor, ITextSelection selection) {
		BreakpointToggleUtils.setUnsetLambdaEntryBreakpoint(true);
		return true;
	}
}
