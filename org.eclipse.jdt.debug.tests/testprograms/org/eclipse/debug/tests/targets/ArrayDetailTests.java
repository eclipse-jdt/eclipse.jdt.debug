/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.targets;

public class ArrayDetailTests {

	public static void main(String[] args) {
		new ArrayDetailTests().test();
	}
	
	public class InnerType {
		
		public class SecondInnerType {

			public String toString() {
				return "aSecondInnerObject";
			}
			
		}

		public String toString() {
			return "anInnerObject";
		}
		
		public SecondInnerType newObject() {
			return new SecondInnerType();
		}
		
	}
	
	public String toString() {
		return "OutermostObject";
	}
	
	public void test() {
		String[] strings = new String[5];
		int[] primitives = new int[5];
		ArrayDetailTests[] outers = new ArrayDetailTests[5];
		InnerType[] middle = new InnerType[5];
		InnerType.SecondInnerType[] inners = new InnerType.SecondInnerType[5];
		for (int i = 0; i < outers.length; i++) {
			strings[i] = Integer.toBinaryString(i);
			primitives[i] = i;
			outers[i] = new ArrayDetailTests();
			middle[i] = new InnerType();
			inners[i] = middle[i].newObject();
		}
		System.out.println(outers);
	}

}
