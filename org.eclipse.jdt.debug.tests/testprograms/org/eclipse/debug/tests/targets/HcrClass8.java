/*******************************************************************************
 * Copyright (c) May 28, 2013 IBM Corporation and others.
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

public class HcrClass8 {

	class Inner {
		public void run() {}
	}
	
	public void run() {
		Inner inner = new Inner() {
			public void run() {
				class Local {
					public void run() {
						String s = new String("Inner$Local#run()");
					}
				}
				new Local().run();
			};
		};
		inner.run();
	}
	
	public static void main(String[] args) {
		HcrClass8 clazz = new HcrClass8();
		clazz.run();
	}
}
