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

/**
 * Waits for an event on a specific element
 */
public class DebugElementEventWaiter extends DebugEventWaiter {

	protected Object fElement;

	/**
	 * Constructor
	 * @param kind
	 * @param element
	 */
	public DebugElementEventWaiter(int kind, Object element) {
		super(kind);
		fElement = element;
	}

	/**
	 * @see org.eclipse.jdt.debug.testplugin.DebugEventWaiter#accept(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	public boolean accept(DebugEvent event) {
		return super.accept(event) && fElement == event.getSource();
	}

}
