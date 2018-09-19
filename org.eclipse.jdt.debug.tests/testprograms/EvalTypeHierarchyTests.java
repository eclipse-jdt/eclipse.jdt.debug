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

public class EvalTypeHierarchyTests {
	
	interface I_A {
		int m1();
	}
	
	static class A implements I_A {
		public int m1() {
			return 1;
		}
		
		public int m2() {
			return 2;
		}
		
		public static int s2() {
			return 9;
		}
		
		public void testA() {
			System.out.println("test");
		}
	}
	
	interface I_B extends I_A {
		int m1();
		
		int m3();
	}
	
	static class B extends A implements I_B {
		public int m1() {
			return 11;
		}
		
		public int m2() {
			return 22;
		}

		public static int s2() {
			return 99;
		}
		
		public int m3() {
			return 33;
		}
		
		public int m4() {
			return 44;
		}

		public static int s4() {
			return 88;
		}
		
		public void testB() {
			System.out.println("test");
		}
	}
	
	interface I_C extends I_B {
		int m1();
		
		int m3();
		
		int m5();
	}
	
	static class C extends B implements I_C {
		
		public int m1() {
			return 111;
		}
		
		public int m2() {
			return 222;
		}

		public static int s2() {
			return 999;
		}
		
		public int m3() {
			return 333;
		}
		
		public int m4() {
			return 444;
		}

		public static int s4() {
			return 888;
		}
		
		public int m5() {
			return 555;
		}
		
		public int m6() {
			return 666;
		}
		
		public static int s6() {
			return 777;
		}
		
		public void testC() {
			System.out.println("test");
		}
	}
	
	public static void main(String[] args) {
		I_A iaa= new A();
		I_A iab= new B();
		I_A iac= new C();
		A aa= new A();
		A ab= new B();
		A ac= new C();
		I_B ibb= new B();
		I_B ibc= new C();
		B bb= new B();
		B bc= new C();
		I_C icc= new C();
		C cc= new C();
		aa.testA();
		ab.testA();
		ac.testA();
		bb.testA();
		bb.testB();
		bc.testA();
		bc.testB();
		cc.testA();
		cc.testB();
		cc.testC();
		System.out.println("test");
	}

}
