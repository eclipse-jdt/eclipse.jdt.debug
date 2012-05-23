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

import com.sun.jdi.ByteValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ByteValueImpl extends PrimitiveValueImpl implements ByteValue, Comparable<ByteValue> {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.BYTE_TAG;

	/**
	 * Creates new instance.
	 * @param vmImpl the VM
	 * @param value the underlying byte value
	 */
	public ByteValueImpl(VirtualMachineImpl vmImpl, Byte value) {
		super("ByteValue", vmImpl, value); //$NON-NLS-1$
	}

	/**
	 * @returns tag.
	 */
	@Override
	public byte getTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ByteValue o) {
		return ((Byte)byteValue()).compareTo(o.byteValue());
	}
	
	/**
	 * @returns type of value.
	 */
	@Override
	public Type type() {
		return virtualMachineImpl().getByteType();
	}

	/**
	 * @return the underlying byte value
	 */
	public byte value() {
		return byteValue();
	}

	/**
	 * @param target the target
	 * @param in the stream
	 * @return Reads and returns new instance.
	 * @throws IOException if the read fails
	 */
	public static ByteValueImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		byte value = target.readByte("byteValue", in); //$NON-NLS-1$
		return new ByteValueImpl(vmImpl, new Byte(value));
	}

	/**
	 * Writes value without value tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
		target.writeByte(((Byte) fValue).byteValue(), "byteValue", out); //$NON-NLS-1$
	}
}
