/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.jdwp.JdwpID;

import com.sun.jdi.ShortValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ShortValueImpl extends PrimitiveValueImpl implements ShortValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.SHORT_TAG;

	/**
	 * Creates new instance.
	 */
	public ShortValueImpl(VirtualMachineImpl vmImpl, Short value) {
		super("ShortValue", vmImpl, value); //$NON-NLS-1$
	}

	/**
	 * @returns tag.
	 */
	@Override
	public byte getTag() {
		return tag;
	}

	/**
	 * @returns type of value.
	 */
	@Override
	public Type type() {
		return virtualMachineImpl().getShortType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ShortValue o) {
		return ((Short)shortValue()).compareTo(o.shortValue());
	}
	
	/**
	 * @returns Value.
	 */
	public short value() {
		return shortValue();
	}

	/**
	 * @return Reads and returns new instance.
	 */
	public static ShortValueImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		short value = target.readShort("shortValue", in); //$NON-NLS-1$
		return new ShortValueImpl(vmImpl, new Short(value));
	}

	/**
	 * Writes value without value tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		target.writeShort(((Short) fValue).shortValue(), "shortValue", out); //$NON-NLS-1$
	}
}
