package org.eclipse.debug.tests.targets;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

public class HelloLauncherWithArgs {
	public static void main(String args[]) {
		int argCount = args.length;
		if (argCount > 1) {
			if (args[0].equals("foo") && args[1].equals("bar")) {
				System.out.println("First argument was foo and second argument was bar");
			}
		}			
	}

}