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
import java.util.concurrent.CopyOnWriteArraySet;
public class CompareSetObjects {
	public static void main(String[] ecs) {
		TreeSet<Integer> xx11 = new TreeSet<>();
		xx11.add(1);
		xx11.add(2);
		TreeSet<Integer> xx22 = new TreeSet<>();
		xx22.add(1);
		xx22.add(21);
		Set<Integer> hashS1 = new HashSet<>();
		Set<Integer> hashS2 = new HashSet<>();
		hashS1.add(1);
		hashS1.add(21);
		hashS2.add(1);
		hashS2.add(21);
		
		Set<Integer> linkedHashS1 = new LinkedHashSet<>();
		Set<Integer> linkedHashS2 = new LinkedHashSet<>();
		linkedHashS1.add(1);
		linkedHashS1.add(21);
		linkedHashS2.add(1);
		linkedHashS2.add(2);
		
		Set<String> xx1 = new CopyOnWriteArraySet<>();
		xx1.add("one");
		xx1.add("two");
		Set<String> xx2 = new CopyOnWriteArraySet<>();
		xx2.add("two");
		xx2.add("one");
		int p = 100;
	}
}