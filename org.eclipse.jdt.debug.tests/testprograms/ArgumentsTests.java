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

public class ArgumentsTests {
	public static void nop() {
		// used to allow breakpoint on otherwise empty lines
	}
	
	public static void simpleTest(Object obj) {
		nop();	// should see obj
	}
	
	// Tests recursion (multiple stack frames for the same method with different variable values)
	public static int fact(int n) {
		if (n == 0 || n == 1)
			return 1;
		else
			return n*fact(n-1);
	}
	
	public static void main(String[] args) {
		simpleTest(null);
		fact(2);
	}	
}
