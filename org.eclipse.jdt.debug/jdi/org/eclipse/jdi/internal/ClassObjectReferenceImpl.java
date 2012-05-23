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

import org.eclipse.jdi.internal.jdwp.JdwpClassObjectID;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ReferenceType;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ClassObjectReferenceImpl extends ObjectReferenceImpl implements
		ClassObjectReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.CLASS_OBJECT_TAG;

	/**
	 * Creates new ClassObjectReferenceImpl.
	 */
	public ClassObjectReferenceImpl(VirtualMachineImpl vmImpl,
			JdwpClassObjectID classObjectID) {
		super("ClassObjectReference", vmImpl, classObjectID); //$NON-NLS-1$
	}

	/**
	 * @returns Returns Value tag.
	 */
	@Override
	public byte getTag() {
		return tag;
	}

	/**
	 * @returns Returns the ReferenceType corresponding to this class object.
	 */
	public ReferenceType reflectedType() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.COR_REFLECTED_TYPE, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			return ReferenceTypeImpl.readWithTypeTag(this, replyData);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ClassObjectReferenceImpl read(MirrorImpl target,
			DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpClassObjectID ID = new JdwpClassObjectID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("classObjectReference", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;

		ClassObjectReferenceImpl mirror = new ClassObjectReferenceImpl(vmImpl,
				ID);
		return mirror;
	}
}
