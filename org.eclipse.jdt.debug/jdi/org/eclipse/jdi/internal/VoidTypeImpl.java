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

import com.sun.jdi.Value;
import com.sun.jdi.VoidType;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class VoidTypeImpl extends TypeImpl implements VoidType {
	/**
	 * Creates new instance.
	 */
	public VoidTypeImpl(VirtualMachineImpl vmImpl) {
		super("VoidType", vmImpl, "void", "V"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @return Returns modifier bits.
	 */
	@Override
	public int modifiers() {
		throw new InternalError(
				JDIMessages.VoidTypeImpl_A_VoidType_does_not_have_modifiers_1);
	}

	/**
	 * @return Create a null value instance of the type.
	 */
	@Override
	public Value createNullValue() {
		return new VoidValueImpl(virtualMachineImpl());
	}
}
