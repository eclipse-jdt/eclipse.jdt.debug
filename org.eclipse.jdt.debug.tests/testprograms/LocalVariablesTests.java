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

public class LocalVariablesTests {
	public static void nop() {
		// used to allow breakpoint on otherwise empty lines
	}
	
	public static void simpleTest() {
		nop(); // should see no local variables here
		int i1 = 0;
		nop(); // should see 1 local variable: i1
		int i2 = 1;
		nop(); // should see 2 local variables: i1 && i2
	}

	public static void outerMethod() {
		int i1 = 0;
		innerMethod();
		// i1 visible and i1==0, i2 not visible
		int i2 = 1;
		nop();
	}

	public static void innerMethod() {
		int i2 = 7;
		nop(); // i1 not visible in the top stack frame, i2 visible
	}
	
	public static void testFor() {
		nop(); // should see no variable
		for (int i = 0; i < 1; i++) {
			nop(); // should see i
			for (int j = 0; j < 1; j++) {
				nop(); // 	should see i, j
				Object obj = null;
				nop(); // should see i, j, obj
				obj = "foo";
			}
		}
		nop(); // should not see i and j
	}
	
	public static void testIf(boolean cond) {
		if (cond) {
			Object ifObj = new String("true");
			nop();
		} else {
			Object elseObj = new String("false");
			nop();
		}
		nop();
	}
	
	public static void testWhile() {
		int i = 0;
		while (i < 1) {
			int j = i/2;
			nop(); // should see i & j
			i++;
		}
	}
	
	public static void testTryCatch() {
		try {
			String str = null;
			nop(); // should see str
			str.length();
		} catch (NullPointerException ex) {
			nop(); // should see ex
 		} finally {
 			nop(); // should see str
 		}
	}
	
	public static void testAliasing() {
		String str1 = new String("value");
		String str2 = str1;
		nop();
	}

	public static void main(String[] args) {
		simpleTest();  // @see LocalVariablesTests.testSimple()
		outerMethod(); // @see LocalVariablesTests.testMethodCall()
		testFor();    // @see LocalVariablesTests.testFor()
		testIf(true);  // @see LocalVariablesTests.testIf()
		testIf(false); // @see LocalVariablesTests.testIf()
		testWhile();   // @see LocalVariablesTests.testWhile()
		testTryCatch();// @see LocalVariablesTests.testTryCatch()
		testAliasing(); // @see LocalVariablesTests.testAliasing()

	}
}
