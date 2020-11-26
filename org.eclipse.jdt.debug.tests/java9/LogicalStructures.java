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
		generateGarbage();
		
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
	
	private static void generateGarbage() {
		// generate garbage repeatedly to verify that logical values don't get GCed
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			private List<String> garbage;
			@Override
			public void run() {
				System.gc();
				garbage = Arrays.asList(new String("a"), new String("b"), new String("c"));
			}
		}, 200, 20);
	}

}
