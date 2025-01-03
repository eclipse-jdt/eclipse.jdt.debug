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
public class CompareMapObjects {
	public static void main(String[] ecs) {
		Map<Integer, Integer> map8 = new TreeMap<>();
		map8.put(1, 4);
		Map<Integer, Integer> map9 = new TreeMap<>();
		map9.put(12, 4);
		Map<Integer, Integer> map2 = new HashMap<>();
		map2.put(1, 7);
		Map<Integer, Integer> map1 = new HashMap<>();
		map1.put(1, 7);
		
		Map<String, Double> map4 = new WeakHashMap<>();
		map4.put("key1", 17d);
		Map<String, Double> map5 = new WeakHashMap<>();
		map5.put("key1", 7d);
		
		Map<String, Double> map6 = new IdentityHashMap<>();
		map6.put("key1", 7d);
		Map<String, Double> map7 = new IdentityHashMap<>();
		map7.put("key2", 8d);
		int p = 100;
	}
}