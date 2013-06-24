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

public class HcrClass2 {
	
	public HcrClass2() {
		String s = new String("Constructor");
		class Local {
			public void run() {
				String s = new String("CLocal#run()");
			}
			
			public void run2() {
				String s = new String("CLocal#run2()");
			}
		}
		new Local().run();
	}
	
	public void run() {
		String s = new String("HcrClass#run()");
		class Local {
			public void run() {
				String s = new String("Local#run()");
			}
			
			public void run2() {
				String s = new String("Local#run2()");
			}
		}
		new Local().run();
		new Local().run2();
	}
	
	public static void main(String[] args) {
		new HcrClass2().run();
	}
}
