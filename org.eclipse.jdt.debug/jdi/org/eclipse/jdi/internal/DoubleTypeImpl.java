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
package org.eclipse.jdi.internal;

import com.sun.jdi.DoubleType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class DoubleTypeImpl extends PrimitiveTypeImpl implements DoubleType {
	/**
	 * Creates new instance.
	 */
	public DoubleTypeImpl(VirtualMachineImpl vmImpl) {
		super("DoubleType", vmImpl, "double", "D"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @returns primitive type tag.
	 */
	@Override
	public byte tag() {
		return DoubleValueImpl.tag;
	}

	/**
	 * @return Create a null value instance of the type.
	 */
	@Override
	public Value createNullValue() {
		return virtualMachineImpl().mirrorOf(0.0D);
	}
}
