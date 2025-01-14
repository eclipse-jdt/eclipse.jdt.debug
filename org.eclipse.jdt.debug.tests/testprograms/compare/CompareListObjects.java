/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
package compare;

import java.util.*;

public class CompareListObjects {
	public static void main(String[] ecs) {
		List<String> s1 = new ArrayList<>(Arrays.asList("apple", "banana"));
		List<String> s2 = new ArrayList<>(Arrays.asList("banana", "apple"));
		List<String> s3 = new ArrayList<>(Arrays.asList("apple", "banana"));
		List<String> s4 = new ArrayList<>(Arrays.asList("apple1", "banana"));

		List<Integer> linkedList1 = new LinkedList<>();
		linkedList1.add(22);
		linkedList1.add(12);
		List<Integer> linkedList2 = new LinkedList<>();
		linkedList2.add(12);
		linkedList2.add(221);

		List<String> Stack = new Stack<>();
		Stack.add("Cherry");
		Stack.add("Banana");
		Stack.add("Apple");
		List<String> ArrayList = new ArrayList<>();
		ArrayList.add("Apple");
		ArrayList.add("Banana");
		ArrayList.add("Cherry");
		List<String> Vector = new Vector<>();
		Vector.add("Banana");
		Vector.add("Cherry");
		Vector.add("Apple");
		List<String> LinkedList = new LinkedList<>();
		LinkedList.add("Apple");
		LinkedList.add("Cherry");

		int p = 100;
	}
}