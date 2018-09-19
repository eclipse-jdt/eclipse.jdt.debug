/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
package org.eclipse.debug.jdi.tests.program;

/**
 * Test class with a static object array
 */
public class RefClass {

	/**
	 * Array of two other Ref classes
	 */
	public static Object[] test = new Object[] {
		new RefClass1(), new RefClass2()};
}
