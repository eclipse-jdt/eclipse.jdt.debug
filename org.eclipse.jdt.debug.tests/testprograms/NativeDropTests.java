/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class NativeDropTests {
	public static void foo() {
		System.out.println("foo"); 
		System.out.println("breakpoint"); // breakpoint on this line
	}
	
	public static void bar() {
		foo();
	}

	public static void main(String[] args) throws Exception {
		Class clazz = NativeDropTests.class;
		java.lang.reflect.Method method = clazz.getMethod("bar", new Class[] {});
		method.invoke(null, new Object[] {});
	}
}
