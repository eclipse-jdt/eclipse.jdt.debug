/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
