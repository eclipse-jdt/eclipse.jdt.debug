/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
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
package b;

/**
 * Test class
 */
public class Sample {
	public static void main(String[] args) {
		System.out.println("Main Method");
		test();
	}
	public static void test()  {
		System.out.println("Testing..");
	}
	public void testMethod() {
		System.out.println("Random");
	}
	public static void tes3() {
		System.out.println("Expected_Zero_Parameter");
	}
	public void tes2() {
		
	}
	public static void tesComplex(java.lang.Object x, java.net.URL[] sx) {
		System.out.println("Expected_both_fully_qualified");
	}
	public void tes2(java.lang.Object x,java.lang.Object... args ) {
		System.out.println("Expected_VarArgs");
	}
}