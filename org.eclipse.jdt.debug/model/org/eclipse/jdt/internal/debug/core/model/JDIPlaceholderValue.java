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

/**
 * This class is used to create a variable/value that displays a warning message
 * to the user. Currently used to inform users that references are not available
 * for the current VM. It extends <code>JDINullValue</code> so that most actions
 * will ignore it, but returns the message instead of "null" for it's value.
 * 
 * @since 3.3
 */
public class JDIPlaceholderValue extends JDINullValue {

	private String fMessage;

	/**
	 * Constructor, passes the debug target to the super class.
	 * 
	 * @param target
	 *            debug target this value belongs to
	 */
	public JDIPlaceholderValue(JDIDebugTarget target, String message) {
		super(target);
		fMessage = message;
	}

	/**
	 * @return the message supplied in the constructor
	 * @see org.eclipse.jdt.internal.debug.core.model.JDINullValue#getValueString()
	 */
	@Override
	public String getValueString() {
		return fMessage;
	}

	/**
	 * @return the message supplied in the constructor
	 * @see org.eclipse.jdt.internal.debug.core.model.JDINullValue#toString()
	 */
	@Override
	public String toString() {
		return fMessage;
	}

	/**
	 * Returns signature for a java string object so that the string message
	 * passed in the constructor is displayed in the detail pane.
	 * 
	 * @return signature for a java string object
	 * @see org.eclipse.jdt.internal.debug.core.model.JDINullValue#getSignature()
	 */
	@Override
	public String getSignature() {
		return "Ljava/lang/String;"; //$NON-NLS-1$
	}

}
