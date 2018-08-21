/*******************************************************************************
 * Copyright (c) 2016 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class StepResult3 {
	interface StringSupplier {
		String get();
	}
	static String f() throws Exception {
		System.class.hashCode(); // bp7
		StringSupplier p = (StringSupplier) Proxy.newProxyInstance(StepResult3.class.getClassLoader(),
				new Class[] { StringSupplier.class }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return "hello from proxy";
					}
				});
		p.get();
		h();
		g(0); // bp8
		String x = g(1);
		return x;
	}

	private static Object h() {
		return null;
	}

	private static String g(int a) throws Exception {
		if (a == 1)
			throw new Exception("YYY");
		else {
			return "XXX";
		}
	}

	public static void main(String[] args) {
		try {
			f();
		} catch (Exception e) {
			System.out.println("e:" + e.getMessage());
			System.currentTimeMillis();
		}
	}
}
