package org.eclipse.debug.tests.targets;



/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
