/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
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
public class StatementStepNested {

	public static void main(String[] args) {
		String s1 = "A";
		String s2 = "B";
		String s3 = "C";
		String s4 = "D";

		test(
	            s1,
	            helper1(
	                s2,
	                helper1(s3,
	                		helper1(s2,
	                				s3)
	                		)
	            ),
	            s4
	        );

		System.out.println("Sou");
	}

	static String helper1(String a, String b) {
		return a + b;
	}

	static String helper2(String a) {
		return a;
	}

	static void test(String a, String b, String c) {
	}
}