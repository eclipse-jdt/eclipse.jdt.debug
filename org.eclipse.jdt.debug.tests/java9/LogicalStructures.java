/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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

import java.util.*;
import java.util.Map.Entry;

/**
 * Tests built in logical structures.
 */
public class LogicalStructures {

	public static void main(String[] args) {
		Map<String, Integer> map = new HashMap<>();
		map.put("one", 1);
		map.put("two", 2);
		List<String> list = new ArrayList<>();
		list.add("three");
		list.add("four");
		Set<Entry<String, Integer>> set = map.entrySet();
		Map.Entry<String, Integer> entry = set.iterator().next();
		entry.getKey();
	}

	// called from test to verify that logical values don't get GCed
	private static void generateGarbageAndGC() throws InterruptedException {
		List<Object> garbage;
		for (int i = 0; i < 10; ++i) {
			System.gc();
			garbage = Arrays.asList(new Object(), new Object(), new Object());
			Thread.sleep(20);
		}
	}

}
