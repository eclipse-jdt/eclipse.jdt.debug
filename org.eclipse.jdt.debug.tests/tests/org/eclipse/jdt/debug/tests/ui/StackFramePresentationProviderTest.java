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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.StackFramePresentationProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

public class StackFramePresentationProviderTest extends AbstractDebugTest {

	public StackFramePresentationProviderTest(String name) {
		super(name);
	}

	private StackFramePresentationProvider provider;
	private IPreferenceStore preferenceStore;

	private static class LaunchMock implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return null;
		}
	}
	private static class JavaStackFrameMock implements InvocationHandler {

		final IJavaReferenceType referenceType;
		final boolean synthetic;

		public JavaStackFrameMock(IJavaReferenceType referenceType, boolean synthetic) {
			this.referenceType = referenceType;
			this.synthetic = synthetic;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			var methodName = method.getName();
			if ("getReferenceType".equals(methodName)) {
				return referenceType;
			}
			if ("isSynthetic".equals(methodName)) {
				return synthetic;
			}
			if ("getLaunch".equals(methodName)) {
				return Proxy.newProxyInstance(StackFramePresentationProviderTest.class.getClassLoader(), new Class[] {
						ILaunch.class }, new LaunchMock());

			}
			return null;
		}
	}

	private static class JavaReferenceTypeMock implements InvocationHandler {

		final String name;

		public JavaReferenceTypeMock(String name) {
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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		preferenceStore = new PreferenceStore();
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_ACTIVE_PLATFORM_FRAME_FILTER_LIST, "java.*,javax.*");
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_COLORIZE_STACK_FRAMES, true);
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_COLORIZE_PLATFORM_METHODS, true);
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_COLORIZE_CUSTOM_METHODS, true);
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_COLORIZE_SYNTHETIC_METHODS, true);
		provider = new StackFramePresentationProvider(preferenceStore);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		provider.close();
	}

	private IJavaReferenceType createReference(String name) {
		return (IJavaReferenceType) Proxy.newProxyInstance(StackFramePresentationProviderTest.class.getClassLoader(), new Class[] {
				IJavaReferenceType.class }, new JavaReferenceTypeMock(name));
	}

	private IJavaStackFrame createFrame(IJavaReferenceType refType, boolean syntetic) {
		return (IJavaStackFrame) Proxy.newProxyInstance(StackFramePresentationProviderTest.class.getClassLoader(), new Class[] {
				IJavaStackFrame.class }, new JavaStackFrameMock(refType, syntetic));
	}

	private IJavaStackFrame.Category categorize(String refTypeName, boolean syntetic) throws DebugException {
		return categorize(createReference(refTypeName), syntetic);
	}

	private IJavaStackFrame.Category categorize(IJavaReferenceType refType, boolean syntetic) throws DebugException {
		return provider.categorize(createFrame(refType, syntetic));
	}

	public void testFiltering() throws DebugException {
		assertEquals(IJavaStackFrame.Category.SYNTHETIC, categorize("org.eclipse.Something", true));
		assertEquals(IJavaStackFrame.Category.PLATFORM, categorize("java.lang.String", false));
		assertEquals(IJavaStackFrame.Category.UNKNOWN, categorize("org.eclipse.Other", false));
	}

	public void testUpdateWorks() throws DebugException {
		var something = createReference("org.eclipse.Something");
		var other = createReference("org.eclipse.Other");
		assertEquals(IJavaStackFrame.Category.UNKNOWN, categorize(something, false));
		assertEquals(IJavaStackFrame.Category.UNKNOWN, categorize(other, false));
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_ACTIVE_CUSTOM_FRAME_FILTER_LIST, "org.eclipse.Something");

		assertEquals(IJavaStackFrame.Category.CUSTOM_FILTERED, categorize(something, false));
		assertEquals(IJavaStackFrame.Category.UNKNOWN, categorize(other, false));
	}

	public void testSwitchOffPlatform() throws DebugException {
		assertEquals(IJavaStackFrame.Category.PLATFORM, categorize("java.lang.String", false));
		preferenceStore.setValue(IJDIPreferencesConstants.PREF_COLORIZE_PLATFORM_METHODS, false);
		assertEquals(IJavaStackFrame.Category.UNKNOWN, categorize("java.lang.String", false));
	}

}
