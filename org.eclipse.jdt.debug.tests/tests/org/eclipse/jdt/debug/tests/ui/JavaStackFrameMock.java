/*******************************************************************************
 * Copyright (c) 2025 Zsombor Gegesy and others.
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

import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

class JavaStackFrameMock implements InvocationHandler {

	final IJavaReferenceType referenceType;
	final boolean synthetic;

	JavaStackFrameMock(IJavaReferenceType referenceType, boolean synthetic) {
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
			return LaunchMock.createLaunch();
		}
		if ("getAdapter".equals(methodName)) {
			return "getAdapter called with " + args[0];
		}
		return null;
	}

	/**
	 * Create a mocked {@link IJavaStackFrame}
	 *
	 * @param refType
	 *            the type in which this stack frame's method is declared
	 * @param syntetic
	 *            if the frame is synthetic or not.
	 * @return a mocked stack frame.
	 */
	public static IJavaStackFrame createFrame(IJavaReferenceType refType, boolean syntetic) {
		return (IJavaStackFrame) Proxy.newProxyInstance(JavaStackFrameMock.class.getClassLoader(), new Class[] {
				IJavaStackFrame.class }, new JavaStackFrameMock(refType, syntetic));
	}

}