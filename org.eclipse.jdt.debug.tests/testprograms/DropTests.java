/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

public class DropTests {
 
 	public static void main(String[] args) {
 		DropTests dt = new DropTests();
 		dt.method1();
 	}
 
 	public void method1() {
 		method2();
 	}
 	
 	public void method2() {
 		method3();
 	}
 	
 	public void method3() {
 		method4();
 	}
 	
 	public void method4() {
 		System.out.println("Finally, I got to method 4");
 	}
 
 }