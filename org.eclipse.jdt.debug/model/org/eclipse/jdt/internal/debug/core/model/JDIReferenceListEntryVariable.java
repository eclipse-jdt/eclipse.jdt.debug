/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;

/**
 * This variable is created as the child of <code>JDIReferenceListValue</code>.
 * It represents one reference to a root object stored in the parent value.
 * 
 * @see JDIReferenceListValue
 * @since 3.3
 */
public class JDIReferenceListEntryVariable extends JDIPlaceholderVariable {

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            The name that this variable should use as its label
	 * @param reference
	 *            The value that this variable contains
	 */
	public JDIReferenceListEntryVariable(String name, IJavaValue reference) {
		super(name, reference);
	}

}
