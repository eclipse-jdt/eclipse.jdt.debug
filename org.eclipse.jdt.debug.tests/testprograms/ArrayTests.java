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

public class ArrayTests {
	
	public static void main(String[] args) {
		int[] array = new int[100];
		for (int i = 0; i < array.length; i++) {
			array[i] = i;
		}
		System.out.println("Done");
	}	
}
