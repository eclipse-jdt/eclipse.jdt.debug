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

import java.io.DataOutputStream;

import org.eclipse.jdi.internal.jdwp.JdwpID;

import com.sun.jdi.Type;
import com.sun.jdi.VoidValue;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class VoidValueImpl extends ValueImpl implements VoidValue {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.VOID_TAG;

	/**
	 * Creates new instance.
	 */
	public VoidValueImpl(VirtualMachineImpl vmImpl) {
		super("VoidValue", vmImpl); //$NON-NLS-1$
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
		return new VoidTypeImpl(virtualMachineImpl());
	}

	/**
	 * @return Returns true if two values are equal.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		return object != null && object.getClass().equals(this.getClass());
	}

	/**
	 * @return Returns a has code for this object.
	 * @see java.lang.Object#hashCode
	 */
	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * Writes value without value tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out) {
		// Nothing to write.
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
	public String toString() {
		return "(void)"; //$NON-NLS-1$
	}
}
