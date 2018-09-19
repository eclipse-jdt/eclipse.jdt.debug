/*******************************************************************************
 * Copyright (c) May 24, 2013, 2014 IBM Corporation and others.
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
