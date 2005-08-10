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

public class ArrayDetailTestsDef {

	public static void main(String[] args) {
		new ArrayDetailTestsDef().test();
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
		Runnable[] runs = new Runnable[5];
		String[] strings = new String[5];
		int[] primitives = new int[5];
		ArrayDetailTestsDef[] outers = new ArrayDetailTestsDef[5];
		InnerType[] middle = new InnerType[5];
		InnerType.SecondInnerType[] inners = new InnerType.SecondInnerType[5];
		for (int i = 0; i < outers.length; i++) {
			runs[i] = new Runnable() {
				public void run() {
				}
				public String toString() {
					return "Runnable";
				}
			};
			strings[i] = Integer.toBinaryString(i);
			primitives[i] = i;
			outers[i] = new ArrayDetailTestsDef();
			middle[i] = new InnerType();
			inners[i] = middle[i].newObject();
		}
		System.out.println(outers);
	}

}
