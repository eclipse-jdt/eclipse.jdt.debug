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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.internal.jdwp.JdwpArrayID;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpObjectID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ArrayTypeImpl extends ReferenceTypeImpl implements ArrayType {
	/** JDWP Tag. */
	public static final byte typeTag = JdwpID.TYPE_TAG_ARRAY;
	/** component type */
	private Type fComponentType;
	/** Component type name */
	private String fComponentTypeName;

	/**
	 * Creates new ArrayTypeImpl.
	 * @param vmImpl the VM
	 * @param arrayID the array ID
	 */
	public ArrayTypeImpl(VirtualMachineImpl vmImpl, JdwpArrayID arrayID) {
		super("ArrayType", vmImpl, arrayID); //$NON-NLS-1$
	}

	/**
	 * Creates new ArrayTypeImpl.
	 * @param vmImpl the VM
	 * @param arrayID the array ID
	 * @param signature the array type signature
	 * @param genericSignature the array type generic signature
	 */
	public ArrayTypeImpl(VirtualMachineImpl vmImpl, JdwpArrayID arrayID,
			String signature, String genericSignature) {
		super("ArrayType", vmImpl, arrayID, signature, genericSignature); //$NON-NLS-1$
	}

	/**
	 * @return Returns type tag.
	 */
	@Override
	public byte typeTag() {
		return typeTag;
	}

	/**
	 * @return Create a null value instance of the type.
	 */
	@Override
	public Value createNullValue() {
		return new ArrayReferenceImpl(virtualMachineImpl(), new JdwpObjectID(
				virtualMachineImpl()));
	}

	/**
	 * @return Returns the JNI signature of the components of this array class.
	 */
	public String componentSignature() {
		return signature().substring(1);
	}

	/**
	 * @return Returns the type of the array components.
	 * @throws ClassNotLoadedException if the class has not been loaded
	 */
	public Type componentType() throws ClassNotLoadedException {
		if (fComponentType == null) {
			fComponentType = TypeImpl.create(virtualMachineImpl(),
					componentSignature(), classLoader());
		}
		return fComponentType;
	}

	/**
	 * @return Returns a text representation of the component type.
	 */
	public String componentTypeName() {
		if (fComponentTypeName == null) {
			fComponentTypeName = signatureToName(componentSignature());
		}
		return fComponentTypeName;
	}

	/**
	 * @param length the desired length of the new array
	 * @return Creates and returns a new instance of this array class in the
	 *         target VM.
	 */
	public ArrayReference newInstance(int length) {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			writeInt(length, "length", outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.AT_NEW_INSTANCE, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			ArrayReferenceImpl arrayRef = (ArrayReferenceImpl) ObjectReferenceImpl
					.readObjectRefWithTag(this, replyData);
			return arrayRef;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns a List filled with all Location objects that map to the
	 *         given line number.
	 */
	@Override
	public List<Location> locationsOfLine(int line) {
		// If this reference type is an ArrayType, the returned list is always
		// empty.
		return Collections.EMPTY_LIST;
	}

	/**
	 * @param target the target
	 * @param in the stream
	 * @return Reads JDWP representation and returns new instance.
	 * @throws IOException if the reading fails
	 */
	public static ArrayTypeImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpArrayID ID = new JdwpArrayID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("arrayType", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;

		ArrayTypeImpl mirror = (ArrayTypeImpl) vmImpl.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ArrayTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		return mirror;
	}

	/**
	 * @return Returns modifier bits.
	 */
	@Override
	public int modifiers() {
		return MODIFIER_ACC_PUBLIC | MODIFIER_ACC_FINAL;
	}

	/**
	 * @return Returns a list containing each Field declared in this type.
	 */
	@Override
	public List<Field> fields() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @return Returns a list containing each Method declared in this type.
	 */
	@Override
	public List<Method> methods() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @return a Map of the requested static Field objects with their Value.
	 */
	@Override
	public Map<Field, Value> getValues(List<? extends Field> fields) {
		if (fields.isEmpty()) {
			return new HashMap<Field, Value>();
		}

		throw new IllegalArgumentException(
				JDIMessages.ArrayTypeImpl_getValues_not_allowed_on_array_1);
	}

	/**
	 * @return Returns a List containing each ReferenceType declared within this
	 *         type.
	 */
	@Override
	public List<ReferenceType> nestedTypes() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @return Returns status of class/interface.
	 */
	@Override
	protected int status() {
		return ReferenceTypeImpl.JDWP_CLASS_STATUS_INITIALIZED
				| ReferenceTypeImpl.JDWP_CLASS_STATUS_PREPARED
				| ReferenceTypeImpl.JDWP_CLASS_STATUS_VERIFIED;
	}

	/**
	 * @param target the target
	 * @param withGenericSignature if the generic signature should be read
	 * @param in the stream
	 * @return Reads JDWP representation and returns new instance.
	 * @throws IOException if the read fails
	 */
	public static ArrayTypeImpl readWithSignature(MirrorImpl target,
			boolean withGenericSignature, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpArrayID ID = new JdwpArrayID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("arrayType", ID.value()); //$NON-NLS-1$

		String signature = target.readString("signature", in); //$NON-NLS-1$
		String genericSignature = null;
		if (withGenericSignature) {
			genericSignature = target.readString("generic signature", in); //$NON-NLS-1$
		}
		if (ID.isNull())
			return null;

		ArrayTypeImpl mirror = (ArrayTypeImpl) vmImpl.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ArrayTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		mirror.setSignature(signature);
		mirror.setGenericSignature(genericSignature);
		return mirror;
	}

	/**
	 * @see com.sun.jdi.ReferenceType#allLineLocations()
	 */
	@Override
	public List<Location> allLineLocations() {
		// If this reference type is an ArrayType, the returned list is always
		// empty.
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see com.sun.jdi.ReferenceType#allMethods()
	 */
	@Override
	public List<Method> allMethods() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see com.sun.jdi.ReferenceType#allFields()
	 */
	@Override
	public List<Field> allFields() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @return Returns an identifying name for the source corresponding to the
	 *         declaration of this type.
	 */
	@Override
	public String sourceName() throws AbsentInformationException {
		throw new AbsentInformationException(
				JDIMessages.ArrayTypeImpl_No_source_name_for_Arrays_1);
	}

	/**
	 * @see com.sun.jdi.ReferenceType#visibleFields()
	 */
	@Override
	public List<Field> visibleFields() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see com.sun.jdi.ReferenceType#visibleMethods()
	 */
	@Override
	public List<Method> visibleMethods() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see com.sun.jdi.ReferenceType#fieldByName(String)
	 */
	@Override
	public Field fieldByName(String arg1) {
		return null;
	}

	/**
	 * @see com.sun.jdi.ReferenceType#methodsByName(String)
	 */
	@Override
	public List<Method> methodsByName(String arg1) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see com.sun.jdi.ReferenceType#methodsByName(String, String)
	 */
	@Override
	public List<Method> methodsByName(String arg1, String arg2) {
		return Collections.EMPTY_LIST;
	}
}
