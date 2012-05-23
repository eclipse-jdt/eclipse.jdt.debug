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

import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaType;

import com.sun.jdi.ClassObjectReference;

/**
 * An object on the target VM that is an instance of
 * <code>java.lang.Class</code>.
 * 
 * @see IJavaClassObject
 */
public class JDIClassObjectValue extends JDIObjectValue implements
		IJavaClassObject {

	/**
	 * Constructs a reference to a class object.
	 */
	public JDIClassObjectValue(JDIDebugTarget target,
			ClassObjectReference object) {
		super(target, object);
	}

	/**
	 * @see IJavaClassObject#getInstanceType()
	 */
	public IJavaType getInstanceType() {
		return JDIType.createType((JDIDebugTarget) getDebugTarget(),
				getUnderlyingClassObject().reflectedType());
	}

	/**
	 * Returns the underlying class object
	 */
	protected ClassObjectReference getUnderlyingClassObject() {
		return (ClassObjectReference) getUnderlyingValue();
	}
}
