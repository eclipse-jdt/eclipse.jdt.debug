/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

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