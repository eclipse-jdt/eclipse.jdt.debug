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
