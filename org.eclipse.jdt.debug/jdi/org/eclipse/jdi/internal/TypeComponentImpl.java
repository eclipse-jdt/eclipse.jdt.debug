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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.TypeComponent;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 *
 */
public abstract class TypeComponentImpl extends AccessibleImpl implements
		TypeComponent {
	/** Text representation of this type. */
	private String fName = null;
	/** JNI-style signature for this type. */
	private String fSignature = null;
	/** the generic signature for this type, java 1.5 */
	private String fGenericSignature;
	/** ReferenceType that holds field or method. */
	private ReferenceTypeImpl fDeclaringType;
	/** Modifier bits. */
	protected int fModifierBits;

	/**
	 * Creates new instance.
	 */
	public TypeComponentImpl(String description, VirtualMachineImpl vmImpl,
			ReferenceTypeImpl declaringType, String name, String signature,
			String genericSignature, int modifierBits) {
		super(description, vmImpl);
		fName = name;
		fSignature = signature;
		fGenericSignature = genericSignature;
		fDeclaringType = declaringType;
		fModifierBits = modifierBits;
	}

	/**
	 * @return Returns modifier bits.
	 */
	@Override
	public int modifiers() {
		return fModifierBits;
	}

	/**
	 * @return Returns the ReferenceTypeImpl in which this component was
	 *         declared.
	 */
	public ReferenceTypeImpl referenceTypeImpl() {
		return fDeclaringType;
	}

	/**
	 * @return Returns the type in which this component was declared.
	 */
	@Override
	public ReferenceType declaringType() {
		return fDeclaringType;
	}

	/**
	 * @return Returns true if type component is final.
	 */
	@Override
	public boolean isFinal() {
		return (fModifierBits & MODIFIER_ACC_FINAL) != 0;
	}

	/**
	 * @return Returns true if type component is static.
	 */
	@Override
	public boolean isStatic() {
		return (fModifierBits & MODIFIER_ACC_STATIC) != 0;
	}

	/**
	 * @return Returns true if type component is synthetic.
	 */
	@Override
	public boolean isSynthetic() {
		return (fModifierBits & (MODIFIER_SYNTHETIC | MODIFIER_ACC_SYNTHETIC)) != 0;
	}

	/**
	 * @return Returns text representation of this type.
	 */
	@Override
	public String name() {
		return fName;
	}

	/**
	 * @return JNI-style signature for this type.
	 */
	@Override
	public String signature() {
		return fSignature;
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
	public String toString() {
		return fName;
	}

	@Override
	public String genericSignature() {
		return fGenericSignature;
	}

}
