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
 * Test class with handle to the singleton <code>MainClass</code>
 */
public class RefClass2 {
	/**
	 * A handle to the singleton <code>MainClass</code>
	 */
	public Object temp2 = MainClass.fObject;

}
