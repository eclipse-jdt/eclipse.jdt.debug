/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
public class EvalTestIntf18 {
		public static void main(String[] args) {
			IntfImpl intf18 = new IntfImpl();
			 intf18.test2(); 
			 System.out.println("main");
		}
	}

	interface Intf18 {
		public void test1();
		default int test2() {
			int a = 1;
			System.out.println("a+2 = " +  a + 2);
			return a + 2;
		}
		static void test3() {
			System.out.println("test3");
		}
	}
	
	class IntfImpl implements Intf18 {
		public void test1(){
			System.out.println("test1");
		}
	}

