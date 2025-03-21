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

/**
 * Class to mock {@link IJavaReferenceType}.
 */
class JavaReferenceTypeMock implements InvocationHandler {

	final String name;

	JavaReferenceTypeMock(String name) {
		this.name = name;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("getName".equals(method.getName())) {
			return name;
		}
		return null;
	}

	/**
	 * Create a new mocked {@link IJavaReferenceType}.
	 *
	 * @param name
	 * @return
	 */
	public static IJavaReferenceType createReference(String name) {
		return (IJavaReferenceType) Proxy.newProxyInstance(JavaReferenceTypeMock.class.getClassLoader(), new Class[] {
				IJavaReferenceType.class }, new JavaReferenceTypeMock(name));
	}

}