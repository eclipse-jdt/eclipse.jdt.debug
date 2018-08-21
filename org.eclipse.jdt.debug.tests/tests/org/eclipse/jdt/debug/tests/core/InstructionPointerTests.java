/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.internal.ui.InstructionPointerManager;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * @since 3.2
 */
public class InstructionPointerTests extends AbstractDebugTest {

	/**
	 * @param name
	 */
	public InstructionPointerTests(String name) {
		super(name);
	}

	/**
	 * Tests for instruction pointer leaks.
	 * This test should be run last in the test suite.
	 */
	public void testInstructionPointerLeaks() {
		assertEquals("Leaking instruction pointers", 0, InstructionPointerManager.getDefault().getInstructionPointerCount());
	}

}
