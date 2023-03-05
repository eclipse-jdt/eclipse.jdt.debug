/*******************************************************************************
 * Copyright (c) 2023 Zsombor Gegesy and others.
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

import java.util.Objects;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaStackFramePreferencePage;
import org.eclipse.jdt.internal.ui.filtertable.Filter;
import org.eclipse.jdt.internal.ui.filtertable.FilterManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Add the current stack frame to the highlighted frames.
 *
 */
public class HighlightStackFrameAction extends ObjectActionDelegate {

	@Override
	public void run(IAction action) {
		// Make sure there is a current selection
		IStructuredSelection selection = getCurrentSelection();
		if (selection == null) {
			return;
		}

		final var preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		final var filterManager = JavaStackFramePreferencePage.CUSTOM_STACK_FRAMES;
		for (Object selected : selection) {
			if (selected instanceof IJavaStackFrame frame) {
				try {
					addFilter(filterManager, preferenceStore, frame.getDeclaringTypeName());
					// Reset all the stack frames, to be evaluated on the next step again.
					var thread = frame.getThread();
					var frames = thread.getStackFrames();
					for (var oneFrame : frames) {
						if (oneFrame instanceof IJavaStackFrame javaFrame) {
							javaFrame.setCategory(null);
						}
					}
				} catch (DebugException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}

	}

	private void addFilter(FilterManager filterManager, IPreferenceStore preferenceStore, String filterName) {
		var allStoredFilters = filterManager.getAllStoredFilters(preferenceStore, false);
		if (enableFilter(allStoredFilters, filterName)) {
			filterManager.save(preferenceStore, allStoredFilters);
		} else {
			var plusOne = new Filter[allStoredFilters.length + 1];
			plusOne[0] = new Filter(filterName, true);
			System.arraycopy(allStoredFilters, 0, plusOne, 1, allStoredFilters.length);
			filterManager.save(preferenceStore, plusOne);
		}
	}

	private boolean enableFilter(Filter[] allFilter, String filterName) {
		for (Filter filter : allFilter) {
			if (Objects.equals(filterName, filter.getName())) {
				filter.setChecked(true);
				return true;
			}
		}
		return false;
	}
}
