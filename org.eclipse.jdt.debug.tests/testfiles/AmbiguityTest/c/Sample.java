/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package c;

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
	public static void tes3(int x, String v) {
		System.out.println("Expected_Multiple_Parameter");
	}
	public static void tes3(int x, String v, Sample s) {
		System.out.println("Expected_Multiple_Parameter_Three");
	}
	public void tes2() {
		
	}
	public void testMethod(Object s,Object... sd) {
		System.out.println("Expected_oneNormal&oneVarArgs");
	}
	public void testMethod(Object... sd) {
		System.out.println("Expected_oneVarArgs");
	}
	
}