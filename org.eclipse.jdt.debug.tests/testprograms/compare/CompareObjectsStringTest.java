/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
package compare;

public class CompareObjectsStringTest {
	public static void main(String[] ecs) {
		String s1 = new String("eclipse");
		String s2 = new String("eclipse");
		
		String s3 = "false1";
		String s4 = "false";
		
		StringBuffer s5 = new StringBuffer("SDK");
		StringBuffer s6 = new StringBuffer("SDK");
		
		StringBuilder s7 = new StringBuilder("Java");
		StringBuffer s8 = new StringBuffer("Java");
		
		StringBuilder s9 = new StringBuilder("Java1");
		int p = 100;
	}
}