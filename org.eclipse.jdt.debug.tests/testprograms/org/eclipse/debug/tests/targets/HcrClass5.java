/*******************************************************************************
 * Copyright (c) May 24, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.targets;

public class HcrClass5 {

	static class InnerClass {
		
		public InnerClass() {
			String s = new String("Constructor");
			new HcrClass5().run();
		}
		
		public void run() {
			String s = new String("InnerClass#run()");
			new HcrClass5().run();
		}
		public void run2() {
			String s = new String("InnerClass#run2()");
			new HcrClass5().outerrun();
		}
	}
	
	public void run() {
		String s = new String("LocalHCR#run()");
	}

	public void outerrun() {
		String s = new String("LocalHCR#outerrun()");
	}
	
	public static void main(String[] args) {
		InnerClass clazz = new InnerClass();
		clazz.run();
		clazz.run2();
	}
}
