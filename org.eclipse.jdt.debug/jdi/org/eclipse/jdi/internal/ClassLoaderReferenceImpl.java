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

import org.eclipse.jdi.internal.jdwp.JdwpClassLoaderID;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ReferenceType;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ClassLoaderReferenceImpl extends ObjectReferenceImpl implements ClassLoaderReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.CLASS_LOADER_TAG;

	/**
	 * Creates new ClassLoaderReferenceImpl.
	 */
	public ClassLoaderReferenceImpl(VirtualMachineImpl vmImpl,
			JdwpClassLoaderID classLoaderID) {
		super("ClassLoaderReference", vmImpl, classLoaderID); //$NON-NLS-1$
	}

	/**
	 * @returns Value tag.
	 */
	@Override
	public byte getTag() {
		return tag;
	}

	/**
	 * @returns Returns a list of all loaded classes that were defined by this
	 *          class loader.
	 */
	public List<ReferenceType> definedClasses() {
		// Note that this information should not be cached.
		List<ReferenceType> visibleClasses = visibleClasses();
		List<ReferenceType> result = new ArrayList<ReferenceType>(visibleClasses.size());
		Iterator<ReferenceType> iter = visibleClasses.iterator();
		while (iter.hasNext()) {
			try {
				ReferenceType type = iter.next();
				// Note that classLoader() is null for the bootstrap
				// classloader.
				if (type.classLoader() != null && type.classLoader().equals(this))
					result.add(type);
			} catch (ClassNotPreparedException e) {
				continue;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ClassLoaderReference#visibleClasses()
	 */
	public List<ReferenceType> visibleClasses() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.CLR_VISIBLE_CLASSES, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			List<ReferenceType> elements = new ArrayList<ReferenceType>(nrOfElements);
			for (int i = 0; i < nrOfElements; i++) {
				ReferenceTypeImpl elt = ReferenceTypeImpl.readWithTypeTag(this, replyData);
				if (elt == null)
					continue;
				elements.add(elt);
			}
			return elements;
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
	public static ClassLoaderReferenceImpl read(MirrorImpl target,
			DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpClassLoaderID ID = new JdwpClassLoaderID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("classLoaderReference", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;

		ClassLoaderReferenceImpl mirror = new ClassLoaderReferenceImpl(vmImpl,
				ID);
		return mirror;
	}
}
