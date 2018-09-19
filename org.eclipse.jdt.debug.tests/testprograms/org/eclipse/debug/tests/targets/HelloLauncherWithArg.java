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
