/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

import java.util.Vector;

/**
 * Class for testing 'step into selection'
 */
public class StepIntoSelectionClass {

	public static void main(String[] args) {
		StepIntoSelectionClass stepper = new StepIntoSelectionClass(); 
		stepper.step(); // enforce all used classes to be loaded
		stepper.step();
	}
	
	public StepIntoSelectionClass() {
		super();
	}
	
	public void step() {
		Vector vector = new Vector(); 
		int size= vector.size(); 
		for (int i= 0; i < 100; i++) {
			vector.addElement(new Integer(i));
		}		
		method1(method2(), new String[0]);
	}
	
	public void method1(int[] i, String[] s) {
		new String("test");
	}
	
	public int[] method2() {
		return new int[0];
	}
}
