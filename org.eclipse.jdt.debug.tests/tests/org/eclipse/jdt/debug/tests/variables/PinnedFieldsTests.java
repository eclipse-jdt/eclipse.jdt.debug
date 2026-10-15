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
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.variables.JavaContentProviderFilter;
import org.eclipse.jdt.internal.debug.ui.variables.PinnedFieldsManager;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Tests pinning fields to the top of the variables views.
 */
public class PinnedFieldsTests extends AbstractDebugTest {

	private static final String TYPE_NAME = "InstanceVariablesTests";

	private static final String VIEW_ID = IDebugUIConstants.ID_VARIABLE_VIEW;
	private static final String OTHER_VIEW_ID = IDebugUIConstants.ID_EXPRESSION_VIEW;
	private static final String PINNED_FIELDS_KEY = PinnedFieldsManager.getPreferenceKey(VIEW_ID);

	private final IPresentationContext fContext = new PresentationContext(VIEW_ID);
	private final IPresentationContext fOtherContext = new PresentationContext(OTHER_VIEW_ID);

	private IJavaThread fThread;

	public PinnedFieldsTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PinnedFieldsManager.getDefault().unpinAll(VIEW_ID);
		PinnedFieldsManager.getDefault().unpinAll(OTHER_VIEW_ID);
		ILineBreakpoint bp = createLineBreakpoint(33, TYPE_NAME);
		fThread = launchToLineBreakpoint(TYPE_NAME, bp);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			PinnedFieldsManager.getDefault().unpinAll(VIEW_ID);
			PinnedFieldsManager.getDefault().unpinAll(OTHER_VIEW_ID);
			terminateAndRemove(fThread);
			removeAllBreakpoints();
		} finally {
			super.tearDown();
		}
	}

	public void testNothingPinned() throws Exception {
		IVariable[] fields = getThis().getVariables();
		assertFalse(PinnedFieldsManager.getDefault().hasPinnedFields(VIEW_ID));
		assertEquals(names(fields), names(JavaContentProviderFilter.filterVariables(fields, fContext)));
	}

	public void testPinnedFieldsAreFirstInPinOrder() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		IJavaFieldVariable date = object.getField("date", false);
		IJavaFieldVariable privStr = object.getField("privStr", false);

		PinnedFieldsManager.getDefault().setPinned(VIEW_ID, List.of(date), true);
		PinnedFieldsManager.getDefault().setPinned(VIEW_ID, List.of(privStr), true);

		List<String> expected = new ArrayList<>(names(fields));
		expected.removeAll(List.of("date", "privStr"));
		expected.addAll(0, List.of("date", "privStr"));
		assertEquals(expected, names(JavaContentProviderFilter.filterVariables(fields, fContext)));
		assertTrue(PinnedFieldsManager.getDefault().isPinned(VIEW_ID, date));
		assertTrue(PinnedFieldsManager.getDefault().isPinned(VIEW_ID, privStr));
	}

	public void testPinOrderIsRestoredFromPreference() throws Exception {
		IVariable[] fields = getThis().getVariables();

		JDIDebugUIPlugin.getDefault().getPreferenceStore().setValue(PINNED_FIELDS_KEY,
				TYPE_NAME + "#protStr," + TYPE_NAME + "#nullDate");

		assertEquals(List.of("protStr", "nullDate"), names(JavaContentProviderFilter.filterVariables(fields, fContext)).subList(0, 2));
	}

	public void testMovePin() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		IJavaFieldVariable privStr = object.getField("privStr", false);
		IJavaFieldVariable date = object.getField("date", false);
		PinnedFieldsManager manager = PinnedFieldsManager.getDefault();
		manager.setPinned(VIEW_ID, List.of(privStr, date), true);

		assertTrue(manager.movePin(VIEW_ID, date, fields, true));
		assertEquals(List.of("date", "privStr"), names(JavaContentProviderFilter.filterVariables(fields, fContext)).subList(0, 2));
		assertEquals(TYPE_NAME + "#date," + TYPE_NAME + "#privStr", JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(PINNED_FIELDS_KEY));

		assertTrue(manager.movePin(VIEW_ID, date, fields, false));
		assertEquals(List.of("privStr", "date"), names(JavaContentProviderFilter.filterVariables(fields, fContext)).subList(0, 2));
	}

	public void testMovePinAtBoundaries() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		IJavaFieldVariable privStr = object.getField("privStr", false);
		IJavaFieldVariable date = object.getField("date", false);
		PinnedFieldsManager manager = PinnedFieldsManager.getDefault();
		manager.setPinned(VIEW_ID, List.of(privStr, date), true);

		assertFalse("the first pin should not move up", manager.canMovePin(VIEW_ID, privStr, fields, true));
		assertTrue("the first pin should move down", manager.canMovePin(VIEW_ID, privStr, fields, false));
		assertTrue("the last pin should move up", manager.canMovePin(VIEW_ID, date, fields, true));
		assertFalse("the last pin should not move down", manager.canMovePin(VIEW_ID, date, fields, false));
		assertFalse("an unpinned field should not move", manager.canMovePin(VIEW_ID, object.getField("pubStr", false), fields, true));
		assertFalse("the first pin should not have moved up", manager.movePin(VIEW_ID, privStr, fields, true));
		assertFalse("the last pin should not have moved down", manager.movePin(VIEW_ID, date, fields, false));
		assertFalse("an unpinned field should not have moved", manager.movePin(VIEW_ID, object.getField("pubStr", false), fields, true));
		assertEquals(List.of("privStr", "date"), names(JavaContentProviderFilter.filterVariables(fields, fContext)).subList(0, 2));
	}

	public void testMovePinSkipsPinsNotDisplayed() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		IJavaFieldVariable privStr = object.getField("privStr", false);
		IJavaFieldVariable date = object.getField("date", false);
		IJavaFieldVariable subPubStr = getSubclassInstance().getField("pubStr", false);
		PinnedFieldsManager manager = PinnedFieldsManager.getDefault();
		manager.setPinned(VIEW_ID, List.of(privStr, subPubStr, date), true);

		// the pin of IVTSubclass.pubStr is not displayed among the fields of an InstanceVariablesTests
		assertTrue(manager.movePin(VIEW_ID, date, fields, true));
		assertEquals(List.of("date", "privStr"), names(JavaContentProviderFilter.filterVariables(fields, fContext)).subList(0, 2));
		assertEquals(TYPE_NAME + "#date,IVTSubclass#pubStr," + TYPE_NAME + "#privStr",
				JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(PINNED_FIELDS_KEY));
	}

	public void testPinIsPersisted() throws Exception {
		IJavaFieldVariable privStr = getThis().getField("privStr", false);

		PinnedFieldsManager.getDefault().setPinned(VIEW_ID, List.of(privStr), true);

		assertEquals(TYPE_NAME + "#privStr", JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(PINNED_FIELDS_KEY));
	}

	public void testPinAppliesToSubtypes() throws Exception {
		IJavaObject subclass = getSubclassInstance();
		IJavaFieldVariable privStr = getThis().getField("privStr", false);

		PinnedFieldsManager.getDefault().setPinned(VIEW_ID, List.of(privStr), true);

		IVariable[] subclassFields = subclass.getVariables();
		assertEquals("privStr", names(JavaContentProviderFilter.filterVariables(subclassFields, fContext)).get(0));
		assertTrue(PinnedFieldsManager.getDefault().isPinned(VIEW_ID, subclass.getField("privStr", false)));
	}

	public void testPinDoesNotApplyToHidingField() throws Exception {
		IJavaObject subclass = getSubclassInstance();
		IJavaFieldVariable superPubStr = subclass.getField("pubStr", true);
		IJavaFieldVariable subPubStr = subclass.getField("pubStr", false);

		PinnedFieldsManager.getDefault().setPinned(VIEW_ID, List.of(superPubStr), true);

		assertTrue(PinnedFieldsManager.getDefault().isPinned(VIEW_ID, superPubStr));
		assertFalse(PinnedFieldsManager.getDefault().isPinned(VIEW_ID, subPubStr));
	}

	public void testUnpin() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		IJavaFieldVariable date = object.getField("date", false);
		IJavaFieldVariable privStr = object.getField("privStr", false);
		PinnedFieldsManager.getDefault().setPinned(VIEW_ID, List.of(date, privStr), true);

		PinnedFieldsManager.getDefault().setPinned(VIEW_ID, List.of(date), false);

		assertFalse(PinnedFieldsManager.getDefault().isPinned(VIEW_ID, date));
		assertTrue(PinnedFieldsManager.getDefault().isPinned(VIEW_ID, privStr));
		assertEquals("privStr", names(JavaContentProviderFilter.filterVariables(fields, fContext)).get(0));
	}

	public void testUnpinAll() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		PinnedFieldsManager.getDefault().setPinned(VIEW_ID, List.of(object.getField("date", false)), true);

		PinnedFieldsManager.getDefault().unpinAll(VIEW_ID);

		assertFalse(PinnedFieldsManager.getDefault().hasPinnedFields(VIEW_ID));
		assertEquals("", JDIDebugUIPlugin.getDefault().getPreferenceStore().getString(PINNED_FIELDS_KEY));
		assertEquals(names(fields), names(JavaContentProviderFilter.filterVariables(fields, fContext)));
	}

	public void testPinsAreViewSpecific() throws Exception {
		IJavaObject object = getThis();
		IVariable[] fields = object.getVariables();
		IJavaFieldVariable date = object.getField("date", false);
		IJavaFieldVariable privStr = object.getField("privStr", false);
		PinnedFieldsManager manager = PinnedFieldsManager.getDefault();
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();

		manager.setPinned(VIEW_ID, List.of(date), true);

		assertTrue(manager.isPinned(VIEW_ID, date));
		assertFalse("a field pinned in a view should not be pinned in another view", manager.isPinned(OTHER_VIEW_ID, date));
		assertFalse(manager.hasPinnedFields(OTHER_VIEW_ID));
		assertEquals("date", names(JavaContentProviderFilter.filterVariables(fields, fContext)).get(0));
		assertEquals(names(fields), names(JavaContentProviderFilter.filterVariables(fields, fOtherContext)));
		assertEquals("", store.getString(PinnedFieldsManager.getPreferenceKey(OTHER_VIEW_ID)));

		manager.setPinned(OTHER_VIEW_ID, List.of(privStr), true);

		assertEquals("date", names(JavaContentProviderFilter.filterVariables(fields, fContext)).get(0));
		assertEquals("privStr", names(JavaContentProviderFilter.filterVariables(fields, fOtherContext)).get(0));
		assertFalse(manager.isPinned(VIEW_ID, privStr));
		assertEquals(TYPE_NAME + "#date", store.getString(PINNED_FIELDS_KEY));
		assertEquals(TYPE_NAME + "#privStr", store.getString(PinnedFieldsManager.getPreferenceKey(OTHER_VIEW_ID)));

		manager.setPinned(OTHER_VIEW_ID, List.of(privStr), false);
		assertTrue("unpinning a field in a view should not unpin the fields of another view", manager.isPinned(VIEW_ID, date));

		manager.setPinned(OTHER_VIEW_ID, List.of(privStr), true);
		manager.unpinAll(OTHER_VIEW_ID);
		assertFalse(manager.hasPinnedFields(OTHER_VIEW_ID));
		assertTrue("unpinning all the fields of a view should not unpin the fields of another view", manager.isPinned(VIEW_ID, date));
		assertEquals("date", names(JavaContentProviderFilter.filterVariables(fields, fContext)).get(0));
	}

	private IJavaObject getThis() throws Exception {
		IJavaObject object = topFrame().getThis();
		assertNotNull("'this' is null", object);
		return object;
	}

	private IJavaObject getSubclassInstance() throws Exception {
		IVariable ivt = findVariable(topFrame(), "ivt");
		assertNotNull("Could not find variable 'ivt'", ivt);
		return (IJavaObject) ivt.getValue();
	}

	private static List<String> names(Object[] variables) throws Exception {
		List<String> names = new ArrayList<>();
		for (Object variable : variables) {
			names.add(((IVariable) variable).getName());
		}
		return names;
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
