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
package a;
import java.net.URL;
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
	public static void tes3(int x) {
		System.out.println("Expected_Single_Parameter");
	}
	public void tes2() {
		
	}
	public static void tesComplex(String[] x, java.net.URL[] sx) {
		System.out.println("Expected_One_normal_&_One_fully_qualified");
	}
	public void testBlank() {
		System.out.println("Expected_No_Signature");
	}
	public void tes2(java.lang.Object... objects ) {
		
	}
}