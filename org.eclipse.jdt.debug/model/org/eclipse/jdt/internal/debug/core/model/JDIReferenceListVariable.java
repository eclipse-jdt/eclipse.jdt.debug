/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;

/**
 * A variable that stores a list of references. Used to display reference
 * information collected from the vm using the 'all references' utility in Java
 * 6.0. This variable uses a <code>JDIReferenceListValue</code> as its value.
 * Its children will be <code>JDIReferenceListEntryVariable</code>.
 * 
 * @since 3.3
 * @see JDIReferenceListValue
 * @see JDIReferenceListEntryVariable
 */
public class JDIReferenceListVariable extends JDIPlaceholderVariable {

	/**
	 * Creates a new variable that stores a list of references, all referring to
	 * the java object specified by the parameter root.
	 * 
	 * @param name
	 *            The name this variable should use
	 * @param root
	 *            The root java object that references will be collected for
	 */
	public JDIReferenceListVariable(String name, IJavaObject root) {
		super(name, new JDIReferenceListValue(root));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable
	 * #equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// Two JDIReferenceListVariables are equal if their values are equal
		if (obj instanceof JDIReferenceListVariable) {
			JDIReferenceListVariable var = (JDIReferenceListVariable) obj;
			if (getValue() instanceof JDIPlaceholderValue
					|| var.getValue() instanceof JDIPlaceholderValue) {
				// A placeholder value is only equal to the same instance
				return this == obj;
			}
			return getValue().equals(var.getValue());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable
	 * #hashCode()
	 */
	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
}
