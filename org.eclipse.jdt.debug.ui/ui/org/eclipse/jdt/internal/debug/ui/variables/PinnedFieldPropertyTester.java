/*******************************************************************************
 * Copyright (c) 2026 Hélios Gilles and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hélios Gilles - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;

/**
 * Tests whether a field variable is pinned to the top of a variables view (property <code>isPinned</code>). Pins are
 * specific to a view: the id of the view is given as the argument of the test.
 *
 * @see PinnedFieldsManager
 */
public class PinnedFieldPropertyTester extends PropertyTester {

	private static final String IS_PINNED = "isPinned"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (IS_PINNED.equals(property) && receiver instanceof IJavaFieldVariable field && args.length == 1
				&& args[0] instanceof String viewId) {
			boolean pinned = PinnedFieldsManager.getDefault().isPinned(viewId, field);
			return expectedValue == null ? pinned : expectedValue.equals(Boolean.valueOf(pinned));
		}
		return false;
	}
}
