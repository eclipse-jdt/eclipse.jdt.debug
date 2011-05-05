/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
