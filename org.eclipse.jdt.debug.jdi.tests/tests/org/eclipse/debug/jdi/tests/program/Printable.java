/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 * Interface type for target VM tests.
 * This interface is intended to be loaded by the target VM. 
 *
 * WARNING, WARNING:
 * Tests in org.eclipse.debug.jdi.tests assume the content of this interface.
 * So if this interface or one of the types in this
 * package is changed, the corresponding tests must also be changed.
 */

import java.io.OutputStream;

/**
 *
 */
public interface Printable extends Cloneable {
	/**
	 * the number 1 
	 */
	int CONSTANT = 1;
	/**
	 * Prints to the specified output stream
	 * @param out
	 */
	public void print(OutputStream out);
	}
