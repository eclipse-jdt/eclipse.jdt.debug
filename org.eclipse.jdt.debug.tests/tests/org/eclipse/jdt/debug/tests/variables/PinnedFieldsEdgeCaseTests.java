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
package org.eclipse.jdt.debug.tests.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.variables.JavaContentProviderFilter;
import org.eclipse.jdt.internal.debug.ui.variables.PinnedFieldsManager;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Tests pinning fields of various kinds, and the robustness of the pins.
 */
public class PinnedFieldsEdgeCaseTests extends AbstractDebugTest {

	private static final String TYPE_NAME = "PinnedFieldsTarget";
	private static final int BREAKPOINT_LINE = 48;

	private static final String VIEW_ID = IDebugUIConstants.ID_VARIABLE_VIEW;
	private static final String PINNED_FIELDS_KEY = PinnedFieldsManager.getPreferenceKey(VIEW_ID);

	private final IPresentationContext fContext = new PresentationContext(VIEW_ID);
	private final String fShowStaticsKey = IDebugUIConstants.ID_VARIABLE_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_STATIC_VARIABLES;
	private final String fShowConstantsKey = IDebugUIConstants.ID_VARIABLE_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_CONSTANTS;
	private boolean fShowStatics;
	private boolean fShowConstants;

	private IJavaThread fThread;

	public PinnedFieldsEdgeCaseTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PinnedFieldsManager.getDefault().unpinAll(VIEW_ID);
		IPreferenceStore store = getStore();
		fShowStatics = store.getBoolean(fShowStaticsKey);
		fShowConstants = store.getBoolean(fShowConstantsKey);
		ILineBreakpoint bp = createLineBreakpoint(BREAKPOINT_LINE, TYPE_NAME);
		fThread = launchToLineBreakpoint(TYPE_NAME, bp);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			PinnedFieldsManager.getDefault().unpinAll(VIEW_ID);
			getStore().setValue(fShowStaticsKey, fShowStatics);
			getStore().setValue(fShowConstantsKey, fShowConstants);
			terminateAndRemove(fThread);
			removeAllBreakpoints();
		} finally {
			super.tearDown();
		}
	}

	public void testPinnedStaticFieldWhenStaticsAreShown() throws Exception {
		getStore().setValue(fShowStaticsKey, true);
		getStore().setValue(fShowConstantsKey, true);
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();

		manager().setPinned(VIEW_ID, List.of(field(object, "staticCounter"), field(object, "CONSTANT")), true);

		assertEquals(List.of("staticCounter", "CONSTANT"), firstNames(filter(fields), 2));
	}

	public void testPinnedStaticFieldIsNotShownWhenStaticsAreHidden() throws Exception {
		getStore().setValue(fShowStaticsKey, false);
		getStore().setValue(fShowConstantsKey, false);
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		manager().setPinned(VIEW_ID, List.of(field(object, "staticCounter"), field(object, "text"), field(object, "CONSTANT")), true);

		List<String> names = names(filter(fields));

		assertFalse("hidden static field shown: " + names, names.contains("staticCounter"));
		assertFalse("hidden constant shown: " + names, names.contains("CONSTANT"));
		assertEquals("text", names.get(0));
		// the hidden pins are not displayed, so they are skipped when moving the displayed ones
		assertFalse(manager().canMovePin(VIEW_ID, field(object, "text"), filter(fields), true));
	}

	public void testPrimitiveAndNullFields() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();

		manager().setPinned(VIEW_ID, List.of(field(object, "nullString")), true);
		manager().setPinned(VIEW_ID, List.of(field(object, "primitive")), true);

		assertEquals(List.of("nullString", "primitive"), firstNames(filter(fields), 2));
	}

	public void testFieldOfInnerClass() throws Exception {
		IJavaObject inner = (IJavaObject) field(getThis(), "inner").getValue();
		IVariable[] fields = inner.getVariables();
		assertTrue(names(fields).indexOf("other") > names(fields).indexOf("innerField"));

		manager().setPinned(VIEW_ID, List.of(field(inner, "other")), true);

		assertEquals("other", names(filter(fields)).get(0));
		assertEquals(TYPE_NAME + "$Inner#other", getStore().getString(PINNED_FIELDS_KEY));
	}

	public void testFieldOfGenericClass() throws Exception {
		IJavaObject box = (IJavaObject) field(getThis(), "box").getValue();
		IVariable[] fields = box.getVariables();

		manager().setPinned(VIEW_ID, List.of(field(box, "value")), true);

		assertEquals("value", names(filter(fields)).get(0));
		assertEquals(TYPE_NAME + "$Box#value", getStore().getString(PINNED_FIELDS_KEY));
	}

	public void testFieldOfJdkClass() throws Exception {
		IJavaObject list = (IJavaObject) field(getThis(), "list").getValue();
		IVariable[] fields = list.getVariables();
		IJavaFieldVariable size = field(list, "size");
		assertNotNull("ArrayList.size not found in " + names(fields), size);

		manager().setPinned(VIEW_ID, List.of(size), true);

		assertEquals("size", names(filter(fields)).get(0));
	}

	public void testArrayEntriesAreNotReordered() throws Exception {
		IJavaObject object = getThis();
		IVariable[] entries = field(object, "numbers").getValue().getVariables();
		manager().setPinned(VIEW_ID, List.of(field(object, "text"), field(object, "primitive")), true);

		assertSame("array entries should be returned unchanged", entries, manager().movePinnedFirst(VIEW_ID, entries));
	}

	public void testPinIsKeptAfterStep() throws Exception {
		IJavaObject object = getThis();
		manager().setPinned(VIEW_ID, List.of(field(object, "text")), true);

		// after "primitive++"
		fThread = stepOver(topFrame());
		fThread = stepOver(topFrame());

		IJavaObject after = getThis();
		assertEquals(43, Integer.parseInt(field(after, "primitive").getValue().getValueString()));
		assertEquals("text", names(filter(after.getVariables())).get(0));
	}

	public void testPinIsKeptInNewDebugSession() throws Exception {
		manager().setPinned(VIEW_ID, List.of(field(getThis(), "text")), true);
		terminateAndRemove(fThread);

		ILineBreakpoint bp = createLineBreakpoint(BREAKPOINT_LINE, TYPE_NAME);
		fThread = launchToLineBreakpoint(TYPE_NAME, bp);

		IJavaObject object = getThis();
		assertTrue(manager().isPinned(VIEW_ID, field(object, "text")));
		assertEquals("text", names(filter(object.getVariables())).get(0));
	}

	public void testTerminatedTarget() throws Exception {
		IJavaObject object = getThis();
		IJavaFieldVariable text = field(object, "text");
		IVariable[] fields = object.getVariables();
		manager().setPinned(VIEW_ID, List.of(text), true);

		terminateAndRemove(fThread);
		fThread = null;

		// stale variables of a terminated target must not break the views
		manager().isPinned(VIEW_ID, text);
		assertEquals(fields.length, manager().movePinnedFirst(VIEW_ID, fields).length);
		manager().canMovePin(VIEW_ID, text, fields, true);
		manager().movePin(VIEW_ID, text, fields, false);
		manager().setPinned(VIEW_ID, List.of(text), false);
	}

	public void testInvalidPreferenceValues() throws Exception {
		IVariable[] fields = getThis().getVariables();
		int shown = filter(fields).length;

		getStore().setValue(PINNED_FIELDS_KEY, ",,#bad,noSeparator, ,Unknown#field," + TYPE_NAME + "#text," + TYPE_NAME + "#text");

		assertTrue(manager().hasPinnedFields(VIEW_ID));
		assertEquals("text", names(filter(fields)).get(0));
		assertEquals("no field should be added or lost", shown, filter(fields).length);
	}

	public void testConcurrentReadsAndWrites() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		IJavaFieldVariable text = field(object, "text");
		IJavaFieldVariable primitive = field(object, "primitive");
		AtomicReference<Throwable> failure = new AtomicReference<>();
		AtomicBoolean writing = new AtomicBoolean(true);
		List<Thread> readers = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			Thread reader = new Thread(() -> {
				try {
					while (writing.get()) {
						Object[] result = manager().movePinnedFirst(VIEW_ID, fields);
						assertEquals(fields.length, result.length);
						manager().isPinned(VIEW_ID, text);
					}
				} catch (Throwable t) {
					failure.compareAndSet(null, t);
				}
			}, "Pinned fields reader " + i);
			readers.add(reader);
			reader.start();
		}
		try {
			for (int i = 0; i < 200; i++) {
				manager().setPinned(VIEW_ID, List.of(text, primitive), true);
				manager().movePin(VIEW_ID, primitive, fields, true);
				manager().setPinned(VIEW_ID, List.of(text), false);
				manager().unpinAll(VIEW_ID);
			}
		} finally {
			writing.set(false);
		}
		for (Thread reader : readers) {
			reader.join();
		}
		if (failure.get() != null) {
			throw new AssertionError("Concurrent read failed", failure.get());
		}
	}

	private static PinnedFieldsManager manager() {
		return PinnedFieldsManager.getDefault();
	}

	private static IPreferenceStore getStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
	}

	private IJavaObject getThis() throws Exception {
		IJavaObject object = topFrame().getThis();
		assertNotNull("'this' is null", object);
		return object;
	}

	private static IJavaFieldVariable field(IJavaObject object, String name) throws Exception {
		for (IVariable variable : object.getVariables()) {
			if (variable instanceof IJavaFieldVariable field && field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	private Object[] filter(Object[] fields) throws Exception {
		return JavaContentProviderFilter.filterVariables(fields, fContext);
	}

	private static List<String> names(Object[] variables) throws Exception {
		List<String> names = new ArrayList<>();
		for (Object variable : variables) {
			names.add(((IVariable) variable).getName());
		}
		return names;
	}

	private static List<String> firstNames(Object[] variables, int count) throws Exception {
		return names(variables).subList(0, count);
	}

	/**
	 * Returns the top frame of the suspended thread, waiting while it is not available (e.g. while the views evaluate
	 * the details of the values).
	 */
	private IJavaStackFrame topFrame() throws Exception {
		long end = System.currentTimeMillis() + DEFAULT_TIMEOUT;
		IJavaStackFrame frame = (IJavaStackFrame) fThread.getTopStackFrame();
		while (frame == null && System.currentTimeMillis() < end) {
			Thread.sleep(50);
			frame = (IJavaStackFrame) fThread.getTopStackFrame();
		}
		assertNotNull("Missing top frame", frame);
		return frame;
	}
}
