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

import com.sun.jdi.BooleanValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 */
public class BooleanValueImpl extends PrimitiveValueImpl implements
		BooleanValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.BOOLEAN_TAG;

	/**
	 * Creates new instance.
	 * @param vmImpl the VM
	 * @param value the underlying value
	 */
	public BooleanValueImpl(VirtualMachineImpl vmImpl, Boolean value) {
		super("BooleanValue", vmImpl, value); //$NON-NLS-1$
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
		return virtualMachineImpl().getBooleanType();
	}

	/**
	 * @return the underlying value
	 */
	@Override
	public boolean value() {
		return booleanValue();
	}

	/**
	 * @param target the target
	 * @param in the stream
	 * @return Reads and returns new instance.
	 * @throws IOException if the read fails
	 */
	public static BooleanValueImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		boolean value = target.readBoolean("booleanValue", in); //$NON-NLS-1$
		return new BooleanValueImpl(vmImpl, Boolean.valueOf(value));
	}

	/**
	 * Writes value without value tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		target.writeBoolean(((Boolean) fValue).booleanValue(),
				"booleanValue", out); //$NON-NLS-1$
	}
}
