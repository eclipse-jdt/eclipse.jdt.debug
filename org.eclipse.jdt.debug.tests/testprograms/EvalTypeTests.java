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

public class EvalTypeTests {
	
	byte xFieldByte = -2;
	char xFieldChar = (char)-2;
	short xFieldShort = -2;
	int xFieldInt = -2;
	long xFieldLong = -2;
	float xFieldFloat = (float)-2.1;
	double xFieldDouble = -2.1;
	String xFieldString = "minus two";
	boolean xFieldBoolean = true;

	byte yFieldByte = 9;
	char yFieldChar = 9;
	short yFieldShort = 9;
	int yFieldInt = 9;
	long yFieldLong = 9;
	float yFieldFloat = (float)8.6;
	double yFieldDouble = 8.6;
	String yFieldString = "nine";
	boolean yFieldBoolean = false;

	static byte xStaticFieldByte = -1;
	static char xStaticFieldChar = (char)-1;
	static short xStaticFieldShort = -1;
	static int xStaticFieldInt = -1;
	static long xStaticFieldLong = -1;
	static float xStaticFieldFloat = (float)-1.5;
	static double xStaticFieldDouble = -1.5;
	static String xStaticFieldString = "minus one";
	static boolean xStaticFieldBoolean = true;

	static byte yStaticFieldByte = 6;
	static char yStaticFieldChar = 6;
	static short yStaticFieldShort = 6;
	static int yStaticFieldInt = 6;
	static long yStaticFieldLong = 6;
	static float yStaticFieldFloat = (float)6.5;
	static double yStaticFieldDouble = 6.5;
	static String yStaticFieldString = "six";
	static boolean yStaticFieldBoolean = false;

	void test1() {
		System.out.println("Test1 ...");
	}

	static void test2() {
		System.out.println("Test2 ...");
	}
	
	void bar() {
		System.out.println("Tests ...");
	}

	static void foo() {
		System.out.println("Tests ...");
	}
	
	public static void main(String[] args) {
		EvalTypeTests foo = new EvalTypeTests();
		
		System.out.println("Tests ...");
		
		foo.bar();
		
		foo();
		
	}
}
