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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpObjectID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ObjectReferenceImpl extends ValueImpl implements ObjectReference {
	/** JDWP Tag. */
	public static final byte tag = JdwpID.OBJECT_TAG;

	/** ObjectID of object that corresponds to this reference. */
	private JdwpObjectID fObjectID;
	/**
	 * Cached reference type. This value is safe for caching because the type of
	 * an object never changes.
	 */
	private ReferenceType fReferenceType;

	/**
	 * Creates new ObjectReferenceImpl.
	 */
	public ObjectReferenceImpl(VirtualMachineImpl vmImpl, JdwpObjectID objectID) {
		this("ObjectReference", vmImpl, objectID); //$NON-NLS-1$
	}

	/**
	 * Creates new ObjectReferenceImpl.
	 */
	public ObjectReferenceImpl(String description, VirtualMachineImpl vmImpl,
			JdwpObjectID objectID) {
		super(description, vmImpl);
		fObjectID = objectID;
	}

	/**
	 * @returns tag.
	 */
	@Override
	public byte getTag() {
		return tag;
	}

	/**
	 * @return Returns Jdwp Object ID.
	 */
	public JdwpObjectID getObjectID() {
		return fObjectID;
	}

	/**
	 * Prevents garbage collection for this object.
	 */
	public void disableCollection() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_DISABLE_COLLECTION, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Permits garbage collection for this object.
	 */
	public void enableCollection() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_ENABLE_COLLECTION, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Inner class used to return monitor info.
	 */
	private class MonitorInfo {
		ThreadReferenceImpl owner;
		int entryCount;
		ArrayList<ThreadReference> waiters;
	}

	/**
	 * @return Returns monitor info.
	 */
	private MonitorInfo monitorInfo() throws IncompatibleThreadStateException {
		if (!virtualMachine().canGetMonitorInfo()) {
			throw new UnsupportedOperationException();
		}
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_MONITOR_INFO, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new IncompatibleThreadStateException();
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException();
			}

			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			MonitorInfo result = new MonitorInfo();
			result.owner = ThreadReferenceImpl.read(this, replyData);
			result.entryCount = readInt("entry count", replyData); //$NON-NLS-1$
			int nrOfWaiters = readInt("nr of waiters", replyData); //$NON-NLS-1$
			result.waiters = new ArrayList<ThreadReference>(nrOfWaiters);
			for (int i = 0; i < nrOfWaiters; i++)
				result.waiters.add(ThreadReferenceImpl.read(this, replyData));
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns an ThreadReference for the thread, if any, which
	 *         currently owns this object's monitor.
	 */
	public ThreadReference owningThread()
			throws IncompatibleThreadStateException {
		return monitorInfo().owner;
	}

	/**
	 * @return Returns the number times this object's monitor has been entered
	 *         by the current owning thread.
	 */
	public int entryCount() throws IncompatibleThreadStateException {
		return monitorInfo().entryCount;
	}

	/**
	 * @return Returns a List containing a ThreadReference for each thread
	 *         currently waiting for this object's monitor.
	 */
	public List<ThreadReference> waitingThreads() throws IncompatibleThreadStateException {
		return monitorInfo().waiters;
	}

	/**
	 * @return Returns the value of a given instance or static field in this
	 *         object.
	 */
	public Value getValue(Field field) {
		ArrayList<Field> list = new ArrayList<Field>(1);
		list.add(field);
		return getValues(list).get(field);
	}

	/**
	 * @return Returns objects that directly reference this object. Only objects
	 *         that are reachable for the purposes of garbage collection are
	 *         returned. Note that an object can also be referenced in other
	 *         ways, such as from a local variable in a stack frame, or from a
	 *         JNI global reference. Such non-object referrers are not returned
	 *         by this method.
	 * 
	 * @since 3.3
	 */
	public List<Value> referringObjects(long maxReferrers)
			throws UnsupportedOperationException, IllegalArgumentException {
		try {
			int max = (int) maxReferrers;
			if (maxReferrers >= Integer.MAX_VALUE) {
				max = Integer.MAX_VALUE;
			}
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			this.getObjectID().write(outData);
			writeInt(max, "max referrers", outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_REFERRING_OBJECTS, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ReferenceTypeImpl_27);
			case JdwpReplyPacket.ILLEGAL_ARGUMENT:
				throw new IllegalArgumentException(
						JDIMessages.ReferenceTypeImpl_26);
			case JdwpReplyPacket.INVALID_OBJECT:
				throw new ObjectCollectedException(
						JDIMessages.ObjectReferenceImpl_object_not_known);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			int elements = readInt("elements", replyData); //$NON-NLS-1$
			if (max > 0 && elements > max) {
				elements = max;
			}
			ArrayList<Value> list = new ArrayList<Value>();
			for (int i = 0; i < elements; i++) {
				list.add(ValueImpl.readWithTag(this, replyData));
			}
			return list;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the value of multiple instance and/or static fields in
	 *         this object.
	 */
	public Map<Field, Value> getValues(List<? extends Field> allFields) {
		// if the field list is empty, nothing to do.
		if (allFields.isEmpty()) {
			return new HashMap<Field, Value>();
		}
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);

			/*
			 * Distinguish static fields from non-static fields: For static
			 * fields ReferenceTypeImpl.getValues() must be used.
			 */
			List<Field> staticFields = new ArrayList<Field>();
			List<FieldImpl> nonStaticFields = new ArrayList<FieldImpl>();

			// Separate static and non-static fields.
			int allFieldsSize = allFields.size();
			for (int i = 0; i < allFieldsSize; i++) {
				FieldImpl field = (FieldImpl) allFields.get(i);
				checkVM(field);
				if (field.isStatic())
					staticFields.add(field);
				else
					nonStaticFields.add(field);
			}

			// First get values for the static fields.
			Map<Field, Value> resultMap;
			if (staticFields.isEmpty()) {
				resultMap = new HashMap<Field, Value>();
			} else {
				resultMap = referenceType().getValues(staticFields);
			}

			// if no non-static fields are requested, return directly the
			// result.
			if (nonStaticFields.isEmpty()) {
				return resultMap;
			}
			// Then get the values for the non-static fields.
			int nonStaticFieldsSize = nonStaticFields.size();
			write(this, outData);
			writeInt(nonStaticFieldsSize, "size", outData); //$NON-NLS-1$
			for (int i = 0; i < nonStaticFieldsSize; i++) {
				FieldImpl field = nonStaticFields.get(i);
				field.write(this, outData);
			}

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_GET_VALUES, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			if (nrOfElements != nonStaticFieldsSize)
				throw new InternalError(
						JDIMessages.ObjectReferenceImpl_Retrieved_a_different_number_of_values_from_the_VM_than_requested_1);

			for (int i = 0; i < nrOfElements; i++) {
				resultMap.put(nonStaticFields.get(i),
						ValueImpl.readWithTag(this, replyData));
			}
			return resultMap;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the hash code value.
	 */
	@Override
	public int hashCode() {
		return fObjectID.hashCode();
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
				&& fObjectID.equals(((ObjectReferenceImpl) object).fObjectID)
				&& virtualMachine().equals(
						((MirrorImpl) object).virtualMachine());
	}

	/**
	 * @return Returns Jdwp version of given options.
	 */
	private int optionsToJdwpOptions(int options) {
		int jdwpOptions = 0;
		if ((options & INVOKE_SINGLE_THREADED) != 0) {
			jdwpOptions |= MethodImpl.INVOKE_SINGLE_THREADED_JDWP;
		}
		if ((options & INVOKE_NONVIRTUAL) != 0) {
			jdwpOptions |= MethodImpl.INVOKE_NONVIRTUAL_JDWP;
		}
		return jdwpOptions;
	}

	/**
	 * Invokes the specified static Method in the target VM.
	 * 
	 * @return Returns a Value mirror of the invoked method's return value.
	 */
	public Value invokeMethod(ThreadReference thread, Method method, List<? extends Value> arguments, int options) throws InvalidTypeException,
			ClassNotLoadedException, IncompatibleThreadStateException,
			InvocationException {
		checkVM(thread);
		checkVM(method);
		ThreadReferenceImpl threadImpl = (ThreadReferenceImpl) thread;
		MethodImpl methodImpl = (MethodImpl) method;

		// Perform some checks for IllegalArgumentException.
		if (!isAValidMethod(method))
			throw new IllegalArgumentException(
					JDIMessages.ObjectReferenceImpl_Class_does_not_contain_given_method_2);
		if (method.argumentTypeNames().size() != arguments.size())
			throw new IllegalArgumentException(
					JDIMessages.ObjectReferenceImpl_Number_of_arguments_doesn__t_match_3);
		if (method.isConstructor() || method.isStaticInitializer())
			throw new IllegalArgumentException(
					JDIMessages.ObjectReferenceImpl_Method_is_constructor_or_intitializer_4);
		if ((options & INVOKE_NONVIRTUAL) != 0 && method.isAbstract())
			throw new IllegalArgumentException(
					JDIMessages.ObjectReferenceImpl_Method_is_abstract_and_can_therefore_not_be_invoked_nonvirtual_5);

		// check the type and the vm of the argument, convert the value if
		// needed.
		List<Value> checkedArguments = ValueImpl.checkValues(arguments,	method.argumentTypes(), virtualMachineImpl());

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			threadImpl.write(this, outData);
			((ReferenceTypeImpl) referenceType()).write(this, outData);
			methodImpl.write(this, outData);

			writeInt(checkedArguments.size(), "size", outData); //$NON-NLS-1$
			Iterator<Value> iter = checkedArguments.iterator();
			while (iter.hasNext()) {
				ValueImpl elt = (ValueImpl) iter.next();
				if (elt != null) {
					elt.writeWithTag(this, outData);
				} else {
					ValueImpl.writeNullWithTag(this, outData);
				}
			}

			writeInt(optionsToJdwpOptions(options),
					"options", MethodImpl.getInvokeOptions(), outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_INVOKE_METHOD, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.TYPE_MISMATCH:
				throw new InvalidTypeException();
			case JdwpReplyPacket.INVALID_CLASS:
				throw new ClassNotLoadedException(
						JDIMessages.ObjectReferenceImpl_One_of_the_arguments_of_ObjectReference_invokeMethod___6);
			case JdwpReplyPacket.INVALID_THREAD:
				throw new IncompatibleThreadStateException();
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException();
			case JdwpReplyPacket.INVALID_TYPESTATE:
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

	private boolean isAValidMethod(Method method) {
		ReferenceType refType = referenceType();
		if (refType instanceof ArrayType) {
			// if the object is an array, check if the method is declared in
			// java.lang.Object
			return "java.lang.Object".equals(method.declaringType().name()); //$NON-NLS-1$
		}
		return refType.allMethods().contains(method);
	}

	/**
	 * @return Returns if this object has been garbage collected in the target
	 *         VM.
	 */
	public boolean isCollected() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_IS_COLLECTED, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_OBJECT:
				return true;
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				// Workaround for problem in J2ME WTK (wireless toolkit)
				// @see Bug 12966
				try {
					referenceType();
				} catch (ObjectCollectedException e) {
					return true;
				}
				return false;
			default:
				defaultReplyErrorHandler(replyPacket.errorCode());
				break;
			}
			DataInputStream replyData = replyPacket.dataInStream();
			boolean result = readBoolean("is collected", replyData); //$NON-NLS-1$
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return false;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the ReferenceType that mirrors the type of this object.
	 */
	public ReferenceType referenceType() {
		if (fReferenceType != null) {
			return fReferenceType;
		}
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_REFERENCE_TYPE, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fReferenceType = ReferenceTypeImpl.readWithTypeTag(this, replyData);
			return fReferenceType;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the Type that mirrors the type of this object.
	 */
	@Override
	public Type type() {
		return referenceType();
	}

	/**
	 * Sets the value of a given instance or static field in this object.
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

			// check the type and the vm of the value. Convert the value if
			// needed
			ValueImpl checkedValue = ValueImpl.checkValue(value, field.type(),
					virtualMachineImpl());

			if (checkedValue != null) {
				checkedValue.write(this, outData);
			} else {
				ValueImpl.writeNull(this, outData);
			}

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.OR_SET_VALUES, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.TYPE_MISMATCH:
				throw new InvalidTypeException();
			case JdwpReplyPacket.INVALID_CLASS:
				throw new ClassNotLoadedException(referenceType().name());
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns a unique identifier for this ObjectReference.
	 */
	public long uniqueID() {
		return fObjectID.value();
	}

	/**
	 * @return Returns string with value of ID.
	 */
	public String idString() {
		return "(id=" + fObjectID + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
	public String toString() {
		try {
			return type().toString() + " " + idString(); //$NON-NLS-1$
		} catch (ObjectCollectedException e) {
			return JDIMessages.ObjectReferenceImpl__Garbage_Collected__ObjectReference__8
					+ idString();
		} catch (Exception e) {
			return fDescription;
		}
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ObjectReferenceImpl readObjectRefWithoutTag(
			MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpObjectID ID = new JdwpObjectID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("objectReference", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;

		ObjectReferenceImpl mirror = new ObjectReferenceImpl(vmImpl, ID);
		return mirror;
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ObjectReferenceImpl readObjectRefWithTag(MirrorImpl target,
			DataInputStream in) throws IOException {
		byte objectTag = target.readByte("object tag", JdwpID.tagMap(), in); //$NON-NLS-1$
		switch (objectTag) {
		case 0:
			return null;
		case ObjectReferenceImpl.tag:
			return ObjectReferenceImpl.readObjectRefWithoutTag(target, in);
		case ArrayReferenceImpl.tag:
			return ArrayReferenceImpl.read(target, in);
		case ClassLoaderReferenceImpl.tag:
			return ClassLoaderReferenceImpl.read(target, in);
		case ClassObjectReferenceImpl.tag:
			return ClassObjectReferenceImpl.read(target, in);
		case StringReferenceImpl.tag:
			return StringReferenceImpl.read(target, in);
		case ThreadGroupReferenceImpl.tag:
			return ThreadGroupReferenceImpl.read(target, in);
		case ThreadReferenceImpl.tag:
			return ThreadReferenceImpl.read(target, in);
		}
		throw new InternalException(
				JDIMessages.ObjectReferenceImpl_Invalid_ObjectID_tag_encountered___9
						+ objectTag);
	}

	/**
	 * Writes JDWP representation without tag.
	 */
	@Override
	public void write(MirrorImpl target, DataOutputStream out)
			throws IOException {
		fObjectID.write(out);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("objectReference", fObjectID.value()); //$NON-NLS-1$
	}
}
