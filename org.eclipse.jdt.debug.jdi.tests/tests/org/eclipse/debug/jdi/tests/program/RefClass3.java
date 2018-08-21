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
 * Test class with a static reference to <code>RefClass1</code>
 */
public class RefClass3 {
	/**
	 * A static reference to <code>RefClass1</code>
	 */
	public static RefClass1 one = new RefClass1();
}
