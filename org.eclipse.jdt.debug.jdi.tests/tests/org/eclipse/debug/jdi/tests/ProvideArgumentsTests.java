/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.debug.jdi.tests;

import java.util.List;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

/**
 * Test cases for the implementation of providing argumebnt information even if
 * no debugging information is present in the new java 1.6 VM
 *
 * @since 3.3
 */
public class ProvideArgumentsTests extends AbstractJDITest {

	/** setup test info locally **/
	@Override
	public void localSetUp() {}

	/**
	 * tests getting argument values from a stackframe when no debugging
	 * info is available
	 */
	public void testGetArgumentValues() {
		try {
			Method method = getMethod("argValues", "(Ljava/lang/String;ILjava/lang/Object;)V");
			BreakpointRequest br = getBreakpointRequest(method.location());
			br.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			br.enable();

			EventWaiter waiter = new EventWaiter(br, true);
			fEventReader.addEventListener(waiter);
			triggerEvent("argvalues");
			BreakpointEvent bpe = (BreakpointEvent) waiter.waitEvent(10000);
			ThreadReference tref = bpe.thread();
			List<?> list = tref.frame(0).getArgumentValues();
			assertNotNull("list should not be null", list);
			assertTrue("first list item must be a String", list.get(0) instanceof StringReference);
			assertEquals("test string is not the same as was created in MainClass", "teststr", ((StringReference)list.get(0)).value());
			assertTrue("second list item must be an integer", list.get(1) instanceof IntegerValue);
			assertEquals("integer is not the same value as was passed in MainClass", 5, ((IntegerValue)list.get(1)).value());
			assertTrue("third list item must be a Double", list.get(2) instanceof ObjectReference);
			fEventReader.removeEventListener(waiter);
			tref.resume();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvalidStackFrameException e) {
			e.printStackTrace();
		} catch (IncompatibleThreadStateException e) {
			e.printStackTrace();
		}

	}
}
