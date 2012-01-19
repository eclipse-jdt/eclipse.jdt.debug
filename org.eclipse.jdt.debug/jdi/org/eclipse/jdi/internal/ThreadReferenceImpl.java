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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdi.internal.jdwp.JdwpThreadID;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMCannotBeModifiedException;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

/**
 * This class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public class ThreadReferenceImpl extends ObjectReferenceImpl implements	ThreadReference, org.eclipse.jdi.hcr.ThreadReference {
	/** ThreadStatus Constants. */
	public static final int JDWP_THREAD_STATUS_ZOMBIE = 0;
	public static final int JDWP_THREAD_STATUS_RUNNING = 1;
	public static final int JDWP_THREAD_STATUS_SLEEPING = 2;
	public static final int JDWP_THREAD_STATUS_MONITOR = 3;
	public static final int JDWP_THREAD_STATUS_WAIT = 4;

	/** SuspendStatus Constants. */
	public static final int SUSPEND_STATUS_SUSPENDED = 0x01;

	/** Mapping of command codes to strings. */
	private static Map<Integer, String> fgThreadStatusMap = null;

	/** Map with Strings for flag bits. */
	private static String[] fgSuspendStatusStrings = null;

	/** JDWP Tag. */
	protected static final byte tag = JdwpID.THREAD_TAG;

	/** Is thread currently at a breakpoint? */
	private boolean fIsAtBreakpoint = false;

	/**
	 * The cached thread group. A thread's thread group cannot be changed.
	 */
	private ThreadGroupReferenceImpl fThreadGroup = null;

	/**
	 * Creates new ThreadReferenceImpl.
	 */
	public ThreadReferenceImpl(VirtualMachineImpl vmImpl, JdwpThreadID threadID) {
		super("ThreadReference", vmImpl, threadID); //$NON-NLS-1$
	}

	/**
	 * Sets at breakpoint flag.
	 */
	public void setIsAtBreakpoint() {
		fIsAtBreakpoint = true;
	}

	/**
	 * Reset flags that can be set when event occurs.
	 */
	public void resetEventFlags() {
		fIsAtBreakpoint = false;
	}

	/**
	 * @returns Value tag.
	 */
	@Override
	public byte getTag() {
		return tag;
	}

	/**
	 * @returns Returns an ObjectReference for the monitor, if any, for which
	 *          this thread is currently waiting.
	 */
	public ObjectReference currentContendedMonitor()
			throws IncompatibleThreadStateException {
		if (!virtualMachine().canGetCurrentContendedMonitor()) {
			throw new UnsupportedOperationException();
		}
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_CURRENT_CONTENDED_MONITOR, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException(
						JDIMessages.ThreadReferenceImpl_Thread_was_not_suspended_1);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			ObjectReference result = ObjectReferenceImpl.readObjectRefWithTag(
					this, replyData);
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @see com.sun.jdi.ThreadReference#forceEarlyReturn(com.sun.jdi.Value)
	 * @since 3.3
	 */
	public void forceEarlyReturn(Value value) throws InvalidTypeException,
			ClassNotLoadedException, IncompatibleThreadStateException {
		if (!virtualMachineImpl().canBeModified()) {
			throw new VMCannotBeModifiedException(
					JDIMessages.ThreadReferenceImpl_vm_read_only);
		}
		initJdwpRequest();
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		DataOutputStream dataOutStream = new DataOutputStream(byteOutStream);
		try {
			write(this, dataOutStream);
			if (value != null) {
				((ValueImpl) value).writeWithTag((ValueImpl) value,
						dataOutStream);
			} else {
				ValueImpl.writeNullWithTag(this, dataOutStream);
			}
			JdwpReplyPacket reply = requestVM(
					JdwpCommandPacket.TR_FORCE_EARLY_RETURN, byteOutStream);
			switch (reply.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException(
						JDIMessages.ThreadReferenceImpl_thread_object_invalid);
			case JdwpReplyPacket.INVALID_OBJECT:
				throw new ClassNotLoadedException(
						JDIMessages.ThreadReferenceImpl_thread_or_value_unknown);
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
			case JdwpReplyPacket.THREAD_NOT_ALIVE:
				throw new IncompatibleThreadStateException(
						JDIMessages.ThreadReferenceImpl_thread_not_suspended);
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ThreadReferenceImpl_no_force_early_return_on_threads);
			case JdwpReplyPacket.OPAQUE_FRAME:
				throw new NativeMethodException(
						JDIMessages.ThreadReferenceImpl_thread_cannot_force_native_method);
			case JdwpReplyPacket.NO_MORE_FRAMES:
				throw new InvalidStackFrameException(
						JDIMessages.ThreadReferenceImpl_thread_no_stackframes);
			case JdwpReplyPacket.TYPE_MISMATCH:
				throw new InvalidTypeException(
						JDIMessages.ThreadReferenceImpl_incapatible_return_type);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(reply.errorCode());
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @returns Returns the StackFrame at the given index in the thread's
	 *          current call stack.
	 */
	public StackFrame frame(int index) throws IncompatibleThreadStateException {
		return frames(index, 1).get(0);
	}

	/**
	 * @see com.sun.jdi.ThreadReference#frameCount()
	 */
	public int frameCount() throws IncompatibleThreadStateException {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_FRAME_COUNT, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException(
						JDIMessages.ThreadReferenceImpl_Thread_was_not_suspended_1);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			int result = readInt("frame count", replyData); //$NON-NLS-1$
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadReference#frames()
	 */
	public List<StackFrame> frames() throws IncompatibleThreadStateException {
		return frames(0, -1);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadReference#frames(int, int)
	 */
	public List<StackFrame> frames(int start, int length) throws IndexOutOfBoundsException,
			IncompatibleThreadStateException {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			writeInt(start, "start", outData); //$NON-NLS-1$
			writeInt(length, "length", outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_FRAMES, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException(
						JDIMessages.ThreadReferenceImpl_Thread_was_not_suspended_1);
			case JdwpReplyPacket.INVALID_INDEX:
				throw new IndexOutOfBoundsException(
						JDIMessages.ThreadReferenceImpl_Invalid_index_of_stack_frames_given_4);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			List<StackFrame> frames = new ArrayList<StackFrame>(nrOfElements);
			for (int i = 0; i < nrOfElements; i++) {
				StackFrameImpl frame = StackFrameImpl.readWithLocation(this,
						this, replyData);
				if (frame == null) {
					continue;
				}
				frames.add(frame);
			}
			return frames;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadReference#interrupt()
	 */
	public void interrupt() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			requestVM(JdwpCommandPacket.TR_INTERRUPT, this);
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadReference#isAtBreakpoint()
	 */
	public boolean isAtBreakpoint() {
		return isSuspended() && fIsAtBreakpoint;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadReference#isSuspended()
	 */
	public boolean isSuspended() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_STATUS, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			// remove the thread status reply
			readInt("thread status", threadStatusMap(), replyData); //$NON-NLS-1$
			int suspendStatus = readInt(
					"suspend status", suspendStatusStrings(), replyData); //$NON-NLS-1$
			boolean result = suspendStatus == SUSPEND_STATUS_SUSPENDED;
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return false;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadReference#name()
	 */
	public String name() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.TR_NAME,
					this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			return readString("name", replyData); //$NON-NLS-1$
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadReference#ownedMonitors()
	 */
	public List<ObjectReference> ownedMonitors() throws IncompatibleThreadStateException {
		if (!virtualMachine().canGetOwnedMonitorInfo()) {
			throw new UnsupportedOperationException();
		}
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_OWNED_MONITORS, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException(
						JDIMessages.ThreadReferenceImpl_Thread_was_not_suspended_5);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();

			int nrOfMonitors = readInt("nr of monitors", replyData); //$NON-NLS-1$
			List<ObjectReference> result = new ArrayList<ObjectReference>(nrOfMonitors);
			for (int i = 0; i < nrOfMonitors; i++) {
				result.add(ObjectReferenceImpl.readObjectRefWithTag(this,
						replyData));
			}
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.ThreadReference#ownedMonitorsAndFrames()
	 */
	public List<com.sun.jdi.MonitorInfo> ownedMonitorsAndFrames()
			throws IncompatibleThreadStateException {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_OWNED_MONITOR_STACK_DEPTH, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
			case JdwpReplyPacket.INVALID_OBJECT:
				throw new ObjectCollectedException(
						JDIMessages.ThreadReferenceImpl_thread_object_invalid);
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException(
						JDIMessages.ThreadReferenceImpl_Thread_was_not_suspended_5);
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ThreadReferenceImpl_no_force_early_return_on_threads);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();

			int owned = readInt("owned monitors", replyData); //$NON-NLS-1$
			List<com.sun.jdi.MonitorInfo> result = new ArrayList<com.sun.jdi.MonitorInfo>(owned);
			ObjectReference monitor = null;
			int depth = -1;
			for (int i = 0; i < owned; i++) {
				monitor = ObjectReferenceImpl.readObjectRefWithTag(this,
						replyData);
				depth = readInt("stack depth", replyData); //$NON-NLS-1$
				result.add(new MonitorInfoImpl(this, depth, monitor, virtualMachineImpl()));
			}
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Resumes this thread.
	 * 
	 * @see com.sun.jdi.ThreadReference#resume()
	 */
	public void resume() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_RESUME, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			resetEventFlags();
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the thread's status.
	 */
	public int status() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_STATUS, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.ABSENT_INFORMATION:
				return THREAD_STATUS_UNKNOWN;
			case JdwpReplyPacket.INVALID_THREAD:
				return THREAD_STATUS_NOT_STARTED;
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			int threadStatus = readInt(
					"thread status", threadStatusMap(), replyData); //$NON-NLS-1$
			readInt("suspend status", suspendStatusStrings(), replyData); //$NON-NLS-1$
			switch (threadStatus) {
			case JDWP_THREAD_STATUS_ZOMBIE:
				return THREAD_STATUS_ZOMBIE;
			case JDWP_THREAD_STATUS_RUNNING:
				return THREAD_STATUS_RUNNING;
			case JDWP_THREAD_STATUS_SLEEPING:
				return THREAD_STATUS_SLEEPING;
			case JDWP_THREAD_STATUS_MONITOR:
				return THREAD_STATUS_MONITOR;
			case JDWP_THREAD_STATUS_WAIT:
				return THREAD_STATUS_WAIT;
			case -1: // see bug 30816
				return THREAD_STATUS_UNKNOWN;
			}
			throw new InternalException(
					JDIMessages.ThreadReferenceImpl_Unknown_thread_status_received___6
							+ threadStatus);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Stops this thread with an asynchronous exception.
	 * 
	 * @see com.sun.jdi.ThreadReference#stop(com.sun.jdi.ObjectReference)
	 */
	public void stop(ObjectReference throwable) throws InvalidTypeException {
		checkVM(throwable);
		ObjectReferenceImpl throwableImpl = (ObjectReferenceImpl) throwable;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			throwableImpl.write(this, outData);

			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.TR_STOP,
					outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			case JdwpReplyPacket.INVALID_CLASS:
				throw new InvalidTypeException(
						JDIMessages.ThreadReferenceImpl_Stop_argument_not_an_instance_of_java_lang_Throwable_in_the_target_VM_7);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Suspends this thread.
	 * 
	 * @see com.sun.jdi.ThreadReference#suspend()
	 */
	public void suspend() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_SUSPEND, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns the number of pending suspends for this thread.
	 */
	public int suspendCount() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_SUSPEND_COUNT, this);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			int result = readInt("suspend count", replyData); //$NON-NLS-1$
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns this thread's thread group.
	 */
	public ThreadGroupReference threadGroup() {
		if (fThreadGroup != null) {
			return fThreadGroup;
		}
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.TR_THREAD_GROUP, this);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fThreadGroup = ThreadGroupReferenceImpl.read(this, replyData);
			return fThreadGroup;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Simulate the execution of a return instruction instead of executing the
	 * next byte code in a method.
	 * 
	 * @return Returns whether any finally or synchronized blocks are enclosing
	 *         the current instruction.
	 */
	public boolean doReturn(Value returnValue,
			boolean triggerFinallyAndSynchronized)
			throws org.eclipse.jdi.hcr.OperationRefusedException {
		virtualMachineImpl().checkHCRSupported();
		ValueImpl valueImpl;
		if (returnValue != null) { // null is used if no value is returned.
			checkVM(returnValue);
			valueImpl = (ValueImpl) returnValue;
		} else {
			try {
				TypeImpl returnType = (TypeImpl) frame(0).location().method()
						.returnType();
				valueImpl = (ValueImpl) returnType.createNullValue();
			} catch (IncompatibleThreadStateException e) {
				throw new org.eclipse.jdi.hcr.OperationRefusedException(
						e.toString());
			} catch (ClassNotLoadedException e) {
				throw new org.eclipse.jdi.hcr.OperationRefusedException(
						e.toString());
			}
		}

		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			write(this, outData);
			valueImpl.writeWithTag(this, outData);
			writeBoolean(triggerFinallyAndSynchronized,
					"trigger finaly+sync", outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.HCR_DO_RETURN, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new ObjectCollectedException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			boolean result = readBoolean("is enclosed", replyData); //$NON-NLS-1$
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return false;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
	public String toString() {
		try {
			return NLS.bind(JDIMessages.ThreadReferenceImpl_8,
					new String[] { type().toString(), name(),
							getObjectID().toString() });
		} catch (ObjectCollectedException e) {
			return JDIMessages.ThreadReferenceImpl__Garbage_Collected__ThreadReference__9
					+ idString();
		} catch (Exception e) {
			return fDescription;
		}
	}

	/**
	 * @return Reads JDWP representation and returns new instance.
	 */
	public static ThreadReferenceImpl read(MirrorImpl target, DataInputStream in)
			throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		JdwpThreadID ID = new JdwpThreadID(vmImpl);
		ID.read(in);
		if (target.fVerboseWriter != null)
			target.fVerboseWriter.println("threadReference", ID.value()); //$NON-NLS-1$

		if (ID.isNull())
			return null;

		ThreadReferenceImpl mirror = (ThreadReferenceImpl) vmImpl
				.getCachedMirror(ID);
		if (mirror == null) {
			mirror = new ThreadReferenceImpl(vmImpl, ID);
			vmImpl.addCachedMirror(mirror);
		}
		return mirror;
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fgThreadStatusMap != null) {
			return;
		}

		Field[] fields = ThreadReferenceImpl.class.getDeclaredFields();
		fgThreadStatusMap = new HashMap<Integer, String>();
		fgSuspendStatusStrings = new String[32]; // Int

		for (Field field : fields) {
			if ((field.getModifiers() & Modifier.PUBLIC) == 0
					|| (field.getModifiers() & Modifier.STATIC) == 0
					|| (field.getModifiers() & Modifier.FINAL) == 0)
				continue;

			try {
				String name = field.getName();
				int value = field.getInt(null);
				Integer intValue = new Integer(value);

				if (name.startsWith("JDWP_THREAD_STATUS_")) { //$NON-NLS-1$
					name = name.substring(19);
					fgThreadStatusMap.put(intValue, name);
				} else if (name.startsWith("SUSPEND_STATUS_")) { //$NON-NLS-1$
					name = name.substring(15);
					for (int j = 0; j < fgSuspendStatusStrings.length; j++) {
						if ((1 << j & value) != 0) {
							fgSuspendStatusStrings[j] = name;
							break;
						}
					}
				}
			} catch (IllegalAccessException e) {
				// Will not occur for own class.
			} catch (IllegalArgumentException e) {
				// Should not occur.
				// We should take care that all public static final constants
				// in this class are numbers that are convertible to int.
			}
		}
	}

	/**
	 * @return Returns a map with string representations of tags.
	 */
	public static Map<Integer, String> threadStatusMap() {
		getConstantMaps();
		return fgThreadStatusMap;
	}

	/**
	 * @return Returns a map with string representations of tags.
	 */
	public static String[] suspendStatusStrings() {
		getConstantMaps();
		return fgSuspendStatusStrings;
	}

	/**
	 * @see ThreadReference#popFrames(StackFrame)
	 */
	public void popFrames(StackFrame frameToPop)
			throws IncompatibleThreadStateException {
		if (!isSuspended()) {
			throw new IncompatibleThreadStateException();
		}
		if (!virtualMachineImpl().canPopFrames()) {
			throw new UnsupportedOperationException();
		}

		StackFrameImpl frame = (StackFrameImpl) frameToPop;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			frame.writeWithThread(frame, outData);

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.SF_POP_FRAME, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_THREAD:
				throw new InvalidStackFrameException();
			case JdwpReplyPacket.INVALID_FRAMEID:
				throw new InvalidStackFrameException(
						JDIMessages.ThreadReferenceImpl_Unable_to_pop_the_requested_stack_frame_from_the_call_stack__Reasons_include__The_frame_id_was_invalid__The_thread_was_resumed__10);
			case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
				throw new IncompatibleThreadStateException(
						JDIMessages.ThreadReferenceImpl_Unable_to_pop_the_requested_stack_frame__The_requested_stack_frame_is_not_suspended_11);
			case JdwpReplyPacket.NO_MORE_FRAMES:
				throw new InvalidStackFrameException(
						JDIMessages.ThreadReferenceImpl_Unable_to_pop_the_requested_stack_frame_from_the_call_stack__Reasons_include__The_requested_frame_was_the_last_frame_on_the_call_stack__The_requested_frame_was_the_last_frame_above_a_native_frame__12);
			default:
				defaultReplyErrorHandler(replyPacket.errorCode());
			}
		} catch (IOException ioe) {
			defaultIOExceptionHandler(ioe);
		} finally {
			handledJdwpRequest();
		}
	}

}
