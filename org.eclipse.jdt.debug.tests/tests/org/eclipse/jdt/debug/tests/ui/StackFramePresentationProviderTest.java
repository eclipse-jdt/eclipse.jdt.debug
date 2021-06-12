/*******************************************************************************
 * Copyright (c) 2021 Zsombor Gegesy.
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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.StackFramePresentationProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StackFramePresentationProviderTest {

	private StackFramePresentationProvider provider;
	private IPreferenceStore preferenceStore;

	private static class JavaStackFrameMock implements InvocationHandler {

		final String name;

		public JavaStackFrameMock(String name) {
			this.name = name;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getName".equals(method.getName())) {
				return name;
			}
			return null;
		}
	}

	@Before
	public void setup() {
		preferenceStore = new PreferenceStore();
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST, "java.*,javax.*");
		provider = new StackFramePresentationProvider(preferenceStore);
	}

	@After
	public void cleanup() {
		provider.close();
	}

	private IJavaReferenceType createReference(String name) {
		return (IJavaReferenceType) Proxy.newProxyInstance(StackFramePresentationProviderTest.class.getClassLoader(), new Class[] {
				IJavaReferenceType.class }, new JavaStackFrameMock(name));
	}

	@Test
	public void testFiltering() throws DebugException {
		assertEquals(IJavaStackFrame.Category.SYNTHETIC, provider.categorize(createReference("org.eclipse.Something"), true));
		assertEquals(IJavaStackFrame.Category.PLATFORM, provider.categorize(createReference("java.lang.String"), false));
		assertEquals(IJavaStackFrame.Category.UNKNOWN, provider.categorize(createReference("org.eclipse.Other"), false));
	}

	@Test
	public void testUpdateWorks() throws DebugException {
		var something = createReference("org.eclipse.Something");
		var other = createReference("org.eclipse.Other");
		assertEquals(IJavaStackFrame.Category.UNKNOWN, provider.categorize(something, false));
		assertEquals(IJavaStackFrame.Category.UNKNOWN, provider.categorize(other, false));
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST, "org.eclipse.Something");

		assertEquals(IJavaStackFrame.Category.CUSTOM_FILTERED, provider.categorize(something, false));
		assertEquals(IJavaStackFrame.Category.UNKNOWN, provider.categorize(other, false));
	}

}
