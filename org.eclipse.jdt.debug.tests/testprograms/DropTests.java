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

public class DropTests {

 	public static void main(String[] args) {
 		DropTests dt = new DropTests();
 		dt.method1();
 	}

 	public void method1() {
 		method2();
 	}

 	public void method2() {
 		method3();
 	}

 	public void method3() {
 		method4();
 	}

 	public synchronized void method4() {
 		System.out.println("Finally, I got to method 4");
 	}

}
