/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

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