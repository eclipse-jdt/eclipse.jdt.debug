/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * WatchItemTests
 */
public class WatchItemTests {
	
	public Vector fVector =  null;
	public Map fMap = null;

	public static void main(String[] args) {
		WatchItemTests test = new WatchItemTests();
		test.fillVector();
		test.fillMap(); 
	}
	
	public WatchItemTests() {
	}	
	
	public void fillVector() {
		fVector = new Vector();
		for (int i = 0; i < 100; i++) {
			fVector.add(new Integer(i));
		}
	}
	
	public void fillMap() {
		fMap = new HashMap();
		Iterator iterator = fVector.iterator();
		while (iterator.hasNext()) {
			Integer i = (Integer)iterator.next();
			fMap.put(i, i.toString());
		}
	}
}
