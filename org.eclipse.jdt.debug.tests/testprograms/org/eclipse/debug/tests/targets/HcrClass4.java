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

public class HcrClass4 {

	public HcrClass4() {
		String s = new String("Constructor");
		run();
	}
	
	public void run() {
		String s = new String("HcrClass4#run()");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HcrClass4 clazz = new HcrClass4();
	}
}
