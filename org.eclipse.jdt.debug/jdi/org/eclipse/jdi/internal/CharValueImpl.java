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

import com.sun.jdi.CharValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
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
		return virtualMachineImpl().getCharType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(CharValue o) {
		return ((Character)charValue()).compareTo(o.charValue());
	}
	
	/**
	 * @returns Value.
	 */
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
		return new CharValueImpl(vmImpl, new Character(value));
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
