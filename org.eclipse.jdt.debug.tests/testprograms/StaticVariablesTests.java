/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class StaticVariablesTests {
	
	private int i;
	private String s;
	
	public StaticVariablesTests() {
		i = 1;
		s = "string";	
	}
	
	public static void nop() {
	}
	
	public static String pubStr = "public";
	protected static String protStr = "protected";
	/* default */ static String defStr = "default";
	private static String privStr = "private";	
 		
	public static void run() {
		nop();
	}
	
	public int fcn() {
		return 1;	
	}
	
	public static void main(String[] args) {
		run();
		(new StaticVariablesTests()).fcn();
	}
}
