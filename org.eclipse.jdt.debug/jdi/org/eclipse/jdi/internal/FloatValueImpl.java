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

import com.sun.jdi.FloatValue;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class FloatValueImpl extends PrimitiveValueImpl implements FloatValue, Comparable<FloatValue> {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.FLOAT_TAG;

	/**
	 * Creates new instance.
	 */
	public FloatValueImpl(VirtualMachineImpl vmImpl, Float value) {
		super("FloatValue", vmImpl, value); //$NON-NLS-1$
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
		return virtualMachineImpl().getFloatType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(FloatValue o) {
		return ((Float)floatValue()).compareTo(o.floatValue());
	}
	
	/**
	 * @returns Value.
	 */
	public float value() {
		return floatValue();
	}

	/**
	 * @return Reads and returns new instance.
	 */
	public static FloatValueImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		float value = target.readFloat("floatValue", in); //$NON-NLS-1$
		return new FloatValueImpl(vmImpl, new Float(value));
	}

	/**
	 * Writes value without value tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		target.writeFloat(((Float) fValue).floatValue(), "floatValue", out); //$NON-NLS-1$
	}
}
