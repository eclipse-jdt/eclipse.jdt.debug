/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.jdt.core.ClasspathVariableInitializer;

/**
 * A classpath variable initializer for "NULL_VARIABLE", that initializes to null.
 */
public class NullVariableInitializer extends ClasspathVariableInitializer {

	/**
	 * Constructor
	 */
	public NullVariableInitializer() {
		super();
	}

	/**
	 * Performs no initialization.
	 * 
	 * @see org.eclipse.jdt.core.ClasspathVariableInitializer#initialize(java.lang.String)
	 */
	@Override
	public void initialize(String variable) {
	}

}
