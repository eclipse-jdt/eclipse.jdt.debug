/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 * 
 *  Contributors:
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
	
	public void code() {
		boolean
			i
			=
			1
			>
			2
			;
		int j = 22;
		int 
			s
			=
			j
			-
			12
			;
	}
	
	public static void testMethodWithInnerClass(Object type){
		class StaticInnerClass{
			protected StaticInnerClass(Object t){
				System.out.println("test");
			}
		}
	}

}
