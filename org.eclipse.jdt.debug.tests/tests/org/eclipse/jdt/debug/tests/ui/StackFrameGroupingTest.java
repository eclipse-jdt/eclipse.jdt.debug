/*******************************************************************************
 * Copyright (c) 2021, 2025 Zsombor Gegesy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.StackFrameCategorizer;

public class StackFrameGroupingTest extends AbstractDebugTest {

	public StackFrameGroupingTest(String name) {
		super(name);
	}

	private StackFrameCategorizer stackFrameCategorizer;
	private IEclipsePreferences preferences;
	private Map<String, Object> values;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		values = new HashMap<>();
		values.put(IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST, "java.*,javax.*");
		preferences = PreferenceServiceMock.createEclipsePreferences(values);
		stackFrameCategorizer = new StackFrameCategorizer(PreferenceServiceMock.createPreferencesService(values), preferences);
	}

	private IJavaStackFrame.Category categorize(String refTypeName, boolean syntetic) {
		return categorize(JavaReferenceTypeMock.createReference(refTypeName), syntetic);
	}

	private IJavaStackFrame.Category categorize(IJavaReferenceType refType, boolean syntetic) {
		return stackFrameCategorizer.categorize(JavaStackFrameMock.createFrame(refType, syntetic));
	}

	public void testFiltering() {
		assertEquals(StackFrameCategorizer.CATEGORY_SYNTHETIC, categorize("org.eclipse.Something", true));
		assertEquals(StackFrameCategorizer.CATEGORY_PLATFORM, categorize("java.lang.String", false));
		assertEquals(StackFrameCategorizer.CATEGORY_UNKNOWN, categorize("org.eclipse.Other", false));
	}

	public void testUpdateWorks() {
		var something = JavaReferenceTypeMock.createReference("org.eclipse.Something");
		var other = JavaReferenceTypeMock.createReference("org.eclipse.Other");
		assertEquals(StackFrameCategorizer.CATEGORY_UNKNOWN, categorize(something, false));
		assertEquals(StackFrameCategorizer.CATEGORY_UNKNOWN, categorize(other, false));
		assertEquals(StackFrameCategorizer.CATEGORY_PLATFORM, categorize("java.lang.String", false));
		stackFrameCategorizer.addTypesToActiveCustomFilters(Set.of("org.eclipse.Something"));
		stackFrameCategorizer.preferenceChange(new PreferenceChangeEvent(preferences, IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST, null, null));

		assertEquals(StackFrameCategorizer.CATEGORY_CUSTOM_FILTERED, categorize(something, false));
		assertEquals(StackFrameCategorizer.CATEGORY_UNKNOWN, categorize(other, false));
		assertEquals(StackFrameCategorizer.CATEGORY_PLATFORM, categorize("java.lang.String", false));
	}

	public void testSwitchOffPlatform() {
		assertEquals(StackFrameCategorizer.CATEGORY_PLATFORM, categorize("java.lang.String", false));
		stackFrameCategorizer.setEnabled(StackFrameCategorizer.CATEGORY_PLATFORM, false);
		assertEquals(StackFrameCategorizer.CATEGORY_UNKNOWN, categorize("java.lang.String", false));
	}


}
