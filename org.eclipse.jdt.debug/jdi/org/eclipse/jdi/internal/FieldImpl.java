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

import org.eclipse.jdi.internal.jdwp.JdwpFieldID;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.Type;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class FieldImpl extends TypeComponentImpl implements Field {
	/** ID that corresponds to this reference. */
	private JdwpFieldID fFieldID;
	private Type fType;
	private String fTypeName;

	/**
	 * Creates new FieldImpl.
	 */
	public FieldImpl(VirtualMachineImpl vmImpl,
			ReferenceTypeImpl declaringType, JdwpFieldID ID, String name,
			String signature, String genericSignature, int modifierBits) {
		super(
				"Field", vmImpl, declaringType, name, signature, genericSignature, modifierBits); //$NON-NLS-1$
		fFieldID = ID;
	}

	/**
	 * Flushes all stored Jdwp results.
	 */
	public void flushStoredJdwpResults() {
		// Note that no results are cached.
	}

	/**
	 * @return Returns fieldID of field.
	 */
	public JdwpFieldID getFieldID() {
		return fFieldID;
	}

	/**
	 * @return Returns true if two mirrors refer to the same entity in the
	 *         target VM.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		return object != null
				&& object.getClass().equals(this.getClass())
				&& fFieldID.equals(((FieldImpl) object).fFieldID)
				&& referenceTypeImpl().equals(
						((FieldImpl) object).referenceTypeImpl());
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Field object) {
		if (object == null || !object.getClass().equals(this.getClass()))
			throw new ClassCastException(JDIMessages.FieldImpl_Can__t_compare_field_to_given_object_1);

		// See if declaring types are the same, if not return comparison between
		// declaring types.
		Field type2 = object;
		if (!declaringType().equals(type2.declaringType()))
			return declaringType().compareTo(type2.declaringType());

		// Return comparison of position within declaring type.
		int index1 = declaringType().fields().indexOf(this);
		int index2 = type2.declaringType().fields().indexOf(type2);
		if (index1 < index2)
			return -1;
		else if (index1 > index2)
			return 1;
		else
			return 0;
	}

	/**
	 * @return Returns the hash code value.
	 */
	@Override
	public int hashCode() {
		return fFieldID.hashCode();
	}

	/**
	 * @return Returns a text representation of the declared type.
	 */
	public String typeName() {
		if (fTypeName == null) {
			fTypeName = TypeImpl.signatureToName(signature());
		}
		return fTypeName;
	}

	/**
	 * @return Returns the type of the this Field.
	 */
	public Type type() throws ClassNotLoadedException {
		if (fType == null) {
			fType = TypeImpl.create(virtualMachineImpl(), signature(),
					declaringType().classLoader());
		}
		return fType;
	}

	/**
	 * @return Returns true if object is transient.
	 */
	public boolean isTransient() {
		return (fModifierBits & MODIFIER_ACC_TRANSIENT) != 0;
	}

	/**
	 * @return Returns true if object is volatile.
	 */
	public boolean isVolatile() {
		return (fModifierBits & MODIFIER_ACC_VOLITILE) != 0;
	}

	/**
	 * Writes JDWP representation.
	 */
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		fFieldID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("field", fFieldID.value()); //$NON-NLS-1$
	}

	/**
	 * Writes JDWP representation, including ReferenceType.
	 */
	public void writeWithReferenceType(MirrorImpl target, DataOutputStream out)
			throws IOException {
		// See EventRequest case FieldOnly
		referenceTypeImpl().write(target, out);
		write(target, out);
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static FieldImpl readWithReferenceTypeWithTag(MirrorImpl target,
			DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		// See Events FIELD_ACCESS and FIELD_MODIFICATION (refTypeTag + typeID +
		// fieldID).
		ReferenceTypeImpl referenceType = ReferenceTypeImpl.readWithTypeTag(
				target, in);
		if (referenceType == null)
			return null;

		JdwpFieldID ID = new JdwpFieldID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("field", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;
		FieldImpl field = referenceType.findField(ID);
		if (field == null)
			throw new InternalError(
					JDIMessages.FieldImpl_Got_FieldID_of_ReferenceType_that_is_not_a_member_of_the_ReferenceType_2);
		return field;
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static FieldImpl readWithNameSignatureModifiers(
			ReferenceTypeImpl target, ReferenceTypeImpl referenceType,
			boolean withGenericSignature, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpFieldID ID = new JdwpFieldID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("field", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;
		String name = target.readString("name", in); //$NON-NLS-1$
		String signature = target.readString("signature", in); //$NON-NLS-1$
		String genericSignature = null;
		if (withGenericSignature) {
			genericSignature = target.readString("generic signature", in); //$NON-NLS-1$
			if ("".equals(genericSignature)) { //$NON-NLS-1$
				genericSignature = null;
			}
		}
		int modifierBits = target.readInt(
				"modifiers", AccessibleImpl.getModifierStrings(), in); //$NON-NLS-1$

		FieldImpl mirror = new FieldImpl(vmImpl, referenceType, ID, name,
				signature, genericSignature, modifierBits);
		return mirror;
	}

	public boolean isEnumConstant() {
		return (fModifierBits & MODIFIER_ACC_ENUM) != 0;
	}
}
