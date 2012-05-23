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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdi.internal.jdwp.JdwpClassObjectID;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpInterfaceID;

import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class InterfaceTypeImpl extends ReferenceTypeImpl implements
		InterfaceType {
	/** JDWP Tag. */
	public static final byte typeTag = JdwpID.TYPE_TAG_INTERFACE;

	/**
	 * Creates new InterfaceTypeImpl.
	 */
	public InterfaceTypeImpl(VirtualMachineImpl vmImpl,
			JdwpInterfaceID interfaceID) {
		super("InterfaceType", vmImpl, interfaceID); //$NON-NLS-1$
	}

	/**
	 * Creates new InterfaceTypeImpl.
	 */
	public InterfaceTypeImpl(VirtualMachineImpl vmImpl,
			JdwpInterfaceID interfaceID, String signature,
			String genericSignature) {
		super("InterfaceType", vmImpl, interfaceID, signature, genericSignature); //$NON-NLS-1$
	}

	/**
	 * @return Create a null value instance of the type.
	 */
	@Override
	public Value createNullValue() {
		return new ClassObjectReferenceImpl(virtualMachineImpl(),
				new JdwpClassObjectID(virtualMachineImpl()));
	}

	/**
	 * @return Returns type tag.
	 */
	@Override
	public byte typeTag() {
		return typeTag;
	}

	/**
	 * Flushes all stored Jdwp results.
	 */
	@Override
	public void flushStoredJdwpResults() {
		super.flushStoredJdwpResults();

		// For all reference types that have this interface cached, this cache must be
		// undone.
		Iterator<Object> itr = virtualMachineImpl().allCachedRefTypes();
		while (itr.hasNext()) {
			ReferenceTypeImpl refType = (ReferenceTypeImpl) itr.next();
			if (refType.fInterfaces != null
					&& refType.fInterfaces.contains(this)) {
				refType.flushStoredJdwpResults();
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.InterfaceType#implementors()
	 */
	public List<ClassType> implementors() {
		// Note that this information should not be cached.
		List<ClassType> implementors = new ArrayList<ClassType>();
		Iterator<ReferenceType> itr = virtualMachineImpl().allRefTypes();
		while (itr.hasNext()) {
			ReferenceType refType = itr.next();
			if (refType instanceof ClassTypeImpl) {
				try {
					ClassTypeImpl classType = (ClassTypeImpl) refType;
					List<InterfaceType> interfaces = classType.interfaces();
					if (interfaces.contains(this)) {
						implementors.add(classType);
					}
				} catch (ClassNotPreparedException e) {
					continue;
				}
			}
		}
		return implementors;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.InterfaceType#subinterfaces()
	 */
	public List<InterfaceType> subinterfaces() {
		// Note that this information should not be cached.
		List<InterfaceType> implementors = new ArrayList<InterfaceType>();
		Iterator<ReferenceType> itr = virtualMachineImpl().allRefTypes();
		while (itr.hasNext()) {
			try {
				ReferenceType refType = itr.next();
				if (refType instanceof InterfaceTypeImpl) {
					InterfaceTypeImpl interFaceType = (InterfaceTypeImpl) refType;
					List<InterfaceType> interfaces = interFaceType.superinterfaces();
					if (interfaces.contains(this)) {
						implementors.add(interFaceType);
					}
				}
			} catch (ClassNotPreparedException e) {
				continue;
			}
		}
		return implementors;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.InterfaceType#superinterfaces()
	 */
	public List<InterfaceType> superinterfaces() {
		return interfaces();
	}

	/**
	 * @return Returns true if this type has been initialized.
	 */
	@Override
	public boolean isInitialized() {
		return isPrepared();
	}

	/**
	 * @return Reads ID and returns known ReferenceTypeImpl with that ID, or if
	 *         ID is unknown a newly created ReferenceTypeImpl.
	 */
	public static InterfaceTypeImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpInterfaceID ID = new JdwpInterfaceID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("interfaceType", ID.value()); //$NON-NLS-1$
		}

		if (ID.isNull()) {
			return null;
		}

		InterfaceTypeImpl mirror = (InterfaceTypeImpl) vmImpl
				.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new InterfaceTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		return mirror;
	}

	/**
	 * @return Reads ID and returns known ReferenceTypeImpl with that ID, or if
	 *         ID is unknown a newly created ReferenceTypeImpl.
	 */
	public static InterfaceTypeImpl readWithSignature(MirrorImpl target,
			boolean withGenericSignature, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpInterfaceID ID = new JdwpInterfaceID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null) {
			target.fVerboseWriter.println("interfaceType", ID.value()); //$NON-NLS-1$
		}

		String signature = target.readString("signature", in); //$NON-NLS-1$
		String genericSignature = null;
		if (withGenericSignature) {
			genericSignature = target.readString("generic signature", in); //$NON-NLS-1$
		}
		if (ID.isNull()) {
			return null;
		}

		InterfaceTypeImpl mirror = (InterfaceTypeImpl) vmImpl
				.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new InterfaceTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		mirror.setSignature(signature);
		mirror.setGenericSignature(genericSignature);
		return mirror;
	}
}
