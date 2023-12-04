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

import com.sun.jdi.CharValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 */
public class CharValueImpl extends PrimitiveValueImpl implements CharValue, Comparable<CharValue> {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.CHAR_TAG;

	/**
	 * Creates new instance.
	 */
	public CharValueImpl(VirtualMachineImpl vmImpl, Character value) {
		super("CharValue", vmImpl, value); //$NON-NLS-1$
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
		return virtualMachineImpl().getCharType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CharValue o) {
		return ((Character)charValue()).compareTo(o.charValue());
	}

	/**
	 * @return Value.
	 */
	@Override
	public char value() {
		return charValue();
	}

	/**
	 * @return Reads and returns new instance.
	 */
	public static CharValueImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		char value = target.readChar("charValue", in); //$NON-NLS-1$
		return new CharValueImpl(vmImpl, Character.valueOf(value));
	}

	/**
	 * Writes value without value tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		target.writeChar(((Character) fValue).charValue(), "charValue", out); //$NON-NLS-1$
	}
}
