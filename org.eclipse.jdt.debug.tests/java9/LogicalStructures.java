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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Tests built in logical structures.
 */
public class LogicalStructures {
	
	public static void main(String[] args) {
		Map map = new HashMap();
		map.put("one", new Integer(1));
		map.put("two", new Integer(2));
		List list = new ArrayList();
		list.add("three");
		list.add("four");
		Set set = map.entrySet();
		Entry entry = (Entry) set.iterator().next();
		entry.getKey();
	}

}
