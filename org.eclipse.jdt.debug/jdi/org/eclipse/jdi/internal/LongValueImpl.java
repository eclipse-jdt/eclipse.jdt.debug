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

import com.sun.jdi.LongValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 */
public class LongValueImpl extends PrimitiveValueImpl implements LongValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.LONG_TAG;

	/**
	 * Creates new instance.
	 */
	public LongValueImpl(VirtualMachineImpl vmImpl, Long value) {
		super("LongValue", vmImpl, value); //$NON-NLS-1$
	}

	/**
	 * @return tag.
	 */
	@Override
	public byte getTag() {
		return tag;
	}

	/**
	 * @return type of value.
	 */
	@Override
	public Type type() {
		return virtualMachineImpl().getLongType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LongValue o) {
		return ((Long)longValue()).compareTo(o.longValue());
	}

	/**
	 * @return Value.
	 */
	@Override
	public long value() {
		return longValue();
	}

	/**
	 * @return Reads and returns new instance.
	 */
	public static LongValueImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		long value = target.readLong("longValue", in); //$NON-NLS-1$
		return new LongValueImpl(vmImpl, Long.valueOf(value));
	}

	/**
	 * Writes value without value tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		target.writeLong(((Long) fValue).longValue(), "longValue", out); //$NON-NLS-1$
	}
}
