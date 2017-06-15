/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.targets;

/**
 * Class used to test hot code replace
 */
public class HcrClass {
	
	protected String instVar = null;
	
	public static void main(String[] args) {
		new HcrClass().one();
	}
	
	public void one() {
		instVar = "One";
		two();
	}
	
	public void two() {
		three();
	}
	
	public void three() {
		four();
	}
	
	public void four() {
		String x = instVar;
		System.out.println(x);
	}

}
