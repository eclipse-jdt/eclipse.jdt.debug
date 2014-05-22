/*******************************************************************************
 * Copyright (c) May 24, 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.targets;

public class HcrClass3 {

	class Anon {
		public void run() {
		}
	}
	
	public HcrClass3() {
		String s = new String("Constructor");
		Anon aclass = new Anon() {
			public void run() {
				String s = new String("TEST_RUN3");
			}
		};
		aclass.run();
	}
	
	public void run() {
		String s = new String("HcrClass3#run()");
		Anon aclass = new Anon() {
			public void run() {
				String s = new String("TEST_RUN1");
			}
		};
		aclass.run();
	}
	
	public void run2() {
		String s = new String("HcrClass3#run2()");
		Anon aclass = new Anon() {
			public void run() {
				String s = new String("TEST_RUN2");
			}
		};
		aclass.run();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HcrClass3 clazz = new HcrClass3();
		clazz.run();
		clazz.run2();
	}
}
