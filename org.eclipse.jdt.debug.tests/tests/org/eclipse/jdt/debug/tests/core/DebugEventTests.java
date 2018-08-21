/*******************************************************************************
 *  Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * DebugEventTests
 */
public class DebugEventTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name the name of the test
	 */
	public DebugEventTests(String name) {
		super(name);
	}

	/**
	 * Ensure that a model specific event can be dispatched
	 */
	public void testModelSpecificEvent() {
		DebugEvent event = new DebugEvent(this, DebugEvent.MODEL_SPECIFIC, 5000);
		event.setData("TEST");

		DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.MODEL_SPECIFIC);
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {event});
		waiter.waitForEvent();
		DebugEvent received = waiter.getEvent();

		assertEquals("Incorrect detail", 5000, received.getDetail());
		assertEquals("incorrect user data", "TEST", received.getData());
		assertEquals("incorrect source", this, received.getSource());
	}
}
