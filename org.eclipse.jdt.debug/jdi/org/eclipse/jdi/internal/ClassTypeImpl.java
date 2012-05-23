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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdi.internal.jdwp.JdwpClassID;
import org.eclipse.jdi.internal.jdwp.JdwpClassObjectID;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ClassTypeImpl extends ReferenceTypeImpl implements ClassType {

	/** JDWP Tag. */
	public static final byte typeTag = JdwpID.TYPE_TAG_CLASS;

	/** The following are the stored results of JDWP calls. */
	private ClassTypeImpl fSuperclass = null;

	/**
	 * Creates new ClassTypeImpl.
	 */
	public ClassTypeImpl(VirtualMachineImpl vmImpl, JdwpClassID classID) {
		super("ClassType", vmImpl, classID); //$NON-NLS-1$
	}

	/**
	 * Creates new ClassTypeImpl.
	 */
	public ClassTypeImpl(VirtualMachineImpl vmImpl, JdwpClassID classID,
			String signature, String genericSignature) {
		super("ClassType", vmImpl, classID, signature, genericSignature); //$NON-NLS-1$
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
		return new ClassObjectReferenceImpl(virtualMachineImpl(),
				new JdwpClassObjectID(virtualMachineImpl()));
	}

	/**
	 * Flushes all stored Jdwp results.
	 */
	@Override
	public void flushStoredJdwpResults() {
		super.flushStoredJdwpResults();

		// For all classes that have this class cached as superclass, this cache
		// must be undone.
		Iterator<Object> itr = virtualMachineImpl().allCachedRefTypes();
		while (itr.hasNext()) {
			ReferenceTypeImpl refType = (ReferenceTypeImpl) itr.next();
			if (refType instanceof ClassTypeImpl) {
				ClassTypeImpl classType = (ClassTypeImpl) refType;
				if (classType.fSuperclass != null && classType.fSuperclass.equals(this)) {
					classType.flushStoredJdwpResults();
				}
			}
		}

		fSuperclass = null;
	}

	/**
	 * @return Returns Jdwp version of given options.
	 */
	private int optionsToJdwpOptions(int options) {
		int jdwpOptions = 0;
		if ((options & INVOKE_SINGLE_THREADED) != 0)
			jdwpOptions |= MethodImpl.INVOKE_SINGLE_THREADED_JDWP;
		return jdwpOptions;
	}

	/**
	 * @return Returns a the single non-abstract Method visible from this class
	 *         that has the given name and signature.
	 */
	public Method concreteMethodByName(String name, String signature) {
		/*
		 * Recursion is used to find the method: The methods of its own (own
		 * methods() command); The methods of it's superclass.
		 */

		Iterator<Method> methods = methods().iterator();
		Method method;
		while (methods.hasNext()) {
			method = methods.next();
			if (method.name().equals(name) && method.signature().equals(signature)) {
				if (method.isAbstract()) {
					return null;
				}
				return method;
			}
		}

		if (superclass() != null) {
			return superclass().concreteMethodByName(name, signature);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ClassType#invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)
	 */
	public Value invokeMethod(ThreadReference thread, Method method, List<? extends Value> arguments, int options) throws InvalidTypeException,
			ClassNotLoadedException, IncompatibleThreadStateException,
			InvocationException {
		checkVM(thread);
		checkVM(method);
		ThreadReferenceImpl threadImpl = (ThreadReferenceImpl) thread;
		MethodImpl methodImpl = (MethodImpl) method;

		// Perform some checks for IllegalArgumentException.
		if (!visibleMethods().contains(method))
			throw new IllegalArgumentException(
					JDIMessages.ClassTypeImpl_Class_does_not_contain_given_method_1);
		if (method.argumentTypeNames().size() != arguments.size())
			throw new IllegalArgumentException(
					JDIMessages.ClassTypeImpl_Number_of_arguments_doesn__t_match_2);
		if (method.isConstructor() || method.isStaticInitializer())
			throw new IllegalArgumentException(
					JDIMessages.ClassTypeImpl_Method_is_constructor_or_intitializer_3);

		// check the type and the VM of the arguments. Convert the values if
		// needed
		List<Value> checkedArguments = ValueImpl.checkValues(arguments, method.argumentTypes(), virtualMachineImpl());

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			threadImpl.write(this, outData);
			methodImpl.write(this, outData);

			writeInt(checkedArguments.size(), "size", outData); //$NON-NLS-1$
			Iterator<Value> iter = checkedArguments.iterator();
			while (iter.hasNext()) {
				Value elt = iter.next();
				if (elt instanceof ValueImpl) {
					((ValueImpl)elt).writeWithTag(this, outData);
				} else {
					ValueImpl.writeNullWithTag(this, outData);
				}
			}

			writeInt(optionsToJdwpOptions(options),
					"options", MethodImpl.getInvokeOptions(), outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.CT_INVOKE_METHOD, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_METHODID:
				throw new IllegalArgumentException();
			case JdwpReplyPacket.TYPE_MISMATCH:
				throw new InvalidTypeException();
			case JdwpReplyPacket.INVALID_CLASS:
				throw new ClassNotLoadedException(name());
			case JdwpReplyPacket.INVALID_THREAD:
				throw new IncompatibleThreadStateException();
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			ValueImpl value = ValueImpl.readWithTag(this, replyData);
			ObjectReferenceImpl exception = ObjectReferenceImpl
					.readObjectRefWithTag(this, replyData);
			if (exception != null)
				throw new InvocationException(exception);
			return value;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ClassType#newInstance(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)
	 */
	public ObjectReference newInstance(ThreadReference thread, Method method, List<? extends Value> arguments, int options) throws InvalidTypeException,
			ClassNotLoadedException, IncompatibleThreadStateException,
			InvocationException {
		checkVM(thread);
		checkVM(method);
		ThreadReferenceImpl threadImpl = (ThreadReferenceImpl) thread;
		MethodImpl methodImpl = (MethodImpl) method;

		// Perform some checks for IllegalArgumentException.
		if (!methods().contains(method))
			throw new IllegalArgumentException(
					JDIMessages.ClassTypeImpl_Class_does_not_contain_given_method_4);
		if (method.argumentTypeNames().size() != arguments.size())
			throw new IllegalArgumentException(
					JDIMessages.ClassTypeImpl_Number_of_arguments_doesn__t_match_5);
		if (!method.isConstructor())
			throw new IllegalArgumentException(
					JDIMessages.ClassTypeImpl_Method_is_not_a_constructor_6);

		List<Value> checkedArguments = ValueImpl.checkValues(arguments, method.argumentTypes(), virtualMachineImpl());

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			threadImpl.write(this, outData);
			methodImpl.write(this, outData);

			writeInt(checkedArguments.size(), "size", outData); //$NON-NLS-1$
			Iterator<Value> iter = checkedArguments.iterator();
			while (iter.hasNext()) {
				Value elt = iter.next();
				if (elt instanceof ValueImpl) {
					checkVM(elt);
					((ValueImpl)elt).writeWithTag(this, outData);
				} else {
					ValueImpl.writeNullWithTag(this, outData);
				}
			}

			writeInt(optionsToJdwpOptions(options),
					"options", MethodImpl.getInvokeOptions(), outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.CT_NEW_INSTANCE, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_METHODID:
				throw new IllegalArgumentException();
			case JdwpReplyPacket.TYPE_MISMATCH:
				throw new InvalidTypeException();
			case JdwpReplyPacket.INVALID_CLASS:
				throw new ClassNotLoadedException(name());
			case JdwpReplyPacket.INVALID_THREAD:
				throw new IncompatibleThreadStateException();
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			ObjectReferenceImpl object = ObjectReferenceImpl
					.readObjectRefWithTag(this, replyData);
			ObjectReferenceImpl exception = ObjectReferenceImpl
					.readObjectRefWithTag(this, replyData);
			if (exception != null)
				throw new InvocationException(exception);
			return object;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Assigns a value to a static field. .
	 */
	public void setValue(Field field, Value value) throws InvalidTypeException,
			ClassNotLoadedException {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			writeInt(1, "size", outData); // We only set one field //$NON-NLS-1$
			checkVM(field);
			((FieldImpl) field).write(this, outData);

			// check the type and the VM of the value. Convert the value if
			// needed
			ValueImpl checkedValue = ValueImpl.checkValue(value, field.type(),
					virtualMachineImpl());

			if (checkedValue != null) {
				checkedValue.write(this, outData);
			} else {
				ValueImpl.writeNull(this, outData);
			}

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.CT_SET_VALUES, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.TYPE_MISMATCH:
				throw new InvalidTypeException();
			case JdwpReplyPacket.INVALID_CLASS:
				throw new ClassNotLoadedException(name());
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ClassType#subclasses()
	 */
	public List<ClassType> subclasses() {
		// Note that this information should not be cached.
		List<ClassType> subclasses = new ArrayList<ClassType>();
		Iterator<ReferenceType> itr = virtualMachineImpl().allRefTypes();
		while (itr.hasNext()) {
			try {
				ReferenceType refType = itr.next();
				if (refType instanceof ClassTypeImpl) {
					ClassTypeImpl classType = (ClassTypeImpl) refType;
					if (classType.superclass() != null
							&& classType.superclass().equals(this)) {
						subclasses.add(classType);
					}
				}
			} catch (ClassNotPreparedException e) {
				continue;
			}
		}
		return subclasses;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ClassType#superclass()
	 */
	public ClassType superclass() {
		if (fSuperclass != null)
			return fSuperclass;

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.CT_SUPERCLASS, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fSuperclass = ClassTypeImpl.read(this, replyData);
			return fSuperclass;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/*
	 * @return Reads ID and returns known ReferenceTypeImpl with that ID, or if
	 * ID is unknown a newly created ReferenceTypeImpl.
	 */
	public static ClassTypeImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpClassID ID = new JdwpClassID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("classType", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;

		ClassTypeImpl mirror = (ClassTypeImpl) vmImpl.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ClassTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		return mirror;
	}

	/*
	 * @return Reads ID and returns known ReferenceTypeImpl with that ID, or if
	 * ID is unknown a newly created ReferenceTypeImpl.
	 */
	public static ClassTypeImpl readWithSignature(MirrorImpl target,
			boolean withGenericSignature, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpClassID ID = new JdwpClassID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("classType", ID.value()); //$NON-NLS-1$

		String signature = target.readString("signature", in); //$NON-NLS-1$
		String genericSignature = null;
		if (withGenericSignature) {
			genericSignature = target.readString("generic signature", in); //$NON-NLS-1$
		}
		if (ID.isNull())
			return null;

		ClassTypeImpl mirror = (ClassTypeImpl) vmImpl.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ClassTypeImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		mirror.setSignature(signature);
		mirror.setGenericSignature(genericSignature);
		return mirror;
	}

	public boolean isEnum() {
		if (virtualMachineImpl().isJdwpVersionGreaterOrEqual(1, 5)) {
			// there is no modifier for this ... :(
			ClassType superClass = superclass();
			return superClass != null
					&& "<E:Ljava/lang/Enum<TE;>;>Ljava/lang/Object;Ljava/lang/Comparable<TE;>;Ljava/io/Serializable;".equals(superClass.genericSignature()); //$NON-NLS-1$
		}
		// jdwp 1.5 only option
		return false;
	}

}
