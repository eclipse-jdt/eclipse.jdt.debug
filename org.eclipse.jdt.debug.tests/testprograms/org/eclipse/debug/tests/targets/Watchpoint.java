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
package org.eclipse.debug.tests.targets;


import java.util.ArrayList;
import java.util.List;

public class Watchpoint {
	
	public List list;
	
	public static void main(String[] args) {
		Watchpoint wp = new Watchpoint();
		wp.fillList();
	}
	
	public void fillList() {
		list = new ArrayList(10);
		int value = 10;
		while (value > 0) {
			list.add(new Integer(value));
			value--;
		}
		
	}

}
