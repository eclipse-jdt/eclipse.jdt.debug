/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugElement;

/**
 * This event waiter is used to wait for a certain type of event (create, terminate, suspend, etc.)
 * on a *specific* debug element.  Contrast this with DebugElementKindEventWaiter which is similar,
 * but is used to wait for a certain type of event on a *kind* of debug element (thread, debug target, etc.)
 */
public class SpecificDebugElementEventWaiter extends DebugEventWaiter {

	protected IDebugElement fDebugElement;

	/**
	 * Constructor
	 * @param eventKind
	 * @param element
	 */
	public SpecificDebugElementEventWaiter(int eventKind, IDebugElement element) {
		super(eventKind);
		fDebugElement = element;
	}

	/**
	 * @see org.eclipse.jdt.debug.testplugin.DebugEventWaiter#accept(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	public boolean accept(DebugEvent event) {
		Object o = event.getSource();
		if (o instanceof IDebugElement) {
			return super.accept(event) && ((IDebugElement)o).equals(fDebugElement);
		}
		return false;
	}
}
