/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
 * Waits for a type of event on a kind of element.  Compare this to SpecificDebugElementEventWaiter which is
 * used to wait for a type of event on a specific debug element object.
 */

public class DebugElementKindEventWaiter extends DebugEventWaiter {

	protected Class<?> fElementClass;

	/**
	 * Constructor
	 * @param eventKind
	 * @param elementClass
	 */
	public DebugElementKindEventWaiter(int eventKind, Class<?> elementClass) {
		super(eventKind);
		fElementClass = elementClass;
	}

	/**
	 * @see org.eclipse.jdt.debug.testplugin.DebugEventWaiter#accept(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	public boolean accept(DebugEvent event) {
		Object o = event.getSource();
		return super.accept(event) && fElementClass.isInstance(o);
	}

}


