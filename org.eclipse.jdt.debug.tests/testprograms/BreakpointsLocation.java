/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.util.List;
import java.util.ArrayList;

public class BreakpointsLocation {
	
	public void test1() {
		System.out.println("test");
		System.out.println("test");
	}
	
	public class InnerClass {
		
		public int foo() {
			return 1;
		}
		
	}

	private List fList= new ArrayList();
	
	public void test2(List list) {
		System.out.println(list);
	}
	
	public void randomCode() {
		new Runnable() {
			public void run() {
				System.out.println("test");
			}
		};
		
		int
			s
			=
			3
			;
		
	}
	
	private
		int
		i
		=
		3
		;

}