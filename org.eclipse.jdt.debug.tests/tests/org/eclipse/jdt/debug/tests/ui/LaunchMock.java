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

import org.eclipse.debug.core.ILaunch;

/**
 * Class to mock {@link ILaunch}.
 */
class LaunchMock implements InvocationHandler {
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return null;
	}

	public static ILaunch createLaunch() {
		return (ILaunch) Proxy.newProxyInstance(LaunchMock.class.getClassLoader(), new Class[] { ILaunch.class }, new LaunchMock());
	}
}