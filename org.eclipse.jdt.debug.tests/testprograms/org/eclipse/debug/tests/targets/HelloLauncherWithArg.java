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

public class HelloLauncherWithArg {
	public static void main(String args[]) {
		int argCount = args.length;
		if (argCount > 0) {
			if (args[0].equals("foo")) {
				System.out.println("First argument was foo");
			}
		}
	}

}