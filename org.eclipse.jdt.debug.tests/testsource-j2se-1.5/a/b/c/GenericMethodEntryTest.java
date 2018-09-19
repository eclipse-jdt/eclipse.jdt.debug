/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package a.b.c;
public class GenericMethodEntryTest {
	
	public static <T extends Comparable<T>> int func(T[] arr, int m, int n) { // method breakpoint on func(T[],int,int)
		int i = 0;
		++i;
		return i;
	}
	
	public static <T extends Comparable<T>> int func(int m, int n) { // method breakpoint on func(int,int)
		int i = 0;
		++i;
		return i;
	}
	
	public static <T extends Comparable<T>> int func(T t, int m, int n) { // method breakpoint on func(int,int)
		int i = 0;
		++i;
		return i;
	}

	public static void main(String[] args) {
		String[] ss = new String[]{"a","b"};
		func(ss, 1, 2); // should hit in func(T[],int,int)
		func(1, 2); // hits in func(int,int)
		func("s", 1, 2); // hits in func(T,int,int)
	}
	
}