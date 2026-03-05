/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.jdi.tests;

import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.EventRequest;
/**
 * Listen for MethodExitEvent for a specific method.
 */
public class MethodExitEventWaiter extends EventWaiter {
	protected String fMethodName;

	/**
	 * Constructor
	 */
	public MethodExitEventWaiter(EventRequest request, boolean shouldGo, String methodName) {
		super(request, shouldGo);
		fMethodName = methodName;
	}

	/**
	 * @see org.eclipse.debug.jdi.tests.EventWaiter#methodExit((com.sun.jdi.event.MethodExitEvent)
	 */
	@Override
	public boolean methodExit(MethodExitEvent event) {
		if (event.method().name().equals(fMethodName)) {
			notifyEvent(event);
			return fShouldGo;
		}
		return true;
	}
}
