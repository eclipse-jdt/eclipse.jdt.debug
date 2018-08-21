/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jdi.internal.jdwp.JdwpID;

import com.sun.jdi.IntegerValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 *
 */
public class IntegerValueImpl extends PrimitiveValueImpl implements IntegerValue, Comparable<IntegerValue> {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.INT_TAG;

	/**
	 * Creates new instance.
	 */
	public IntegerValueImpl(VirtualMachineImpl vmImpl, Integer value) {
		super("IntegerValue", vmImpl, value); //$NON-NLS-1$
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
		return virtualMachineImpl().getIntegerType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IntegerValue o) {
		return ((Integer)intValue()).compareTo(o.intValue());
	}

	/**
	 * @returns Value.
	 */
	@Override
	public int value() {
		return intValue();
	}

	/**
	 * @return Reads and returns new instance.
	 */
	public static IntegerValueImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		int value = target.readInt("integerValue", in); //$NON-NLS-1$
		return new IntegerValueImpl(vmImpl, new Integer(value));
	}

	/**
	 * Writes value without value tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		target.writeInt(((Integer) fValue).intValue(), "intValue", out); //$NON-NLS-1$
	}
}
