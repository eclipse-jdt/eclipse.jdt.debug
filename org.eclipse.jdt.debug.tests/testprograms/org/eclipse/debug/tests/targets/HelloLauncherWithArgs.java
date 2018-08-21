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
