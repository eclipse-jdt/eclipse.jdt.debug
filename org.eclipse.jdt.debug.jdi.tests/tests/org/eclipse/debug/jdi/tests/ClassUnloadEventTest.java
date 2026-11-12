/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdi.internal.ReferenceTypeImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;

/**
 * Tests for JDI com.sun.jdi.event.ClassUnloadEvent.
 */
@SuppressWarnings("restriction")
public class ClassUnloadEventTest extends AbstractJDITest {
	private static final String UNLOADED_CLASS = "org.eclipse.debug.jdi.tests.program.UnloadedClass";

	private ClassPrepareRequest fPrepareRequest;
	private EventWaiter fPrepareWaiter;
	/** The types of the class prepare events that came in. */
	private final List<ReferenceType> fPreparedTypes = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Creates a new test.
	 */
	public ClassUnloadEventTest() {
		super();
	}

	public ClassUnloadEventTest(String name) {
		super(name);
	}

	/**
	 * Init the fields that are used by this test only.
	 */
	@Override
	public void localSetUp() {
		// Reading a class prepare event puts the mirror of the class in the
		// cache of the VM
		fPrepareRequest = fVM.eventRequestManager().createClassPrepareRequest();
		fPrepareRequest.addClassFilter(UNLOADED_CLASS);
		fPrepareRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		fPrepareRequest.enable();
		fPrepareWaiter = new EventWaiter(fPrepareRequest, true) {
			@Override
			public boolean classPrepare(ClassPrepareEvent event) {
				if (fRequest.equals(event.request())) {
					fPreparedTypes.add(event.referenceType());
				}
				return true;
			}
		};
		fEventReader.addEventListener(fPrepareWaiter);
	}

	/**
	 * Make sure the test leaves the VM in the same state it found it.
	 */
	@Override
	public void localTearDown() {
		fEventReader.removeEventListener(fPrepareWaiter);
		fVM.eventRequestManager().deleteEventRequest(fPrepareRequest);
	}

	/**
	 * Run all tests and output to standard output.
	 */
	public static void main(java.lang.String[] args) {
		new ClassUnloadEventTest().runSuite(args);
	}

	/**
	 * Triggers the given event in the target VM and waits for the class unload
	 * event.
	 */
	private ClassUnloadEvent triggerAndWaitForUnload(String eventType) {
		fPreparedTypes.clear();
		ClassUnloadRequest request = fVM.eventRequestManager().createClassUnloadRequest();
		request.addClassFilter(UNLOADED_CLASS);
		request.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		request.enable();
		EventWaiter waiter = new EventWaiter(request, true);
		fEventReader.addEventListener(waiter);

		triggerEvent(eventType);
		// The target VM does not unload the classes that are known to the
		// debugger while all of its threads are suspended
		for (ThreadReference thread : fVM.allThreads()) {
			while (thread.suspendCount() > 0) {
				fVM.resume();
			}
		}

		Event event = waitForEvent(waiter, TIMEOUT);
		fEventReader.removeEventListener(waiter);
		waitUntilReady();
		fVM.eventRequestManager().deleteEventRequest(request);
		assertNotNull("ClassUnloadEvent for " + UNLOADED_CLASS + " did not come in", event);
		return (ClassUnloadEvent) event;
	}

	private boolean isCached(ReferenceType type) {
		return ((VirtualMachineImpl) fVM).getCachedMirror(((ReferenceTypeImpl) type).getRefTypeID()) != null;
	}

	/**
	 * Test JDI className() and classSignature(), and that the unloaded class
	 * is no longer known to the VM mirror.
	 */
	public void testJDIClassUnload() {
		ClassUnloadEvent event = triggerAndWaitForUnload("ClassUnloadEvent");
		assertEquals("1", UNLOADED_CLASS, event.className());
		assertEquals("2", "Lorg/eclipse/debug/jdi/tests/program/UnloadedClass;", event.classSignature());
		assertEquals("3", 1, fPreparedTypes.size());
		assertFalse("4", isCached(fPreparedTypes.get(0)));
	}

	/**
	 * Test that only the unloaded class is forgotten when another class with
	 * the same name is still loaded. The class that stays loaded is only
	 * prepared the first time.
	 */
	public void testJDIClassUnloadWithSameNameLoaded() {
		ClassUnloadEvent event = triggerAndWaitForUnload("ClassUnloadEvent1");
		assertEquals("1", UNLOADED_CLASS, event.className());
		assertFalse("2", fPreparedTypes.isEmpty());
		// Asking the VM for the loaded classes puts their mirrors in the cache,
		// so look at the cache first
		List<ReferenceType> cached = new ArrayList<>();
		for (ReferenceType type : fPreparedTypes) {
			if (isCached(type)) {
				cached.add(type);
			}
		}
		List<ReferenceType> loaded = fVM.classesByName(UNLOADED_CLASS);
		assertEquals("3", 1, loaded.size());
		for (ReferenceType type : fPreparedTypes) {
			assertEquals("4", loaded.contains(type), cached.contains(type));
		}
	}

	/**
	 * Test that an unloaded class is forgotten when its mirror was not created
	 * for a class prepare event, but by asking the VM for the classes with its
	 * name.
	 */
	public void testJDIClassUnloadOfLookedUpClass() {
		fPrepareRequest.disable();
		try {
			List<ReferenceType> loadedBefore = fVM.classesByName(UNLOADED_CLASS);
			triggerEvent("ClassUnloadEvent2");
			waitUntilReady();
			List<ReferenceType> lookedUp = new ArrayList<>(fVM.classesByName(UNLOADED_CLASS));
			lookedUp.removeAll(loadedBefore);
			assertEquals("1", 1, lookedUp.size());
			assertTrue("2", isCached(lookedUp.get(0)));

			triggerAndWaitForUnload("ClassUnloadEvent3");
			assertTrue("3", fPreparedTypes.isEmpty());
			assertFalse("4", isCached(lookedUp.get(0)));
		} finally {
			fPrepareRequest.enable();
		}
	}
}
